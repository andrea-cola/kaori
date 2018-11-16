package com.kaori.kaori.LoginRegistrationFragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kaori.kaori.Kaori;
import com.kaori.kaori.R;

public class LoginFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login2, container, false);

        if(getActivity() != null && isAdded())
            ((Kaori)getActivity()).getSupportActionBar().show();

        return view;
    }
}
