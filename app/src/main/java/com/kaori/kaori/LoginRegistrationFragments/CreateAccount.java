package com.kaori.kaori.LoginRegistrationFragments;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.facebook.login.widget.LoginButton;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.AuthMethods;
import com.kaori.kaori.Utils.Constants;

/**
 * This class handles the first pass in account creation.
 */
public class CreateAccount extends Fragment {

    /**
     * Constants.
     */
    private final String BACK_STATE_NAME = getClass().getName();

    /**
     * Variables.
     */
    private LoginButton buttonFacebook;

    /**
     * Method override.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reg1, container, false);

        Button buttonCreateNewAccount = view.findViewById(R.id.button);
        Button buttonFakeFacebook = view.findViewById(R.id.button_fake_facebook);
        Button buttonGoogle = view.findViewById(R.id.button_fake_google);
        buttonFacebook = view.findViewById(R.id.button_facebook);
        buttonFacebook.setReadPermissions("email", "public_profile");
        //buttonFacebook.setFragment(this);

        // set listeners for the buttons.
        buttonCreateNewAccount.setOnClickListener(v -> invokeNextFragment(new CreateAccountWithEmail()));
        buttonFakeFacebook.setOnClickListener(v -> {
            Object[] p = new Object[1];
            p[0] = buttonFacebook;

            LoginWaitFragment loginWaitFragment = new LoginWaitFragment();
            loginWaitFragment.setParameters(Constants.SIGNIN, AuthMethods.FACEBOOK, p);
            invokeNextFragment(loginWaitFragment);
        });
        buttonGoogle.setOnClickListener(v -> {
            LoginWaitFragment loginWaitFragment = new LoginWaitFragment();
            loginWaitFragment.setParameters(Constants.SIGNIN, AuthMethods.GOOGLE, null);
            invokeNextFragment(loginWaitFragment);
        });

        return view;
    }

    /**
     * Invokes the next fragment without passing parameters.
     */
    private void invokeNextFragment(Fragment fragment){
        if(getActivity() != null && isAdded())
            getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_layout, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(BACK_STATE_NAME)
                .commit();
    }

}
