package com.kaori.kaori.Model;

public class Professor {
    private String name;
    private String exam;
    private String university;

    public Professor(){};

    public void setExam(String exam) {
        this.exam = exam;
    }

    public String getExam() {
        return exam;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

    public String getUniversity() {
        return university;
    }
}
