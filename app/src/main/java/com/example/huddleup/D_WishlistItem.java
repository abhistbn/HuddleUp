package com.example.huddleup;

import android.os.Parcel;
import android.os.Parcelable;
public class D_WishlistItem implements Parcelable{

    private String title;
    private String date;
    private String location;
    private int image; // Kita gunakan int dulu untuk menampung ID drawable

    // Constructor
    public D_WishlistItem(String title, String date, String location, int image) {
        this.title = title;
        this.date = date;
        this.location = location;
        this.image = image;
    }

    // Getter untuk semua variabel
    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }

    public int getImage() {
        return image;
    }


    // --- Kode Parcelable (dibuat otomatis atau ditulis manual) ---

    protected D_WishlistItem(Parcel in) {
        title = in.readString();
        date = in.readString();
        location = in.readString();
        image = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(date);
        dest.writeString(location);
        dest.writeInt(image);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<D_WishlistItem> CREATOR = new Creator<D_WishlistItem>() {
        @Override
        public D_WishlistItem createFromParcel(Parcel in) {
            return new D_WishlistItem(in);
        }

        @Override
        public D_WishlistItem[] newArray(int size) {
            return new D_WishlistItem[size];
        }
    };
}
