package it.feio.android.omninotes.utils;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Pair;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * A utility for performing read/write operations on Drive files via the REST API and opening a
 * file picker UI via Storage Access Framework.
 */
public class DriveServiceHelper {
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final Drive mDriveService;

    public static String TYPE_AUDIO = "application/vnd.google-apps.audio";
    public static String TYPE_GOOGLE_DOCS = "application/vnd.google-apps.document";
    public static String TYPE_GOOGLE_DRAWING = "application/vnd.google-apps.drawing";
    public static String TYPE_GOOGLE_DRIVE_FILE = "application/vnd.google-apps.file";
    public static String TYPE_GOOGLE_DRIVE_FOLDER = "application/vnd.google-apps.folder";
    public static String TYPE_GOOGLE_FORMS = "application/vnd.google-apps.form";
    public static String TYPE_GOOGLE_FUSION_TABLES = "application/vnd.google-apps.fusiontable";
    public static String TYPE_GOOGLE_MY_MAPS = "application/vnd.google-apps.map";
    public static String TYPE_PHOTO = "application/vnd.google-apps.photo";
    public static String TYPE_GOOGLE_SLIDES = "application/vnd.google-apps.presentation";
    public static String TYPE_GOOGLE_APPS_SCRIPTS = "application/vnd.google-apps.script";
    public static String TYPE_GOOGLE_SITES = "application/vnd.google-apps.site";
    public static String TYPE_GOOGLE_SHEETS = "application/vnd.google-apps.spreadsheet";
    public static String TYPE_UNKNOWN = "application/vnd.google-apps.unknown";
    public static String TYPE_VIDEO = "application/vnd.google-apps.video";
    public static String TYPE_3_RD_PARTY_SHORTCUT = "application/vnd.google-apps.drive-sdk";

    public DriveServiceHelper(Drive driveService) {
        mDriveService = driveService;
    }

    /**
     * Find file that contains the string
     * @return
     */
    public Task<DriveFileHolder> findFileByPrefix(String prefix) {
        return Tasks.call(mExecutor, () -> {

            FileList result = mDriveService.files().list()
                    .setQ("name contains '" + prefix + "' and trashed = false")
                    .setSpaces("drive")
                    .setFields("files(id, name,size,createdTime,modifiedTime,starred)")
                    .execute();
            DriveFileHolder googleDriveFileHolder = new DriveFileHolder();
            if (result.getFiles().size() > 0) {

                googleDriveFileHolder.setId(result.getFiles().get(0).getId());
                googleDriveFileHolder.setName(result.getFiles().get(0).getName());
                googleDriveFileHolder.setModifiedTime(result.getFiles().get(0).getModifiedTime());
                googleDriveFileHolder.setSize(result.getFiles().get(0).getSize());
            }


            return googleDriveFileHolder;
        });
    }

    /**
     * Find files that contain the string
     * @return
     */
    public Task<List<DriveFileHolder>> findFilesByPrefix(String prefix) {
        return Tasks.call(mExecutor, () -> {

            FileList result = mDriveService.files().list()
                    .setQ("name contains '" + prefix + "' and trashed = false")
                    .setSpaces("drive")
                    .setFields("files(id, name,size,createdTime,modifiedTime,starred)")
                    .execute();

            List<DriveFileHolder> fileList = new ArrayList<>();
            if (result.getFiles().size() > 0) {
                result.getFiles().forEach(file -> {
                    DriveFileHolder holder =  new DriveFileHolder();
                    holder.setId(file.getId());
                    holder.setName(file.getName());
                    holder.setModifiedTime(file.getModifiedTime());
                    holder.setSize(file.getSize());
                    fileList.add(holder);
                });
            }


            return fileList;
        });
    }

    /**
     * Find file that contains the string
     * @return
     */
    public Task<DriveFileHolder> findFile(String filename) {
        return Tasks.call(mExecutor, () -> {

            FileList result = mDriveService.files().list()
                    .setQ("name = '" + filename + "' and trashed = false")
                    .setSpaces("drive")
                    .setFields("files(id, name,size,createdTime,modifiedTime,starred)")
                    .execute();
            DriveFileHolder googleDriveFileHolder = new DriveFileHolder();
            if (result.getFiles().size() > 0) {

                googleDriveFileHolder.setId(result.getFiles().get(0).getId());
                googleDriveFileHolder.setName(result.getFiles().get(0).getName());
                googleDriveFileHolder.setModifiedTime(result.getFiles().get(0).getModifiedTime());
                googleDriveFileHolder.setSize(result.getFiles().get(0).getSize());
            }


            return googleDriveFileHolder;
        });
    }

    public Task<Void> deleteFolderFile(String fileId) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the metadata as a File object.
            if (fileId != null) {
                mDriveService.files().delete(fileId).execute();
            }

