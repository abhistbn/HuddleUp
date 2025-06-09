package com.example.huddleup;

public class NotificationItem {
    // Definisikan tipe item untuk header dan notifikasi
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_NOTIFICATION = 1;

    private int type;
    private String title;
    private String Description;
    private String time;
    private boolean isRead;

    public NotificationItem(int type, String title, String Description, String time, boolean isRead) {
        this.type = type;
        this.title = title;
        this.Description = Description;
        this.time = time;
        this.isRead = isRead;
    }

    // Getter untuk properti
    public int getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return Description;
    }

    public String getTime() {
        return time;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.Description = description;
    }

}
