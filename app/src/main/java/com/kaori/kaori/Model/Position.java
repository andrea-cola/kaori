package com.kaori.kaori.Model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

public class Position {

    private String id; // id of the position, that is equal to the used id (uid).
    private MiniUser user;
    private GeoPoint geoPoint; // position of the user.
    private String description; // what the user is doing in that position.
    private Timestamp timestamp; // time when the position is published.

    public Position(MiniUser user, GeoPoint geoPoint, String description, Timestamp timestamp){
        this.user = user;
        this.geoPoint = geoPoint;
        this.timestamp = timestamp;
        this.description = description;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

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

    public String getId() {
        return id;
    }

    public void setId(String positionID) {
        this.id = positionID;
    }

    public String getActivity() {
        return description;
    }

    public void setActivity(String activity) {
        this.description = activity;
    }
}
