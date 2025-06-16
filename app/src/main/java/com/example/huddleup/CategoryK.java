package com.example.huddleup;

import java.util.HashMap;
import java.util.Map;

public class CategoryK {
    private String id;
    private String name;
    private Map<String, Boolean> events;

    public CategoryK() {}

    public CategoryK(String id, String name) {
        this.id = id;
        this.name = name;
        this.events = new HashMap<>();
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public Map<String, Boolean> getEvents() { return events; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEvents(Map<String, Boolean> events) { this.events = events; }

    public void addEvent(String eventId) {
        if (this.events == null) {
            this.events = new HashMap<>();
        }
        this.events.put(eventId, true);
    }

    public void removeEvent(String eventId) {
        if (this.events != null) {
            this.events.remove(eventId);
        }
    }
}