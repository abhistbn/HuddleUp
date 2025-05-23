package com.example.huddleup;

import java.io.Serializable;
import java.util.UUID;

public class Note implements Serializable {
    private String id;
    private String title;
    private String content;
    private String imageUri;

    public Note() {
        this.id = UUID.randomUUID().toString();
    }

    public Note(String title, String content, String imageUri) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.content = content;
        this.imageUri = imageUri;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
}
