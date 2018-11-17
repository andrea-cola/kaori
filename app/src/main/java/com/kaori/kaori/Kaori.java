package com.kaori.kaori;

import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.kaori.kaori.LoginRegistrationFragments.LoginRegistrationFragment;

/**
 * Entry point of the app.
 */
public class Kaori extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {

    /**
     * Constants.
     */
    private final String BACK_STATE_NAME = getClass().getName();

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
                    return true;
                case R.id.navigation_hub:
                    return true;
                case R.id.navigation_study_with_me:
                    return true;
                case R.id.navigation_my_profile:
                    return true;
            }
            return false;
        }
    };

    /**
     * On create method override.
     * If the login is needed we handle the press on the two buttons by invoking the right fragment.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(checkLoginStatus()) {

            setContentView(R.layout.activity_main);
            BottomNavigationView navigation = findViewById(R.id.navigation);
            navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

            // TODO: settare il primo fragment
        }
        else {
            getSupportActionBar().hide();

            //Listen for changes in the back stack
            getSupportFragmentManager().addOnBackStackChangedListener(this);
            //Handle when activity is recreated like on orientation Change
            shouldDisplayHomeUp();

            setContentView(R.layout.activity_empty_main);

            // go to the first phase of the login/registration
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new LoginRegistrationFragment())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
        }
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

    /**
     * Enable Up button only if there are entries in the back stack.
     * I set the limit to 1 because we do not want to return to empty activity.
     */
    public void shouldDisplayHomeUp(){
        boolean canGoBack = getSupportFragmentManager().getBackStackEntryCount() > 0;
        getSupportActionBar().setDisplayHomeAsUpEnabled(canGoBack);
    }

    @Override
    public boolean onSupportNavigateUp() {
        //This method is called when the up button is pressed. Just the pop back stack.
        getSupportFragmentManager().popBackStack();
        return true;
    }

    @Override
    public void onBackStackChanged() {
        shouldDisplayHomeUp();
    }

}
