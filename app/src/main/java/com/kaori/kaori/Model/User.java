package com.kaori.kaori.Model;

import com.kaori.kaori.Utils.Constants;

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
    private List<String> tokenIDs;
    private int authMethod;

    public User(){
        exams = new ArrayList<>();
        tokenIDs = new ArrayList<>();
        email = "";
        name = "";
        photosUrl = Constants.STORAGE_DEFAULT_PROFILE_IMAGE;
        university = "";
        course = "";
        uid = "";
        authMethod = Constants.NATIVE;
    }

    public User(String uid, String email, String name, String photosUrl, int authMethod){
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.photosUrl = photosUrl;
        this.authMethod = authMethod;
        this.tokenIDs = new ArrayList<>();
        this.exams = new ArrayList<>();
        this.university = "";
        this.course = "";
    }

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

    public List<String> getTokenIDs() {
        return tokenIDs;
    }

    public void setTokenIDs(List<String> tokenIDs) {
        this.tokenIDs = tokenIDs;
    }

    public void addTokenID(String tokenID) {
        tokenIDs.add(tokenID);
    }

    public int getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(int authMethod) {
        this.authMethod = authMethod;
    }
}
