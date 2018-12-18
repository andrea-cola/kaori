package com.kaori.kaori.Model;

import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Chat implements Serializable {

    private String chatID;
    private Timestamp lastMessageSent;
    private List<MiniUser> users;

    public Chat(){ }

    public Timestamp getLastMessageSent() {
        return lastMessageSent;
    }

    public void setLastMessageSent(Timestamp lastMessageSent) {
        this.lastMessageSent = lastMessageSent;
    }

    public List<MiniUser> getUsers(){
        return users;
    }

    public void addUsers(MiniUser user1, MiniUser user2){
        users = new ArrayList<>();
        this.users.add(user1);
        this.users.add(user2);
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
        if(!users.get(0).getUid().equalsIgnoreCase(uid))
            return users.get(0);
        return users.get(1);
    }

    public void setUsers(List<MiniUser> users) {
        this.users = users;
    }
}
