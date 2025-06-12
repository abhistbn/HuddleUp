package com.example.huddleup;

public class NotificationItem {
    private String title;
    private String Description;
    private String time;

    public NotificationItem(String title, String Description, String time) {
        this.title = title;
        this.Description = Description;
        this.time = time;
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
}
