package com.kaori.kaori.Kaori;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.kaori.kaori.App;
import com.kaori.kaori.Chat.KaoriChat;
import com.kaori.kaori.Kaori.FinderFragment.FinderFragment;
import com.kaori.kaori.Kaori.ProfileFragments.*;
import com.kaori.kaori.Kaori.ProfileFragments.UploadFragments.*;
import com.kaori.kaori.Kaori.HomeFragments.HomeFragment;
import com.kaori.kaori.Kaori.MyMaterialFragments.MyMaterialFragment;
import com.kaori.kaori.Kaori.ProfileFragments.ProfileFragment;
import com.kaori.kaori.Kaori.SearchFragments.SearchFragment;
import com.kaori.kaori.R;
import com.kaori.kaori.Services.DataManager;
import com.kaori.kaori.Services.LogManager;


/**
 * This is the main activity of the app.
 * Kaori App handles the flow to the fragments.
 */
public class KaoriApp extends AppCompatActivity {

    private TextView fragmentTitle;

    /**
     * Listener used to handle selections in the bottom bar.
     * Each branch of the switch handle the selection of one icon in the bar.
     */
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                fragmentTitle.setText(R.string.title_feed);
                entryPointFragmentCall(new HomeFragment());
                return true;
            case R.id.navigation_search:
                fragmentTitle.setText(R.string.title_search);
                entryPointFragmentCall(new SearchFragment());
                return true;
            case R.id.navigation_preferred:
                fragmentTitle.setText(R.string.title_starred);
                entryPointFragmentCall(new MyMaterialFragment());
                return true;
            case R.id.navigation_share:
                fragmentTitle.setText(R.string.title_finder);
                entryPointFragmentCall(new FinderFragment());
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
        setContentView(R.layout.activity_kaoriapp);

        App.setWaitView(findViewById(R.id.wait_view));
        LogManager.initialize(findViewById(R.id.coordinator));

        setupToolbar();

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        entryPointFragmentCall(new HomeFragment());

        activateDrawer();

    }

    private void activateDrawer(){
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(
                menuItem -> {
                    switch (menuItem.getItemId()){
                        case R.id.nav_profile:
                            entryPointFragmentCall(new ProfileFragment());
                            drawerLayout.closeDrawers();
                            return true;
                        case R.id.nav_uploads:
                            entryPointFragmentCall(new UploadFragment());
                            drawerLayout.closeDrawers();
                            return true;
                        case R.id.nav_esami:
                            entryPointFragmentCall(new EditPlanFragment());
                            drawerLayout.closeDrawers();
                            return true;
                        case R.id.nav_impostazioni:
                            entryPointFragmentCall(new EditProfileInfo());
                            drawerLayout.closeDrawers();
                            return true;
                    }
                    return false;
                });


        ImageView toolbarImage = findViewById(R.id.toolbar_account_image);
        toolbarImage.setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));

        View view = navigationView.inflateHeaderView(R.layout.navigation_header);
        ImageView drawerImage = view.findViewById(R.id.nav_header_image);
        ((TextView)view.findViewById(R.id.nav_header_username)).setText(DataManager.getInstance().getUser().getName());
        ((TextView)view.findViewById(R.id.nav_header_mail)).setText(DataManager.getInstance().getUser().getEmail());
        DataManager.getInstance().loadImageIntoView(DataManager.getInstance().getUser().getPhotosUrl(), toolbarImage, this);
        DataManager.getInstance().loadImageIntoView(DataManager.getInstance().getUser().getPhotosUrl(), drawerImage, this);
        ((TextView) view.findViewById(R.id.uploads_cont)).setText(DataManager.getInstance().getMyFiles().size() + " uploads");
        ((TextView) view.findViewById(R.id.starred_book_cont)).setText(DataManager.getInstance().getStarredBooks().size()+" books");
        ((TextView) view.findViewById(R.id.starred_doc_count)).setText(DataManager.getInstance().getStarredDocuments().size()+" docs");
        fragmentTitle = findViewById(R.id.toolbar_title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.message_action_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_message) {
            startActivity(new Intent(this, KaoriChat.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            return true;
        } else if (item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        shouldDisplayHomeUp();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Setup the toolbar.
     */
    private void setupToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
    }

    /**
     * Method used to hide the bottom bar when it is useless.
     */
    public void hideBottomBar(boolean flag){
        findViewById(R.id.navigation).setVisibility(flag ? View.GONE : View.VISIBLE);
    }

    /**
     * Enable Up button only if there are entries in the back stack.
     * I set the limit to 1 because we do not want to return to empty activity.
     */
    public void shouldDisplayHomeUp(){
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(getSupportFragmentManager().getBackStackEntryCount() > 0);
    }

    /**
     * This methods is called by the application when it is opened.
     * It invokes the first fragment.
     */
    private void entryPointFragmentCall(Fragment fragment){
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        shouldDisplayHomeUp();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }
}