package it.feio.android.omninotes.utils;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.models.Note;

public class SyncManager {
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

    public boolean isAccountConnected() {
        return acc != null;
    }
    public boolean isDriveReady() { return mDriveHelper != null; }

    public GoogleSignInAccount getConnectedAccount() {
        return acc;
    }

    public void initAccountConnection(Activity activity) {
        GoogleSignInClient client = GoogleSignIn.getClient(OmniNotes.getAppContext(), gso);
        Intent signInIntent = client.getSignInIntent();
        activity.startActivityForResult(signInIntent, SIGNIN_REQUEST_CODE);
    }

    public void disconnectAccount(Activity activity, AccountDisconnectionResultHandler handler) {
        GoogleSignInClient client = GoogleSignIn.getClient(OmniNotes.getAppContext(), gso);
        client.signOut().addOnCompleteListener(activity, task -> {
            try {
                task.getResult();
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

    public void syncNote(Note note) {
        String notePrefix = "omni_" + note.get_id().toString();
        if (isDriveReady()) {
            mDriveHelper
                    .findFileByPrefix(notePrefix)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DriveFileHolder result = task.getResult();
                            if (result.exists()) {
                                updateNote(result.getId(), note);
                            } else {
                                uploadNote(note);
                            }
                        }
                    });

        }
    }

    protected void updateNote(String fileId, Note note) {
        String notePrefix = "omni_" + note.get_id().toString();
        String noteFile = notePrefix + "_" + note.getLastModification();
        mDriveHelper.saveFile(fileId, noteFile, note.toJSON())
                .addOnCompleteListener(task ->  {
                    if (task.isSuccessful()) {
                        Log.d("omni_upload", "file update finished");
                    } else {
                        Log.d("omni_upload", "file upload failed");
                    }
                });
        syncAttachments(note);
    }

    protected void uploadNote(Note note) {
        String notePrefix = "omni_" + note.get_id().toString();
        String noteFile = notePrefix + "_" + note.getLastModification();
        mDriveHelper.createFile(noteFile, note.toJSON())
                .addOnCompleteListener(task ->  {
                    if (task.isSuccessful()) {
                        Log.d("omni_upload", "file upload finished");
                    } else {
                        Log.d("omni_upload", "file upload failed");
                    }
                });
        syncAttachments(note);
    }

    protected void syncAttachments(Note note) {
        note.getAttachmentsList().forEach(attachment -> {
                List<String> segments = attachment.getUri().getPathSegments();
                String filename = segments.get(segments.size() - 1);
                File f = new File(StorageHelper.getAttachmentDir() + "/" + filename);
                updateAttachment(f, attachment.getMime_type());
        });
    }

    protected void updateAttachment(File file, String mime) {
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
            Log.d("sync-sign-in", acc.toString());
            handler.onSuccess(acc);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("sync-sign-in", "signInResult:failed code=" + e.getStatusCode());
            handler.onFailure(e);
        }
    }
}
