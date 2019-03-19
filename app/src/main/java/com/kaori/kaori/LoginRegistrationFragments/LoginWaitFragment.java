package com.kaori.kaori.LoginRegistrationFragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kaori.kaori.Model.User;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.LoginManager;
import com.kaori.kaori.Utils.SignInManager;

public class LoginWaitFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wait_layout, container, false);

        int processType = getArguments().getInt("processType");
        int authMethod = getArguments().getInt("authMethod");

        if(processType == Constants.LOGIN) {
            LoginManager loginManager = LoginManager.getInstance();

            /*if (authMethod == Constants.NATIVE)
                loginManager.loginWithEmail(params[0].toString(), params[1].toString());
            else if (authMethod == Constants.FACEBOOK)
                loginManager.loginWithFacebook((LoginButton) params[0]);
            else if (authMethod == Constants.GOOGLE)
                loginManager.loginWithGoogle();*/
        }
        else if(processType == Constants.SIGNIN) {
            User user = (User) getArguments().getSerializable("user");
            String password = getArguments().getString("password");
            byte[] bitmap = (byte[]) getArguments().getSerializable("image");

            SignInManager.getInstance().signInWithEmail(user, password, bitmap);
        }

        return view;
    }

}
