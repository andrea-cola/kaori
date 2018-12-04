package com.kaori.kaori.LoginRegistrationFragments;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.kaori.kaori.Constants;
import com.kaori.kaori.Kaori;
import com.kaori.kaori.R;

/**
 * Second step in signin process.
 * The user must provide a method of signin.
 * 1) native: username and password
 * 2) social network
 * 3) password forgotten
 */
public class LoginFragment extends Fragment {

    /**
     * Varialbles.
     */
    private CallbackManager mCallbackManager;
    private LoginButton buttonFacebookLogin;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login2, container, false);

        // get views from the layout.
        buttonFacebookLogin = view.findViewById(R.id.button_facebook);
        Button loginButton = view.findViewById(R.id.login_button);
        TextView forgottenPassowrd = view.findViewById(R.id.password_forgotten);
        ImageView mFacebook = view.findViewById(R.id.button_fake_facebook);
        ImageView mGoogle = view.findViewById(R.id.button_google);
        final EditText mUsername = view.findViewById(R.id.login_username);
        final EditText mPassword = view.findViewById(R.id.login_password);

        // instantiate the Firebase Authentication.
        mAuth = FirebaseAuth.getInstance();

        // add listener to the button signin button.
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nativeLogin(mUsername.getText().toString(), mPassword.getText().toString());
            }
        });

        // add listener to the forgotten button.
        forgottenPassowrd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onForgottenPassword();
            }
        });

        // add listener to the facebook button.
        mFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                facebookLogin();
            }
        });

        // add listener to the google button.
        mGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleLogin();
            }
        });

        return view;
    }

    /**
     * Get the result from Facebook and Google signin.
     * The main branch of the if is relative to Google,
     * the else is relative to Facebook.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 0) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(Constants.TAG, "Google sign in failed", e);
            }
        } else {
            // Pass the activity result back to the Facebook SDK
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * This method is responsible for the native signin (username and password).
     * It calls Firebase and checks if the credentials are correct.
     */
    private void nativeLogin(@NonNull final String username, @NonNull final String password){
        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if(getActivity() != null) {
                                Log.d(Constants.TAG, "signInWithEmail:success");
                                startActivity(new Intent(getActivity(), Kaori.class));
                            }
                        } else {
                            Log.w(Constants.TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(getActivity(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Set up the Facebook signin and perform a click on the real button.
     */
    private void facebookLogin(){
        mCallbackManager = CallbackManager.Factory.create();
        buttonFacebookLogin.setReadPermissions("email", "public_profile");
        buttonFacebookLogin.setFragment(this);
        buttonFacebookLogin.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(Constants.TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(Constants.TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(Constants.TAG, "facebook:onError", error);
            }
        });
        buttonFacebookLogin.performClick();
    }

    /**
     * Handle the Facebook response.
     */
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(Constants.TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(Constants.TAG, "signInWithCredential:success");
                            getActivity().recreate();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(Constants.TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getActivity(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Set up the Google signin.
     */
    private void googleLogin(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(getContext(), gso);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 0);
    }

    /**
     * Hande the Google response.
     * @param acct
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(Constants.TAG, "signInWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(Constants.TAG, "signInWithCredential:success");
                            getActivity().recreate();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(Constants.TAG, "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

    /**
     * This method handles the transition to the fragment responsible
     * for the reset of the password.
     */
    private void onForgottenPassword() {
        // we need to pass to another fragment and following this procedure:
        // 1) the user enters his email
        // 2) we make a call to the server to send an email with the new password
    }

}