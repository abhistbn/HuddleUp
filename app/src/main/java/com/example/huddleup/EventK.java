// File: app/src/main/java/com/example/huddleup/EventK.java
package com.example.huddleup; // PASTIKAN INI SESUAI DENGAN NAMA PACKAGE ANDA

import java.util.HashMap;
import java.util.Map;

public class EventK {
    private String id;
    private String name;
    private String date;
    private String location;
    private String description;
    // PENTING: Tambahkan properti lain di sini jika ada di node /events Firebase Anda
    // Contoh: private String organizer;
    // Contoh: private long timestamp;
    // Contoh: private String imageUrl;

    public EventK() {
        // Konstruktor kosong WAJIB untuk deserialisasi Firebase
    }

    public EventK(String id, String name, String date, String location, String description) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.location = location;
        this.description = description;
    }

    // --- Getters (WAJIB untuk deserialisasi Firebase) ---
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDate() { return date; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }

    // --- Setters (Disarankan jika Anda ingin memodifikasi objek EventK setelah dibuat) ---
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDate(String date) { this.date = date; }
    public void setLocation(String location) { this.location = location; }
    public void setDescription(String description) { this.description = description; }

    // Metode toMap untuk menyimpan ke Firebase (berguna jika EventK juga digunakan untuk menulis ke DB)
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("name", name);
        result.put("date", date);
        result.put("location", location);
        result.put("description", description);
        // Tambahkan properti lain yang ada di kelas ini juga
        return result;
    }
}