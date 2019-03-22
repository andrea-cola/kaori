package com.kaori.kaori.ChatFragments;

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
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.DataManager;

import java.util.List;

/**
 * This class shows all the chat started by the user.
 */
public class ChatListFragment extends Fragment {

    /**
     * Constants.
     */
    private final String BACK_STATE_NAME = getClass().getName();

    /**
     * Variables.
     */
    private RecyclerView mRecyclerView;
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.chat_list_layout, container, false);
        mRecyclerView = view.findViewById(R.id.chat_list);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        // specify an adapter (see also next example)
        mRecyclerView.setAdapter(new ChatAdapter(DataManager.getInstance().getAllChats()));

        DataManager.getInstance().downloadMyChats(mRecyclerView, view);

        return view;
    }

    /**
     * Call the next fragment.
     */
    private void invokeNextFragment(Fragment fragment){
        if(getActivity() != null)
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack(BACK_STATE_NAME)
                    .commit();
    }



    /**
     * Recycler view adapter.
     */
    private class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {

        /**
         * My chat dataset.
         */
        private List<Chat> mDataset;

        /**
         * Class constructor.
         */
        /*package-private*/ ChatAdapter(List<Chat> myDataset) {
            mDataset = myDataset;
        }

        @Override
        public ChatAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_list_item, parent, false);

            // click listener on the single chat
            v.setOnClickListener(view -> {
                ChatFragment chatFragment = new ChatFragment();
                chatFragment.setParams(mDataset.get(mRecyclerView.getChildLayoutPosition(v)));
                invokeNextFragment(chatFragment);
            });

            return new MyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            MiniUser otherUser = mDataset.get(position).getTheOtherUserByUid(DataManager.getInstance().getUser().getUid());
            holder.chatUser.setText(otherUser.getName());
            holder.chatDate.setText(Constants.dateFormat2.format(mDataset.get(position).getLastMessageSent()));
            DataManager.getInstance().loadImageIntoView(otherUser.getThumbnail(), holder.chatImage, getContext());
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }

        /**
         * Holder of the chat.
         */
        /*package-private*/ class MyViewHolder extends RecyclerView.ViewHolder {
            TextView chatUser, chatDate;
            ImageView chatImage;

            MyViewHolder(View v) {
                super(v);
                chatUser = v.findViewById(R.id.chat_user);
                chatImage = v.findViewById(R.id.image);
                chatDate = v.findViewById(R.id.date);
            }
        }
    }

}
