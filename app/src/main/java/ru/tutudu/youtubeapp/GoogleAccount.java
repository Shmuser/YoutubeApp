package ru.tutudu.youtubeapp;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

//import com.google.android.gms.auth.api.signin.GoogleSignIn;
//import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;


/**
 * Created by vlad on 08.07.18.
 */

public class GoogleAccount {

    private static GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 13512;



    public static void init(Context context) {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .build();
        //Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    public static GoogleSignInAccount getAccount(Context context) {

        return GoogleSignIn.getLastSignedInAccount(context);
    }

    public static GoogleSignInClient getClient() {
        return mGoogleSignInClient;
    }

    public static int getCode() {
        return RC_SIGN_IN;
    }

    public static void signOut(Context context) {
        Task<GoogleSignInAccount> task = mGoogleSignInClient.silentSignIn();
        if (task.isSuccessful()) {
            mGoogleSignInClient.signOut();
        }
        else {
            Toast.makeText(context, "Something gone wrong :(", Toast.LENGTH_SHORT).show();
        }
    }
}
