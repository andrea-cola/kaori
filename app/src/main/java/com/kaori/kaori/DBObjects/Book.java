package com.kaori.kaori.DBObjects;

import java.io.Serializable;

public class Book{

    private String title;
    private String author;
    private String url;

    public Book(){}

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
