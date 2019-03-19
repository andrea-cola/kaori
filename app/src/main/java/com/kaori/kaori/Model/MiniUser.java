package com.kaori.kaori.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MiniUser implements Serializable  {

    private String uid;
    private List<String> tokenID;
    private String name;
    private String thumbnail;

    public MiniUser(){
        tokenID = new ArrayList<>();
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

    public List<String> getTokenID() {
        return tokenID;
    }

    public void setTokenID(List<String> tokenID) {
        this.tokenID = tokenID;
    }
}
