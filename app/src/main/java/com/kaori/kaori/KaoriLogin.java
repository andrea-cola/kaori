package com.kaori.kaori;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.kaori.kaori.LoginRegistrationFragments.CreateAccount;
import com.kaori.kaori.LoginRegistrationFragments.LoginFragment;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.LogManager;
import com.kaori.kaori.Utils.LoginManager;
import com.kaori.kaori.Utils.SignInManager;

public class KaoriLogin extends AppCompatActivity {

    /**
     * Variables.
     */
    private final String BACK_STATE_NAME = getClass().getName();
    private Button mLogin, mRegistration;
    private LoginManager loginManager;
    private SignInManager signInManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        mLogin = findViewById(R.id.login_button);
        mRegistration = findViewById(R.id.signin_button);
        setClickListeners();

        loginManager = LoginManager.getInstance(this);
        signInManager = SignInManager.getInstance(this);
    }

    private void setClickListeners(){
        mLogin.setOnClickListener(view -> invokeNextFragment(new LoginFragment()));
        mRegistration.setOnClickListener(view -> invokeNextFragment(new CreateAccount()));
    }

    private void invokeNextFragment(Fragment f){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_layout, f)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(BACK_STATE_NAME)
                .commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.GOOGLE_LOGIN_REQUEST) {
            LogManager.getInstance().printConsoleMessage("onActivityResult:GoogleLoginRequest");
            try {
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                loginManager.firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                LogManager.getInstance().showVisualError(this, e, "Google sign in failed");
            }
        }
        else if(requestCode == Constants.GOOGLE_SIGNIN_REQUEST) {
            LogManager.getInstance().printConsoleMessage("onActivityResult:GoogleSignInRequest");
            signInManager.handleGoogleSignIn(GoogleSignIn.getSignedInAccountFromIntent(data));
        }
        else if(signInManager.getSignInFacebookStarted()) {
            LogManager.getInstance().printConsoleMessage("onActivityResult:FacebookSignInRequest");
            signInManager.getCallbackManager().onActivityResult(requestCode, resultCode, data);
        }
        else {
            LogManager.getInstance().printConsoleMessage("onActivityResult:FacebookLoginRequest");
            loginManager.getCallbackManager().onActivityResult(requestCode, resultCode, data);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
