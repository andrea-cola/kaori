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
import android.widget.TextView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kaori.kaori.Model.Chat;
import com.kaori.kaori.Model.MiniUser;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.DataManager;
import com.kaori.kaori.Utils.LogManager;

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
    private List<Chat> chatList; // list of all chats.
    private String myUid; // id of the user that uses the app.
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat_list_layout, container, false);
        mRecyclerView = view.findViewById(R.id.chat_list);

        chatList = new ArrayList<>();
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        myUid = DataManager.getInstance().getUser().getUid();

        // specify an adapter (see also next example)
        mAdapter = new MyAdapter(chatList);
        mRecyclerView.setAdapter(mAdapter);

        loadAllChats();

        return view;
    }

    private void loadAllChats(){
        FirebaseFirestore.getInstance()
                .collection(Constants.DB_COLL_MESSAGES)
                .orderBy(Constants.FIELD_LAST_MESSAGE)
                .addSnapshotListener((value, e) -> {
                    if(value != null) {
                        for (DocumentChange doc : value.getDocumentChanges()) {
                            switch (doc.getType()) {
                                case ADDED:
                                    Chat c = doc.getDocument().toObject(Chat.class);
                                    LogManager.getInstance().printConsoleMessage(c.getChatID());
                                    if(containsMyUid(c.getUsers())) {
                                        chatList.add(c);
                                        mAdapter.notifyDataSetChanged();
                                    }
                                    break;
                                case MODIFIED:
                                    // todo
                                    break;
                                case REMOVED:
                                    // todo
                                    break;
                            }
                        }
                    } else {
                        LogManager.getInstance().showVisualMessage(getContext(), "Nessun messaggio");
                    }
                });
    }

    private boolean containsMyUid(List<MiniUser> users){
        return users.get(0).getUid().equalsIgnoreCase(myUid) || users.get(1).getUid().equalsIgnoreCase(myUid);
    }

    private void invokeNextFragment(Fragment fragment){
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(BACK_STATE_NAME)
                .commit();
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

        private List<Chat> mDataset;
        class MyViewHolder extends RecyclerView.ViewHolder {

            // each data item is just a string in this case
            TextView mTextView;
            MyViewHolder(View v) {
                super(v);
                mTextView = v.findViewById(R.id.chat_user);
            }
        }
        // Provide a suitable constructor (depends on the kind of dataset)

        MyAdapter(List<Chat> myDataset) {
            mDataset = myDataset;
        }
        // Create new views (invoked by the layout manager)

        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_list_item, parent, false);

            v.setOnClickListener(view -> {
                ChatFragment chatFragment = new ChatFragment();
                chatFragment.setParams(mDataset.get(mRecyclerView.getChildLayoutPosition(v)));
                invokeNextFragment(chatFragment);
            });

            MyViewHolder vh = new MyViewHolder(v);
            return vh;
        }
        // Replace the contents of a view (invoked by the layout manager)

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            MiniUser otherUser = mDataset.get(position).getTheOtherUserByUid(myUid);
            holder.mTextView.setText(otherUser.getName());
        }
        // Return the size of your dataset (invoked by the layout manager)

        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }

}
