package com.example.huddleup;

import java.io.Serializable;
import java.util.UUID;

public class Event implements Serializable {
    private String id;
    private String title;
    private String date;
    private String location;
    private String imageUrl;
    private boolean isWishlisted;

    public Event() {
        this.id = UUID.randomUUID().toString();
    }

    public Event(String title, String date, String location, String imageUrl) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.date = date;
        this.location = location;
        this.imageUrl = imageUrl;
        this.isWishlisted = false;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isWishlisted() {
        return isWishlisted;
    }

    public void setWishlisted(boolean wishlisted) {
        isWishlisted = wishlisted;
    }
}