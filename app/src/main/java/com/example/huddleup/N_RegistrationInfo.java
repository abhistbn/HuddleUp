package com.example.huddleup;

public class N_RegistrationInfo {
    private String status;
    private long timestamp;

    public N_RegistrationInfo() {
    }

    public N_RegistrationInfo(String status, long timestamp) {
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}