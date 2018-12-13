package com.kaori.kaori.ChatFragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kaori.kaori.DBObjects.Message;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.DataManager;
import com.kaori.kaori.Utils.LogManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatFragment extends Fragment {

    private boolean isNewChat;
    private String receiverUID, chatID;
    private ImageButton mSendMessage;
    private String messageBucketID;
    private EditText editText;
    private List<String> users;
    private Map<String, Object> chatParameters;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<Message> messages;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat_layout, container, false);
        editText = view.findViewById(R.id.edittext);
        mSendMessage = view.findViewById(R.id.send_button);

        users = new ArrayList<>();
        chatParameters = new HashMap<>();
        chatParameters.put("users", users);
        users.add(DataManager.getInstance().getUser().getUid());
        users.add(receiverUID);
        messageBucketID = buildMessageBucketID();
        LogManager.getInstance().printConsoleMessage("Message Bucket ID -> " + messageBucketID);

        mRecyclerView = view.findViewById(R.id.chat_list);

        messages = new ArrayList<>();
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new MyAdapter(messages);
        mRecyclerView.setAdapter(mAdapter);

        addOnClickListener();

        if(!isNewChat)
            readMessages();

        return view;
    }

    private void readMessages(){
        FirebaseFirestore.getInstance()
                .collection(Constants.DB_COLL_MESSAGES)
                .document(chatID)
                .collection(Constants.DB_SUBCOLL_MESSAGES)
                .addSnapshotListener((value, e) -> {
                    for (DocumentSnapshot doc : value) {
                        LogManager.getInstance().printConsoleMessage(doc.getId());
                        messages.add(doc.toObject(Message.class));
                        mAdapter.notifyDataSetChanged();
                    }
                });
    }

    private String buildMessageBucketID(){
        String uid = DataManager.getInstance().getUser().getUid();
        if(uid.compareTo(receiverUID) > 0)
            return receiverUID + "_" + uid;
        return uid + "_" + receiverUID;
    }

    public void setParams(boolean newChat, String uid, String chatID){
        this.isNewChat = newChat;
        this.receiverUID = uid;
        this.chatID = chatID;
    }

    private void addOnClickListener(){
        mSendMessage.setOnClickListener(view -> {
            Message message = new Message();
            message.setChatID(messageBucketID);
            message.setMessage(String.valueOf(editText.getText()));
            message.setReceiver(receiverUID);
            message.setSenderID(DataManager.getInstance().getUser().getUid());
            message.setTimestamp(Timestamp.now());
            writeDatabase(message);

            if(isNewChat)
                isNewChat = false;
        });
    }

    private void writeDatabase(Message m) {
        LogManager.getInstance().printConsoleMessage("writeDatabase");
        DocumentReference documentReference = FirebaseFirestore
                .getInstance()
                .collection(Constants.DB_COLL_MESSAGES)
                .document(messageBucketID);

        chatParameters.put("lastMessage", Timestamp.now());
        documentReference.set(chatParameters).addOnSuccessListener(aVoid -> {
            documentReference.collection(Constants.DB_SUBCOLL_MESSAGES)
                    .document()
                    .set(m)
                    .addOnSuccessListener(aVoid1 -> {
                        LogManager.getInstance().showVisualMessage(getContext(),"writeDatabase:written");
                    })
                    .addOnFailureListener(e -> {
                        LogManager.getInstance().showVisualMessage(getContext(),"writeDatabase:fail");
                    });
        });
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

        private final int MY_MESSAGE = 0;
        private final int OTHER_MESSAGE = 1;
        private List<Message> mDataset;

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView author, content, timestamp;

            MyViewHolder(View v) {
                super(v);
                author = v.findViewById(R.id.message_author);
                content = v.findViewById(R.id.message_content);
                timestamp = v.findViewById(R.id.message_time);
            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        MyAdapter(List<Message> myDataset) {
            mDataset = myDataset;
        }

        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            View v;
            if(viewType == MY_MESSAGE)
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_right, parent, false);
            else
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_left, parent, false);

            MyAdapter.MyViewHolder vh = new MyAdapter.MyViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(MyAdapter.MyViewHolder holder, int position) {
            if(getItemViewType(position) == MY_MESSAGE)
                if(mDataset.get(position).getSenderID().equalsIgnoreCase(DataManager.getInstance().getUser().getUid()))
                    holder.author.setText(mDataset.get(position).getSenderID());
                else
                    holder.author.setText(DataManager.getInstance().getUser().getUid());
            else
                if(mDataset.get(position).getSenderID().equalsIgnoreCase(DataManager.getInstance().getUser().getUid()))
                    holder.author.setText(DataManager.getInstance().getUser().getUid());
                else
                    holder.author.setText(mDataset.get(position).getSenderID());

            holder.content.setText(mDataset.get(position).getMessage());
            SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            holder.timestamp.setText(sfd.format(mDataset.get(position).getTimestamp().toDate()));
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }

        @Override
        public int getItemViewType(int position) {
            if(messages.get(position).getSenderID().equalsIgnoreCase(DataManager.getInstance().getUser().getUid()))
                return MY_MESSAGE;
            return OTHER_MESSAGE;
        }
    }

}
