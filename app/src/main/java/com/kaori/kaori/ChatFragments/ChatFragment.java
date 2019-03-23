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

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.kaori.kaori.Model.Chat;
import com.kaori.kaori.Model.Message;
import com.kaori.kaori.Model.MiniUser;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.DataManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class ChatFragment extends Fragment {

    private Chat chat;
    private MiniUser myUser, otherUser;
    private ImageButton mSendMessage;
    private EditText editText;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private List<Object> messages;
    private List<String> dates;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat_layout, container, false);
        editText = view.findViewById(R.id.feedbackEdittext);
        mSendMessage = view.findViewById(R.id.send_button);
        mRecyclerView = view.findViewById(R.id.chat_list);

        myUser = DataManager.getInstance().getMiniUser();

        messages = new LinkedList<>();
        dates = new LinkedList<>();
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new MyAdapter(messages);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.scrollToPosition(View.FOCUS_DOWN);
        ImageView userImage = view.findViewById(R.id.user_profile_image);
        TextView userName = view.findViewById(R.id.user_profile_name);

        userName.setText(otherUser.getName());

        DataManager.getInstance().loadImageIntoView(otherUser.getThumbnail(), userImage, getContext());

        downloadChat(Chat.createChatID(myUser.getUid(), otherUser.getUid()));

        addOnClickListener();

        return view;
    }

    public void downloadChat(String chatID){
        if(chat == null)
            FirebaseFirestore.getInstance()
                    .collection("chats")
                    .document(chatID)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                chat = document.toObject(Chat.class);
                            } else {
                                chat = new Chat();
                                chat.addUsers(myUser, otherUser);
                                chat.setChatID(chatID);
                            }
                            readMessages();
                        } else {
                            // TODO: tornare alle chat.
                        }
                    });
        else
            readMessages();
    }

    public void setParams(MiniUser receiverUser){
        otherUser = receiverUser;
    }

    private void sendMessage(Message message) {
        chat.setLastMessageSent(Timestamp.now().getSeconds());
        DataManager.getInstance().uploadMessage(chat, message);
    }

    private void addOnClickListener(){
        mSendMessage.setOnClickListener(view -> {
            if(editText.getText().length() > 0) {
                Message message = new Message();
                message.setChatID(chat.getChatID());
                message.setMessage(String.valueOf(editText.getText()));
                message.setReceiver(otherUser);
                message.setSender(myUser);
                message.setTimestamp(Timestamp.now().getSeconds());

                sendMessage(message);
                editText.setText("");
            }
        });
    }

    private void readMessages(){
        FirebaseFirestore.getInstance()
                .collection("chats")
                .document(chat.getChatID())
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, e) -> {
                    if(value != null)
                        for (DocumentChange doc : value.getDocumentChanges()) {
                            if (doc.getType().equals(DocumentChange.Type.ADDED)) {
                                Message m = doc.getDocument().toObject(Message.class);
                                Date date = new Date(m.getTimestamp()*1000L);
                                if(!dates.contains(Constants.dateFormat2.format(date))) {
                                    dates.add(Constants.dateFormat2.format(date));
                                    messages.add(Constants.dateFormat2.format(date));
                                }
                                messages.add(m);
                                mAdapter.notifyDataSetChanged();
                                mRecyclerView.scrollToPosition(mRecyclerView.getAdapter().getItemCount() - 1);
                            }
                        }
                });
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

        private final int MY_MESSAGE = 0;
        private final int OTHER_MESSAGE = 1;
        private final int DATE_HEADER = 2;
        private List<Object> mDataset;

        class MyViewHolder extends RecyclerView.ViewHolder {

            MyViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }

        class MyViewHolderMessage extends MyViewHolder {
            ImageView userImage;
            TextView content, timestamp;

            MyViewHolderMessage(View v) {
                super(v);
                content = v.findViewById(R.id.message_content);
                timestamp = v.findViewById(R.id.message_time);
                userImage = v.findViewById(R.id.user_profile_image);
            }
        }

        class MyViewHolderDate extends MyViewHolder {
            TextView date;

            MyViewHolderDate(View v) {
                super(v);
                date = v.findViewById(R.id.date);
            }
        }

        MyAdapter(List<Object> myDataset) {
            mDataset = myDataset;
        }

        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if(viewType == MY_MESSAGE)
                return new MyViewHolderMessage(LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_right, parent, false));
            else if(viewType == OTHER_MESSAGE)
                return new MyViewHolderMessage(LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_left, parent, false));
            else
                return new MyViewHolderDate(LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_date, parent, false));
        }

        @Override
        public void onBindViewHolder(MyAdapter.MyViewHolder holder, int position) {
            if(getItemViewType(position) == MY_MESSAGE) {
                Message m = (Message) mDataset.get(position);

                DataManager.getInstance().loadImageIntoView(myUser.getThumbnail(), ((MyViewHolderMessage) holder).userImage, getContext());

                ((MyViewHolderMessage) holder).content.setText(m.getMessage());
                ((MyViewHolderMessage) holder).timestamp.setText(convert(m.getTimestamp()));
            } else if (getItemViewType(position) == OTHER_MESSAGE) {
                Message m = (Message) mDataset.get(position);

                DataManager.getInstance().loadImageIntoView(otherUser.getThumbnail(), ((MyViewHolderMessage) holder).userImage, getContext());

                ((MyViewHolderMessage) holder).content.setText(m.getMessage());
                ((MyViewHolderMessage) holder).timestamp.setText(convert(m.getTimestamp()));
            } else
                ((MyViewHolderDate)holder).date.setText(mDataset.get(position).toString());
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }

        @Override
        public int getItemViewType(int position) {
            if(messages.get(position) instanceof Message) {
                if (((Message)messages.get(position)).getSender().getUid().equalsIgnoreCase(myUser.getUid()))
                    return MY_MESSAGE;
                return OTHER_MESSAGE;
            }
            return DATE_HEADER;
        }
    }

    private String convert(long timestamp){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        sdf.setTimeZone(Calendar.getInstance().getTimeZone());
        return sdf.format(new Date(timestamp * 1000));
    }

}
