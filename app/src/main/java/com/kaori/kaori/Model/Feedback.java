package com.kaori.kaori.Model;

import com.google.firebase.Timestamp;

import java.io.Serializable;

public class Feedback implements Serializable {

    private MiniUser user;
    private long timestamp;
    private String text;

    public MiniUser getUser() {
        return user;
    }

    public void setUser(MiniUser user) {
        this.user = user;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
