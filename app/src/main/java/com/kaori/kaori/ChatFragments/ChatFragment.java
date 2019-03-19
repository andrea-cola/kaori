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
import com.kaori.kaori.Model.Chat;
import com.kaori.kaori.Model.Message;
import com.kaori.kaori.Model.MiniUser;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.DataManager;

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
        chat = c;
        myUser = DataManager.getInstance().getMiniUser();

        for(MiniUser u : c.getUsers())
            if(!u.getUid().equalsIgnoreCase(myUser.getUid()))
                otherUser = u;
    }

    private void sendMessage(Message m) {
        // get document reference.
        /*DocumentReference documentReference = FirebaseFirestore
                .getInstance()
                .collection(Constants.DB_COLL_MESSAGES)
                .document(chat.getChatID());

        chat.setLastMessageSent(Timestamp.now());

        documentReference
                .set(chat)
                .addOnSuccessListener(aVoid -> documentReference.collection(Constants.DB_SUBCOLL_MESSAGES)
                .document()
                .set(m)
                .addOnFailureListener(e -> {
                    LogManager.getInstance().showVisualMessage("Invio fallito. Riprovare.");
                }));*/
    }

    private void addOnClickListener(){
        mSendMessage.setOnClickListener(view -> {
            if(editText.getText().length() > 0) {
                Message message = new Message();
                message.setChatID(chat.getChatID());
                message.setMessage(String.valueOf(editText.getText()));
                message.setReceiver(otherUser);
                message.setSender(myUser);
                message.setTimestamp(Timestamp.now());

                sendMessage(message);
                editText.setText("");
            }
        });
    }

    private void readMessages(){
        /*FirebaseFirestore.getInstance()
                .collection(Constants.DB_COLL_MESSAGES)
                .document(chat.getChatID())
                .collection(Constants.DB_SUBCOLL_MESSAGES)
                .orderBy(Constants.FIELD_TIMESTAMP, Query.Direction.ASCENDING)
                .addSnapshotListener((value, e) -> {
                    if(value != null)
                        for (DocumentChange doc : value.getDocumentChanges()) {
                            if (doc.getType().equals(DocumentChange.Type.ADDED)) {
                                Message m = doc.getDocument().toObject(Message.class);
                                if(!dates.contains(Constants.dateFormat2.format(m.getTimestamp().toDate()))) {
                                    dates.add(Constants.dateFormat2.format(m.getTimestamp().toDate()));
                                    messages.add(Constants.dateFormat2.format(m.getTimestamp().toDate()));
                                }
                                messages.add(m);
                                mAdapter.notifyDataSetChanged();
                                mRecyclerView.scrollToPosition(mRecyclerView.getAdapter().getItemCount() - 1);
                            }
                        }
                });*/
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
            // create a new view
            View v;
            if(viewType == MY_MESSAGE)
                return new MyViewHolderMessage(LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_right, parent, false));
            else if(viewType == OTHER_MESSAGE)
                return new MyViewHolderMessage(LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_left, parent, false));
            else
                return new MyViewHolderDate(LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_date, parent, false));
        }

        @Override
        public void onBindViewHolder(MyAdapter.MyViewHolder holder, int position) {
            if(getItemViewType(position) == MY_MESSAGE || getItemViewType(position) == OTHER_MESSAGE) {
                Message m = (Message) mDataset.get(position);

                DataManager.getInstance().loadImageIntoView(otherUser.getThumbnail(), ((MyViewHolderMessage)holder).userImage, getContext());

                ((MyViewHolderMessage)holder).content.setText(m.getMessage());
                ((MyViewHolderMessage)holder).timestamp.setText(Constants.dateFormat3.format(m.getTimestamp().toDate()));
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

}
