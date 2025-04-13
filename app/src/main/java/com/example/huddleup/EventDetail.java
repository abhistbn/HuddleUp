package com.example.huddleup;

public class EventDetail {
    private String eventName;
    private int eventCount;

    // Constructor
    public EventDetail(String eventName, int eventCount) {
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
