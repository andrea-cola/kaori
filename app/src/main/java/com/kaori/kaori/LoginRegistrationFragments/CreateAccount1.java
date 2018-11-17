package com.kaori.kaori.LoginRegistrationFragments;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.kaori.kaori.Constants;
import com.kaori.kaori.DBObjects.User;
import com.kaori.kaori.Kaori;
import com.kaori.kaori.R;

/**
 * This class handles the first pass in account creation.
 */
public class CreateAccount1 extends Fragment {

    /**
     * Constants.
     */
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

        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        final Button buttonCreateNewAccount, buttonFakeFacebook, buttonFacebook, buttonFakeGoogle;
        final SignInButton buttonGoogle;
        buttonCreateNewAccount = view.findViewById(R.id.button_create_new_account);
        buttonFakeFacebook = view.findViewById(R.id.button_fake_facebook);
        buttonFacebook = view.findViewById(R.id.button_facebook);
        buttonFakeGoogle = view.findViewById(R.id.button_fake_google);

        buttonCreateNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //invokeTheNextFragment();
            }
        });

        buttonFakeFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonFacebook.performClick();
            }
        });

        buttonFakeGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(getContext(), gso);
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, 0);
            }
        });

        return view;
    }

    /**
     * Method that handle the transition to the next fragment.
     * TODO: scegliere la transizione più bella
     */
    private void invokeTheNextFragment(String name, String surname, String email, String birthday){
        if(getActivity() != null && isAdded()) {
            CreateAccount2 createAccount2 = new CreateAccount2();

            User user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setSurname(surname);
            user.setBirthday(birthday);
            createAccount2.setUser(user);

            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, createAccount2)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack(BACK_STATE_NAME)
                    .commit();
        }
    }
}
