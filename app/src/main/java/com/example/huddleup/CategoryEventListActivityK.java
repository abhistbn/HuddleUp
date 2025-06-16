// File: app/src/main/java/com/example/huddleup/CategoryEventListActivityK.java
package com.example.huddleup;

import      android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CategoryEventListActivityK extends AppCompatActivity { // Nama kelas diakhiri 'K'

    private TextView tvCategoryTitle;
    private RecyclerView rvCategoryEvents;
    private Button btnEditCategoryFromDetail; // Tombol untuk mengedit kategori
    private Button btnViewAllEvents; // Tombol untuk melihat semua event (optional)
    private FloatingActionButton fabAddEventToCategory; // FAB untuk menambah event ke kategori ini

    private DatabaseReference mDatabase; // Referensi ke Firebase Database
    private String categoryId; // ID kategori yang sedang ditampilkan
    private String categoryName; // Nama kategori yang sedang ditampilkan
    private List<EventK> categoryEvents = new ArrayList<>(); // Daftar EventK di kategori ini
    private CategoryEventAdapterK categoryEventAdapter; // Adapter untuk daftar event di kategori

    private static final String TAG = "CatEventListActivityK";

    // Launcher untuk memulai Activity lain dan menerima hasilnya (misal dari CategoryCreationActivityK)
    private final ActivityResultLauncher<Intent> editCategoryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Muat ulang data kategori dan event-nya setelah kembali dari pengeditan
                if (result.getResultCode() == RESULT_OK) {
                    loadCategoryDetailsAndEvents();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_event_list); // Mengaitkan dengan layout XML

        mDatabase = FirebaseDatabase.getInstance().getReference(); // Inisialisasi Firebase Database

        // Mengambil referensi View dari layout
        tvCategoryTitle = findViewById(R.id.tvCategoryTitle);
        rvCategoryEvents = findViewById(R.id.rvCategoryEvents);
        btnEditCategoryFromDetail = findViewById(R.id.btnEditCategoryFromDetail);
        btnViewAllEvents = findViewById(R.id.btnViewAllEvents);
        fabAddEventToCategory = findViewById(R.id.fabAddEventToCategory);

        rvCategoryEvents.setLayoutManager(new LinearLayoutManager(this)); // Mengatur layout manager

        // Menerima data ID dan nama kategori dari Intent
        categoryId = getIntent().getStringExtra("categoryId");
        categoryName = getIntent().getStringExtra("categoryName");

        if (categoryId == null || categoryName == null) {
            Toast.makeText(this, "Error: Category data missing.", Toast.LENGTH_SHORT).show();
            finish(); // Tutup Activity jika data kategori tidak ada
            return;
        }

        tvCategoryTitle.setText("Events in " + categoryName); // Set judul halaman

        // Inisialisasi adapter dengan listener untuk menghapus event dari kategori
        categoryEventAdapter = new CategoryEventAdapterK(categoryEvents, position -> { // Menggunakan CategoryEventAdapterK
            EventK eventToRemove = categoryEvents.get(position); // Dapatkan event yang akan dihapus
            confirmRemoveEventFromCategory(eventToRemove); // Konfirmasi penghapusan
        });
        rvCategoryEvents.setAdapter(categoryEventAdapter); // Menghubungkan adapter

        loadCategoryDetailsAndEvents(); // Muat detail kategori dan event-nya

        // Mengatur aksi klik tombol "Edit Category"
        btnEditCategoryFromDetail.setOnClickListener(v -> {
            Intent intent = new Intent(CategoryEventListActivityK.this, CategoryCreationActivityK.class); // Buka CategoryCreationActivityK
            intent.putExtra("categoryId", categoryId); // Kirim ID kategori untuk mode edit
            editCategoryLauncher.launch(intent); // Memulai Activity dan menunggu hasilnya
        });

        // Mengatur aksi klik tombol "View All Events" (opsional)
//        btnViewAllEvents.setOnClickListener(v -> {
//            Intent intent = new Intent(CategoryEventListActivityK.this); // Buka EventDashboardActivityK
//            startActivity(intent);
//        });

//        btnViewAllEvents.setOnClickListener(v -> {
//            Intent intent = new Intent(CategoryEventListActivityK.this, EventDashboardActivityK.class); // Buka EventDashboardActivityK
//            startActivity(intent);
//        });

        // Mengatur aksi klik FAB "Add Event To Category" (sama dengan "Edit Category")
        fabAddEventToCategory.setOnClickListener(v -> {
            Intent intent = new Intent(CategoryEventListActivityK.this, CategoryCreationActivityK.class); // Buka CategoryCreationActivityK
            intent.putExtra("categoryId", categoryId); // Kirim ID kategori agar bisa diedit (dan menambah event)
            editCategoryLauncher.launch(intent); // Memulai Activity dan menunggu hasilnya
        });
    }

    // Memuat detail kategori dan semua event yang terkait dengannya dari Firebase
    private void loadCategoryDetailsAndEvents() {
        mDatabase.child("categories").child(categoryId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                CategoryK category = dataSnapshot.getValue(CategoryK.class); // Deserialisasi ke CategoryK
                if (category != null && category.getEvents() != null) {
                    categoryEvents.clear(); // Bersihkan daftar lama
                    List<String> eventIdsInThisCategory = new ArrayList<>(category.getEvents().keySet()); // Ambil semua ID event dari kategori

                    if (eventIdsInThisCategory.isEmpty()) {
                        Toast.makeText(CategoryEventListActivityK.this, "No events in this category.", Toast.LENGTH_SHORT).show();
                        categoryEventAdapter.notifyDataSetChanged(); // Perbarui adapter (kosongkan tampilan)
                        return;
                    }

                    // Ambil detail setiap event dari node 'events'
                    for (String eventId : eventIdsInThisCategory) {
                        mDatabase.child("events").child(eventId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot eventSnapshot) {
                                EventK event = eventSnapshot.getValue(EventK.class); // Deserialisasi ke EventK
                                if (event != null) {
                                    // Hanya tambahkan jika event belum ada di daftar (untuk menghindari duplikasi saat ada update)
                                    boolean found = false;
                                    for (EventK existingEvent : categoryEvents) {
                                        if (existingEvent.getId().equals(event.getId())) {
                                            found = true;
                                            break;
                                        }
                                    }
                                    if (!found) {
                                        categoryEvents.add(event);
                                    }
                                    categoryEventAdapter.notifyDataSetChanged(); // Perbarui RecyclerView
                                } else {
                                    // Jika event tidak ditemukan di node 'events' (mungkin sudah dihapus),
                                    // hapus juga relasinya dari kategori ini.
                                    removeBrokenEventFromCategory(categoryId, eventId);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.e(TAG, "Failed to load event " + eventId + ": " + databaseError.getMessage());
                            }
                        });
                    }
                } else {
                    categoryEvents.clear();
                    categoryEventAdapter.notifyDataSetChanged();
                    Toast.makeText(CategoryEventListActivityK.this, "No events in this category.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to load category events: " + databaseError.getMessage());
                Toast.makeText(CategoryEventListActivityK.this, "Failed to load category events.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Konfirmasi penghapusan event dari kategori
    private void confirmRemoveEventFromCategory(EventK event) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Event from Category")
                .setMessage("Are you sure you want to remove '" + event.getName() + "' from this category? This will NOT delete the event from your overall event list.")
                .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        removeEventFromCategory(event.getId());
                    }
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // Melakukan penghapusan event dari relasi kategori di Firebase
    private void removeEventFromCategory(String eventId) {
        mDatabase.child("categories").child(categoryId).child("events").child(eventId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CategoryEventListActivityK.this, "Event removed from category.", Toast.LENGTH_SHORT).show();
                    loadCategoryDetailsAndEvents(); // Muat ulang daftar setelah penghapusan
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CategoryEventListActivityK.this, "Failed to remove event from category: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Fungsi helper untuk membersihkan relasi event yang sudah tidak ada di node 'events' utama
    private void removeBrokenEventFromCategory(String categoryId, String eventId) {
        mDatabase.child("categories").child(categoryId).child("events").child(eventId).removeValue()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Removed broken event ID " + eventId + " from category " + categoryId))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to remove broken event ID " + eventId + ": " + e.getMessage()));
    }
}