package com.kaori.kaori.LoginRegistrationFragments;

import android.app.FragmentTransaction;
import android.content.Intent;
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

import com.android.volley.Response;
import com.facebook.login.widget.LoginButton;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.DataManager;
import com.kaori.kaori.Utils.LogManager;
import com.kaori.kaori.Utils.LoginManager;

public class LoginFragment extends Fragment {

    private LoginButton buttonFacebookLogin;
    private LoginManager loginManager;
    private int method;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login, container, false);
        loginManager = LoginManager.getInstance();

        // get true facebook button and setup the button.
        buttonFacebookLogin = view.findViewById(R.id.button_facebook);
        buttonFacebookLogin.setReadPermissions("email", "public_profile");

        // get views from the layout.
        Button mNative = view.findViewById(R.id.button);
        TextView registration = view.findViewById(R.id.registration);
        ImageView mFacebook = view.findViewById(R.id.button_fake_facebook);
        ImageView mGoogle = view.findViewById(R.id.button_google);
        EditText mUsername = view.findViewById(R.id.username);
        EditText mPassword = view.findViewById(R.id.password);

        registration.setOnClickListener(view12 -> invokeNextFragment(new CreateAccountWithEmail()));

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
        Response.Listener<String> listener = response -> {
            if(response.equalsIgnoreCase("1000"))
                LogManager.getInstance().showVisualMessage("L'utente non esiste, devi registrarti");
            else if(response.equalsIgnoreCase("1001"))
                loginManager.loginWithEmail(mail, password);
            else
                LogManager.getInstance().showVisualMessage("Hai usato un metodo diverso per la registrazione.");
        };

        Response.ErrorListener errorListener = error -> {
            LogManager.getInstance().printConsoleError(error.toString());
            LogManager.getInstance().showVisualMessage("Errore nel login dell'utente.");
        };

        DataManager.getInstance().checkIfTheUserAlreadyExists(mail, Constants.NATIVE, listener, errorListener);
    }

    private void facebookLogin() {
        method = Constants.FACEBOOK;
        FacebookLogin.initialize(getContext(), buttonFacebookLogin);
        FacebookLogin.getInstance().loginWithFacebook();
        //loginManager.loginWithFacebook(buttonFacebookLogin);
    }

    private void googleLogin() {
        loginManager.loginWithGoogle();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogManager.getInstance().printConsoleMessage("entrati in LoginFragment");
        if(method == Constants.FACEBOOK)
            FacebookLogin.getInstance().getCallbackManager().onActivityResult(requestCode, resultCode, data);
    }
}