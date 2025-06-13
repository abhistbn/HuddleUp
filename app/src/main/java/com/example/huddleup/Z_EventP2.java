package com.example.huddleup;

import com.google.firebase.database.Exclude;
import android.net.Uri;

public class Z_EventP2 {
    private String id;
    private String name;
    private String imageUrl;
    private String date;
    private String time;
    private String location;
    private String about;

    @Exclude
    private Uri imageUri;

    public Z_EventP2() {
    }

    public Z_EventP2(String id, String name, String imageUrl, String date, String time, String location, String about) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.date = date;
        this.time = time;
        this.location = location;
        this.about = about;
    }


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getAbout() { return about; }
    public void setAbout(String about) { this.about = about; }

    @Exclude
    public Uri getImageUri() { return imageUri; }
    @Exclude
    public void setImageUri(Uri imageUri) { this.imageUri = imageUri; }
}