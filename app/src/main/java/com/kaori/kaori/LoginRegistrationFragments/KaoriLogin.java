package com.kaori.kaori.LoginRegistrationFragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.LogManager;

/**
 * Activity responsible for the Login and Signin.
 */
public class KaoriLogin extends AppCompatActivity {

    /**
     * OnCreate override.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kaorilogin);
        LogManager.getInstance(findViewById(R.id.coordinator), findViewById(R.id.wait_layout));

        entryPointFragmentCall(new LoginFragment());
    }

    /**
     * Method used to retrieve and use the result of the login
     * done by Facebook and Google.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.GOOGLE_LOGIN_REQUEST)
            try {
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                GoogleLogin.getInstance().validateProvider(account);
            } catch (ApiException e) {
                LogManager.getInstance().showVisualError(e, getString(R.string.google_error));
            }
        else
            FacebookLogin.getInstance().getCallbackManager().onActivityResult(requestCode, resultCode, data);
    }

    /**
     * This methods is called by the application when it is opened.
     * It invokes the first fragment.
     */
    private void entryPointFragmentCall(Fragment fragment){
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

}