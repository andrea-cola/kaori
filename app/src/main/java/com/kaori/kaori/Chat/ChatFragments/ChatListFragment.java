package com.kaori.kaori.Chat.ChatFragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kaori.kaori.Model.Chat;
import com.kaori.kaori.Model.MiniUser;
import com.kaori.kaori.R;
import com.kaori.kaori.Constants;
import com.kaori.kaori.Services.DataManager;

import java.util.List;

public class ChatListFragment extends Fragment {

    private final String BACK_STATE_NAME = getClass().getName();
    private RecyclerView mRecyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat_list_layout, container, false);
        mRecyclerView = view.findViewById(R.id.chat_list);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.setAdapter(new ChatAdapter(DataManager.getInstance().getAllChats()));

        DataManager.getInstance().downloadMyChats(mRecyclerView, view);

        return view;
    }

    private void invokeNextFragment(Fragment fragment){
        if(getActivity() != null)
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack(BACK_STATE_NAME)
                    .commit();
    }

    private class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {

        private List<Chat> mDataset;

        ChatAdapter(List<Chat> myDataset) {
            mDataset = myDataset;
        }

        @Override
        public ChatAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            MiniUser otherUser = mDataset.get(position).getTheOtherUserByUid(DataManager.getInstance().getUser().getUid());
            holder.chatUser.setText(otherUser.getName());
            holder.chatDate.setText(Constants.dateFormat.format(mDataset.get(position).getLastMessageSent()));
            DataManager.getInstance().loadImageIntoView(otherUser.getThumbnail(), holder.chatImage, getContext());
            holder.view.setOnClickListener(view -> {
                ChatFragment chatFragment = new ChatFragment();
                chatFragment.setParams(otherUser);
                invokeNextFragment(chatFragment);
            });
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView chatUser, chatDate;
            ImageView chatImage;
            View view;

            MyViewHolder(View v) {
                super(v);
                view = v;
                chatUser = v.findViewById(R.id.chat_user);
                chatImage = v.findViewById(R.id.image);
                chatDate = v.findViewById(R.id.date);
            }
        }
    }

}
