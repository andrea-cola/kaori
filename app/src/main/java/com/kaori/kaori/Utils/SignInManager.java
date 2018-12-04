package com.kaori.kaori.Utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.facebook.AccessToken;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
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

public class SignInManager {

    private ProgressDialog confirmationProgressDialog, waitingProgressDialog;
    private AlertDialog.Builder builder;
    private User user;
    private String password;
    private FirebaseFirestore mDatabase;
    private FirebaseAuth mAuth;
    private AuthMethod authenticationMethod;
    private Bitmap profileImageBitmap;
    private Context context;

    public SignInManager(Context context) {
        confirmationProgressDialog = new ProgressDialog(context);
        waitingProgressDialog = new ProgressDialog(context);
        builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);

        this.context = context;
        mDatabase = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * It takes in input a FirebaseUser and return a User to pass
     * It takes in input a FirebaseUser and return a User to pass
     * to the next fragment.
     */
    private void createNewUserAndSignIn(FirebaseUser firebaseUser, AuthMethod authMethod){
        this.authenticationMethod = authMethod;
        user = new User();
        user.setEmail(firebaseUser.getEmail());

        String[] names = firebaseUser.getDisplayName().split(" ");
        user.setUid(firebaseUser.getUid());
        user.setName(names[0]);
        user.setSurname(names[1]);

        String tmp = firebaseUser.getPhotoUrl().toString();
        if(tmp.isEmpty())
            user.setPhotosUrl(Constants.STORAGE_DEFAULT_PROFILE_IMAGE);
        else
            user.setPhotosUrl(tmp);

        writeUserInDatabase();
    }

    public void signin(AuthMethod method, User user, String password, Bitmap bitmap){
        this.user = user;
        this.password = password;
        this.authenticationMethod = method;
        this.profileImageBitmap = bitmap;
        writeUserInDatabase();
    }

    public void signInWithGoogle(GoogleSignInAccount account){
        Timber.d("signInWithGoogle:" + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener((Activity)context, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Timber.d("GoogleSignInWithCredential --> success.");
                    FirebaseUser user = mAuth.getCurrentUser();
                    createNewUserAndSignIn(user, AuthMethod.GOOGLE);
                } else {
                    Timber.d(task.getException(), "GoogleSignInWithCredential --> failure.");
                    //Snackbar.make(context.findViewById(R.id.main_container), "Authentication with Google Failed.", Snackbar.LENGTH_LONG).show();
                    // TODO: gestire l'errore e andare ad un punto della UI che possa permttere di ricominciare.
                    //updateUI(null);
                }
            }
        });
    }

    public void signInWithFacebook(AccessToken token) {
        Timber.d("handleFacebookAccessToken:" + token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener((Activity)context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Timber.d("signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Timber.d("mail: " + user.getEmail());
                            createNewUserAndSignIn(user, AuthMethod.FACEBOOK);
                        } else {
                            // If sign in fails, display a message to the user.
                            Timber.tag(Constants.TAG).w(task.getException(), "signInWithCredential:failure");
                            //Toast.makeText(FacebookLoginActivity.this, "Authentication failed.",Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                    }
                });
    }

    private void writeUserInDatabase(){
        builder.setTitle(Constants.DIALOG_TITLE_CONFIRM).setMessage(Constants.DIALOG_MESSAGE_CONFIRM)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        signInNewUser();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void signInNewUser() {
        waitingProgressDialog.setMessage("Creating new user...");
        waitingProgressDialog.show();

        if(authenticationMethod == AuthMethod.NATIVE) {
            mAuth.createUserWithEmailAndPassword(user.getEmail(), password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Timber.d("createUserWithEmail:success");
                                user.setUid(task.getResult().getUser().getUid());

                                if (profileImageBitmap != null)
                                    uploadProfileImageOnTheServer();
                                else {
                                    user.setPhotosUrl(Constants.STORAGE_DEFAULT_PROFILE_IMAGE);
                                    uploadNewUserOnTheServer();
                                }
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(Constants.TAG, "createUserWithEmail:failure", task.getException());
                                // TODO
                            }
                        }
                    });
        } else if(authenticationMethod == AuthMethod.GOOGLE || authenticationMethod == AuthMethod.FACEBOOK ){
            uploadNewUserOnTheServer();
        }
    }

    private void uploadNewUserOnTheServer(){
        waitingProgressDialog.setMessage("Uploading the user...");
        mDatabase.collection("users").document()
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(Constants.TAG, "DocumentSnapshot successfully written!");
                        endRegistration();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // TODO: fare riprovare l'utente.
                        Log.w(Constants.TAG, "Error writing document", e);
                    }
                });
    }

    private void uploadProfileImageOnTheServer(){
        waitingProgressDialog.setMessage("Uploading the profile image...");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        profileImageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        final StorageReference mStorage = FirebaseStorage.getInstance().getReference().child(Constants.STORAGE_PATH_PROFILE_IMAGES + user.getUid());
        UploadTask uploadTask = mStorage.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.w(Constants.TAG, exception.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mStorage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d(Constants.TAG, uri.toString());
                        user.setPhotosUrl(uri.toString());
                        uploadNewUserOnTheServer();
                    }
                });
            }
        });
    }

    private void endRegistration(){
        waitingProgressDialog.dismiss();
        // TODO: mandare mail in caso di conferma.
        if (context != null){
            context.startActivity(new Intent(context, Kaori.class));
            ((Activity)context).finish();
        }
    }

}
