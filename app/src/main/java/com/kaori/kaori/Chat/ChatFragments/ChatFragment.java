package com.kaori.kaori.Chat.ChatFragments;

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
import android.widget.TextView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.kaori.kaori.Chat.KaoriChat;
import com.kaori.kaori.Constants;
import com.kaori.kaori.Model.Chat;
import com.kaori.kaori.Model.Message;
import com.kaori.kaori.Model.MiniUser;
import com.kaori.kaori.R;
import com.kaori.kaori.Services.DataManager;
import com.kaori.kaori.Services.LogManager;

import java.util.List;

public class ChatFragment extends Fragment {

    private Chat chat;
    private MiniUser myUser, otherUser;
    private EditText editText;
    private RecyclerView mRecyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat_layout, container, false);
        editText = view.findViewById(R.id.feedbackEditText);
        ((KaoriChat) getActivity()).getSupportActionBar().hide();

        myUser = DataManager.getInstance().getMiniUser();

        ((TextView) view.findViewById(R.id.user_profile_name)).setText(otherUser.getName());
        DataManager.getInstance().loadImageIntoView(otherUser.getThumbnail(),
                view.findViewById(R.id.user_profile_image), getContext());

        mRecyclerView = view.findViewById(R.id.chatList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.scrollToPosition(View.FOCUS_DOWN);

        if (chat == null) {
            DataManager.getInstance().addListener(Chat.createChatID(myUser.getUid(), otherUser.getUid()));
            downloadChat(Chat.createChatID(myUser.getUid(), otherUser.getUid()));
        } else
            readMessages();

        addOnClickListener(view);

        view.findViewById(R.id.back).setOnClickListener(
                v -> getActivity().getSupportFragmentManager().popBackStackImmediate());

        return view;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
        LogManager.getInstance().printConsoleMessage(new Gson().toJson(chat));
        for (MiniUser u : chat.getUsers())
            if (!u.getUid().equalsIgnoreCase(DataManager.getInstance().getMiniUser().getUid()))
                this.otherUser = u;
    }

    public void setParams(MiniUser receiverUser) {
        this.otherUser = receiverUser;
    }

    private void downloadChat(String chatID) {
        FirebaseFirestore.getInstance().collection("chats")
                .document(chatID).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult()
                            .exists()) {
                        chat = task.getResult().toObject(Chat.class);
                    } else {
                        chat = new Chat();
                        chat.addUsers(myUser, otherUser);
                        chat.setChatID(chatID);
                    }
                    readMessages();
                });
    }

    private void sendMessage(Message message) {
        chat.setLastMessageSent(Timestamp.now().getSeconds());
        chat.setLastMessageText(message.getMessage());
        chat.setLastMessageUserID(myUser.getUid());
        DataManager.getInstance()
                .uploadMessage(chat, message, myUser.getName(), myUser.getThumbnail());
    }

    private void addOnClickListener(View view) {
        view.findViewById(R.id.sendButton).setOnClickListener(v -> {
            if (editText.getText().length() > 0) {
                Message message = new Message();
                message.setChatID(chat.getChatID());
                message.setMessage(String.valueOf(editText.getText()));
                message.setReceiver(otherUser.getUid());
                message.setSender(myUser.getUid());
                message.setTimestamp(Timestamp.now().getSeconds());

                sendMessage(message);
                editText.setText("");
            }
        });
    }

    private void readMessages() {
        RecyclerView.Adapter mAdapter = new MyAdapter(
                DataManager.getInstance().getMessages(chat.getChatID()));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.scrollToPosition(mRecyclerView.getAdapter().getItemCount() - 1);
        DataManager.getInstance().addAdapter(mRecyclerView, chat.getChatID());
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
            TextView content, timestamp;

            MyViewHolderMessage(View v) {
                super(v);
                content = v.findViewById(R.id.message_content);
                timestamp = v.findViewById(R.id.message_time);
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
        public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == MY_MESSAGE)
                return new MyViewHolderMessage(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_message_right, parent, false));
            else if (viewType == OTHER_MESSAGE)
                return new MyViewHolderMessage(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_message_left, parent, false));
            else
                return new MyViewHolderDate(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_message_date, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MyAdapter.MyViewHolder holder, int position) {
            if (getItemViewType(position) == MY_MESSAGE) {
                Message m = (Message) mDataset.get(position);
                ((MyViewHolderMessage) holder).content.setText(m.getMessage());
                ((MyViewHolderMessage) holder).timestamp
                        .setText(Constants.getHour(m.getTimestamp()));
            } else if (getItemViewType(position) == OTHER_MESSAGE) {
                Message m = (Message) mDataset.get(position);
                ((MyViewHolderMessage) holder).content.setText(m.getMessage());
                ((MyViewHolderMessage) holder).timestamp
                        .setText(Constants.getHour(m.getTimestamp()));
            } else
                ((MyViewHolderDate) holder).date.setText(mDataset.get(position).toString());
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (mDataset.get(position) instanceof Message) {
                if (((Message) mDataset.get(position)).getSender()
                        .equalsIgnoreCase(myUser.getUid()))
                    return MY_MESSAGE;
                return OTHER_MESSAGE;
            }
            return DATE_HEADER;
        }
    }

}
