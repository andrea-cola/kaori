package com.kaori.kaori;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.kaori.kaori.LoginRegistrationFragments.CreateAccount1;
import com.kaori.kaori.LoginRegistrationFragments.LoginFragment;

public class KaoriLogin extends AppCompatActivity {

    private final String BACK_STATE_NAME = getClass().getName();
    private Button mLogin, mRegistration;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        mLogin = findViewById(R.id.login_button);
        mRegistration = findViewById(R.id.signin_button);
        setClickListeners();
    }

    private void setClickListeners(){
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                invokeNextFragment(new LoginFragment());
            }
        });

        mRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                invokeNextFragment(new CreateAccount1());
            }
        });

    }

    private void invokeNextFragment(Fragment f){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_layout, f)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(BACK_STATE_NAME)
                .commit();
    }

}
