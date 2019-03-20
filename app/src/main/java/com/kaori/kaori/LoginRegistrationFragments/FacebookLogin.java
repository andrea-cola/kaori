package com.kaori.kaori.LoginRegistrationFragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.android.volley.Response;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.kaori.kaori.Kaori;
import com.kaori.kaori.Model.User;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.DataManager;
import com.kaori.kaori.Utils.LogManager;

/*package-private*/ class FacebookLogin {

    private boolean facebookSignInStarted;
    private CallbackManager mCallbackManager;
    private Context context;
    private LoginButton loginButton;
    private static FacebookLogin instance;

    private FacebookLogin(Context context, LoginButton loginButton) {
        this.context = context;
        this.loginButton = loginButton;
    }

    /*package-private*/ static void initialize(Context context, LoginButton loginButton){
        instance = new FacebookLogin(context, loginButton);
    }

    /*package-private*/ static FacebookLogin getInstance() {
        return instance;
    }

    /*package-private*/ CallbackManager getCallbackManager(){
        return mCallbackManager;
    }

    /*package-private*/ void loginWithFacebook(){
        LogManager.getInstance().printConsoleMessage("loginWithFacebook");
        this.facebookSignInStarted = true;
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
                //loginError = LOGIN_FAILED;
                endLogin(false);
            }

            @Override
            public void onError(FacebookException error) {
                LogManager.getInstance().printConsoleMessage("loginWithFacebook:error");
                //loginError = LOGIN_FAILED;
                endLogin(false);
            }
        });
        loginButton.performClick();
    }

    private void firebaseAuthWithFacebook(AccessToken token) {
        LogManager.getInstance().printConsoleMessage("firebaseAuthWithFacebook");
        FirebaseAuth.getInstance().signInWithCredential(FacebookAuthProvider.getCredential(token.getToken()))
                .addOnCompleteListener((Activity)context, task -> {
                    if (task.isSuccessful()) {
                        LogManager.getInstance().printConsoleMessage("firebaseAuthWithFacebook:success");
                        validateLogin(task.getResult().getUser());
                    } else {
                        LogManager.getInstance().printConsoleMessage("firebaseAuthWithFacebook:failure");
                        //loginError = LOGIN_FAILED;
                        endLogin(false);
                    }
                });
    }

    private void validateLogin(FirebaseUser firebaseUser){
        Response.Listener<String> listener = response -> {
            int code = Integer.parseInt(response);
            if(code == Constants.USER_NOT_EXISTS) {
                LogManager.getInstance().printConsoleMessage("L'utente va registrato");
                registerNewUser(firebaseUser, Constants.FACEBOOK);
            }
            else if(code == Constants.USER_EXISTS_AND_CORRECT_METHOD) {
                LogManager.getInstance().printConsoleMessage("Login effettuato.");
                endLogin(true);
            }
            else {
                //logout e ritorno alla home
                // TODO: dire all'utente che metodo ha usato la volta precedente,
                // TODO: basta confrontare il code
                LogManager.getInstance().printConsoleMessage(response);
                FirebaseAuth.getInstance().signOut();
                endLogin(false);
            }
        };

        Response.ErrorListener errorListener = error -> {
            LogManager.getInstance().showVisualMessage("Errore durante l'autenticazione. " + error);
            FirebaseAuth.getInstance().signOut();
            LoginManager.getInstance().logOut();
            endLogin(false);
        };

        DataManager.getInstance().checkIfTheUserAlreadyExists(firebaseUser.getEmail(), Constants.FACEBOOK, listener, errorListener);
    }

    private void registerNewUser(FirebaseUser firebaseUser, int method) {
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            LogManager.getInstance().printConsoleMessage("Getting the token...");
            String tokenID = "";
            if (task.isSuccessful())
                tokenID = task.getResult().getToken();

            User user = new User(firebaseUser.getUid(), firebaseUser.getEmail(), firebaseUser.getDisplayName(),
                    firebaseUser.getPhotoUrl().toString(), tokenID, method);
            DataManager.getInstance().createNewUser(user, response1 -> endLogin(true), error -> endLogin(false));
        });
    }

    // TODO: sistemare mettendo i messaggi completi per fare capire all'utente cosa succede.
    private void endLogin(boolean isSuccess){
        if(this.facebookSignInStarted)
            facebookSignInStarted = false;

        if (isSuccess && context != null) {
            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    LogManager.getInstance().printConsoleError(task.getException().getMessage() + "getInstanceId failed");
                    return;
                }
                context.startActivity(new Intent(context, Kaori.class));
                ((Activity) context).finish();
            });
        }
        else if(!isSuccess && context != null) {
            LogManager.getInstance().showVisualError(null, "Errore nel FacebookLogin");
            (new Handler()).postDelayed(() -> {
                context.startActivity(new Intent(context, KaoriLogin.class));
                ((Activity)context).finish();
            }, 3000);
        }
    }

}
