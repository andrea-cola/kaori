package com.kaori.kaori;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
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
        LogManager.getInstance();

        Runnable r = () -> {
            // initialize the DataHub
            hub = DataManager.getInstance();
            hub.setAuthenticated(checkLoginStatus());

            FirebaseAuth.getInstance().signOut();
            if(hub.isAuthenticated())
                downloadUserProfile();
            else
                startKaoriLogin();
        };
        new Handler().postDelayed(r, Constants.SPLASH_SCREEN_WAITING_TIME);
    }

    /**
     * Start the main activity.
     */
    private void startKaoriLogin(){
        startActivity(new Intent(this, KaoriLogin.class));
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
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    hub.setUser(task.getResult().getDocuments().get(0).toObject(User.class));
                    //startKaori();
                }
            });
    }

}