            return null;
        });
    }

    /**
     * Creates a text file in the user's My Drive folder and returns its file ID.
     */
    public Task<String> createFile(String filename, String content, @Nullable Map<String, String> properties) {
        return Tasks.call(mExecutor, () -> {
            File metadata = new File()
                    .setParents(Collections.singletonList("root"))
                    .setMimeType("text/plain")
                    .setName(filename);
            if (properties != null) {
                metadata.setProperties(properties);
            }



            ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", content);
            Drive.Files.Create request = mDriveService.files().create(metadata, contentStream);
            File googleFile = request.execute();
            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }

            return googleFile.getId();
        });
    }

    /**
     * Opens the file identified by {@code fileId} and returns a {@link Pair} of its name and
     * contents.
     */
    public Task<Pair<String, String>> readFile(String fileId) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the metadata as a File object.
            File metadata = mDriveService.files().get(fileId).execute();
            String name = metadata.getName();

            // Stream the file contents to a String.
            try (InputStream is = mDriveService.files().get(fileId).executeMediaAsInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                String contents = stringBuilder.toString();

                return Pair.create(name, contents);
            }
        });
    }

    /**
     * Updates the file identified by {@code fileId} with the given {@code name} and {@code
     * content}.
     */
    public Task<Void> saveFile(String fileId, String name, String content, @Nullable Map<String, String> properties) {
        return Tasks.call(mExecutor, () -> {
            // Create a File containing any metadata changes.
            File metadata = new File().setName(name);
            if (properties != null) {
                metadata.setProperties(properties);
            }

            // Convert content to an AbstractInputStreamContent instance.
            ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", content);

            // Update the metadata and contents.
            mDriveService.files().update(fileId, metadata, contentStream).execute();
            return null;
        });
    }

    /**
     * Updates the file identified by {@code fileId} with the given {@code name} and {@code
     * content}.
     */
    public Task<Void> saveFile(String fileId, java.io.File localFile, String mime) {
        return Tasks.call(mExecutor, () -> {
            // Create a File containing any metadata changes.
            File metadata = new File().setName(localFile.getName());

            // Convert content to an AbstractInputStreamContent instance.
            FileContent fileContent = new FileContent(mime, localFile);

            // Update the metadata and contents.
            mDriveService.files().update(fileId, metadata, fileContent).execute();
            return null;
        });
    }

    /**
     * Returns a {@link FileList} containing all the visible files in the user's My Drive.
     *
     * <p>The returned list will only contain files visible to this app, i.e. those which were
     * created by this app. To perform operations on files not created by the app, the project must
     * request Drive Full Scope in the <a href="https://play.google.com/apps/publish">Google
     * Developer's Console</a> and be submitted to Google for verification.</p>
     */
    public Task<FileList> queryFiles() {
        return Tasks.call(mExecutor, () ->
                mDriveService.files().list().setSpaces("drive").execute());
    }

    /**
     * Returns an {@link Intent} for opening the Storage Access Framework file picker.
     */
    public Intent createFilePickerIntent() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");

        return intent;
    }

    /**
     * Opens the file at the {@code uri} returned by a Storage Access Framework {@link Intent}
     * created by {@link #createFilePickerIntent()} using the given {@code contentResolver}.
     */
    public Task<Pair<String, String>> openFileUsingStorageAccessFramework(
            ContentResolver contentResolver, Uri uri) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the document's display name from its metadata.
            String name;
            try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    name = cursor.getString(nameIndex);
                } else {
                    throw new IOException("Empty cursor returned for file.");
                }
            }

            // Read the document's contents as a String.
            String content;
            try (InputStream is = contentResolver.openInputStream(uri);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                content = stringBuilder.toString();
            }

            return Pair.create(name, content);
        });
    }


    /**
     * Uploads generic file using local filename
     */
    public Task<DriveFileHolder> uploadFile(final java.io.File localFile, final String mimeType, @Nullable final String folderId) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the metadata as a File object.
            List<String> root;
            if (folderId == null) {
                root = Collections.singletonList("root");
            } else {

                root = Collections.singletonList(folderId);
            }

            File metadata = new File()
                    .setParents(root)
                    .setMimeType(mimeType)
                    .setName(localFile.getName());

            FileContent fileContent = new FileContent(mimeType, localFile);

            File fileMeta = mDriveService.files().create(metadata, fileContent).execute();
            DriveFileHolder googleDriveFileHolder = new DriveFileHolder();
            googleDriveFileHolder.setId(fileMeta.getId());
            googleDriveFileHolder.setName(fileMeta.getName());
            return googleDriveFileHolder;
        });
    }

    public Task<String> downloadFileContents(String fileId) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the metadata as a File object.
            OutputStream outputStream = new ByteArrayOutputStream();
            mDriveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
            return outputStream.toString();
        });
    }

    public Task<String> downloadFile(String fileId, String path) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the metadata as a File object.
            OutputStream outputStream = new FileOutputStream(path);
            mDriveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
            return outputStream.toString();
        });
    }
}