package com.kaori.kaori.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Chat implements Serializable {

    private String chatID;
    private long lastMessageSent;
    private String lastMessageText;
    private String lastMessageUserID;
    private List<MiniUser> users;

    public Chat(){ }

    public long getLastMessageSent() {
        return lastMessageSent;
    }

    public void setLastMessageSent(long lastMessageSent) {
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

    public static String createChatID(String uid1, String uid2){
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

    public String getLastMessageText() {
        return lastMessageText;
    }

    public void setLastMessageText(String lastMessageText) {
        this.lastMessageText = lastMessageText;
    }

    public String getLastMessageUserID() {
        return lastMessageUserID;
    }

    public void setLastMessageUserID(String lastMessageUserID) {
        this.lastMessageUserID = lastMessageUserID;
    }
}
