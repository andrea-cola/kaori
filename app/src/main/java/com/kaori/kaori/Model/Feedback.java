package com.kaori.kaori.Model;

import com.google.firebase.Timestamp;

import java.io.Serializable;

public class Feedback implements Serializable {

    private MiniUser user;
    private Timestamp timestamp;
    private String text;

    public MiniUser getUser() {
        return user;
    }

    public void setUser(MiniUser user) {
        this.user = user;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
