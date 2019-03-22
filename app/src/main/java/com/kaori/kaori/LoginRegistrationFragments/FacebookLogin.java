package com.kaori.kaori.LoginRegistrationFragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.android.volley.Response;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
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

import org.json.JSONException;

/*package-private*/ class FacebookLogin {

    private static FacebookLogin instance;
    private CallbackManager mCallbackManager;
    private Context context;
    private LoginButton loginButton;
    private boolean facebookFlag;

    private FacebookLogin(Context context, LoginButton loginButton) {
        this.context = context;
        this.loginButton = loginButton;
        facebookFlag = false;
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
        facebookFlag = true;
        LogManager.getInstance().printConsoleMessage("Facebook login -> step 1");
        mCallbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                getFacebookEmail(loginResult);
            }

            @Override
            public void onCancel() {
                endLogin(false, null);
            }

            @Override
            public void onError(FacebookException error) {
                endLogin(false, Constants.FACEBOOK_ERROR);
            }
        });
        loginButton.performClick();
    }

    private void getFacebookEmail(LoginResult loginResult){
        LogManager.getInstance().printConsoleMessage("Facebook login -> step 2");
        GraphRequest request = GraphRequest.newMeRequest(
                loginResult.getAccessToken(),
                (object, response) -> {
                    try {
                        validateProvider(object.getString("email"), loginResult.getAccessToken());
                    } catch (JSONException e) {
                        endLogin(false, Constants.GENERIC_ERROR);
                    }
                });
        Bundle params = new Bundle();
        params.putString("fields", "email");
        request.setParameters(params);
        request.executeAsync();
        request.executeAsync();
    }

    private void validateProvider(String email, AccessToken token) {
        LogManager.getInstance().printConsoleMessage("Facebook login -> step 3");
        DataManager.getInstance().createValidationProviderRequest(
                response -> {
                    if(Integer.parseInt(response) == 1)
                        firebaseAuthWithFacebook(token);
                    else {
                        LoginManager.getInstance().logOut();
                        endLogin(false, Constants.WRONG_PROVIDER + Constants.translateResponseCode(Integer.parseInt(response)));
                    }
                },
                error -> {
                    LoginManager.getInstance().logOut();
                    endLogin(false, Constants.GENERIC_ERROR);
                },
                email,
                Constants.FACEBOOK);
    }

    private void firebaseAuthWithFacebook(AccessToken token) {
        LogManager.getInstance().printConsoleMessage("Facebook login -> step 4");
        FirebaseAuth.getInstance().signInWithCredential(FacebookAuthProvider.getCredential(token.getToken()))
                .addOnCompleteListener((Activity)context, task -> {
                    if (task.isSuccessful() && task.getResult() != null)
                        validateLogin(task.getResult().getUser());
                    else {
                        LoginManager.getInstance().logOut();
                        FirebaseAuth.getInstance().signOut();
                        endLogin(false, Constants.GENERIC_ERROR);
                    }
                });
    }

    private void validateLogin(FirebaseUser firebaseUser){
        LogManager.getInstance().printConsoleMessage("Facebook login -> step 5");
        Response.Listener<String> listener = response -> {
            if(Integer.parseInt(response) == Constants.USER_EXISTS)
                updateTokenID(firebaseUser.getUid());
            else if(Integer.parseInt(response) == Constants.USER_NOT_EXISTS)
                registerNewUser(firebaseUser, Constants.GOOGLE);
            else
                endLogin(false, Constants.GENERIC_ERROR);
        };

        Response.ErrorListener errorListener = error -> {
            LoginManager.getInstance().logOut();
            FirebaseAuth.getInstance().signOut();
            endLogin(false, Constants.GENERIC_ERROR);
        };

        DataManager.getInstance().checkIfTheUserAlreadyExists(listener, errorListener, firebaseUser.getEmail());
    }

    private void registerNewUser(FirebaseUser firebaseUser, int method) {
        LogManager.getInstance().printConsoleMessage("Facebook login -> step 5.1");
        User user = new User(firebaseUser.getUid(), firebaseUser.getEmail(), firebaseUser.getDisplayName(),
                firebaseUser.getPhotoUrl().toString() + "?height=500", method);
        DataManager.getInstance().createNewUser(user, response -> updateTokenID(user.getUid()), error -> endLogin(false, Constants.NEW_USER_CREATION_ERROR));
    }

    private void endLogin(boolean isSuccess, final String message){
        if (isSuccess)
            invokeActivity();
        else {
            facebookFlag = false;
            if(message != null)
                LogManager.getInstance().showVisualMessage(message);
            LogManager.getInstance().hideWaitView();
        }
    }

    private void invokeActivity(){
        LogManager.getInstance().showVisualMessage(Constants.LOGIN_SUCCESS);
        (new Handler()).postDelayed(() -> {
            context.startActivity(new Intent(context, Kaori.class));
            ((Activity)context).finish();
        }, 3000);
    }

    boolean getFacebookFlag() {
        return facebookFlag;
    }

    private void updateTokenID(String uid){
        LogManager.getInstance().printConsoleMessage("Facebook login -> update token");
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if (task.isSuccessful())
                DataManager.getInstance().postToken(uid, task.getResult().getToken(), response -> endLogin(true, null), error -> endLogin(false, Constants.NEW_USER_CREATION_ERROR));
            else
                endLogin(true, null);
        });
    }
}
