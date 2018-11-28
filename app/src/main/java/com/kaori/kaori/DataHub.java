package com.kaori.kaori;

import android.widget.ImageView;

import com.kaori.kaori.DBObjects.User;

import java.util.ArrayList;

/**
 * Singleton class that represent a store
 * for the most important and common data.
 */
public class DataHub {

    private static DataHub dataHub;

    private boolean isAuthenticated;
    private User user;
    private ImageView profileImageView;
    private ArrayList<String> universities;
    private ArrayList<String> courseTypes;
    private ArrayList<String> exams;

    private DataHub(){
        isAuthenticated = false;
        user = new User();
        universities = new ArrayList<>();
        courseTypes = new ArrayList<>();
        exams = new ArrayList<>();
    }

    public static DataHub getInstance(){
        if(dataHub == null)
            dataHub = new DataHub();
        return dataHub;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        isAuthenticated = authenticated;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


    public ArrayList<String> getUniversities() {
        return universities;
    }

    public void setUniversities(ArrayList<String> universities) {
        this.universities = universities;
    }

    public ArrayList<String> getCourseTypes() {
        return courseTypes;
    }

    public void setCourseType(ArrayList<String> courseType) {
        this.courseTypes = courseType;
    }

    public ArrayList<String> getExams() {
        return exams;
    }

    public void setExams(ArrayList<String> exams) {
        this.exams = exams;
    }
}
