package com.example.huddleup;

public class EventModel {
    private String Judul, Tanggal, Waktu, Lokasi, About;
    public EventModel(String Judul, String Tanggal, String Waktu, String Lokasi, String About) {
        this.Judul = Judul;
        this.Tanggal = Tanggal;
        this.Waktu = Waktu;
        this.Lokasi = Lokasi;
        this.About = About;
    }
    public String getJudul() {
        return Judul;
    }
    public void setJudul(String Judul) {
        this.Judul = Judul;
    }
    public String getTanggal() {
        return Tanggal;
    }
    public void setTanggal(String Tanggal) {
        this.Tanggal = Tanggal;
    }
    public String getWaktu() {
        return Waktu;
    }
    public void setWaktu(String Waktu) {
        this.Waktu = Waktu;
    }
    public String getLokasi() {
        return Lokasi;
    }
    public void setLokasi(String Lokasi) {
        this.Lokasi = Lokasi;
    }

    public String getAbout() {
        return About;
    }
    public void setAbout(String About) {
        this.About = About;
    }
}
