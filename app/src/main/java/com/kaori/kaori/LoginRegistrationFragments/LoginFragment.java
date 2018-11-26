package com.kaori.kaori.LoginRegistrationFragments;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kaori.kaori.Constants;
import com.kaori.kaori.Kaori;
import com.kaori.kaori.R;

/**
 * Second step in login process.
 * The user must provide a method of login.
 * 1) native: username and password
 * 2) social network
 * 3) password forgotten
 */
public class LoginFragment extends Fragment {

    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login2, container, false);

        Button loginButton = view.findViewById(R.id.login_button);
        TextView forgottenPassowrd = view.findViewById(R.id.password_forgotten);
        final EditText mUsername, mPassword;
        mUsername = view.findViewById(R.id.login_username);
        mPassword = view.findViewById(R.id.login_password);

        auth = FirebaseAuth.getInstance();
        //TODO: inizializzare le immagini dei social

        // show the title bar
        if(getActivity() != null && isAdded()) {
            ((Kaori) getActivity()).getSupportActionBar().show();
            ((Kaori) getActivity()).getSupportActionBar().setTitle(Constants.titleSignIn);
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nativeLogin(mUsername.getText().toString(), mPassword.getText().toString());
            }
        });

        forgottenPassowrd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onForgottenPassword();
            }
        });

        return view;
    }

    /**
     * This method is responsible for the native login (username and password).
     * It calls Firebase and checks if the credentials are correct.
     * @param username of the user
     * @param password of the user
     */
    private void nativeLogin(@NonNull final String username, @NonNull final String password){
        auth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = auth.getCurrentUser();
                            if(getActivity() != null) {
                                Log.d(Constants.TAG, "signInWithEmail:success");

                                SharedPreferences preferences = getActivity().getSharedPreferences(Constants.KAORI_SHARED_PREFERENCES, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putBoolean(String.valueOf(R.string.preferences_master_login), true);
                                editor.putString(String.valueOf(R.string.preferences_master_login_uid), user.getUid());
                                editor.putString(String.valueOf(R.string.preferences_master_login_username), user.getEmail());
                                editor.apply();
                                getActivity().recreate();
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(Constants.TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(getActivity(), "Authentication failed.", Toast.LENGTH_SHORT).show();
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
