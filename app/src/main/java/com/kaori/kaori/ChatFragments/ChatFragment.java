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
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kaori.kaori.KaoriChat;
import com.kaori.kaori.Model.Chat;
import com.kaori.kaori.Model.Message;
import com.kaori.kaori.Model.MiniUser;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.DataManager;
import com.kaori.kaori.Utils.LogManager;

import java.util.LinkedList;
import java.util.List;

public class ChatFragment extends Fragment {

    private Chat chat;
    private MiniUser myUser, otherUser;
    private ImageButton mSendMessage;
    private EditText editText;
    private RecyclerView mRecyclerView;
    private ImageView userImage;
    private TextView userName;
    private RecyclerView.Adapter mAdapter;
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
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new MyAdapter(messages);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.scrollToPosition(View.FOCUS_DOWN);
        userImage = view.findViewById(R.id.user_profile_image);
        userName = view.findViewById(R.id.user_profile_name);

        userName.setText(otherUser.getName());

        Glide.with(getContext())
                .load(otherUser.getThumbnail())
                .apply(DataManager.getInstance().getGetGlideRequestOptionsCircle())
                .into(userImage);

        ((KaoriChat)getActivity()).getSupportActionBar().hide();

        // add listener to the button that sends messages.
        addOnClickListener();

        // if the chat is new, we don't need to load messages.
        readMessages();

        return view;
    }

    public void newChatParams(MiniUser receiverUser){
        myUser = DataManager.getInstance().getMiniUser();
        chat = new Chat();
        chat.addUsers(myUser, receiverUser);
        otherUser = receiverUser;
    }

    public void setParams(Chat c){
        this.chat = c;

        myUser = DataManager.getInstance().getMiniUser();

        //set the other user.
        for(MiniUser u : c.getUsers())
            if(!u.getUid().equalsIgnoreCase(myUser.getUid()))
                otherUser = u;
    }

    private void sendMessage(Message m) {
        // get document reference.
        DocumentReference documentReference = FirebaseFirestore
                .getInstance()
                .collection(Constants.DB_COLL_MESSAGES)
                .document(chat.getChatID());

        chat.setLastMessageSent(Timestamp.now());

        // create the chat in the database and add the message in the collection.
        documentReference
                .set(chat)
                .addOnSuccessListener(aVoid -> documentReference.collection(Constants.DB_SUBCOLL_MESSAGES)
                .document()
                .set(m)
                .addOnSuccessListener(aVoid1 -> {
                    // TODO: da eliminare
                    LogManager.getInstance().showVisualMessage("Messaggio inviato");
                })
                .addOnFailureListener(e -> {
                    // TODO : eliminare il messaggio dalla coda.
                    LogManager.getInstance().showVisualMessage("Invio fallito. Riprovare.");
                }));
    }

    private void addOnClickListener(){
        mSendMessage.setOnClickListener(view -> {
            Message message = new Message();
            message.setChatID(chat.getChatID());
            message.setMessage(String.valueOf(editText.getText()));
            message.setReceiver(otherUser);
            message.setSender(myUser);
            message.setTimestamp(Timestamp.now());

            sendMessage(message);

            // reset the text field
            editText.setText("");
        });
    }

    private void readMessages(){
        FirebaseFirestore.getInstance()
                .collection(Constants.DB_COLL_MESSAGES)
                .document(chat.getChatID())
                .collection(Constants.DB_SUBCOLL_MESSAGES)
                .addSnapshotListener((value, e) -> {
                    if(value != null)
                        for (DocumentChange doc : value.getDocumentChanges()) {
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
            ImageView userImage;
            TextView content, timestamp;

            MyViewHolder(View v) {
                super(v);
                content = v.findViewById(R.id.message_content);
                timestamp = v.findViewById(R.id.message_time);
                userImage = v.findViewById(R.id.user_profile_image);
            }
        }

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
            String thumbnail;
            if(mDataset.get(position).getSender().getUid().equalsIgnoreCase(myUser.getUid()))
                thumbnail = mDataset.get(position).getSender().getThumbnail();
            else
                thumbnail = mDataset.get(position).getReceiver().getThumbnail();

            Glide.with(getContext())
                    .load(thumbnail)
                    .apply(DataManager.getInstance().getGetGlideRequestOptionsCircle())
                    .into(holder.userImage);

            holder.content.setText(mDataset.get(position).getMessage());
            holder.timestamp.setText(Constants.dateFormat.format(mDataset.get(position).getTimestamp().toDate()));
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }

        @Override
        public int getItemViewType(int position) {
            if(messages.get(position).getSender().getUid().equalsIgnoreCase(myUser.getUid()))
                return MY_MESSAGE;
            return OTHER_MESSAGE;
        }
    }

}
