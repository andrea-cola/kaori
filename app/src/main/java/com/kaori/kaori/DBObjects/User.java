package com.kaori.kaori.DBObjects;

import android.net.Uri;

public class User {

    private String email;
    private String name;
    private String surname;
    private String birthday;
    private Uri photosUrl;

    public User(){
    }

    public String getBirthday() {
        return birthday;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Uri getPhotosUrl() {
        return photosUrl;
    }

    public void setPhotosUrl(Uri photosUrl) {
        this.photosUrl = photosUrl;
    }
}
