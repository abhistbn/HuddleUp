package com.example.huddleup;

import com.google.firebase.database.Exclude;

public class N_EventModel {
    @Exclude
    private String key;

    private String name;
    private String date;
    private String time;
    private String location;
    private String about;
    private String imageUrl;
    private Long creationTimestamp; // <-- Dipastikan ada dan getter/setter

    public N_EventModel() {
        // Konstruktor default dibutuhkan Firebase
    }

    // Konstruktor saat event baru dibuat (dari admin atau form lain)
    public N_EventModel(String name, String date, String time, String location, String about, String imageUrl) {
        this.name = name;
        this.date = date;
        this.time = time;
        this.location = location;
        this.about = about;
        this.imageUrl = imageUrl;
        this.creationTimestamp = System.currentTimeMillis(); // Set timestamp saat event dibuat
    }

    // Constructor lengkap (jika perlu memuat dari Firebase dengan timestamp)
    public N_EventModel(String name, String date, String time, String location, String about, String imageUrl, Long creationTimestamp) {
        this.name = name;
        this.date = date;
        this.time = time;
        this.location = location;
        this.about = about;
        this.imageUrl = imageUrl;
        this.creationTimestamp = creationTimestamp;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(Long creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    @Exclude
    public String getJudul() {
        return name;
    }

    @Exclude
    public String getTanggal() {
        return date;
    }

    @Exclude
    public String getWaktu() {
        return time;
    }

    @Exclude
    public String getLokasi() {
        return location;
    }
}