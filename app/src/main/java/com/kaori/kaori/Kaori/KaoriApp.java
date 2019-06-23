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
import com.kaori.kaori.Kaori.HomeFragments.HomeFragment;
import com.kaori.kaori.Kaori.MyMaterialFragments.MyMaterialFragment;
import com.kaori.kaori.Kaori.ProfileFragments.EditProfileInfo;
import com.kaori.kaori.Kaori.ProfileFragments.StudyPlanFragment;
import com.kaori.kaori.Kaori.ProfileFragments.ProfileFragment;
import com.kaori.kaori.Kaori.ProfileFragments.UploadFragments.UploadFragment;
import com.kaori.kaori.Kaori.SearchFragments.SearchFragment;
import com.kaori.kaori.R;
import com.kaori.kaori.Services.DataManager;
import com.kaori.kaori.Services.LogManager;

public class KaoriApp extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private TextView fragmentTitle;
    private DrawerLayout drawerLayout;
    private ImageView toolbarImage, headerImage;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kaoriapp);

        App.setWaitView(findViewById(R.id.wait_view));
        LogManager.initialize(findViewById(R.id.coordinator));

        setupToolbar();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        entryPointFragmentCall(new HomeFragment());
        fragmentTitle = findViewById(R.id.toolbar_title);
        activateNavigation();
    }

    public void callStarredFragment(){
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new MyMaterialFragment())
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack("PROFILE")
                .commit();
    }

    public void callProfileFragment(){
        setDrawerImages();
        entryPointFragmentCall(new ProfileFragment());
    }

    private void activateNavigation(){
        drawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(
                menuItem -> {
                    switch (menuItem.getItemId()){
                        case R.id.nav_profile:
                            fragmentTitle.setText(R.string.title_profile);
                            entryPointFragmentCallDrawer(new ProfileFragment());
                            return true;
                        case R.id.nav_uploads:
                            fragmentTitle.setText(R.string.title_upload);
                            entryPointFragmentCallDrawer(new UploadFragment());
                            return true;
                        case R.id.nav_esami:
                            fragmentTitle.setText(R.string.title_study_plan);
                            entryPointFragmentCallDrawer(new StudyPlanFragment());
                            return true;
                        case R.id.nav_impostazioni:
                            fragmentTitle.setText(R.string.title_profile);
                            entryPointFragmentCallDrawer(new EditProfileInfo());
                            return true;
                    }
                    return false;
                });


        toolbarImage = findViewById(R.id.toolbar_account_image);
        toolbarImage.setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));

        View view = navigationView.inflateHeaderView(R.layout.navigation_header);
        headerImage = view.findViewById(R.id.nav_header_image);
        ((TextView)view.findViewById(R.id.mail)).setText(DataManager.getInstance().getUser().getEmail());
        ((TextView)view.findViewById(R.id.nav_header_username)).setText(DataManager.getInstance().getUser().getName());
        setDrawerImages();
    }

    public void setDrawerImages(){
        DataManager.getInstance().loadImageIntoView(DataManager.getInstance().getUser().getPhotosUrl(),toolbarImage , this);
        DataManager.getInstance().loadImageIntoView(DataManager.getInstance().getUser().getPhotosUrl(), headerImage, this);
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
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setupToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
    }

    private void entryPointFragmentCall(Fragment fragment){
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    private void entryPointFragmentCallDrawer(Fragment fragment){
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
        drawerLayout.closeDrawers();
    }

}