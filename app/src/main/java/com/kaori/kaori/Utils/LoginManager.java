package com.kaori.kaori.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.kaori.kaori.Kaori;
import com.kaori.kaori.KaoriLogin;
import com.kaori.kaori.R;

/**
 * This class handles the login process.
 */
public class LoginManager {

    /**
     * Variables.
     */
    private static LoginManager loginManager;
    private static Context context;
    private FirebaseAuth mAuth;
    private CallbackManager mCallbackManager;
    private String loginError;

    /**
     * Constants.
     */
    private final String WRONG_CREDENTIALS = "Le credenziali sono errate";
    private final String LOGIN_FAILED = "Login fallito. Riprova.";

    /**
     * Class constructor.
     */
    private LoginManager(){
        mAuth = FirebaseAuth.getInstance();
        loginError = "";
    }

    /**
     * Return the instance of the Login Manager.
     */
    public static LoginManager getInstance(){
        if(loginManager == null)
            loginManager = new LoginManager();
        return loginManager;
    }

    /**
     * This method is used to change the context of the login manager
     * and retrieve the instance.
     */
    public static LoginManager getInstance(Context c){
        context = c;
        return getInstance();
    }

    /**
     * Handles the login with email.
     */
    public void loginWithEmail(String username, String password){
        LogManager.getInstance().printConsoleMessage("loginWithEmail");
        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if(context != null) {
                            LogManager.getInstance().printConsoleMessage("loginWithEmail:success");
                            endLogin(true);
                        }
                    } else {
                        LogManager.getInstance().printConsoleError("loginWithEmail:failure");
                        loginError = WRONG_CREDENTIALS;
                        endLogin(false);
                    }
                });
    }

    /**
     * Set up the Facebook signInWithEmail and perform a click on the real button.
     */
    public void loginWithFacebook(LoginButton loginButton){
        LogManager.getInstance().printConsoleMessage("loginWithFacebook");
        mCallbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                LogManager.getInstance().printConsoleMessage("loginWithFacebook:success");
                firebaseAuthWithFacebook(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                LogManager.getInstance().printConsoleMessage("loginWithFacebook:cancel");
                loginError = LOGIN_FAILED;
                endLogin(false);
            }

            @Override
            public void onError(FacebookException error) {
                LogManager.getInstance().printConsoleMessage("loginWithFacebook:error");
                loginError = LOGIN_FAILED;
                endLogin(false);
            }

        });
        loginButton.performClick();
    }

    /**
     * Getter of CallbackManager.
     */
    public CallbackManager getCallbackManager(){
        return this.mCallbackManager;
    }

    /**
     * Hande the Google response.
     */
    public void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        LogManager.getInstance().printConsoleMessage("loginWithGoogle");
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener((Activity)context, task -> {
                    if (task.isSuccessful()) {
                        LogManager.getInstance().printConsoleMessage("loginWithGoogle:success");
                        endLogin(true);
                    } else {
                        LogManager.getInstance().printConsoleMessage("loginWithGoogle:fail");
                        loginError = LOGIN_FAILED;
                        endLogin(false);
                    }
                });
    }

    /**
     * Handle the Facebook response.
     */
    private void firebaseAuthWithFacebook(AccessToken token) {
        LogManager.getInstance().printConsoleMessage("firebaseAuthWithFacebook");
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener((Activity)context, task -> {
                    if (task.isSuccessful()) {
                        LogManager.getInstance().printConsoleMessage("firebaseAuthWithFacebook:success");
                        endLogin(true);
                    } else {
                        LogManager.getInstance().printConsoleMessage("firebaseAuthWithFacebook:failure");
                        loginError = LOGIN_FAILED;
                        endLogin(false);
                    }
                });
    }

    /**
     * This method is called whenever the process is terminated.
     * If successful restart the app, if not it only dismisses the
     * dialog.
     */
    private void endLogin(boolean isSuccess){
        if (isSuccess && context != null) {
            LogManager.getInstance().printConsoleMessage("endLogin:success");
            context.startActivity(new Intent(context, Kaori.class));
            ((Activity) context).finish();
        }
        else if(!isSuccess && context != null) {
            LogManager.getInstance().showVisualError(context, null, loginError);
            (new Handler()).postDelayed(() -> {
                context.startActivity(new Intent(context, KaoriLogin.class));
                ((Activity)context).finish();
            }, 3000);
        }
    }

    /**
     * Set up the Google signInWithEmail.
     */
    public void loginWithGoogle(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        ((KaoriLogin)context).startActivityForResult(signInIntent, 0);
    }

}