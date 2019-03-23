package com.kaori.kaori.Model;

import com.google.firebase.firestore.GeoPoint;

public class Position {

    private String id; // id of the position, that is equal to the used id (uid).
    private MiniUser user;
    private GeoPoint geoPoint; // position of the user.
    private String description; // what the user is doing in that position.
    private long timestamp; // time when the position is published.
    private String placeName;

    public Position(MiniUser user, GeoPoint geoPoint, String description, long timestamp, String placeName){
        this.user = user;
        this.geoPoint = geoPoint;
        this.timestamp = timestamp;
        this.description = description;
        this.placeName = placeName;
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
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

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }
}
