// Contoh NotificationItem.java (pastikan ada)
package com.example.huddleup;

import android.net.Uri;

public class NotificationItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_NOTIFICATION = 1;

    private int type;
    private String title;
    private String description;
    private String time;
    private boolean isRead;
    private Uri imageUri; // Tambahkan ini

    // Konstruktor untuk header
    public NotificationItem(int type, String title, String description, String time, boolean isRead, Uri imageUri) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.time = time;
        this.isRead = isRead;
        this.imageUri = imageUri;
    }

    // Getter
    public int getType() { return type; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getTime() { return time; }
    public boolean isRead() { return isRead; }
    public Uri getImageUri() { return imageUri; } // Getter untuk imageUri

    // Setter (untuk editing)
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setTime(String time) { this.time = time; }
    public void setRead(boolean read) { isRead = read; }
    public void setImageUri(Uri imageUri) { this.imageUri = imageUri; } // Setter untuk imageUri
}