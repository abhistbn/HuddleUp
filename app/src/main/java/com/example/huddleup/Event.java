package com.example.huddleup;

public class Event {
    private String name;
    private String date;
    private String location;
    private String category;

    public Event(String name, String date, String location, String category) {
        this.name = name;
        this.date = date;
        this.location = location;
        this.category = category;
    }

    public String getName() { return name; }
    public String getDate() { return date; }
    public String getLocation() { return location; }
    public String getCategory() { return category; }

    public void setCategory(String category) { this.category = category; }
}
