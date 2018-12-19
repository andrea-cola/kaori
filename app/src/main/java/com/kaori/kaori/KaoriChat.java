package com.kaori.kaori;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.kaori.kaori.ChatFragments.ChatListFragment;
import com.kaori.kaori.Model.MiniUser;

public class KaoriChat extends AppCompatActivity {

    MiniUser otherUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kaorichat);
        setupToolbar();

        otherUser = null;
        if(getIntent() != null) {
            otherUser = (MiniUser) getIntent().getSerializableExtra("user");
            this.getIntent().removeExtra("user");
        }

        ChatListFragment chatListFragment = new ChatListFragment();
        if(otherUser != null)
            chatListFragment.setOtherUser(otherUser);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, chatListFragment)
                .commit();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    /**
     * Setup the toolbar.
     */
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
    }

}