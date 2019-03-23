package com.kaori.kaori;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.kaori.kaori.ChatFragments.ChatFragment;
import com.kaori.kaori.ChatFragments.ChatListFragment;
import com.kaori.kaori.Model.MiniUser;
import com.kaori.kaori.Utils.LogManager;

public class KaoriChat extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kaorichat);
        setupToolbar();

        MiniUser friend = (MiniUser) getIntent().getExtras().get("user");
        LogManager.getInstance().printConsoleMessage("Eccoci . " + friend.getUid());

        if(getIntent() != null && getIntent().getExtras() != null && friend != null) {
            ChatFragment chatFragment = new ChatFragment();
            chatFragment.setParams(friend);
            invokeNextFragment(chatFragment);
        } else {
            invokeNextFragment(new ChatListFragment());
        }
    }

    public void invokeNextFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
    }

}