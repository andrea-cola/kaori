package com.kaori.kaori.DBObjects;

import com.google.firebase.Timestamp;

public class Book{

    private String title;
    private String author;
    private String imageUrl;
    private Timestamp timestamp;
    private Boolean isSigned;
    private String exam;
    private String comment;
    private String course;
    private String professor;
    private String url;

    public Book(){}

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getExams() {
        return exam;
    }

    public void setExams(String exams) {
        this.exam = exams;
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

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTitle(){
        return title;
    }

    public String getAuthor(){
        return author;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getProfessor() {
        return professor;
    }

    public void setProfessor(String professor) {
        this.professor = professor;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
