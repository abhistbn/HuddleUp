package com.example.huddleup;

public class Event {
    private String eventName;
    private int eventCount;

    // Constructor
    public Event(String eventName, int eventCount) {
        this.eventName = eventName;
        this.eventCount = eventCount;
    }

    // Getter dan Setter
    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public int getEventCount() {
        return eventCount;
    }

    public void setEventCount(int eventCount) {
        this.eventCount = eventCount;
    }
}
