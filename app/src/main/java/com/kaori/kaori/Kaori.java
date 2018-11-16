package com.kaori.kaori;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * Entry point of the app.
 */
public class Kaori extends AppCompatActivity {

    /**
     * Variables.
     */
    private TextView mTextMessage;

    /**
     * Listener used to handle selections in the bottom bar.
     * Each branch of the switch handle the selection of one icon in the bar.
     */
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_hub:
                    mTextMessage.setText(R.string.title_hub);
                    return true;
                case R.id.navigation_study_with_me:
                    mTextMessage.setText(R.string.title_study_with_me);
                    return true;
                case R.id.navigation_my_profile:
                    mTextMessage.setText(R.string.title_my_profile);
                    return true;
            }
            return false;
        }
    };

    /**
     * On create method override.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(checkLoginStatus()) {
            setContentView(R.layout.activity_main);
            BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
            navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        }
        else
            setContentView(R.layout.login1);
    }

    /**
     * This method checks if the user has already logged in the app.
     * If yes returns true, otherwise false.
     */
    private boolean checkLoginStatus(){
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = app_preferences.edit();
        return app_preferences.getBoolean("master_login", false);
    }

}
