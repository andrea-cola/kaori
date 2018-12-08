package com.kaori.kaori.LoginRegistrationFragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.login.widget.LoginButton;
import com.kaori.kaori.DBObjects.User;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.AuthMethods;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.LogManager;
import com.kaori.kaori.Utils.LoginManager;
import com.kaori.kaori.Utils.SignInManager;

/**
 * This class is a simple wait layout that calls the function
 * needed to make the log in correctly. In the end restart the activity
 * on the base of the result.
 */
public class WaitFragment extends Fragment {

    /**
     * Variables.
     */
    private AuthMethods authMethod;
    private Object[] params;
    private int processType;

    /**
     * Override of the method onCreateView.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wait_fragment, container, false);
        LogManager.setView(view);

        if(processType == Constants.LOGIN) {
            LoginManager loginManager = LoginManager.getInstance();

            if (authMethod == AuthMethods.NATIVE)
                loginManager.loginWithEmail(params[0].toString(), params[1].toString());
            else if (authMethod == AuthMethods.FACEBOOK)
                loginManager.loginWithFacebook((LoginButton) params[0]);
            else if (authMethod == AuthMethods.GOOGLE)
                loginManager.loginWithGoogle();
        }
        else if(processType == Constants.SIGNIN) {
            SignInManager signInManager = SignInManager.getInstance();
            if (authMethod == AuthMethods.NATIVE)
                signInManager.signInWithEmail((User)params[0], params[1].toString(), (Bitmap)params[2]);
            else if (authMethod == AuthMethods.FACEBOOK)
                signInManager.signInWithFacebook((LoginButton) params[0]);
            else if (authMethod == AuthMethods.GOOGLE)
                signInManager.signInWithGoogle();
        }

        return view;
    }

    /**
     * This method allow the previous fragment to set the parameters
     * relative to the login.
     * AuthMethod describes the method used to log in.
     * Params are additional parameters.
     */
    public void setParameters(int processType, AuthMethods a, Object[] params){
        this.processType = processType;
        this.authMethod = a;
        this.params = params;
    }

}
