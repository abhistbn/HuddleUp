package com.example.huddleup;

import com.google.firebase.database.Exclude;

public class N_EventModel {
    @Exclude
    private String key;

    private String judul, tanggal, waktu, lokasi, about;

    public N_EventModel() {
    }

    public N_EventModel(String Judul, String Tanggal, String Waktu, String Lokasi, String About) {
        this.judul = Judul;
        this.tanggal = Tanggal;
        this.waktu = Waktu;
        this.lokasi = Lokasi;
        this.about = About;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getJudul() {
        return judul;
    }

    public void setJudul(String judul) {
        this.judul = judul;
    }

    public String getTanggal() {
        return tanggal;
    }

    public void setTanggal(String tanggal) {
        this.tanggal = tanggal;
    }

    public String getWaktu() {
        return waktu;
    }

    public void setWaktu(String waktu) {
        this.waktu = waktu;
    }

    public String getLokasi() {
        return lokasi;
    }

    public void setLokasi(String lokasi) {
        this.lokasi = lokasi;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }
}