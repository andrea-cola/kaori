package com.kaori.kaori.LoginRegistrationFragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.android.volley.Response;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kaori.kaori.Kaori;
import com.kaori.kaori.Model.User;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.DataManager;
import com.kaori.kaori.Utils.LogManager;

/**
 * This class handles the login and the signInWithEmail of the user in the app.
 */
class SignInManager {

    /**
     * Variables.
     */
    private static SignInManager instance;
    private Context context;

    /**
     * Class constructor.
     */
    private SignInManager(Context context) {
        this.context = context;
    }

    static void initialize(Context context) {
        instance = new SignInManager(context);
    }

    static SignInManager getInstance() {
        return instance;
    }

    void validateLogin(User user, String password, byte[] bitmap){
        DataManager.getInstance().checkIfTheUserAlreadyExists(
                response -> {
                    if(Integer.parseInt(response) == Constants.USER_EXISTS)
                        endSignIn(false, Constants.USER_ALREADY_EXISTS);
                    else if(Integer.parseInt(response) == Constants.USER_NOT_EXISTS)
                        signInWithEmail(user, password, bitmap);
                },
                error -> endSignIn(false, Constants.GENERIC_ERROR),
                user.getEmail());
    }

    private void signInWithEmail(User user, String password, byte[] bitmap){
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if (task.isSuccessful())
                user.addTokenID(task.getResult().getToken());
            authWithEmail(user, password, bitmap);
        });
    }

    private void authWithEmail(User user, String password, byte[] profileImageBitmap) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(user.getEmail(), password).addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    user.setUid(task.getResult().getUser().getUid());
                    uploadNewUser(user, profileImageBitmap);
                } else {
                    FirebaseAuth.getInstance().signOut();
                    endSignIn(false, Constants.GENERIC_ERROR);
                }
            });
    }

    private void uploadNewUser(User user, byte[] profileImageBitmap){
        final StorageReference mStorage = FirebaseStorage.getInstance().getReference().child(Constants.STORAGE_PATH_PROFILE_IMAGES + user.getUid());
        mStorage.putBytes(profileImageBitmap).addOnSuccessListener(
                taskSnapshot -> mStorage.getDownloadUrl().addOnSuccessListener(uri -> {
                    user.setPhotosUrl(uri.toString());
                    createNewUser(user);
                }))
                .addOnFailureListener(e -> {
                    FirebaseAuth.getInstance().signOut();
                    endSignIn(false, Constants.GENERIC_ERROR);
                });
    }
    private void createNewUser(User user) {
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if (task.isSuccessful())
                user.addTokenID(task.getResult().getToken());
            updateUserDatabase(user);
        });
    }

    private void updateUserDatabase(User user){
        Response.Listener<String> listener = response -> {
            if(response.equalsIgnoreCase("1"))
                endSignIn(true, null);
            else {
                FirebaseAuth.getInstance().signOut();
                endSignIn(false, Constants.GENERIC_ERROR);
            }
        };
        Response.ErrorListener errorListener = error -> endSignIn(false, Constants.NEW_USER_CREATION_ERROR);
        DataManager.getInstance().createNewUser(user, listener, errorListener);
    }

    private void endSignIn(boolean isSuccess, final String message){
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

}