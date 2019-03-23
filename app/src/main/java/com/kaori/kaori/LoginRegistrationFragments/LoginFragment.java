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
import com.kaori.kaori.Utils.LogManager;

public class LoginFragment extends Fragment {

    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.login, container, false);
        Button mNative = view.findViewById(R.id.button);
        Button registration = view.findViewById(R.id.button2);
        ImageView mFacebook = view.findViewById(R.id.button_fake_facebook);
        ImageView mGoogle = view.findViewById(R.id.button_google);
        EditText mUsername = view.findViewById(R.id.username);
        EditText mPassword = view.findViewById(R.id.password);

        LoginButton buttonFacebookLogin = view.findViewById(R.id.button_facebook);
        buttonFacebookLogin.setReadPermissions("email", "public_profile");
        FacebookLogin.initialize(getContext(), buttonFacebookLogin);

        GoogleLogin.initialize(getContext());

        NativeLogin.initialize(getContext());

        registration.setOnClickListener(v -> invokeNextFragment(new CreateAccountWithEmail()));
        mNative.setOnClickListener(v -> nativeLogin(mUsername.getText().toString(), mPassword.getText().toString()));
        mFacebook.setOnClickListener(v -> facebookLogin());
        mGoogle.setOnClickListener(v -> googleLogin());

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
        LogManager.getInstance().showWaitView();
        NativeLogin.getInstance().validateProvider(mail, password);
    }

    private void facebookLogin() {
        LogManager.getInstance().showWaitView();
        FacebookLogin.getInstance().loginWithFacebook();
    }

    private void googleLogin() {
        LogManager.getInstance().showWaitView();
        GoogleLogin.getInstance().loginWithGoogle();
    }

}