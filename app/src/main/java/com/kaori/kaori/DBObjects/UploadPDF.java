package com.kaori.kaori.DBObjects;

/**
 * This class provides the abstraction for the update implemented in UploadPDFDialog
 */
public class UploadPDF {

    public String fileName;
    public String url;

    public UploadPDF(){}

    public UploadPDF(String name, String url ){
        this.fileName = name;
        this.url = url;
    }

    public String getFileName() {
        return fileName;
    }

    public String getUrl() {
        return url;
    }
}
