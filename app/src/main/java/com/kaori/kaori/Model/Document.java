package com.kaori.kaori.Model;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class Document {

    private String id; // id of the document
    private MiniUser user; // owner user of the document
    private String note; // note of the owner user
    private String university;
    private String course;
    private List<String> exams;
    private List<String> professors;
    private Timestamp timestamp;
    private String title;
    private String type;
    private String url;
    private Boolean modified;
    private List<Feedback> feedbacks;

    public Document(){
        exams = new ArrayList<>();
        professors = new ArrayList<>();
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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
        this.modified = modified;
    }

    public Boolean getModified() {
        return modified;
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

    public List<Feedback> getFeedbacks() {
        if(feedbacks == null)
            feedbacks = new ArrayList<>();
        return feedbacks;
    }

    public void setFeedbacks(List<Feedback> feedbacks) {
        this.feedbacks = feedbacks;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

}
