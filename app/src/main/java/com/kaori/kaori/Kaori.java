package com.kaori.kaori;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.DataManager;
import com.kaori.kaori.Utils.LogManager;

/**
 * Splash screen activity.
 */
public class Kaori extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        setup();
    }

    /**
     * Creates a new thread that handles the setup.
     * It verifies the authentication:
     * If auth=true => download the user from db
     * If auth=false => start the app
     */
    private void setup(){
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);

        LogManager.getInstance();
        DataManager.getInstance(this);

        new Handler().postDelayed(
                () -> {
                    LogManager.getInstance().printConsoleMessage("Kaori started.");
                    if(FirebaseAuth.getInstance().getCurrentUser() != null)
                        downloadUserProfile();
                    else
                        startLogin();
        }, Constants.SPLASH_SCREEN_WAITING_TIME);
    }

    /**
     * Start the login activity.
     */
    public void startLogin(){
        startActivity(new Intent(this, KaoriLogin.class));
        finish();
    }

    /**
     * Start the app activity.
     */
    public void startApp(){
        startActivity(new Intent(this, KaoriApp.class));
        finish();
    }

    /**
     * Download the user from the database and run the main activity.
     */
    private void downloadUserProfile(){
        DataManager.getInstance().downloadUserProfile(FirebaseAuth.getInstance().getCurrentUser().getUid(), this);
    }

}