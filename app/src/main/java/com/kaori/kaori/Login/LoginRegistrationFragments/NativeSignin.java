package com.kaori.kaori.Login.LoginRegistrationFragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.android.volley.Response;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kaori.kaori.App;
import com.kaori.kaori.MainActivity;
import com.kaori.kaori.Model.User;
import com.kaori.kaori.Constants;
import com.kaori.kaori.R;
import com.kaori.kaori.Services.DataManager;
import com.kaori.kaori.Services.LogManager;

class NativeSignin {

    private static NativeSignin instance;
    private Context context;

    private NativeSignin(Context context) {
        this.context = context;
    }

    static void initialize(Context context) {
        instance = new NativeSignin(context);
    }

    static NativeSignin getInstance() {
        return instance;
    }

    void validateLogin(User user, String password, byte[] bitmap) {
        LogManager.getInstance().printConsoleMessage("Sign in -> step 1");
        DataManager.getInstance().checkIfTheUserAlreadyExists(
                response -> {
                    if(Integer.parseInt(response) == Constants.USER_EXISTS)
                        endSignIn(false, App.getStringFromRes(R.string.login_user_already_exists));
                    else if(Integer.parseInt(response) == Constants.USER_NOT_EXISTS)
                        signInWithEmail(user, password, bitmap);
                },
                error -> endSignIn(false, App.getStringFromRes(R.string.generic_error)),
                user.getEmail());
    }

    private void signInWithEmail(User user, String password, byte[] bitmap) {
        LogManager.getInstance().printConsoleMessage("Sign in -> step 2");
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if (task.isSuccessful())
                user.addTokenID(task.getResult().getToken());
            authWithEmail(user, password, bitmap);
        });
    }

    private void authWithEmail(User user, String password, byte[] profileImageBitmap) {
        LogManager.getInstance().printConsoleMessage("Sign in -> step 3");
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(user.getEmail(), password).addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    user.setUid(task.getResult().getUser().getUid());
                    uploadNewUser(user, profileImageBitmap);
                } else {
                    FirebaseAuth.getInstance().signOut();
                    endSignIn(false, App.getStringFromRes(R.string.generic_error));
                }
            });
    }

    private void uploadNewUser(User user, byte[] profileImageBitmap) {
        LogManager.getInstance().printConsoleMessage("Sign in -> step 4");
        final StorageReference mStorage = FirebaseStorage.getInstance().getReference().child(Constants.STORAGE_PATH_PROFILE_IMAGES + user.getUid());
        mStorage.putBytes(profileImageBitmap).addOnSuccessListener(
                taskSnapshot -> mStorage.getDownloadUrl().addOnSuccessListener(uri -> {
                    user.setPhotosUrl(uri.toString());
                    createNewUser(user);
                }))
                .addOnFailureListener(e -> {
                    FirebaseAuth.getInstance().signOut();
                    endSignIn(false, App.getStringFromRes(R.string.generic_error));
                });
    }
    private void createNewUser(User user) {
        LogManager.getInstance().printConsoleMessage("Sign in -> step 5");
        updateUserDatabase(user);
    }

    private void updateUserDatabase(User user) {
        Response.Listener<String> listener = response -> {
            if(response.equalsIgnoreCase("1"))
                updateTokenID(user.getUid());
            else {
                FirebaseAuth.getInstance().signOut();
                endSignIn(false, App.getStringFromRes(R.string.generic_error));
            }
        };
        Response.ErrorListener errorListener = error -> endSignIn(false, App.getStringFromRes(R.string.login_creation_error));
        DataManager.getInstance().createNewUser(user, listener, errorListener);
    }

    private void endSignIn(boolean isSuccess, final String message) {
        if (isSuccess)
            invokeActivity();
        else {
            if(message != null)
                LogManager.getInstance().showVisualMessage(message);
        }
    }

    private void invokeActivity(){
        LogManager.getInstance().showVisualMessage(App.getStringFromRes(R.string.login_success));
        (new Handler()).postDelayed(() -> {
            context.startActivity(new Intent(context, MainActivity.class));
            ((Activity)context).finish();
        }, 3000);
    }

    private void updateTokenID(String uid){
        LogManager.getInstance().printConsoleMessage("Google login -> update token");
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if (task.isSuccessful())
                DataManager.getInstance().postToken(uid, task.getResult().getToken(), response -> endSignIn(true, null), error -> endSignIn(false, App.getStringFromRes(R.string.login_creation_error)));
            else
                endSignIn(true, null);
        });
    }

}