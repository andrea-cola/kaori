package com.kaori.kaori.DBObjects;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

public class Position {

    private GeoPoint point;
    private String location;
    private String uid;
    private String username;
    private Timestamp timestamp;

    public Position(){}

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getLocation() {
        return location;
    }

    public GeoPoint getPoint() {
        return point;
    }

    public String getUid() {
        return uid;
    }

    public String getUsername() {
        return username;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setPoint(GeoPoint point) {
        this.point = point;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
