package com.kaori.kaori;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.auth.FirebaseAuth;
import com.kaori.kaori.BottomBarFragments.FeedFragment;
import com.kaori.kaori.BottomBarFragments.MapFragment;
import com.kaori.kaori.BottomBarFragments.SearchFragment;
import com.kaori.kaori.BottomBarFragments.UserPositionsFragment;
import com.kaori.kaori.LoginRegistrationFragments.LoginRegistrationFragment;
import com.kaori.kaori.ProfileFragments.ProfileFragment;

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
    private boolean authenticated;

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
                    bottomBarFragmentCall(new FeedFragment());
                    return true;
                case R.id.navigation_hub:
                    bottomBarFragmentCall(new SearchFragment());
                    return true;
                case R.id.navigation_study_with_me:
                    bottomBarFragmentCall(new UserPositionsFragment());
                    return true;
                case R.id.navigation_my_profile:
                    bottomBarFragmentCall(new ProfileFragment());
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

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        if(checkLoginStatus()) {
            setContentView(R.layout.activity_main);
            BottomNavigationView navigation = findViewById(R.id.navigation);
            navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

            // empty the stack.
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            entryPointFragmentCall(new FeedFragment());
        }
        else {
            getSupportActionBar().hide();

            //Listen for changes in the back stack
            getSupportFragmentManager().addOnBackStackChangedListener(this);
            //Handle when activity is recreated like on orientation Change
            shouldDisplayHomeUp();
            setContentView(R.layout.activity_empty_main);

            entryPointFragmentCall(new LoginRegistrationFragment());
        }
    }

    /**
     * This method checks if the user has already logged in the app.
     * If yes returns true, otherwise false.
     */
    private boolean checkLoginStatus(){
        if(authenticated)
            return true;
        authenticated = FirebaseAuth.getInstance().getCurrentUser() != null;
        return authenticated;
    }

    /**
     * Enable Up button only if there are entries in the back stack.
     * I set the limit to 1 because we do not want to return to empty activity.
     */
    public void shouldDisplayHomeUp(){
        boolean canGoBack = getSupportFragmentManager().getBackStackEntryCount() > 0;
        getSupportActionBar().setDisplayHomeAsUpEnabled(canGoBack);
    }

    /**
     * This method overrides the default one, letting the user get
     * back to the previous fragment by clicking on the arrow on the top of the screen.
     */
    @Override
    public boolean onSupportNavigateUp() {
        //This method is called when the up button is pressed. Just the pop back stack.
        getSupportFragmentManager().popBackStack();
        return true;
    }

    /**
     * Handles the changes in the back stack.
     */
    @Override
    public void onBackStackChanged() {
        shouldDisplayHomeUp();
    }

    /**
     * Override of the default method.
     * Checks if the user is already logged in.
     */
    @Override
    public void onStart(){
        super.onStart();
        authenticated = FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    /**
     * This method handles the invocation of a new fragment
     * when the user clicks on the bottom bar.
     */
    private void bottomBarFragmentCall(Fragment fragment){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(BACK_STATE_NAME)
                .commit();
    }

    /**
     * This methods is called by the application when it is opened.
     * It invokes the first fragment.
     */
    private void entryPointFragmentCall(Fragment fragment){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

}
