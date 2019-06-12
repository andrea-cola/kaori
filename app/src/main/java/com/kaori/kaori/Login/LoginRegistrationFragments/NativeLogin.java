package com.kaori.kaori.Login.LoginRegistrationFragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.kaori.kaori.App;
import com.kaori.kaori.MainActivity;
import com.kaori.kaori.Constants;
import com.kaori.kaori.R;
import com.kaori.kaori.Services.DataManager;
import com.kaori.kaori.Services.LogManager;

/*package-private*/ class NativeLogin {

    private static NativeLogin instance;
    private Activity context;

    private NativeLogin(Activity context){
        this.context = context;
    }

    /*package-private*/ static void initialize(Activity context){
        instance = new NativeLogin(context);
    }

    /*package-private*/  static NativeLogin getInstance(){
        return instance;
    }

    /*package-private*/ void validateProvider(String email, String password) {
        LogManager.getInstance().printConsoleMessage("Email login -> step 1");

        App.setAuxiliarViewsStatus(Constants.WAIT_VIEW_ACTIVE);
        DataManager.getInstance().createValidationProviderRequest(
                response -> {
                    if(Integer.parseInt(response) == 1)
                        loginWithEmail(email, password);
                    else
                        endLogin(false, App.getStringFromRes(R.string.login_wrong_provider) + Constants.translateResponseCode(Integer.parseInt(response)));
                },
                error -> endLogin(false, App.getStringFromRes(R.string.generic_error)),
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
                        endLogin(false, App.getStringFromRes(R.string.wrong_credentials));
                    }
                });
    }

    private void validateLogin(FirebaseUser firebaseUser){
        LogManager.getInstance().printConsoleMessage("Email login -> step 3");
        DataManager.getInstance().checkIfTheUserAlreadyExists(
                response -> {
                    if(Integer.parseInt(response) == Constants.USER_EXISTS)
                        updateTokenID(firebaseUser.getUid());
                    else
                        endLogin(false, App.getStringFromRes(R.string.login_not_registered));
                },
                error -> {
                    FirebaseAuth.getInstance().signOut();
                    endLogin(false, App.getStringFromRes(R.string.generic_error));
                },
                firebaseUser.getEmail());
    }

    private void updateTokenID(String uid){
        LogManager.getInstance().printConsoleMessage("Email login -> step 4");
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