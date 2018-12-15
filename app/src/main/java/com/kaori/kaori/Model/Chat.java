package com.kaori.kaori.Model;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class Chat {

    private String chatID;
    private Timestamp lastMessage;
    private List<MiniUser> users;

    public Timestamp getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Timestamp lastMessage) {
        this.lastMessage = lastMessage;
    }

    public List<MiniUser> getUsers() {
        return users;
    }

    public void setUsers(List<MiniUser> users) {
        this.users = users;
    }

    public void addUsers(MiniUser user1, MiniUser user2){
        users = new ArrayList<>();
        users.add(user1);
        users.add(user2);
        chatID = createChatID();
    }

    private String createChatID(){
        String uid1, uid2;
        uid1 = users.get(0).getUid();
        uid2 = users.get(1).getUid();
        return (uid1.compareTo(uid2) > 0) ? uid2 + "_" + uid1 : uid1 + "_" + uid2;
    }

    public String getChatID() {
        return chatID;
    }

    public void setChatID(String chatID) {
        this.chatID = chatID;
    }

    public MiniUser getTheOtherUserByUid(String uid){
        for(MiniUser u : users)
            if(!u.getUid().equalsIgnoreCase(uid))
                return u;
        return null;
    }
}
