package com.kaori.kaori.LoginRegistrationFragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.kaori.kaori.Kaori;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.DataManager;
import com.kaori.kaori.Utils.LogManager;

/*package-private*/ class NativeLogin {

    private static NativeLogin instance;
    private Context context;

    private NativeLogin(Context context){
        this.context = context;
    }

    /*package-private*/ static void initialize(Context context){
        instance = new NativeLogin(context);
    }

    /*package-private*/  static NativeLogin getInstance(){
        return instance;
    }

    /*package-private*/ void validateProvider(String email, String password) {
        LogManager.getInstance().printConsoleMessage("Email login -> step 1");
        DataManager.getInstance().createValidationProviderRequest(
                response -> {
                    if(Integer.parseInt(response) == 1)
                        loginWithEmail(email, password);
                    else
                        endLogin(false, Constants.WRONG_PROVIDER + Constants.translateResponseCode(Integer.parseInt(response)));
                },
                error -> endLogin(false, Constants.GENERIC_ERROR),
                email,
                Constants.NATIVE);
    }

    private void loginWithEmail(String username, String password){
        LogManager.getInstance().printConsoleMessage("Email login -> step 2");
        FirebaseAuth.getInstance().signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        validateLogin(task.getResult().getUser());
                    } else {
                        FirebaseAuth.getInstance().signOut();
                        endLogin(false, Constants.WRONG_CREDENTIALS);
                    }
                });
    }

    private void validateLogin(FirebaseUser firebaseUser){
        DataManager.getInstance().checkIfTheUserAlreadyExists(
                response -> {
                    if(Integer.parseInt(response) == Constants.USER_EXISTS)
                        updateTokenID(firebaseUser.getUid());
                    else
                        endLogin(false, Constants.USER_NOT_EXISTS_ERROR);
                },
                error -> {
                    FirebaseAuth.getInstance().signOut();
                    endLogin(false, Constants.GENERIC_ERROR);
                },
                firebaseUser.getEmail());
    }

    private void endLogin(boolean isSuccess, final String message){
        if (isSuccess)
            invokeActivity();
        else {
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

    private void updateTokenID(String uid){
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                LogManager.getInstance().printConsoleMessage("Native login -> update token");
                DataManager.getInstance().postToken(uid, task.getResult().getToken(), response -> endLogin(true, null), error -> endLogin(false, Constants.NEW_USER_CREATION_ERROR));
            }
            else
                endLogin(true, null);
        });
    }

}