// File: app/src/main/java/com/example/huddleup/CategoryCreationActivityK.java
package com.example.huddleup;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CategoryCreationActivityK extends AppCompatActivity { // Nama kelas diakhiri 'K'

    private EditText etCategoryName;
    private RecyclerView rvEventSelectionList;
    private Button btnSaveCategory;

    private DatabaseReference mDatabase;
    private List<EventK> allEvents = new ArrayList<>(); // Daftar semua EventK yang ada di Firebase
    private EventSelectionAdapterK eventSelectionAdapter; // Adapter untuk menampilkan dan memilih event

    private String categoryId = null; // Akan berisi ID jika dalam mode edit, null jika membuat baru
    private CategoryK currentCategory; // Objek CategoryK yang sedang diedit (jika ada)

    private static final String TAG = "CategoryCreationActivityK";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_creation); // Mengaitkan dengan layout XML

        mDatabase = FirebaseDatabase.getInstance().getReference(); // Inisialisasi Firebase Database

        // Mengambil referensi View dari layout
        etCategoryName = findViewById(R.id.etCategoryName);
        rvEventSelectionList = findViewById(R.id.rvEventSelectionList);
        btnSaveCategory = findViewById(R.id.btnSaveCategory);

        rvEventSelectionList.setLayoutManager(new LinearLayoutManager(this)); // Mengatur layout manager untuk RecyclerView

        // Mengecek apakah Activity ini dibuka dalam mode edit (ada ID kategori)
        if (getIntent().hasExtra("categoryId")) {
            categoryId = getIntent().getStringExtra("categoryId");
            loadCategoryData(categoryId); // Muat data kategori yang akan diedit
        }

        loadAllEvents(); // Muat semua event yang ada dari Firebase

        // Mengatur aksi klik untuk tombol "Save Category"
        btnSaveCategory.setOnClickListener(v -> saveCategory());
    }

    // Memuat data kategori jika dalam mode edit
    private void loadCategoryData(String id) {
        mDatabase.child("categories").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentCategory = dataSnapshot.getValue(CategoryK.class); // Deserialisasi ke CategoryK
                if (currentCategory != null) {
                    etCategoryName.setText(currentCategory.getName()); // Isi nama kategori
                    // Inisialisasi adapter dengan event yang sudah terpilih di kategori ini
                    Set<String> selectedIds = (currentCategory.getEvents() != null) ?
                            currentCategory.getEvents().keySet() : new HashSet<>();
                    eventSelectionAdapter = new EventSelectionAdapterK(allEvents, selectedIds); // Menggunakan EventSelectionAdapterK
                    rvEventSelectionList.setAdapter(eventSelectionAdapter);
                } else {
                    Toast.makeText(CategoryCreationActivityK.this, "Category not found.", Toast.LENGTH_SHORT).show();
                    finish(); // Tutup Activity jika kategori tidak ditemukan
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error loading category: " + databaseError.getMessage());
                Toast.makeText(CategoryCreationActivityK.this, "Failed to load category.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    // Memuat semua event dari Firebase Realtime Database
    private void loadAllEvents() {
        mDatabase.child("events").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allEvents.clear(); // Bersihkan daftar lama
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    EventK event = postSnapshot.getValue(EventK.class); // Deserialisasi ke EventK
                    if (event != null) {
                        allEvents.add(event); // Tambahkan ke daftar
                    }
                }
                // Jika adapter belum diinisialisasi (ini terjadi di mode buat baru atau sebelum loadCategoryData selesai)
                if (eventSelectionAdapter == null) {
                    eventSelectionAdapter = new EventSelectionAdapterK(allEvents, new HashSet<>()); // Inisialisasi dengan daftar kosong (tidak ada yang terpilih)
                    rvEventSelectionList.setAdapter(eventSelectionAdapter);
                } else {
                    // Jika adapter sudah ada (misal di mode edit dan data event diperbarui), update datanya
                    // Penting: Dapatkan seleksi yang sudah ada sebelum memperbarui data di adapter
                    Set<String> previouslySelectedIds = eventSelectionAdapter.getSelectedEventIds();
                    eventSelectionAdapter = new EventSelectionAdapterK(allEvents, previouslySelectedIds);
                    rvEventSelectionList.setAdapter(eventSelectionAdapter);
                }
                eventSelectionAdapter.notifyDataSetChanged(); // Beri tahu adapter data telah berubah
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error loading events: " + databaseError.getMessage());
                Toast.makeText(CategoryCreationActivityK.this, "Failed to load event list.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Menyimpan kategori ke Firebase (membuat baru atau mengedit yang sudah ada)
    private void saveCategory() {
        String categoryName = etCategoryName.getText().toString().trim();
        if (categoryName.isEmpty()) {
            Toast.makeText(this, "Category name cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        Set<String> selectedEvents = eventSelectionAdapter.getSelectedEventIds(); // Dapatkan event yang terpilih dari adapter

        if (categoryId == null) { // Jika categoryId null, berarti ini mode membuat kategori baru
            categoryId = mDatabase.child("categories").push().getKey(); // Buat ID unik baru
            currentCategory = new CategoryK(categoryId, categoryName); // Buat objek CategoryK baru
        } else { // Jika categoryId tidak null, berarti ini mode mengedit kategori
            if (currentCategory == null) { // Pastikan kategori yang akan diedit sudah dimuat
                Toast.makeText(this, "Error: Category not loaded. Please try again.", Toast.LENGTH_SHORT).show();
                return;
            }
            currentCategory.setName(categoryName); // Perbarui nama kategori
            // Hapus semua event lama dari kategori ini sebelum menambahkan yang baru terpilih
            if (currentCategory.getEvents() != null) {
                currentCategory.getEvents().clear();
            } else {
                currentCategory.setEvents(new HashMap<>()); // Pastikan map tidak null jika awalnya kosong
            }
        }

        // Tambahkan semua event yang terpilih ke objek kategori
        for (String eventId : selectedEvents) {
            currentCategory.addEvent(eventId);
        }

        // Simpan objek kategori ke Firebase
        if (categoryId != null) {
            mDatabase.child("categories").child(categoryId).setValue(currentCategory)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(CategoryCreationActivityK.this, "Category saved successfully!", Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK); // Beri tahu Activity pemanggil bahwa operasi berhasil
                        finish(); // Tutup Activity
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(CategoryCreationActivityK.this, "Failed to save category: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }
}