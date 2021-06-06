package it.feio.android.omninotes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.accounts.Account;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import it.feio.android.omninotes.utils.DriveServiceHelper;
import it.feio.android.omninotes.utils.SyncManager;

public class DriveActivity extends AppCompatActivity {
    public SyncManager sync;
    private final Executor mExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive);

        sync = SyncManager.getInstance();
        DriveActivity s = this;
        sync.disconnectAccount(s, new SyncManager.AccountDisconnectionResultHandler() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(Exception e) {

            }
        });

    }

    private void showFiles() {
        GoogleAccountCredential credential =
                GoogleAccountCredential.usingOAuth2(
                        getApplicationContext(), Collections.singleton(DriveScopes.DRIVE_FILE));
        Account acc = sync.getConnectedAccount().getAccount();
        credential.setSelectedAccount(acc);
        com.google.api.services.drive.Drive googleDriveService =
                new com.google.api.services.drive.Drive.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        new GsonFactory(),
                        credential)
                        .setApplicationName("Sekcja-OmniNotes")
                        .build();
        DriveServiceHelper mDriveServiceHelper = new DriveServiceHelper(googleDriveService);
//        Task<String> t = mDriveServiceHelper.createFile("121_Nazwa_12131231");
//        t.addOnCompleteListener(task -> {
//            if (task.isSuccessful() && task.getResult() != null) {
//                Log.d("TAG", task.getResult());
//            } else {
//                startActivityForResult(((UserRecoverableAuthIOException) task.getException()).getIntent(), 10);
//                Log.w("TAG", "getLastLocation:exception", task.getException());
//            }
//        });
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
