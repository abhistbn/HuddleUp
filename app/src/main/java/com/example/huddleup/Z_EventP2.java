package com.example.huddleup;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import com.google.firebase.database.Exclude;

public class Z_EventP2 implements Parcelable {
    // --- Variabel yang sudah ada ---
    private String id;
    private String name;
    private String imageUrl;
    private String date;
    private String time;
    private String location;
    private String about;

    // --- VARIABEL BARU UNTUK CATATAN PRIBADI ---
    private String personalNote;
    private String personalNoteImageUrl;

    @Exclude
    private Uri imageUri;

    public Z_EventP2() {

    }

    public Z_EventP2(String id, String name, String imageUrl, String date, String time, String location, String about) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.date = date;
        this.time = time;
        this.location = location;
        this.about = about;
    }


    protected Z_EventP2(Parcel in) {
        id = in.readString();
        name = in.readString();
        imageUrl = in.readString();
        date = in.readString();
        time = in.readString();
        location = in.readString();
        about = in.readString();
        // Membaca data baru dari Parcel
        personalNote = in.readString();
        personalNoteImageUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(imageUrl);
        dest.writeString(date);
        dest.writeString(time);
        dest.writeString(location);
        dest.writeString(about);

        dest.writeString(personalNote);
        dest.writeString(personalNoteImageUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Z_EventP2> CREATOR = new Creator<Z_EventP2>() {
        @Override
        public Z_EventP2 createFromParcel(Parcel in) {
            return new Z_EventP2(in);
        }

        @Override
        public Z_EventP2[] newArray(int size) {
            return new Z_EventP2[size];
        }
    };

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getAbout() { return about; }
    public void setAbout(String about) { this.about = about; }

    // --- GETTER & SETTER UNTUK CATATAN ---
    public String getPersonalNote() { return personalNote; }
    public void setPersonalNote(String personalNote) { this.personalNote = personalNote; }
    public String getPersonalNoteImageUrl() { return personalNoteImageUrl; }
    public void setPersonalNoteImageUrl(String personalNoteImageUrl) { this.personalNoteImageUrl = personalNoteImageUrl; }


    @Exclude
    public Uri getImageUri() { return imageUri; }
    @Exclude
    public void setImageUri(Uri imageUri) { this.imageUri = imageUri; }
}
