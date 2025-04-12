package com.example.huddleup;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class WishlistActivity extends AppCompatActivity {

    private LinearLayout wishlistContainer;
    private TextView tvEmptyWishlist;
    private List<Event> wishlistedEvents;
    private SharedPreferences sharedPreferences;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        // Inisialisasi UI
        wishlistContainer = findViewById(R.id.wishlistContainer);
        tvEmptyWishlist = findViewById(R.id.tvEmptyWishlist);

        // Inisialisasi SharedPreferences dan Gson
        sharedPreferences = getSharedPreferences("WishlistPrefs", MODE_PRIVATE);
        gson = new Gson();

        // Load dan tampilkan event
        loadWishlistedEvents();
        displayWishlistedEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload dan tampilkan event jika ada perubahan
        loadWishlistedEvents();
        displayWishlistedEvents();
    }

    private void loadWishlistedEvents() {
        // Ambil data dari SharedPreferences
        String json = sharedPreferences.getString("wishlistedEvents", "");

        if (json.isEmpty()) {
            wishlistedEvents = new ArrayList<>();
        } else {
            Type type = new TypeToken<List<Event>>() {}.getType();
            wishlistedEvents = gson.fromJson(json, type);
        }
    }

    private void saveWishlistedEvents() {
        // Simpan data ke SharedPreferences
        String json = gson.toJson(wishlistedEvents);
        sharedPreferences.edit().putString("wishlistedEvents", json).apply();
    }

    private void displayWishlistedEvents() {
        // Bersihkan tampilan sebelumnya
        wishlistContainer.removeAllViews();

        if (wishlistedEvents.isEmpty()) {
            tvEmptyWishlist.setVisibility(View.VISIBLE);
        } else {
            tvEmptyWishlist.setVisibility(View.GONE);

            for (int i = 0; i < wishlistedEvents.size(); i++) {
                Event event = wishlistedEvents.get(i);
                addEventToWishlistView(event, i);
            }
        }
    }

    private void addEventToWishlistView(Event event, int position) {
        // Buat CardView baru untuk setiap event
        CardView cardView = new CardView(this);
        cardView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        cardView.setRadius(16);
        cardView.setCardElevation(6);
        cardView.setUseCompatPadding(true);

        // Buat Layout dalam CardView
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        // Tambahkan ImageView (placeholder)
        ImageView ivEventImage = new ImageView(this);
        ivEventImage.setImageResource(R.drawable.placeholder_event);
        ivEventImage.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                400
        ));
        layout.addView(ivEventImage);

        // Tambahkan TextView untuk judul event
        TextView tvEventTitle = new TextView(this);
        tvEventTitle.setText(event.getTitle());
        tvEventTitle.setTextSize(18);
        tvEventTitle.setPadding(0, 16, 0, 8);
        layout.addView(tvEventTitle);

        // Tambahkan TextView untuk tanggal event
        TextView tvEventDate = new TextView(this);
        tvEventDate.setText("Tanggal: " + event.getDate());
        layout.addView(tvEventDate);

        // Tambahkan TextView untuk lokasi event
        TextView tvEventLocation = new TextView(this);
        tvEventLocation.setText("Lokasi: " + event.getLocation());
        layout.addView(tvEventLocation);

        // Buat Button daftar
        Button btnRegister = new Button(this);
        btnRegister.setText("Daftar");
        btnRegister.setOnClickListener(v -> onRegisterClicked(event, position));
        layout.addView(btnRegister);

        // Buat tombol hapus dari wishlist
        ImageButton btnRemoveWishlist = new ImageButton(this);
        btnRemoveWishlist.setImageResource(android.R.drawable.ic_delete);
        btnRemoveWishlist.setBackground(null);
        btnRemoveWishlist.setOnClickListener(v -> onRemoveFromWishlistClicked(event, position));
        layout.addView(btnRemoveWishlist);

        // Tambahkan layout ke CardView
        cardView.addView(layout);

        // Tambahkan CardView ke wishlistContainer
        wishlistContainer.addView(cardView);
    }

    private void onRegisterClicked(Event event, int position) {
        // Hapus dari wishlist
        wishlistedEvents.remove(position);
        saveWishlistedEvents();
        displayWishlistedEvents();

        Toast.makeText(this, "Berhasil mendaftar ke event: " + event.getTitle(), Toast.LENGTH_SHORT).show();
    }

    private void onRemoveFromWishlistClicked(Event event, int position) {
        // Hapus event dari wishlist
        wishlistedEvents.remove(position);
        saveWishlistedEvents();
        displayWishlistedEvents();

        Toast.makeText(this, "Event dihapus dari wishlist", Toast.LENGTH_SHORT).show();
    }
}
