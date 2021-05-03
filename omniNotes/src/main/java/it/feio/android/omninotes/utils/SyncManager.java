package it.feio.android.omninotes.utils;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.services.drive.DriveScopes;

public class SyncManager {
    public static final int SIGNIN_REQUEST_CODE = 101;
    private GoogleSignInAccount acc;
    private GoogleSignInOptions gso;
    private Activity context;

    public interface AccountConnectionResultHandler {
        void onSuccess(GoogleSignInAccount account);
        void onFailure(ApiException e);
    }

    public interface AccountDisconnectionResultHandler {
        void onSuccess();
        void onFailure(Exception e);
    }

    public SyncManager(Activity context) {
        this.context = context;
        acc = GoogleSignIn.getLastSignedInAccount(context);
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(DriveScopes.DRIVE))
                .requestEmail()
                .build();
    }

    public boolean isAccountConnected() {
        return acc != null;
    }

    public GoogleSignInAccount getConnectedAccount() {
        return acc;
    }

    public void initAccountConnection() {
        GoogleSignInClient client = GoogleSignIn.getClient(context, gso);
        Intent signInIntent = client.getSignInIntent();
        context.startActivityForResult(signInIntent, SIGNIN_REQUEST_CODE);
    }

    public void disconnectAccount(AccountDisconnectionResultHandler handler) {
        GoogleSignInClient client = GoogleSignIn.getClient(context, gso);
        client.signOut().addOnCompleteListener(context, task -> {
            try {
                task.getResult();
                handler.onSuccess();
            } catch (Exception e) {
                Log.w("sign-out", "signOut:failed -" + e.toString());
                handler.onFailure(e);
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

    public void setContext(Activity context) {
        this.context = context;
    }
}
