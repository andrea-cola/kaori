package com.kaori.kaori.Utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AlertDialog;

import com.facebook.AccessToken;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kaori.kaori.Constants;
import com.kaori.kaori.DBObjects.User;
import com.kaori.kaori.Kaori;

import java.io.ByteArrayOutputStream;

import timber.log.Timber;

/**
 * This class handles the login and the signInWithEmail of the user in the app.
 */
public class SignInManager {

    /**
     * Variables.
     */
    private ProgressDialog waitingProgressDialog;
    private AlertDialog.Builder builder;
    private User user;
    private String password;
    private FirebaseFirestore mDatabase;
    private FirebaseAuth mAuth;
    private Bitmap profileImageBitmap;
    private Context context;

    /**
     * Class constructor.
     */
    public SignInManager(Context context) {
        waitingProgressDialog = new ProgressDialog(context);
        builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);

        this.context = context;
        mDatabase = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * It takes in input a Firebase User and return a User to pass
     * to the next fragment.
     */
    private void createNewUserAndSignIn(FirebaseUser firebaseUser){
        user = new User();
        user.setEmail(firebaseUser.getEmail());

        String[] names = createsNameAndSurname(firebaseUser.getDisplayName());
        user.setUid(firebaseUser.getUid());
        user.setName(names[0]);
        user.setSurname(names[1]);

        String tmp = firebaseUser.getPhotoUrl().toString();
        if(tmp.isEmpty())
            user.setPhotosUrl(Constants.STORAGE_DEFAULT_PROFILE_IMAGE);
        else
            user.setPhotosUrl(tmp);

        waitingProgressDialog.setMessage("Creating new user...");
        waitingProgressDialog.show();

        uploadNewUserOnTheServer();
    }

    /**
     * This method is used in native sign in with email and password.
     */
    public void signInWithEmail(User user, String password, Bitmap bitmap){
        this.user = user;
        this.password = password;
        this.profileImageBitmap = bitmap;

        startNativeSignInProcess();
    }

    /**
     * This method is used in Google sign in.
     */
    public void signInWithGoogle(GoogleSignInAccount account){
        Timber.d("signInWithGoogle:%s", account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener((Activity)context, task -> {
            if (task.isSuccessful()) {
                Timber.d("GoogleSignInWithCredential --> success.");
                FirebaseUser user = mAuth.getCurrentUser();
                createNewUserAndSignIn(user);
            } else {
                Timber.d(task.getException(), "GoogleSignInWithCredential --> failure.");
                //Snackbar.make(context.findViewById(R.id.main_container), "Authentication with Google Failed.", Snackbar.LENGTH_LONG).show();
                // TODO: gestire l'errore e andare ad un punto della UI che possa permttere di ricominciare.
            }
        });
    }

    /**
     * This method is used in Facebook sign in.
     */
    public void signInWithFacebook(AccessToken token) {
        Timber.d("handleFacebookAccessToken:%s", token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener((Activity)context, task -> {
                    if (task.isSuccessful()) {
                        Timber.d("signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        Timber.d("mail: %s", user.getEmail());
                        createNewUserAndSignIn(user);
                    } else {
                        Timber.tag(Constants.TAG).w(task.getException(), "signInWithCredential:failure");
                        //Toast.makeText(FacebookLoginActivity.this, "Authentication failed.",Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Show a dialog asking the user if he wants to continue.
     */
    private void startNativeSignInProcess(){
        builder.setTitle(Constants.DIALOG_TITLE_CONFIRM).setMessage(Constants.DIALOG_MESSAGE_CONFIRM)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> signInNewUser())
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                    // TODO
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * Sign in the user throw Firebase Auth.
     */
    private void signInNewUser() {
        waitingProgressDialog.setMessage("Creating new user...");
        waitingProgressDialog.show();

        mAuth.createUserWithEmailAndPassword(user.getEmail(), password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Timber.d("createUserWithEmail:success");
                        user.setUid(task.getResult().getUser().getUid());

                        if (profileImageBitmap != null)
                            uploadProfileImageOnTheServer();
                        else {
                            user.setPhotosUrl(Constants.STORAGE_DEFAULT_PROFILE_IMAGE);
                            uploadNewUserOnTheServer();
                        }
                    } else {
                        Timber.tag(Constants.TAG).w(task.getException(), "createUserWithEmail:failure");
                        // TODO
                    }
                });
    }

    /**
     * This method writes the user in the database.
     */
    private void uploadNewUserOnTheServer(){
        waitingProgressDialog.setMessage("Uploading the user...");
        waitingProgressDialog.show();
        mDatabase.collection("users").document()
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Timber.d("DocumentSnapshot successfully written!");
                    endRegistration();
                })
                .addOnFailureListener(e -> {
                    // TODO: fare riprovare l'utente.
                    Timber.tag(Constants.TAG).w(e, "Error writing document");
                });
    }

    /**
     * This method uploads the image in the Firebase storage.
     */
    private void uploadProfileImageOnTheServer(){
        waitingProgressDialog.setMessage("Uploading the profile image...");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        profileImageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

        final StorageReference mStorage = FirebaseStorage.getInstance().getReference().child(Constants.STORAGE_PATH_PROFILE_IMAGES + user.getUid());
        UploadTask uploadTask = mStorage.putBytes(baos.toByteArray());
        uploadTask.addOnFailureListener(exception -> Timber.tag(Constants.TAG).w(exception))
            .addOnSuccessListener(taskSnapshot -> mStorage.getDownloadUrl().addOnSuccessListener(uri -> {
                Timber.tag(Constants.TAG).d(uri.toString());
                user.setPhotosUrl(uri.toString());
                uploadNewUserOnTheServer();
            }));
    }

    /**
     * Ends the registration process, dismissing the progress dialog.
     */
    private void endRegistration(){
        waitingProgressDialog.dismiss();
        // TODO: mandare mail in caso di conferma.
        if (context != null){
            context.startActivity(new Intent(context, Kaori.class));
            ((Activity)context).finish();
        }
    }

    /**
     * Takes in input a string and returns a vector of two elements.
     * The first element is the name and the second is the surname.
     */
    private String[] createsNameAndSurname(String nameAndSurname){
        String[] vector = new String[2];
        String[] parts = nameAndSurname.split(" ");

        vector[0] = parts[0];
        vector[1] = parts[1];

        if(parts.length > 2)
            for (int i = 2; i < parts.length; i++)
                vector[1] = " " + vector[i];

        return vector;
    }

}
