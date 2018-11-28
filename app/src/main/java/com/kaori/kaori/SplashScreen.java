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

public class SplashScreen extends AppCompatActivity {

    private DataHub hub;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        setup();
    }

    private void setup(){
        Runnable r = new Runnable() {
            public void run() {
                // initialize the DataHub
                hub = DataHub.getInstance();
                hub.setAuthenticated(checkLoginStatus());

                if(hub.isAuthenticated())
                    downloadUserProfile();
                else
                    startKaori();
            }
        };
        new Handler().postDelayed(r, 3000);
    }

    private void startKaori(){
        startActivity(new Intent(this, Kaori.class));
    }

    private boolean checkLoginStatus(){
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

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
                        startKaori();
                    }
                });
    }

}
