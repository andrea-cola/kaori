package com.kaori.kaori;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.kaori.kaori.LoginRegistrationFragments.CreateAccount;
import com.kaori.kaori.LoginRegistrationFragments.LoginFragment;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.LogManager;
import com.kaori.kaori.Utils.LoginManager;
import com.kaori.kaori.Utils.SignInManager;

/**
 * Activity responsible for the Login and Signin.
 */
public class KaoriLogin extends AppCompatActivity {

    /**
     * Variables.
     */
    private final String BACK_STATE_NAME = getClass().getName();
    private LoginManager loginManager;
    private SignInManager signInManager;

    /**
     * OnCreate override.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // instantiate the managers for login and sign in.
        loginManager = LoginManager.getInstance(this);
        signInManager = SignInManager.getInstance(this);
        LogManager.setView(findViewById(R.id.coordinator));

        // set click listeners
        findViewById(R.id.login_button).setOnClickListener(view -> invokeNextFragment(new LoginFragment()));
        findViewById(R.id.signin_button).setOnClickListener(view -> invokeNextFragment(new CreateAccount()));
    }

    /**
     * Method used to invoke a new fragment.
     */
    private void invokeNextFragment(Fragment f) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_layout, f)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(BACK_STATE_NAME)
                .commit();
    }

    /**
     * Method used to retrieve and use the result of the login
     * done by Facebook and Google.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.GOOGLE_LOGIN_REQUEST) {
            try {
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                loginManager.firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                LogManager.getInstance().showVisualError(this, e, "Google sign in failed");
            }
        } else if (requestCode == Constants.GOOGLE_SIGNIN_REQUEST) {
            LogManager.getInstance().printConsoleMessage("onActivityResult:GoogleSignInRequest");
            signInManager.handleGoogleSignIn(GoogleSignIn.getSignedInAccountFromIntent(data));
        } else if (signInManager.getSignInFacebookStarted()) {
            LogManager.getInstance().printConsoleMessage("onActivityResult:FacebookSignInRequest");
            signInManager.getCallbackManager().onActivityResult(requestCode, resultCode, data);
        } else if (loginManager.getSignInFacebookStarted()) {
            LogManager.getInstance().printConsoleMessage("onActivityResult:FacebookLoginRequest");
            loginManager.getCallbackManager().onActivityResult(requestCode, resultCode, data);
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

}