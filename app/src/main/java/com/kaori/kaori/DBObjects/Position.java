package com.kaori.kaori.DBObjects;

import com.google.firebase.firestore.GeoPoint;

public class Position {
    private GeoPoint point;
    private String locationName;
    private String uid;
    private String username;

    public Position(){}

    public String getLocationName() {
        return locationName;
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

    public void setLocationName(String locationName) {
        this.locationName = locationName;
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
