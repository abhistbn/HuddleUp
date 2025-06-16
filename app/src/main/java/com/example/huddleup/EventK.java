package com.example.huddleup;

import java.util.HashMap;
import java.util.Map;

public class EventK {
    private String id;
    private String name;
    private String date;
    private String location;
    private String description;

    public EventK() {

    }

    public EventK(String id, String name, String date, String location, String description) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.location = location;
        this.description = description;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDate() { return date; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDate(String date) { this.date = date; }
    public void setLocation(String location) { this.location = location; }
    public void setDescription(String description) { this.description = description; }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("name", name);
        result.put("date", date);
        result.put("location", location);
        result.put("description", description);
        return result;
    }
}