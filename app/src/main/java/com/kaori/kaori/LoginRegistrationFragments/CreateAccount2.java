package com.kaori.kaori.LoginRegistrationFragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kaori.kaori.Constants;
import com.kaori.kaori.DBObjects.User;
import com.kaori.kaori.Kaori;
import com.kaori.kaori.R;

public class CreateAccount2 extends Fragment {

    private User user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reg2, container, false);

        // show the title bar
        if(getActivity() != null && isAdded())
            ((Kaori) getActivity()).getSupportActionBar().setTitle(Constants.titleRegistrationForm);

        return view;
    }

    /*package-private*/ void setUser(User user){
        this.user = user;
    }
}
