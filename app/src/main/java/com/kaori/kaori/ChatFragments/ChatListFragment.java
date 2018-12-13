package com.kaori.kaori.ChatFragments;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kaori.kaori.BottomBarFragments.UploadBookFragment;
import com.kaori.kaori.R;

public class ChatListFragment extends Fragment {

    private final String BACK_STATE_NAME = getClass().getName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat_list_layout, container, false);

        view.findViewById(R.id.fab).setOnClickListener(v -> {
            invokeNextFragment(new UploadBookFragment());
        });

        return view;
    }

    /**
     * This method handles the invocation of a new fragment
     * when the user clicks on the bottom bar.
     */
    private void invokeNextFragment(Fragment fragment){
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(BACK_STATE_NAME)
                .commit();
    }
}
