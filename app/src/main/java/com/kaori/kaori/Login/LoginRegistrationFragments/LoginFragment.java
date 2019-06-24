package com.kaori.kaori.Login.LoginRegistrationFragments;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.facebook.login.widget.LoginButton;
import com.kaori.kaori.R;
import com.kaori.kaori.Services.LogManager;

public class LoginFragment extends Fragment {

    private final String BACK_STATE_NAME = getClass().getName();
    public View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.login, container, false);
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
                    .addToBackStack(BACK_STATE_NAME)
                    .commit();
        }
    }

    private void nativeLogin(String mail, String password) {
        hideKeyboardFrom(getContext(), view);
        if(mail != null && !mail.isEmpty() && password != null && !password.isEmpty())
            NativeLogin.getInstance().validateProvider(mail, password);
        else
            LogManager.getInstance().showVisualMessage("Empty fields. Retry.");
    }

    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}