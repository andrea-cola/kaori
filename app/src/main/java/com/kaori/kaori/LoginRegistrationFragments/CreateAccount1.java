package com.kaori.kaori.LoginRegistrationFragments;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.kaori.kaori.Constants;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.SignInManager;

import timber.log.Timber;

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
    private CallbackManager callbackManager;
    private Button buttonCreateNewAccount, buttonFakeFacebook, buttonGoogle;
    private LoginButton buttonFacebook;
    private SignInManager signInManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reg1, container, false);
        buttonCreateNewAccount = view.findViewById(R.id.button_create_new_account);
        buttonFakeFacebook = view.findViewById(R.id.button_fake_facebook);
        buttonFacebook = view.findViewById(R.id.button_facebook);
        buttonGoogle = view.findViewById(R.id.button_fake_google);

        signInManager = new SignInManager(getContext());

        // set up the real Facebook button
        callbackManager = CallbackManager.Factory.create();
        buttonFacebook.setReadPermissions("email", "public_profile");
        buttonFacebook.setFragment(this);
        buttonFacebook.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(Constants.TAG, "facebook:onSuccess:" + loginResult);
                signInManager.signInWithFacebook(loginResult.getAccessToken());
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

        return view;
    }

    /**
     * This method is used to handle the Google signInWithEmail.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // google sign in request
        if(requestCode == OK_GOOGLE) {
            Timber.tag(Constants.TAG).w("Google request code.");
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignIn(task);
        } else {
            Timber.tag(Constants.TAG).w("Facebook request code.");
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
                signInManager.signInWithGoogle(account);
        } catch (ApiException e) {
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Timber.tag(Constants.TAG).w("GoogleSignInResult:failed code=" + e.getStatusCode());
            //TODO: ritornare alla schermata iniziale
        }
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
                .replace(R.id.main_layout, new CreateAccount2())
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(BACK_STATE_NAME)
                .commit();
    }

}
