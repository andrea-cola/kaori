package com.kaori.kaori.LoginRegistrationFragments;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.login.widget.LoginButton;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.Constants;

/**
 * Second step in signInWithEmail process.
 * The user must provide a method of signInWithEmail.
 * 1) native: username and password
 * 2) social network
 * 3) password forgotten
 */
public class LoginFragment extends Fragment {

    /**
     * Varialbles.
     */
    private LoginButton buttonFacebookLogin;

    /**
     * Override of the onCreateView method.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login, container, false);

        // get true facebook button and setup the button.
        buttonFacebookLogin = view.findViewById(R.id.button_facebook);
        buttonFacebookLogin.setReadPermissions("email", "public_profile");

        // get views from the layout.
        Button loginButton = view.findViewById(R.id.button);
        TextView registration = view.findViewById(R.id.registration);
        ImageView mFacebook = view.findViewById(R.id.button_fake_facebook);
        ImageView mGoogle = view.findViewById(R.id.button_google);
        EditText mUsername = view.findViewById(R.id.username);
        EditText mPassword = view.findViewById(R.id.password);

        // add listener to the button signInWithEmail button.
        loginButton.setOnClickListener(view1 -> {
                Object[] params = new Object[2];
                params[0] = mUsername.getText().toString();
                params[1] = mPassword.getText().toString();
                startLogin(Constants.NATIVE, params);
        });

        // add listener to the facebook button.
        mFacebook.setOnClickListener(view13 -> {
            Object[] params = new Object[1];
            params[0] = buttonFacebookLogin;
            startLogin(Constants.FACEBOOK, params);
        });

        // add listener to the google button.
        mGoogle.setOnClickListener(view14 -> startLogin(Constants.GOOGLE, null));

        registration.setOnClickListener(view12 -> invokeNextFragment(new CreateAccountWithEmail()));

        return view;
    }

    /**
     * Pass to the next fragment and start the login process.
     */
    private void startLogin(int authMethod, Object[] params){
        LoginWaitFragment loginWaitFragment = new LoginWaitFragment();
        //loginWaitFragment.setParameters(Constants.LOGIN, authMethod, params);

        if(getActivity() != null && getActivity().getSupportFragmentManager() != null) {
            getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, loginWaitFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
        }
    }

    private void invokeNextFragment(Fragment fragment){
        if(getActivity() != null && getActivity().getSupportFragmentManager() != null) {
            getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
        }
    }

    /**
     * This method handles the transition to the fragment responsible
     * for the reset of the password.
     */
    private void onForgottenPassword() {
        // TODO
        // we need to pass to another fragment and following this procedure:
        // 1) the user enters his email
        // 2) we make a call to the server to send an email with the new password
    }

}