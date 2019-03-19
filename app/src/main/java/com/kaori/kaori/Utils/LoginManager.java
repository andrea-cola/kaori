package com.kaori.kaori.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.android.volley.Response;
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
import com.google.firebase.iid.FirebaseInstanceId;
import com.kaori.kaori.Kaori;
import com.kaori.kaori.KaoriLogin;
import com.kaori.kaori.R;

public class LoginManager {

    private static LoginManager loginManager;
    private static Context context;
    private FirebaseAuth mAuth;
    private CallbackManager mCallbackManager;
    private String loginError;

    private final String WRONG_CREDENTIALS = "Le credenziali sono errate";
    private final String LOGIN_FAILED = "Login fallito. Riprova.";
    private boolean facebookSignInStarted;

    private LoginManager(){
        mAuth = FirebaseAuth.getInstance();
        loginError = "";
        facebookSignInStarted = false;
    }

    public static LoginManager getInstance(){
        if(loginManager == null)
            loginManager = new LoginManager();
        return loginManager;
    }

    public static LoginManager getInstance(Context c){
        context = c;
        return getInstance();
    }

    public boolean getSignInFacebookStarted(){
        return this.facebookSignInStarted;
    }

    public CallbackManager getCallbackManager(){
        return this.mCallbackManager;
    }

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

    public void loginWithFacebook(LoginButton loginButton){
        LogManager.getInstance().printConsoleMessage("loginWithFacebook");
        this.facebookSignInStarted = true;
        loginButton.registerCallback(CallbackManager.Factory.create(), new FacebookCallback<LoginResult>() {
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

    private void firebaseAuthWithFacebook(AccessToken token) {
        Response.Listener<String> listener = response -> {
            if(response.equalsIgnoreCase("1000")) // l'utente va registrato
                endLogin(true);
            else if(response.equalsIgnoreCase("1001")) // login ok
                endLogin(true);
            else {
                //logout e ritorno alla home TODO
                LogManager.getInstance().showVisualMessage(response);
                mAuth.signOut();
                endLogin(false);
            }
        };
        Response.ErrorListener errorListener = error -> {
            LogManager.getInstance().showVisualMessage("Errore durante l'autenticazione.");
            mAuth.signOut();
            endLogin(false);
        };

        LogManager.getInstance().printConsoleMessage("firebaseAuthWithFacebook");
        mAuth.signInWithCredential(FacebookAuthProvider.getCredential(token.getToken()))
                .addOnCompleteListener((Activity)context, task -> {
                    if (task.isSuccessful()) {
                        LogManager.getInstance().printConsoleMessage("firebaseAuthWithFacebook:success");
                        DataManager.getInstance().checkIfTheUserAlreadyExists(task.getResult().getUser().getEmail(), Constants.FACEBOOK, listener, errorListener);
                    } else {
                        LogManager.getInstance().printConsoleMessage("firebaseAuthWithFacebook:failure");
                        loginError = LOGIN_FAILED;
                        endLogin(false);
                    }
                });
    }

    public void loginWithGoogle(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        ((KaoriLogin)context).startActivityForResult(signInIntent, 0);
    }

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

    private void endLogin(boolean isSuccess){
        if(this.facebookSignInStarted)
            facebookSignInStarted = false;

        if (isSuccess && context != null) {
            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    LogManager.getInstance().printConsoleError(task.getException().getMessage() + "getInstanceId failed");
                    return;
                }
                updateTokens(task.getResult().getToken());
                context.startActivity(new Intent(context, Kaori.class));
                ((Activity) context).finish();
            });
        }
        else if(!isSuccess && context != null) {
            LogManager.getInstance().showVisualError(null, loginError);
            (new Handler()).postDelayed(() -> {
                context.startActivity(new Intent(context, KaoriLogin.class));
                ((Activity)context).finish();
            }, 3000);
        }
    }

    private void updateTokens(String token){
        /*FirebaseFirestore.getInstance()
                .collection(Constants.DB_COLL_USERS)
                .document(mAuth.getUid())
                .update(Constants.FIELD_TOKEN, token);

        CollectionReference messages = FirebaseFirestore.getInstance().collection(Constants.DB_COLL_MESSAGES);
        messages.get()
                .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task1.getResult()) {
                            Chat c = document.toObject(Chat.class);
                            if(c.getUsers().get(0).getUid().equals(mAuth.getUid()))
                                c.getUsers().get(0).setTokenID(token);
                            else if(c.getUsers().get(1).getUid().equals(mAuth.getUid()))
                                c.getUsers().get(1).setTokenID(token);
                            messages.document(c.getChatID()).set(c);
                        }
                    }
                });*/
    }

}