package com.kaori.kaori.Login.LoginRegistrationFragments;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.facebook.login.widget.LoginButton;
import com.kaori.kaori.R;

public class LoginFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login, container, false);
        EditText mUsername = view.findViewById(R.id.mail);
        EditText mPassword = view.findViewById(R.id.password);
        LoginButton buttonFacebookLogin = view.findViewById(R.id.btn_facebook_2);
        buttonFacebookLogin.setReadPermissions("email", "public_profile");

        FacebookLogin.initialize(getActivity(), buttonFacebookLogin);
        GoogleLogin.initialize(getActivity());
        NativeLogin.initialize(getActivity());

        view.findViewById(R.id.txt_registration)
                .setOnClickListener(v -> invokeNextFragment(new CreateAccountWithEmail()));
        view.findViewById(R.id.btn_login)
                .setOnClickListener(v -> nativeLogin(mUsername.getText().toString(), mPassword.getText().toString()));
        view.findViewById(R.id.btn_facebook)
                .setOnClickListener(v -> FacebookLogin.getInstance().loginWithFacebook());
        view.findViewById(R.id.btn_google)
                .setOnClickListener(v -> GoogleLogin.getInstance().loginWithGoogle());

        return view;
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

    private void nativeLogin(String mail, String password) {
        NativeLogin.getInstance().validateProvider(mail, password);
    }

}