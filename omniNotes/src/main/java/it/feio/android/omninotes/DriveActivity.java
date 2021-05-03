package it.feio.android.omninotes;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;

import java.util.Collections;

import it.feio.android.omninotes.utils.DriveServiceHelper;
import it.feio.android.omninotes.utils.SyncManager;

public class DriveActivity extends AppCompatActivity {
    public SyncManager sync;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive);

        sync = new SyncManager(this);

        if (!sync.isAccountConnected()){
            sync.initAccountConnection();
        }
        else {

            showFiles();

        }
    }

    private void showFiles() {
        GoogleAccountCredential credential =
                GoogleAccountCredential.usingOAuth2(
                        this, Collections.singleton(DriveScopes.DRIVE_FILE));
        credential.setSelectedAccount(sync.getConnectedAccount().getAccount());
        com.google.api.services.drive.Drive googleDriveService =
                new com.google.api.services.drive.Drive.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        new GsonFactory(),
                        credential)
                        .setApplicationName("AppName")
                        .build();
        DriveServiceHelper mDriveServiceHelper = new DriveServiceHelper(googleDriveService);
        Task createFile = mDriveServiceHelper.saveFile("foo", "foo", "Hello");
        createFile.getResult();
        Task listFiles = mDriveServiceHelper.queryFiles();
        FileList files = (FileList) listFiles.getResult();
        Log.d("files", files.toString());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == SyncManager.SIGNIN_REQUEST_CODE) {
            sync.onAccountConnectionResult(intent, new SyncManager.AccountConnectionResultHandler() {
                @Override
                public void onSuccess(GoogleSignInAccount account) {
                    showFiles();
                }

                @Override
                public void onFailure(ApiException e) {

                }
            });
        }
    }
}
