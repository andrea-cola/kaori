package com.kaori.kaori.Model;

public class MiniUser {

    private String uid;
    private String name;
    private String thumbnail;

    public MiniUser(){ }

    public MiniUser(String uid, String name, String thumbnail){
        this.uid = uid;
        this.name = name;
        this.thumbnail = thumbnail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbail) {
        this.thumbnail = thumbail;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}
