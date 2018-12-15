package com.kaori.kaori.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {

    private String email;
    private String name;
    private String photosUrl;
    private String university;
    private String course;
    private String uid;
    private List<String> exams;

    public User(){ }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotosUrl() {
        return photosUrl;
    }

    public void setPhotosUrl(String photosUrl) {
        this.photosUrl = photosUrl;
    }

    public List<String> getExams() {
        if(exams ==  null)
            exams = new ArrayList<>();
        return exams;
    }

    public void setExams(List<String> exams) {
        this.exams = exams;
    }

    public String getUniversity() {
        return (university == null) ? "" : university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

    public String getCourse() {
        return (course == null) ? "" : course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
