package com.itstep.coursework.db.models;

public class ShortenUrlModel {
    private String originalUrl;

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getInvalidFields() {
        if(originalUrl == null) return "originalUrl";
        return null;
    }
}
