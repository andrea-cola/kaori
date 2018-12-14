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
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kaori.kaori.DBObjects.Chat;
import com.kaori.kaori.DBObjects.Message;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.DataManager;
import com.kaori.kaori.Utils.LogManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ChatFragment extends Fragment {

    private boolean isNewChat;
    private Chat chat;
    private String senderUID, receiverUID, receiverName, chatID;
    private ImageButton mSendMessage;
    private EditText editText;
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
        mRecyclerView = view.findViewById(R.id.chat_list);

        messages = new LinkedList<>();
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyAdapter(messages);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.scrollToPosition(View.FOCUS_DOWN);

        // add listener to the button that sends messages.
        addOnClickListener();

        // if the chat is new, we don't need to load messages.
        readMessages();

        // Todo: la soluzione al problema è nascondere la barra sotto

        return view;
    }

    public void newChatParams(String uid, String name){
        this.isNewChat = true;
        this.senderUID = DataManager.getInstance().getUser().getUid();
        this.receiverUID = uid;
        this.receiverName = name;
        this.chatID = createChatID(senderUID, receiverUID);

        // create a new chat
        this.chat = new Chat();
        ArrayList<String> users = new ArrayList<>();
        users.add(senderUID);
        users.add(receiverUID);
        chat.setUsers(users);
    }

    private String createChatID(String u1, String u2){
        return (u1.compareTo(u2) > 0) ? u2 + "_" + u1 : u1 + "_" + u2;
    }

    public void setParams(Chat c, String senderUID, String receiverUID){
        this.isNewChat = false;
        this.senderUID = senderUID;
        this.receiverUID = receiverUID;
        this.chatID = createChatID(senderUID, receiverUID);
        this.chat = c;
    }

    private void sendMessage(Message m) {
        LogManager.getInstance().printConsoleMessage("sendMessage");

        // get document reference.
        DocumentReference documentReference = FirebaseFirestore
                .getInstance()
                .collection(Constants.DB_COLL_MESSAGES)
                .document(chatID);

        chat.setLastMessage(Timestamp.now());

        // create the chat in the database and add the message in the collection.
        documentReference
                .set(chat)
                .addOnSuccessListener(aVoid -> documentReference.collection(Constants.DB_SUBCOLL_MESSAGES)
                .document()
                .set(m)
                .addOnSuccessListener(aVoid1 -> {
                    LogManager.getInstance().showVisualMessage(getContext(),"sendMessage:written");

                    if(isNewChat)
                        isNewChat = false;
                })
                .addOnFailureListener(e -> {
                    LogManager.getInstance().showVisualMessage(getContext(),"sendMessage:fail");
                }));
    }

    private void addOnClickListener(){
        mSendMessage.setOnClickListener(view -> {
            Message message = new Message();
            message.setChatID(chatID);
            message.setMessage(String.valueOf(editText.getText()));
            message.setReceiver(receiverUID);
            message.setSenderID(senderUID);
            message.setTimestamp(Timestamp.now());
            sendMessage(message);
        });
    }

    private void readMessages(){
        FirebaseFirestore.getInstance()
                .collection(Constants.DB_COLL_MESSAGES)
                .document(chatID)
                .collection(Constants.DB_SUBCOLL_MESSAGES)
                .addSnapshotListener((value, e) -> {
                    if(value != null)
                        for (DocumentChange doc : value.getDocumentChanges()) {
                            LogManager.getInstance().printConsoleMessage(doc.getDocument().getId());
                            if (doc.getType().equals(DocumentChange.Type.ADDED)) {
                                messages.add(doc.getDocument().toObject(Message.class));
                                mAdapter.notifyDataSetChanged();
                                mRecyclerView.scrollToPosition(mRecyclerView.getAdapter().getItemCount() - 1);
                            }
                        }
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
