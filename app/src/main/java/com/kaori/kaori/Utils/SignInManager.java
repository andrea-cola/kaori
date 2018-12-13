package com.kaori.kaori.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kaori.kaori.DBObjects.User;
import com.kaori.kaori.Kaori;
import com.kaori.kaori.KaoriLogin;
import com.kaori.kaori.R;

import java.io.ByteArrayOutputStream;

/**
 * This class handles the login and the signInWithEmail of the user in the app.
 */
public class SignInManager {

    /**
     * Variables.
     */
    private static SignInManager signInManager;
    private static Context context;
    private boolean facebookSignInStarted;
    private User user;
    private String password;
    private FirebaseFirestore mDatabase;
    private FirebaseAuth mAuth;
    private Bitmap profileImageBitmap;
    private CallbackManager callbackManager;
    private String loginError;

    /**
     * Class constructor.
     */
    private SignInManager() {
        mDatabase = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        facebookSignInStarted = false;
        loginError = "";
    }

    /**
     * Retrieve the singleton instance.
     */
    public static SignInManager getInstance() throws IllegalStateException {
        if (signInManager == null)
            signInManager = new SignInManager();
        return signInManager;
    }

    /**
     * Retrieve the singleton instance and set the context.
     */
    public static SignInManager getInstance(Context c){
        context = c;
        return getInstance();
    }

    /**
     * Return the flag relative to facebook sign in started.
     */
    public boolean getSignInFacebookStarted(){
        return this.facebookSignInStarted;
    }

    /**
     * Return callback manager.
     */
    public CallbackManager getCallbackManager(){
        return this.callbackManager;
    }

    /**
     * Start sign in with google.
     */
    public void signInWithGoogle(){
        LogManager.getInstance().printConsoleMessage("signInWithGoogle");
        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();
        if(context != null) {
            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            ((KaoriLogin)context).startActivityForResult(signInIntent, Constants.GOOGLE_SIGNIN_REQUEST);
        }
    }

