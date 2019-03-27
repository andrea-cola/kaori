package com.kaori.kaori.Model;

public class Message {

    private String chatID;
    private String message;
    private MiniUser sender;
    private MiniUser receiver;
    private long timestamp;

    public Message() { }

    public String getChatID() {
        return chatID;
    }

    public void setChatID(String chatID) {
        this.chatID = chatID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public MiniUser getReceiver() {
        return receiver;
    }

    public void setReceiver(MiniUser receiver) {
        this.receiver = receiver;
    }

    public MiniUser getSender() {
        return sender;
    }

    public void setSender(MiniUser sender) {
        this.sender = sender;
    }
}
