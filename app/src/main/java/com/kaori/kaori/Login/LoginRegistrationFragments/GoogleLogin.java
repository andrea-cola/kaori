package com.kaori.kaori.Login.LoginRegistrationFragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;

import com.android.volley.Response;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;
import com.kaori.kaori.App;
import com.kaori.kaori.MainActivity;
import com.kaori.kaori.Model.User;
import com.kaori.kaori.R;
import com.kaori.kaori.Constants;
import com.kaori.kaori.Services.DataManager;
import com.kaori.kaori.Services.LogManager;

public class GoogleLogin {

    private static GoogleLogin instance;
    private Activity context;
    private GoogleSignInClient googleSignInClient;

    private GoogleLogin(Activity context) {
        this.context = context;
    }

    public static void initialize(Activity c) {
        instance = new GoogleLogin(c);
    }

    public static GoogleLogin getInstance() {
        return instance;
    }

    public void loginWithGoogle(){
        App.setAuxiliarViewsStatus(Constants.WAIT_VIEW_ACTIVE);
        LogManager.getInstance().printConsoleMessage("Google login -> step 1");
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(App.getStringFromRes(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(context, gso);
        Intent signInIntent = googleSignInClient.getSignInIntent();
        context.startActivityForResult(signInIntent, Constants.GOOGLE_LOGIN_REQUEST);
    }

    public void validateProvider(final GoogleSignInAccount googleSignInAccount) {
        LogManager.getInstance().printConsoleMessage("Google login -> step 2");
        DataManager.getInstance().createValidationProviderRequest(
                response -> {
                    if(Integer.parseInt(response) == 1)
                        firebaseAuthWithGoogle(GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null));
                    else{
                        googleSignInClient.signOut();
                        endLogin(false, Constants.WRONG_PROVIDER + Constants.translateResponseCode(Integer.parseInt(response)));
                    }
                },
                error -> endLogin(false, Constants.GENERIC_ERROR),
                googleSignInAccount.getEmail(),
                Constants.GOOGLE);
    }

    private void firebaseAuthWithGoogle(AuthCredential credential) {
        LogManager.getInstance().printConsoleMessage("Google login -> step 3");
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(context, task -> {
                    if (task.isSuccessful())
                        validateLogin(task.getResult().getUser());
                    else {
                        FirebaseAuth.getInstance().signOut();
                        googleSignInClient.signOut();
                        endLogin(false, Constants.GENERIC_ERROR);
                    }
                });
    }

    private void validateLogin(FirebaseUser firebaseUser){
        LogManager.getInstance().printConsoleMessage("Google login -> step 4");
        Response.Listener<String> listener = response -> {
            LogManager.getInstance().printConsoleMessage(response);
            if(Integer.parseInt(response) == Constants.USER_EXISTS)
                updateTokenID(firebaseUser.getUid());
            else if(Integer.parseInt(response) == Constants.USER_NOT_EXISTS)
                registerNewUser(firebaseUser, Constants.GOOGLE);
            else
                endLogin(false, Constants.GENERIC_ERROR);
        };

        Response.ErrorListener errorListener = error -> {
            googleSignInClient.signOut();
            FirebaseAuth.getInstance().signOut();
            endLogin(false, Constants.GENERIC_ERROR);
        };

        DataManager.getInstance().checkIfTheUserAlreadyExists(listener, errorListener, firebaseUser.getEmail());
    }

    private void registerNewUser(FirebaseUser firebaseUser, int method) {
        LogManager.getInstance().printConsoleMessage("Google login -> step 4.1");
        User user = new User(firebaseUser.getUid(), firebaseUser.getEmail(), firebaseUser.getDisplayName(), firebaseUser.getPhotoUrl().toString(), method);
        DataManager.getInstance().createNewUser(user, response -> updateTokenID(user.getUid()), error -> endLogin(false, Constants.NEW_USER_CREATION_ERROR));
    }

    private void updateTokenID(String uid){
        LogManager.getInstance().printConsoleMessage("Google login -> step 4.2");
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if (task.isSuccessful())
                DataManager.getInstance().postToken(uid, task.getResult().getToken(), response -> endLogin(true, Constants.LOGIN_SUCCESS), error -> endLogin(false, Constants.NEW_USER_CREATION_ERROR));
            else
                endLogin(true, Constants.LOGIN_SUCCESS);
        });
    }

    private void endLogin(boolean isSuccess, final String message){
        App.setAuxiliarViewsStatus(Constants.NO_VIEW_ACTIVE);
        LogManager.getInstance().showVisualMessage(message);
        if (isSuccess)
            invokeActivity();
    }

    private void invokeActivity(){
        LogManager.getInstance().showVisualMessage(Constants.LOGIN_SUCCESS);
        (new Handler()).postDelayed(() -> {
            context.startActivity(new Intent(context, MainActivity.class));
            context.finish();
        }, 1000);
    }

}
