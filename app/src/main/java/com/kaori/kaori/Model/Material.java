package com.kaori.kaori.Model;

import com.google.firebase.Timestamp;

public class Material {

    private String author;
    private String authorUrl;
    private String comment;
    private String course;
    private String professor;
    private String exam;
    private Timestamp timestamp;
    private String title;
    private String type;
    private String url;

    public Material(){}

    public Material(String title, String author, String url, String type, Timestamp timestamp, String exam, String course, String professor, String comment, String authorUrl){

    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getExam() {
        return exam;
    }

    public String getType() {
        return type;
    }

    public void setExam(String exam) {
        this.exam = exam;
    }

    public void setType(String type) {
        this.type = type;
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

    public void setAuthorUrl(String authorUrl) {
        this.authorUrl = authorUrl;
    }

    public String getTitle(){
        return title;
    }

    public String getAuthor(){
        return author;
    }

    public String getAuthorUrl() {
        return authorUrl;
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
