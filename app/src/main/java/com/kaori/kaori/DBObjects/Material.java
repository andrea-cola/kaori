package com.kaori.kaori.DBObjects;

import com.google.firebase.Timestamp;

public class Material {

    private String author;
    private String authorUrl;
    private String comment;
    private String course;
    private String exam;
    private String professor;
    private Timestamp timestamp;
    private String title;
    private String type;
    private String url;

    public Material() {
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String couurse) {
        this.course = couurse;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getProfessor() {
        return professor;
    }

    public void setProfessor(String professor) {
        this.professor = professor;
    }

    public String getExam() {
        return exam;
    }

    public void setExam(String exam) {
        this.exam = exam;
    }

    public String getAuthorUrl() {
        return authorUrl;
    }

    public void setAuthorUrl(String authorUrl) {
        this.authorUrl = authorUrl;
    }
}
