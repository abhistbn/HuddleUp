package com.example.huddleup;

public class J_NotificationItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_NOTIFICATION = 1;
    public static final int TYPE_SUB_HEADER = 2; // Pastikan ini ada

    private int type;
    private String title;
    private String description;
    private String timeStamp; // Ini akan menjadi string format "HH:mm"
    private boolean isRead;
    private String eventDateString; // Untuk grouping tanggal event
    private long rawTimestamp; // Untuk timestamp asli (saat notif dibuat/event didaftar/ditambah)

    // Konstruktor utama baru
    public J_NotificationItem(int type, String title, String description, String timeStamp, boolean isRead, String eventDateString, long rawTimestamp) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.timeStamp = timeStamp;
        this.isRead = isRead;
        this.eventDateString = eventDateString;
        this.rawTimestamp = rawTimestamp;
    }

    // Konstruktor fallback (misal untuk notifikasi manual tanpa eventDateString/rawTimestamp awal)
    public J_NotificationItem(int type, String title, String description, String timeStamp, boolean isRead) {
        this(type, title, description, timeStamp, isRead, null, System.currentTimeMillis());
    }

    // --- Getters ---
    public int getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public String getEventDateString() {
        return eventDateString;
    }

    public long getRawTimestamp() {
        return rawTimestamp;
    }

    // --- Setters ---
    public void setType(int type) {
        this.type = type;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public void setEventDateString(String eventDateString) {
        this.eventDateString = eventDateString;
    }

    public void setRawTimestamp(long rawTimestamp) {
        this.rawTimestamp = rawTimestamp;
    }
}