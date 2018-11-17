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

import com.firebase.ui.auth.AuthUI;
import com.kaori.kaori.Kaori;
import com.kaori.kaori.R;

import java.util.Arrays;
import java.util.List;

/**
 * This fragment handles the two option:
 *    1) login: call the LoginFragment
 *    2) registration: call RegistrationFragment
 */
public class LoginRegistrationFragment extends Fragment {

    /**
     * Constants.
     */
    private final String BACK_STATE_NAME = getClass().getName();
    private final List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.GoogleBuilder().build(),
                    new AuthUI.IdpConfig.FacebookBuilder().build());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login1, container, false);

        // hide the title bar
        if(getActivity() != null && isAdded())
            ((Kaori)getActivity()).getSupportActionBar().hide();

        Button loginButton, registrationButton;
        loginButton = view.findViewById(R.id.button_login);
        registrationButton = view.findViewById(R.id.button_registration);

        // login button listener
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                invokeTheNextFragment(new LoginFragment());
            }
        });

        // register button listener
        registrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                invokeTheNextFragment(new CreateAccount1());
            }
        });

        return view;
    }

    /**
     * Method that handle the transition to the next fragment.
     * TODO: scegliere la transizione più bella
     */
    private void invokeTheNextFragment(Fragment fragment){
        if(getActivity() != null && isAdded())
            getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(BACK_STATE_NAME)
                .commit();
    }

}
