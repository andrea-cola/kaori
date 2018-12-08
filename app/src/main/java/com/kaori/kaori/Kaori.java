package com.kaori.kaori;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kaori.kaori.DBObjects.User;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.DataManager;
import com.kaori.kaori.Utils.LogManager;

/**
 * Splash screen activity.
 */
public class Kaori extends AppCompatActivity {

    /**
     * Variables.
     */
    private DataManager hub;

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
        // initialize Log manager.
        LogManager.getInstance();

        // initialize the DataHub
        hub = DataManager.getInstance();
        hub.setAuthenticated(checkLoginStatus());

<<<<<<< HEAD
            FirebaseAuth.getInstance().signOut();
<<<<<<< HEAD
=======
        Runnable r = () -> {
>>>>>>> 9ec62d99d080bfcad7189dc1723bc5693444443f
            if(hub.isAuthenticated())
                downloadUserProfile();
            else
                startKaoriLogin();
<<<<<<< HEAD
=======
            //if(hub.isAuthenticated())
                //downloadUserProfile();
            //else
            startKaoriLogin();
>>>>>>> 8087fa2e83ba9e5efc7343995bc73d0bfeb4a71a
=======
>>>>>>> 9ec62d99d080bfcad7189dc1723bc5693444443f
        };
        new Handler().postDelayed(r, Constants.SPLASH_SCREEN_WAITING_TIME);
    }

    /**
     * Start the login activity.
     */
    private void startKaoriLogin(){
        startActivity(new Intent(this, KaoriLogin.class));
        finish();
    }

    /**
     * Start the app activity.
     */
    private void startKaoriApp(){
        startActivity(new Intent(this, KaoriApp.class));
        finish();
    }

    /**
     * Check if the user is authenticated.
     */
    private boolean checkLoginStatus(){
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    /**
     * Download the user from the database and run the main activity.
     */
    private void downloadUserProfile(){
        FirebaseFirestore
            .getInstance()
            .collection(Constants.DB_COLL_USERS)
            .whereEqualTo("uid", FirebaseAuth.getInstance().getCurrentUser().getUid())
            .get()
            .addOnCompleteListener(task -> {
                hub.setUser(task.getResult().getDocuments().get(0).toObject(User.class));
                startKaoriApp();
            });
    }

}
