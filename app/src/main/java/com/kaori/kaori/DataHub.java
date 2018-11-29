package com.kaori.kaori;

import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
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

    /**
     * Request options for Glide.
     */
    private RequestOptions glideRequestOptions;
    private RequestOptions getGlideRequestOptionsCircle;

    private DataHub(){
        isAuthenticated = false;
        user = new User();
        universities = new ArrayList<>();
        courseTypes = new ArrayList<>();
        exams = new ArrayList<>();
        glideRequestOptions = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.placeholder);

        getGlideRequestOptionsCircle = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .circleCrop()
                .error(R.drawable.placeholder);
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

    public RequestOptions getGlideRequestOptions() {
        return glideRequestOptions;
    }

    public RequestOptions getGetGlideRequestOptionsCircle() {
        return getGlideRequestOptionsCircle;
    }
}
