package com.kaori.kaori.Chat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.kaori.kaori.App;
import com.kaori.kaori.Chat.ChatFragments.ChatFragment;
import com.kaori.kaori.Chat.ChatFragments.ChatListFragment;
import com.kaori.kaori.Model.MiniUser;
import com.kaori.kaori.R;
import com.kaori.kaori.Services.LogManager;

public class KaoriChat extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kaorichat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        App.setWaitView(findViewById(R.id.wait_view));
        LogManager.initialize(findViewById(R.id.coordinator));

        MiniUser friend = (MiniUser) getIntent().getExtras().get("user");
        String name = (String)getIntent().getExtras().get("name");

        if(getIntent() != null && getIntent().getExtras() != null && friend != null) {
            ChatFragment chatFragment = new ChatFragment();
            chatFragment.setParams(friend);
            getSupportFragmentManager().beginTransaction().replace(R.id.container, chatFragment).commit();
        } else {


            ChatListFragment fragment = new ChatListFragment();
            if (name != null && !name.isEmpty())
                fragment.setName(name);

            getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment)
                    .commit();
        }
    }

}