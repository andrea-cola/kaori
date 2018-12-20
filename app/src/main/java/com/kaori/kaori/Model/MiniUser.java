package com.kaori.kaori.Model;

import java.io.Serializable;

public class MiniUser implements Serializable  {

    private String uid;
    private String tokenID;
    private String name;
    private String thumbnail;

    public MiniUser(){ }

    public MiniUser(String uid, String name, String thumbnail, String tokenID){
        this.uid = uid;
        this.name = name;
        this.thumbnail = thumbnail;
        this.tokenID = tokenID;
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

    public String getTokenID() {
        return tokenID;
    }

    public void setTokenID(String tokenID) {
        this.tokenID = tokenID;
    }
}
