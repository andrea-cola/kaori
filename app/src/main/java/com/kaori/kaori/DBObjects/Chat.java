package com.kaori.kaori.DBObjects;

import com.google.firebase.Timestamp;

import java.util.List;

public class Chat {

    private Timestamp lastMessage;
    private List<String> users;

    public Timestamp getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Timestamp lastMessage) {
        this.lastMessage = lastMessage;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }
}
