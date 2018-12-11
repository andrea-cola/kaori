package com.kaori.kaori;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.kaori.kaori.BottomBarFragments.FeedFragment;
import com.kaori.kaori.BottomBarFragments.SearchFragment;
import com.kaori.kaori.BottomBarFragments.UsersPositionsFragment;
import com.kaori.kaori.DBObjects.User;
import com.kaori.kaori.ProfileFragments.ProfileFragment;
import com.kaori.kaori.Utils.DataManager;

public class KaoriApp extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {

    /**
     * Constants.
     */
    private final String BACK_STATE_NAME = getClass().getName();

    /**
     * Variables.
     */
    private User user;
    private DataManager hub;

    /**
     * Listener used to handle selections in the bottom bar.
     * Each branch of the switch handle the selection of one icon in the bar.
     */
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        bottomBarFragmentCall(new FeedFragment());
                        return true;
                    case R.id.navigation_hub:
                        bottomBarFragmentCall(new SearchFragment());
                        return true;
                    case R.id.navigation_study_with_me:
                        bottomBarFragmentCall(new UsersPositionsFragment());
                        return true;
                    case R.id.navigation_my_profile:
                        bottomBarFragmentCall(new ProfileFragment());
                        return true;
                }
                return false;
            };

    /**
     * On create method override.
     * If the signInWithEmail is needed we handle the press on the two buttons by invoking the right fragment.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hub = DataManager.getInstance();

        // toolbar initialization
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        user = hub.getUser();
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        entryPointFragmentCall(new FeedFragment());
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
