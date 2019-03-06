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

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kaori.kaori.KaoriChat;
import com.kaori.kaori.Model.Chat;
import com.kaori.kaori.Model.MiniUser;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.DataManager;

import java.util.ArrayList;
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
    private List<Chat> chatList;
    private String myUid;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private MiniUser otherUser;
    private View view;
    private Fragment fragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.chat_list_layout, container, false);
        mRecyclerView = view.findViewById(R.id.chat_list);

        if(getActivity() != null)
            ((KaoriChat)getActivity()).getSupportActionBar().show();

        fragment = this;

        if(otherUser != null) {
            ChatFragment chatFragment = new ChatFragment();
            chatFragment.newChatParams(otherUser);
            otherUser = null;
            invokeNextFragment(chatFragment);
        } else {
            chatList = new ArrayList<>();
            mRecyclerView.setHasFixedSize(true);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
            mRecyclerView.setLayoutManager(mLayoutManager);
            myUid = DataManager.getInstance().getUser().getUid();

            // specify an adapter (see also next example)
            mAdapter = new ChatAdapter(chatList);
            mRecyclerView.setAdapter(mAdapter);

            loadAllChats();
        }

        return view;
    }

    /**
     * Load all the chats from the database.
     */
    private void loadAllChats(){
        FirebaseFirestore.getInstance()
                .collection(Constants.DB_COLL_MESSAGES)
                .orderBy(Constants.FIELD_LAST_MESSAGE)
                .addSnapshotListener((value, e) -> {
                    if (value != null){
                        for (DocumentChange doc : value.getDocumentChanges()) {
                            handleDocumentChange(doc.getType(), doc.getDocument().toObject(Chat.class));
                            view.findViewById(R.id.wait_layout).setVisibility(View.GONE);
                        }
                        boolean notmine = true;
                        for (DocumentSnapshot doc : value.getDocuments()){
                            if(containsMyUid(doc.toObject(Chat.class).getUsers()))
                               notmine = false;
                        }
                        if(notmine)
                            view.findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
                    } else
                        view.findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
                });
    }

    /**
     * Handle the realtime update of the chats.
     */
    private void handleDocumentChange(DocumentChange.Type type, Chat c){
        switch (type) {
            case ADDED:
                if(containsMyUid(c.getUsers())) {
                    chatList.add(c);
                    mAdapter.notifyDataSetChanged();
                }
                break;
            case MODIFIED:

                break;
            case REMOVED:
                if(containsMyUid(c.getUsers())) {
                    chatList.remove(c);
                    mAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    /**
     * Set the adversary user of the chat.
     */
    public void setOtherUser(MiniUser miniUser){
        this.otherUser = miniUser;
    }

    /**
     * Check if the chat contains my uid.
     */
    private boolean containsMyUid(List<MiniUser> users){
        return users.get(0).getUid().equalsIgnoreCase(myUid) || users.get(1).getUid().equalsIgnoreCase(myUid);
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
            MiniUser otherUser = mDataset.get(position).getTheOtherUserByUid(myUid);
            holder.chatUser.setText(otherUser.getName());
            holder.chatDate.setText(Constants.dateFormat2.format(mDataset.get(position).getLastMessageSent().toDate()));
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
