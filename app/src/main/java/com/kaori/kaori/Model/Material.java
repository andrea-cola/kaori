package com.kaori.kaori.Model;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class Material {

    private MiniUser user;
    private String comment;
    private String course;
    private List<String> professors;
    private List<String> exams;
    private Timestamp timestamp;
    private String title;
    private String type;
    private String url;
    private Boolean isModified;

    public Material(){
        exams = new ArrayList<>();
        professors = new ArrayList<>();
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getType() {
        return type;
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

    public MiniUser getUser() {
        return user;
    }

    public void setUser(MiniUser user) {
        this.user = user;
    }

    public String getTitle(){
        return title;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setModified(Boolean modified) {
        isModified = modified;
    }

    public Boolean getModified() {
        return isModified;
    }

    public void addExam(String exam){
        exams.add(exam);
    }

    public void addProfessor(String professor){
        professors.add(professor);
    }

    public List<String> getProfessors() {
        return professors;
    }

    public void setProfessors(List<String> professors) {
        this.professors = professors;
    }

    public List<String> getExams() {
        return exams;
    }

    public void setExams(List<String> exams) {
        this.exams = exams;
    }
}
