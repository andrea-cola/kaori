package com.kaori.kaori.Model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

public class Position {

    private String positionID;
    private MiniUser user;
    private GeoPoint geoPoint;
    private String location;
    private Timestamp timestamp;

    public Position(){ }

    public Position(MiniUser user, GeoPoint geoPoint, String location, Timestamp timestamp){
        this.user = user;
        this.geoPoint = geoPoint;
        this.location = location;
        this.timestamp = timestamp;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getPositionID() {
        return positionID;
    }

    public void setPositionID(String positionID) {
        this.positionID = positionID;
    }
}
