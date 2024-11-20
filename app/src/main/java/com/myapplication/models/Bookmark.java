package com.myapplication.models;

public class Bookmark {
    private String title;
    private String subtitle;

    public Bookmark(String title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }
}