package it.feio.android.omninotes.utils;

import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import de.greenrobot.event.EventBus;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.SettingsFragment;
import it.feio.android.omninotes.async.bus.NotesSyncedEvent;
import it.feio.android.omninotes.async.notes.SaveNoteTask;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Note;

public class SyncManager {
    private static SyncManager instance;
    public static final int SIGNIN_REQUEST_CODE = 101;
    private GoogleSignInAccount acc;
    private GoogleSignInOptions gso;
    private Drive mDriveService;
    private DriveServiceHelper mDriveHelper;

    public interface AccountConnectionResultHandler {
        void onSuccess(GoogleSignInAccount account);
        void onFailure(ApiException e);
    }

    public interface AccountDisconnectionResultHandler {
        void onSuccess();
        void onFailure(Exception e);
    }

    public SyncManager() {
        acc = GoogleSignIn.getLastSignedInAccount(OmniNotes.getAppContext());
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .requestEmail()
                .build();
        if (acc != null) {
            prepareDriveService();
        }
    }

    public static SyncManager getInstance() {
        if (instance == null) {
            instance = new SyncManager();
        }
        return instance;
    }

    public void watch() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                getInstance().fullSync();
            }
        }, 0, 10 * 1000);
    }

    public boolean isAccountConnected() {
        return acc != null;
    }
    public boolean isDriveReady() { return mDriveHelper != null; }

    public GoogleSignInAccount getConnectedAccount() {
        return acc;
    }

    public void initAccountConnection(SettingsFragment fragment) {
        GoogleSignInClient client = GoogleSignIn.getClient(OmniNotes.getAppContext(), gso);
        Intent signInIntent = client.getSignInIntent();
        fragment.startActivityForResult(signInIntent, SIGNIN_REQUEST_CODE);
    }

    public void disconnectAccount(Activity activity, AccountDisconnectionResultHandler handler) {
        GoogleSignInClient client = GoogleSignIn.getClient(OmniNotes.getAppContext(), gso);
        client.signOut().addOnCompleteListener(activity, task -> {
            try {
                task.getResult();
                acc = null;
                handler.onSuccess();
            } catch (Exception e) {
                Log.w("sign-out", "signOut:failed -" + e.toString());
                handler.onFailure(e);
            }
        });
    }

    protected void prepareDriveService() {
        if (!isAccountConnected()) {
            return;
        }

        GoogleAccountCredential credential =
                GoogleAccountCredential.usingOAuth2(
                        OmniNotes.getAppContext(), Collections.singleton(DriveScopes.DRIVE_FILE));
        Account acc = getConnectedAccount().getAccount();
        credential.setSelectedAccount(acc);

        mDriveService =
                new com.google.api.services.drive.Drive.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        new GsonFactory(),
                        credential)
                        .setApplicationName("Sekcja-OmniNotes")
                        .build();
        mDriveHelper = new DriveServiceHelper(mDriveService);
    }

    public void fullSync() {
        if (!isDriveReady()) {
            return;
        }
        List<Note> localNotes = DbHelper.getInstance().getNotes("", true);
        mDriveHelper
                .findFilesByPrefix("omni_")
                .addOnCompleteListener(task -> {
                   if (task.isSuccessful()) {
                       // Fetched remote notes
                       List<DriveFileHolder> remoteNotes = task.getResult();

                       // All notes that do not exist locally
                       List<DriveFileHolder> notesToDownload = pickNotesToDownload(remoteNotes, localNotes);

                       // All remote notes with fresher modification date
                       List<DriveFileHolder> notesToFetch = pickNotesToFetch(remoteNotes, localNotes);

                       // All local notes that are not present on drive,
                       // Don't check for potential updates as those are
                       // made when the note is saved locally.
                       List<Note> notesToDelete = pickNotesToDelete(remoteNotes, localNotes);

                       notesToDownload.forEach(file -> updateLocalNote(file));
                       notesToFetch.forEach(file -> updateLocalNote(file));
                       notesToDelete.forEach(note -> DbHelper.getInstance().deleteNoteWithoutSync(note, false));

                       if (notesToDelete.size() > 0) {
                           EventBus.getDefault().post(new NotesSyncedEvent(notesToDelete, true));
                       }
                   } else {
                       task.getException().printStackTrace();
                   }
                });
    }

    List<DriveFileHolder> pickNotesToDownload(List<DriveFileHolder> remoteNotes, List<Note> localNotes) {
        return remoteNotes.stream()
                .filter(f ->
                        localNotes.stream()
                                .filter(ln -> ln.get_id().equals(f.getNoteId()))
                                .findFirst()
                                .orElse(null) == null
                )
                .collect(Collectors.toList());
    }

    List<DriveFileHolder> pickNotesToFetch(List<DriveFileHolder> remoteNotes, List<Note> localNotes) {
        return remoteNotes.stream()
                .filter(f -> {
                    Note localCounterpart = localNotes.stream()
                            .filter(ln -> ln.get_id().equals(f.getNoteId()))
                            .findFirst()
                            .orElse(null);
                    return localCounterpart != null ? localCounterpart.getLastModification() < f.getNoteModifiedTime() : false;
                })
                .collect(Collectors.toList());
    }

    List<Note> pickNotesToDelete(List<DriveFileHolder> remoteNotes, List<Note> localNotes) {
        return localNotes.stream()
                .filter(ln -> !remoteNotes.stream().anyMatch(rn -> ln.get_id().equals(rn.getNoteId())))
                .collect(Collectors.toList());
    }


    public void updateLocalNote(DriveFileHolder file) {
        mDriveHelper.downloadFileContents(file.getId())
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String content = task.getResult();
                    Note note = new Note();
                    note.buildFromJson(content);
                    saveDownloadedNote(note);
                }
            });
    }

    protected void saveDownloadedNote(Note note) {
        syncAttachmentsToLocal(note);
        updateLocalNote(note);
    }

    protected void updateLocalNote(Note note) {
        new SaveNoteTask(noteSaved -> {
            Log.d("omni:note_saved", noteSaved.toString());
            List<Note> newNotes = new ArrayList<>();
            newNotes.add(noteSaved);
            EventBus.getDefault().post(new NotesSyncedEvent(newNotes));
        }, false, false)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, note);
    }

    protected void syncAttachmentsToLocal(Note note) {
        List<Attachment> attachments = note.getAttachmentsList()
                .stream()
                .map(attachment -> {
                    File f = new File(getStoragePath(attachment.getUri().toString()));
                    if (!f.exists()) {
                        f.getParentFile().mkdirs();
                        downloadAttachment(attachment.getUri().toString());
                    }
                    attachment.setUri(getStorageUriFromRemoteAttachment(attachment));
                    return attachment;
                })
                .collect(Collectors.toList());
        note.setAttachmentsList(attachments);
    }

    public void syncNoteToRemote(Note note) {
        String notePrefix = "omni_" + note.get_id().toString();
        if (isDriveReady()) {
            mDriveHelper
                    .findFileByPrefix(notePrefix)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DriveFileHolder result = task.getResult();
                            if (result.exists()) {
                                updateRemoteNote(result.getId(), note);
                            } else {
                                uploadNote(note);
                            }
                        }
                    });

        }
    }

    protected void updateRemoteNote(String fileId, Note original) {
        Note note = original.clone();
        String notePrefix = "omni_" + note.get_id().toString();
        String noteFile = notePrefix + "_" + note.getLastModification();

        normalizeNote(note);
        mDriveHelper.saveFile(fileId, noteFile, note.toJSON(), makeNoteFileProperties(note))
                .addOnCompleteListener(task ->  {
                    if (task.isSuccessful()) {
                        Log.d("omni_upload", "file update finished");
                    } else {
                        Log.d("omni_upload", "file upload failed");
                    }
                });
        syncAttachmentsToRemote(note);
    }

    protected void uploadNote(Note original) {
        Note note = original.clone();
        String notePrefix = "omni_" + note.get_id().toString();
        String noteFile = notePrefix + "_" + note.getLastModification();
        Map<String, String> properties = makeNoteFileProperties(note);

        normalizeNote(note);
        mDriveHelper.createFile(noteFile, note.toJSON(), properties)
                .addOnCompleteListener(task ->  {
                    if (task.isSuccessful()) {
                        Log.d("omni_upload", "file upload finished");
                    } else {
                        Log.d("omni_upload", "file upload failed");
                    }
                });
        syncAttachmentsToRemote(note);
    }

    protected Map<String, String> makeNoteFileProperties(Note note) {
        Map<String, String> properties = new HashMap<>();

        properties.put("title", note.getTitle());
        String excerpt = note.getContent().length() > 60 ? note.getContent().substring(0, 60) : note.getContent();
        properties.put("excerpt", excerpt);
        properties.put("archived", note.isArchived() ? "true" : "false");
        properties.put("trashed", note.isTrashed() ? "true" : "false");

        return properties;
    }

    protected void normalizeNote(Note note) {
        note.setAttachmentsList(note.getAttachmentsList()
                .stream().
                map(attachment -> {
                    attachment.setUri(Uri.parse(basename(attachment.getUri().toString())));
                    return attachment;
                })
                .collect(Collectors.toList()));
    }

    public static String basename(String path) {
        String filename = path.substring(path.lastIndexOf('/') + 1);

        if (filename == null || filename.equalsIgnoreCase("")) {
            filename = "";
        }
        return filename;
    }

    public void deleteRemoteNote(Note note) {
        String noteFile = "omni_" + note.get_id().toString();

        if (isDriveReady()) {
            mDriveHelper
                    .findFileByPrefix(noteFile)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DriveFileHolder result = task.getResult();
                            if (result.exists()) {
                                mDriveHelper.deleteFolderFile(result.getId());
                            }
                        }
                    });
        }
    }



    protected void downloadAttachment(String name) {
        mDriveHelper
                .findFile(name)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DriveFileHolder result = task.getResult();
                            String path = getStoragePath(result.getName());
                            mDriveHelper.downloadFile(result.getId(), path)
                                    .addOnCompleteListener(uploadTask -> {
                                        if (uploadTask.isSuccessful()) {
                                            Log.d("omni_download", "Downloaded attachment" + result.getName());
                                        } else {
                                            uploadTask.getException().printStackTrace();
                                        }
                                    });
                    }
                });
    }


    protected void syncAttachmentsToRemote(Note note) {
        note.getAttachmentsList().forEach(attachment -> {
                List<String> segments = attachment.getUri().getPathSegments();
                String filename = segments.get(segments.size() - 1);
                File f = new File(StorageHelper.getAttachmentDir() + "/" + filename);
                updateRemoteAttachment(f, attachment.getMime_type());
        });
    }

    protected void updateRemoteAttachment(File file, String mime) {
        mDriveHelper
                .findFile(file.getName())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DriveFileHolder result = task.getResult();
                        if (!result.exists()) {
                            mDriveHelper.uploadFile(file, mime, null)
                                    .addOnCompleteListener(uploadTask -> {
                                        if (uploadTask.isSuccessful()) {
                                            Log.d("omni_upload", "Synced file" + file.getName());
                                        } else {
                                            uploadTask.getException().printStackTrace();
                                        }
                                    });
                        }
                    }
                });
    }

    public void onAccountConnectionResult(Intent intent, AccountConnectionResultHandler handler) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(intent);
        try {
            acc = task.getResult(ApiException.class);
            gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                    .requestEmail()
                    .build();
            if (acc != null) {
                prepareDriveService();
            }
            Log.d("sync-sign-in", acc.toString());
            handler.onSuccess(acc);
        } catch (Exception e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            e.printStackTrace();
            handler.onFailure((ApiException) e);
        }
    }


    protected Uri getStorageUriFromRemoteAttachment(Attachment attachment) {
        return Uri.parse("file://" + getStoragePath(attachment.getUri().toString()));
    }

    protected String getStoragePath(String filename) {
        return StorageHelper.getAttachmentDir() + "/" + filename;
    }
}
