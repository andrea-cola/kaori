package com.kaori.kaori.Model;

import java.io.Serializable;

public class MiniUser implements Serializable  {

    private String uid;
    private String name;
    private String thumbnail;

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
