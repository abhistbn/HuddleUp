// File: app/src/main/java/com/example/huddleup/CategoryK.java
package com.example.huddleup;

import java.util.HashMap;
import java.util.Map;

public class CategoryK { // Nama kelas diakhiri 'K'
    private String id;
    private String name;
    // Map untuk menyimpan event IDs yang terkait. Key: eventId, Value: true (untuk cek keberadaan cepat)
    private Map<String, Boolean> events;

    // Konstruktor kosong diperlukan oleh Firebase untuk deserialisasi data
    public CategoryK() {}

    public CategoryK(String id, String name) {
        this.id = id;
        this.name = name;
        this.events = new HashMap<>(); // Inisialisasi map saat objek dibuat
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public Map<String, Boolean> getEvents() { return events; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEvents(Map<String, Boolean> events) { this.events = events; }

    // Metode helper untuk menambahkan event ID ke map (digunakan saat memilih event untuk kategori)
    public void addEvent(String eventId) {
        if (this.events == null) {
            this.events = new HashMap<>();
        }
        this.events.put(eventId, true);
    }

    // Metode helper untuk menghapus event ID dari map (digunakan saat menghapus event dari kategori)
    public void removeEvent(String eventId) {
        if (this.events != null) {
            this.events.remove(eventId);
        }
    }
}