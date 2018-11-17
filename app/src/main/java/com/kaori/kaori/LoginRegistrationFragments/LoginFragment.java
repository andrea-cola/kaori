package com.kaori.kaori.LoginRegistrationFragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login2, container, false);

        Button loginButton = view.findViewById(R.id.login_button);
        TextView forgottenPassowrd = view.findViewById(R.id.password_forgotten);

        //TODO: inizializzare le immagini dei social

        // show the title bar
        if(getActivity() != null && isAdded()) {
            ((Kaori) getActivity()).getSupportActionBar().show();
            ((Kaori) getActivity()).getSupportActionBar().setTitle(Constants.titleSignIn);
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText mUsername, mPassword;
                mUsername = view.findViewById(R.id.login_username);
                mPassword = view.findViewById(R.id.login_password);

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
       // we need to call Firebase, make the login and link the user to the database.
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
