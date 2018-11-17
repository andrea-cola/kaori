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

import com.kaori.kaori.Constants;
import com.kaori.kaori.Kaori;
import com.kaori.kaori.R;

/**
 * This class handles the first pass in account creation.
 */
public class CreateAccount1 extends Fragment {

    private final String BACK_STATE_NAME = getClass().getName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reg1, container, false);

        // show the title bar
        if(getActivity() != null && isAdded()) {
            ((Kaori) getActivity()).getSupportActionBar().show();
            ((Kaori) getActivity()).getSupportActionBar().setTitle(Constants.titleCreate);
        }

        Button buttonCreateNewAccount, buttonFacebook, buttonTwitter, buttonGoogle;
        buttonCreateNewAccount = view.findViewById(R.id.button_create_new_account);
        buttonFacebook = view.findViewById(R.id.button_create_with_facebook);
        buttonGoogle = view.findViewById(R.id.button_create_with_google);
        buttonTwitter = view.findViewById(R.id.button_create_with_twitter);

        buttonCreateNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                invokeTheNextFragment(new CreateAccount2());
            }
        });

        buttonFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        buttonGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        buttonTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                    .addToBackStack(BACK_STATE_NAME)
                    .commit();
    }
}