    /**
     * Start the sign in with facebook.
     */
    public void signInWithFacebook(LoginButton button){
        LogManager.getInstance().printConsoleMessage("signInWithFacebook");

        this.facebookSignInStarted = true;
        FirebaseAuth.getInstance().signOut();
        callbackManager = CallbackManager.Factory.create();

        button.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                LogManager.getInstance().printConsoleMessage("signInWithFacebook:onSuccess -> " + loginResult);
                authWithFacebook(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                LogManager.getInstance().showVisualMessage(context,"signInWithFacebook:onCancel");
                endSignIn(false);
            }

            @Override
            public void onError(FacebookException error) {
                LogManager.getInstance().showVisualError(context, error,"signInWithFacebook:onCancel");
                endSignIn(false);
            }
        });

        button.performClick();
    }

    /**
     * This method is used in native sign in with email and password.
     */
    public void signInWithEmail(User user, String password, Bitmap bitmap){
        LogManager.getInstance().printConsoleMessage("signInWithEmail");
        this.user = user;
        this.password = password;
        this.profileImageBitmap = bitmap;

        authWithEmail();
    }

    /**
     * This method is used in Google sign in.
     */
    private void authWithGoogle(GoogleSignInAccount account){
        LogManager.getInstance().printConsoleMessage("authWithGoogle");

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener((Activity)context, task -> {
            if (task.isSuccessful()) {
                LogManager.getInstance().printConsoleMessage("authWithGoogle:success");
                FirebaseUser user = mAuth.getCurrentUser();
                if(user != null)
                    createNewUserAndSignIn(user);
                else {
                    LogManager.getInstance().showVisualError(context,null, "signInWithGoogle:nullUser");
                    endSignIn(false);
                }
            } else {
                LogManager.getInstance().showVisualError(context,null, "signInWithGoogle:failure");
                endSignIn(false);
            }
        });
    }

    /**
     * This method is used in Facebook sign in.
     */
    private void authWithFacebook(AccessToken token) {
        LogManager.getInstance().printConsoleMessage("authWithFacebook");

        this.facebookSignInStarted = false;

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener((Activity)context, task -> {
                    if (task.isSuccessful()) {
                        LogManager.getInstance().printConsoleMessage("authWithFacebook:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if(user != null && user.getEmail() != null && !user.getEmail().isEmpty())
                            createNewUserAndSignIn(user);
                        else{
                            LogManager.getInstance().showVisualError(context, null, "authWithFacebook:nullUser");
                            endSignIn(false);
                        }

                    } else {
                        LogManager.getInstance().showVisualError(context, null, "authWithFacebook:failure");
                        endSignIn(false);
                    }
                });
    }

    /**
     * Sign in the user throw Firebase Auth.
     */
    private void authWithEmail() {
        LogManager.getInstance().printConsoleMessage("authWithEmail");
        mAuth.createUserWithEmailAndPassword(user.getEmail(), password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        LogManager.getInstance().printConsoleMessage("authWithEmail:success");

                        if (task.getResult().getUser() != null) {
                            user.setUid(task.getResult().getUser().getUid());

                            if (profileImageBitmap != null)
                                uploadProfileImageOnTheServer();
                            else {
                                user.setPhotosUrl(Constants.STORAGE_DEFAULT_PROFILE_IMAGE);
                                uploadNewUserOnTheServer();
                            }

                        }
                        else{
                            LogManager.getInstance().showVisualError(context, null, "authWithEmail:nullUser");
                            endSignIn(false);
                        }

                    } else {
                        LogManager.getInstance().showVisualError(context, task.getException(), "authWithEmail:failure");
                        endSignIn(false);
                    }
                });
    }

    /**
     * Handle the sign in result of GoogleSignIn
     */
    public void handleGoogleSignIn(Task<GoogleSignInAccount> completedTask){
        LogManager.getInstance().printConsoleMessage("handleGoogleSignIn");
        try {
            LogManager.getInstance().printConsoleMessage("handleGoogleSignIn:success");
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if(account != null)
                signInManager.authWithGoogle(account);
        } catch (ApiException e) {
            LogManager.getInstance().showVisualError(context, e, "handleGoogleSignIn:fail");
            endSignIn(false);
        }
    }

    /**
     * It takes in input a Firebase User and return a User to pass
     * to the next fragment.
     */
    private void createNewUserAndSignIn(FirebaseUser firebaseUser){
        LogManager.getInstance().printConsoleMessage("createNewUserAndSignIn");
        user = new User();
        user.setEmail(firebaseUser.getEmail());

        user.setUid(firebaseUser.getUid());
        user.setName(firebaseUser.getDisplayName());

        String tmp = firebaseUser.getPhotoUrl().toString();
        if(tmp.isEmpty())
            user.setPhotosUrl(Constants.STORAGE_DEFAULT_PROFILE_IMAGE);
        else
            user.setPhotosUrl(tmp);

        uploadNewUserOnTheServer();
    }

    /**
     * This method writes the user in the database.
     */
    private void uploadNewUserOnTheServer(){
        LogManager.getInstance().printConsoleMessage("uploadNewUserOnTheServer");
        mDatabase.collection("users").document(user.getUid())
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    LogManager.getInstance().printConsoleMessage("uploadNewUserOnTheServer:written");
                    endSignIn(true);
                })
                .addOnFailureListener(e -> {
                    LogManager.getInstance().showVisualError(context, e, "uploadNewUserOnTheServer");
                    endSignIn(false);
                });
    }

    /**
     * This method uploads the image in the Firebase storage.
     */
    private void uploadProfileImageOnTheServer(){
        LogManager.getInstance().printConsoleMessage("uploadProfileImageOnTheServer");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        profileImageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

        final StorageReference mStorage = FirebaseStorage.getInstance().getReference().child(Constants.STORAGE_PATH_PROFILE_IMAGES + user.getUid());
        UploadTask uploadTask = mStorage.putBytes(baos.toByteArray());
        uploadTask
                .addOnSuccessListener(taskSnapshot -> mStorage.getDownloadUrl().addOnSuccessListener(uri -> {
                    LogManager.getInstance().printConsoleMessage("uploadProfileImageOnTheServer:success");
                    user.setPhotosUrl(uri.toString());
                    uploadNewUserOnTheServer();
                }))
                .addOnFailureListener(e -> {
                    LogManager.getInstance().showVisualError(context, e, "uploadProfileImageOnTheServer:error");
                    endSignIn(false);
                });
    }

    /**
     * This method is called whenever the process is terminated.
     * If successful restart the app, if not it only dismisses the
     * dialog.
     */
    private void endSignIn(boolean isSuccess){
        if (isSuccess && context != null) {
            LogManager.getInstance().printConsoleMessage("endSignIn:success");
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

}
