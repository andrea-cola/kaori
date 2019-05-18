package com.kaori.kaori.Configuration;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kaori.kaori.R;

public class ConfigFramgent extends Fragment {

    private final String BACK_STATE_NAME = getClass().getName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.config_layout, container, false);

        view.findViewById(R.id.button).setOnClickListener(view1 -> {
            getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new EditUniveristyInfo())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack(BACK_STATE_NAME)
                    .commit();
        });

        return view;
    }

}
