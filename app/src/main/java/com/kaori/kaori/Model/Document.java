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
    private Timestamp timestamp; // creation timestamp
    private String title; // title of the document
    private String url; // storage url of the document
    private Boolean modified; // state if the document has been modified by the owner
    private List<Feedback> feedbacks; // list of users' feedbacks
    private int type; //book or document
    private int subtype; // book || file or url
    private Boolean starred;

    //book fields
    private String author;
    private String editor;
    private float price;
    private String thumbnail; // TODO

    public Document(){
        id = "";
        exams = new ArrayList<>();
        starred = false;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getAuthor() {
        return author;
    }

    public String getEditor() {
        return editor;
    }

    public void setEditor(String editor) {
        this.editor = editor;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Boolean getStarred() {
        return starred;
    }

    public void setStarred(Boolean starred) {
        this.starred = starred;
    }

    public int getSubtype() {
        return subtype;
    }

    public void setSubtype(int subtype) {
        this.subtype = subtype;
    }
}
