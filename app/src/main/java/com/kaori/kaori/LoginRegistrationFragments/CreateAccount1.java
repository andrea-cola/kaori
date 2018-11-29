package com.kaori.kaori.LoginRegistrationFragments;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kaori.kaori.Constants;
import com.kaori.kaori.DBObjects.User;
import com.kaori.kaori.DataHub;
import com.kaori.kaori.Kaori;
import com.kaori.kaori.R;

import java.util.ArrayList;

/**
 * This class handles the first pass in account creation.
 */
public class CreateAccount1 extends Fragment {

    /**
     * Constants.
     */
    private final String BACK_STATE_NAME = getClass().getName();
    private final int OK_GOOGLE = 1;

    /**
     * Variables.
     */
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private CallbackManager callbackManager;
    private Button buttonCreateNewAccount, buttonFakeFacebook, buttonGoogle;
    private LoginButton buttonFacebook;
    private View waitLayout;
    private DataHub hub;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reg1, container, false);
        waitLayout = view.findViewById(R.id.wait_layout);
        buttonCreateNewAccount = view.findViewById(R.id.button_create_new_account);
        buttonFakeFacebook = view.findViewById(R.id.button_fake_facebook);
        buttonFacebook = view.findViewById(R.id.button_facebook);
        buttonGoogle = view.findViewById(R.id.button_fake_google);

        // objects initialization
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        hub = DataHub.getInstance();

        // show the title bar
        if(getActivity() != null && isAdded()) {
            ((Kaori) getActivity()).getSupportActionBar().show();
            ((Kaori) getActivity()).getSupportActionBar().setTitle(Constants.titleCreate);
        }

        // set up the real Facebook button
        callbackManager = CallbackManager.Factory.create();
        buttonFacebook.setReadPermissions("email", "public_profile");
        buttonFacebook.setFragment(this);
        buttonFacebook.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(Constants.TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(Constants.TAG, "facebook:onCancel");
                // TODO
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(Constants.TAG, "facebook:onError", error);
                // TODO
            }
        });

        // set listeners
        setCreateNewAccountButtonListener();
        setFacebookButtonLister();
        setGoogleButtonListener();

        getUniversitiesListFromDatabase();

        return view;
    }

    private void getUniversitiesListFromDatabase() {
        db.collection(Constants.DB_COLL_UNIVERSITIES).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayList<String> universities = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult())
                        universities.add(document.getString("name"));
                    hub.setUniversities(universities);
                    Log.d(Constants.TAG, universities.size() + "");
                }
                else
                    //TODO
                    Log.d(Constants.TAG, "Error getting documents: ", task.getException());

                getCourseTypesFromDatabase();
            }
        });
    }

    private void getCourseTypesFromDatabase() {
        db.collection(Constants.DB_COLL_COURSE_TYPES).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayList<String> courseTypes = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult())
                        courseTypes.add(document.getString("name"));
                    hub.setCourseType(courseTypes);
                    Log.d(Constants.TAG, courseTypes.size() + "");
                }
                else
                    //TODO
                    Log.d(Constants.TAG, "Error getting documents: ", task.getException());

                getCoursesFromDatabase();
            }
        });
    }

    private void getCoursesFromDatabase() {
        db.collection(Constants.DB_COLL_COURSES).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayList<String> exams = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult())
                        exams.add(document.getString("name"));
                    hub.setExams(exams);
                    Log.d(Constants.TAG, exams.size() + "");
                }
                else
                    //TODO
                    Log.d(Constants.TAG, "Error getting documents: ", task.getException());

                waitLayout.setVisibility(View.GONE);
            }
        });
    }

    /**
     * This method is used to handle the Google signin.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // google sign in request
        if(requestCode == OK_GOOGLE) {
            Log.w(Constants.TAG, "Google request code.");
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignIn(task);
        } else {
            Log.w(Constants.TAG, "Facebook request code.");
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Handle the sign in result of GoogleSignIn
     */
    private void handleGoogleSignIn(Task<GoogleSignInAccount> completedTask){
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if(account != null)
                firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(Constants.TAG, "GoogleSignInResult:failed code=" + e.getStatusCode());
            //TODO: ritornare alla schermata iniziale
        }
    }

    /**
     * Handle the firebase auth with Google.
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount account){
        Log.d(Constants.TAG, "firebaseAuthWithGoogle:" + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Log.d(Constants.TAG, "GoogleSignInWithCredential --> success.");
                        FirebaseUser user = auth.getCurrentUser();
                        Log.d(Constants.TAG, "mail: " + user.getEmail());
                        invokeNextFragmentWithParams(createNewUser(user));
                    } else {
                        Log.d(Constants.TAG, "GoogleSignInWithCredential --> failure.", task.getException());
                        Snackbar.make(getActivity().findViewById(R.id.container), "Authentication with Google Failed.", Snackbar.LENGTH_LONG).show();
                        // TODO: gestire l'errore e andare ad un punto della UI che possa permttere di ricominciare.
                        //updateUI(null);
                    }
                }
            });
    }

    /**
     * Handle the facebook sign in.
     */
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(Constants.TAG, "handleFacebookAccessToken:" + token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(Constants.TAG, "signInWithCredential:success");
                            FirebaseUser user = auth.getCurrentUser();
                            Log.d(Constants.TAG, "mail: " + user.getEmail());
                            invokeNextFragmentWithParams(createNewUser(user));
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(Constants.TAG, "signInWithCredential:failure", task.getException());
                            //Toast.makeText(FacebookLoginActivity.this, "Authentication failed.",Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                    }
                });
    }

    /**
     * Invoke the fragment that let the user enter the basic information.
     */
    private void setCreateNewAccountButtonListener(){
        buttonCreateNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                invokeNextFragment();
            }
        });
    }

    /**
     * Set up the facebook button listener.
     * In particular, when the user clicks on the button,
     * the true facebook button that is hidden is clicked.
     *
     * The reason why the true facebook button is hidden is that
     * button is not easy to style.
     */
    private void setFacebookButtonLister(){
        buttonFakeFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                LoginManager.getInstance().logOut();
                buttonFacebook.performClick();
            }
        });
    }

    /**
     * Set up the google button listener.
     */
    private void setGoogleButtonListener(){
        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();

        buttonGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getContext() != null) {
                    GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(getContext(), gso);
                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, OK_GOOGLE);
                }
            }
        });
    }

    /**
     * Invokes the next fragment without passing parameters.
     */
    private void invokeNextFragment(){
        if(getActivity() != null && isAdded())
            getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new CreateAccount2())
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(BACK_STATE_NAME)
                .commit();
    }

    /**
     * Invoce the next fragment passing parameters about the users.
     */
    private void invokeNextFragmentWithParams(User user){
        if(getActivity() != null && isAdded()) {
            CreateAccount3 createAccount3 = new CreateAccount3();
            createAccount3.setParams(user);
            createAccount3.setMethod(Constants.SOCIAL_SIGNIN);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, createAccount3)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack(BACK_STATE_NAME)
                    .commit();
        }
    }

    /**
     * It takes in input a FirebaseUser and return a User to pass
     * It takes in input a FirebaseUser and return a User to pass
     * to the next fragment.
     */
    private User createNewUser(FirebaseUser firebaseUser){
        User user = new User();
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

        return user;
    }

}
