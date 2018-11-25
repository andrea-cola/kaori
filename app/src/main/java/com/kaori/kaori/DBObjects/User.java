package com.kaori.kaori.DBObjects;

import android.net.Uri;

import java.util.List;

public class User {

    private String email;
    private String name;
    private String surname;
    private String birthday;
    private String photosUrl;
    private String university;
    private String courseType;
    private List<String> exams;
    private String uid;

    public User(){ }

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

    public String getPhotosUrl() {
        return photosUrl;
    }

    public void setPhotosUrl(Uri photosUrl) {
        this.photosUrl = photosUrl.toString();
    }

    public List<String> getExams() {
        return exams;
    }

    public void setExams(List<String> exams) {
        this.exams = exams;
    }

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

    public String getCourseType() {
        return courseType;
    }

    public void setCourseType(String courseType) {
        this.courseType = courseType;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
