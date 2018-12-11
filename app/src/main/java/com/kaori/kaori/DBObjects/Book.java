package com.kaori.kaori.DBObjects;

import com.google.firebase.Timestamp;
import java.util.ArrayList;

public class Book{

    private String title;
    private String author;
    private String url;
    private Timestamp timestamp;
    private Boolean isSigned;
    private ArrayList<String> courses;
    private String comment;

    public Book(){}

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public ArrayList<String> getCourses() {
        return courses;
    }

    public void setCourses(ArrayList<String> courses) {
        this.courses = courses;
    }

    public void setSigned(Boolean signed) {
        isSigned = signed;
    }

    public Boolean getSigned() {
        return isSigned;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setAuthor(String author){
        this.author = author;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle(){
        return title;
    }

    public String getAuthor(){
        return author;
    }

    public String getUrl() {
        return url;
    }
}
