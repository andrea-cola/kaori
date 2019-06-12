package com.kaori.kaori.Login.LoginRegistrationFragments;

import android.app.Activity;
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
import com.kaori.kaori.App;
import com.kaori.kaori.MainActivity;
import com.kaori.kaori.Model.User;
import com.kaori.kaori.Constants;
import com.kaori.kaori.R;
import com.kaori.kaori.Services.DataManager;
import com.kaori.kaori.Services.LogManager;

import org.json.JSONException;

public class FacebookLogin {

    private static FacebookLogin instance;
    private CallbackManager mCallbackManager;
    private Activity context;
    private LoginButton loginButton;
    private boolean facebookFlag;

    private FacebookLogin(Activity context, LoginButton loginButton) {
        this.context = context;
        this.loginButton = loginButton;
        facebookFlag = false;
        mCallbackManager = CallbackManager.Factory.create();
    }

    public static void initialize(Activity context, LoginButton loginButton){
        instance = new FacebookLogin(context, loginButton);
    }

    public static FacebookLogin getInstance() {
        return instance;
    }

    public CallbackManager getCallbackManager(){
        return mCallbackManager;
    }

    public boolean getFacebookFlag() {
        return facebookFlag;
    }

    public void loginWithFacebook() {
        LogManager.getInstance().printConsoleMessage("Facebook login -> step 1");

        App.setAuxiliarViewsStatus(Constants.WAIT_VIEW_ACTIVE);
        facebookFlag = true;
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                getFacebookEmail(loginResult);
            }

            @Override
            public void onCancel() {
                endLogin(false, "Login cancellato.");
            }

            @Override
            public void onError(FacebookException error) {
                endLogin(false, App.getStringFromRes(R.string.facebook_error));
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
                        endLogin(false, App.getStringFromRes(R.string.generic_error));
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
                        endLogin(false, App.getStringFromRes(R.string.login_wrong_provider) + Constants.translateResponseCode(Integer.parseInt(response)));
                    }
                },
                error -> {
                    LoginManager.getInstance().logOut();
                    endLogin(false, App.getStringFromRes(R.string.generic_error));
                },
                email,
                Constants.FACEBOOK);
    }

    private void firebaseAuthWithFacebook(AccessToken token) {
        LogManager.getInstance().printConsoleMessage("Facebook login -> step 4");
        FirebaseAuth.getInstance().signInWithCredential(FacebookAuthProvider.getCredential(token.getToken()))
                .addOnCompleteListener(context, task -> {
                    if (task.isSuccessful() && task.getResult() != null)
                        validateLogin(task.getResult().getUser());
                    else {
                        LoginManager.getInstance().logOut();
                        FirebaseAuth.getInstance().signOut();
                        endLogin(false, App.getStringFromRes(R.string.login_auth_different_method));
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
                endLogin(false, App.getStringFromRes(R.string.generic_error));
        };

        Response.ErrorListener errorListener = error -> {
            LoginManager.getInstance().logOut();
            FirebaseAuth.getInstance().signOut();
            endLogin(false, App.getStringFromRes(R.string.generic_error));
        };

        DataManager.getInstance().checkIfTheUserAlreadyExists(listener, errorListener, firebaseUser.getEmail());
    }

    private void registerNewUser(FirebaseUser firebaseUser, int method) {
        LogManager.getInstance().printConsoleMessage("Facebook login -> step 5.1");
        User user = new User(firebaseUser.getUid(), firebaseUser.getEmail(), firebaseUser.getDisplayName(),
                firebaseUser.getPhotoUrl().toString() + "?height=500", method);
        DataManager.getInstance().createNewUser(user, response -> updateTokenID(user.getUid()), error -> endLogin(false, App.getStringFromRes(R.string.login_creation_error)));
    }

    private void updateTokenID(String uid){
        LogManager.getInstance().printConsoleMessage("Facebook login -> step 5.2");
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null)
                DataManager.getInstance()
                        .postToken(uid, task.getResult().getToken(),
                                response -> endLogin(true, App.getStringFromRes(R.string.login_success)),
                                error -> endLogin(false, App.getStringFromRes(R.string.login_creation_error)));
            else
                endLogin(true, App.getStringFromRes(R.string.login_success));
        });
    }

    private void endLogin(boolean isSuccess, final String message){
        facebookFlag = false;
        App.setAuxiliarViewsStatus(Constants.NO_VIEW_ACTIVE);
        LogManager.getInstance().showVisualMessage(message);
        if (isSuccess)
            invokeActivity();
    }

    private void invokeActivity(){
        new Handler().postDelayed(() -> {
            context.startActivity(new Intent(context, MainActivity.class));
            context.finish();
        }, 1000);
    }

}
