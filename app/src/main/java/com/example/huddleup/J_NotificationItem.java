package com.example.huddleup;

public class J_NotificationItem {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_NOTIFICATION = 1;

    private int type;
    private String title;
    private String description;
    private String time; // HH:mm
    private boolean isRead;
    private String eventDateString; // Tanggal event, untuk pengelompokan
    private long rawTimestamp; // Timestamp mentah untuk sorting
    private String imageUrl;
    private String key; // <-- TAMBAHKAN INI UNTUK MENYIMPAN KEY FIREBASE

    // Constructor kosong (NO-ARGUMENT CONSTRUCTOR) <-- INI SANGAT PENTING!
    public J_NotificationItem() {
        // Konstruktor default yang dibutuhkan Firebase
    }

    // Konstruktor untuk Header
    public J_NotificationItem(int type, String title, String description, String time, boolean isRead) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.time = time;
        this.isRead = isRead;
        this.eventDateString = null;
        this.rawTimestamp = 0;
        this.imageUrl = null;
    }

    // Konstruktor untuk Notifikasi (tanpa gambar)
    public J_NotificationItem(int type, String title, String description, String time, boolean isRead, String eventDateString, long rawTimestamp) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.time = time;
        this.isRead = isRead;
        this.eventDateString = eventDateString;
        this.rawTimestamp = rawTimestamp;
        this.imageUrl = null;
    }

    // Konstruktor LENGKAP untuk Notifikasi (dengan gambar)
    public J_NotificationItem(int type, String title, String description, String time, boolean isRead, String eventDateString, long rawTimestamp, String imageUrl) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.time = time;
        this.isRead = isRead;
        this.eventDateString = eventDateString;
        this.rawTimestamp = rawTimestamp;
        this.imageUrl = imageUrl;
    }

    // --- Getters ---
    public int getType() { return type; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getTime() { return time; }
    public boolean isRead() { return isRead; }
    public String getEventDateString() { return eventDateString; }
    public long getRawTimestamp() { return rawTimestamp; }
    public String getImageUrl() { return imageUrl; }
    public String getKey() { return key; } // <-- GETTER UNTUK KEY

    // --- Setters ---
    public void setType(int type) { this.type = type; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setTime(String time) { this.time = time; }
    public void setRead(boolean read) { isRead = read; }
    public void setEventDateString(String eventDateString) { this.eventDateString = eventDateString; }
    public void setRawTimestamp(long rawTimestamp) { this.rawTimestamp = rawTimestamp; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setKey(String key) { this.key = key; } // <-- SETTER UNTUK KEY
}