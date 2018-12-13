package com.kaori.kaori.ChatFragments;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kaori.kaori.DBObjects.Chat;
import com.kaori.kaori.DBObjects.User;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.DataManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatListFragment extends Fragment {

    private final String BACK_STATE_NAME = getClass().getName();
    private List<Chat> chatList;
    private HashMap<String, User> chatUsers;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat_list_layout, container, false);
        mRecyclerView = view.findViewById(R.id.chat_list);

        chatList = new ArrayList<>();
        chatUsers = new HashMap<>();
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new MyAdapter(chatList);
        mRecyclerView.setAdapter(mAdapter);


        // TODO : da mettere nelle mappe
        /*view.findViewById(R.id.fab).setOnClickListener(v -> {
            ChatFragment chatFragment = new ChatFragment();
            chatFragment.setParams(true, "B3Vt13bfWfgIPDq8lHyzZJTLXCo1", "");
            invokeNextFragment(chatFragment);
        });*/

        readMessages();

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

    private void readMessages(){
        FirebaseFirestore.getInstance()
                .collection(Constants.DB_COLL_MESSAGES)
                .whereArrayContains("users", DataManager.getInstance().getUser().getUid())
                .orderBy("lastMessage")
                .addSnapshotListener((value, e) -> {
                    for (QueryDocumentSnapshot doc : value) {
                        chatList.add(doc.toObject(Chat.class));
                        mAdapter.notifyDataSetChanged();
                    }
                    //if(chatUidList.size() > 0)
                      //  loadUsers();
                });
    }

    /*private void loadUsers() {
        for (String id : chatUidList)
            FirebaseFirestore.getInstance()
                    .collection(Constants.DB_COLL_USERS)
                    .whereEqualTo("uid", id)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful())
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                chatUsers.put(id, document.toObject(User.class));
                                mAdapter.notifyDataSetChanged();
                            }
                    });
    }*/

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
                //chatFragment.setParams(false, "", mDataset.get(mRecyclerView.getChildLayoutPosition(view)));
                invokeNextFragment(chatFragment);
            });

            MyViewHolder vh = new MyViewHolder(v);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            holder.mTextView.setText(mDataset.get(position).getUsers().get(0));

        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }
}
