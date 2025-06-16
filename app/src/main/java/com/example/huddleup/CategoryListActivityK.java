package com.example.huddleup;

import android.content.DialogInterface;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CategoryListActivityK extends AppCompatActivity implements CategoryAdapterK.OnCategoryActionListener { // Nama kelas diakhiri 'K'

    private RecyclerView rvCategoryList;
    private CategoryAdapterK categoryAdapter; // Adapter untuk RecyclerView kategori
    private List<CategoryK> categories = new ArrayList<>(); // Daftar objek CategoryK
    private DatabaseReference mDatabase; // Referensi ke Firebase Database

    private TextView tvEmptyCategory; // TextView untuk pesan "Tidak ada kategori"
    private Button btnSelectDeleteMode; // Tombol "Pilih untuk Hapus"
    private Button btnDeleteSelected; // Tombol "Hapus Terpilih" (muncul saat seleksi)
    private Button btnCancelSelection; // Tombol "Batal" (muncul saat seleksi)
    private TextView tvSelectionCount; // Menampilkan jumlah kategori yang terpilih

    private Set<String> selectedCategoryIds = new HashSet<>(); // Menyimpan ID kategori yang dipilih saat multi-seleksi

    private static final String TAG = "CategoryListActivityK";

    // Launcher untuk memulai Activity lain dan menerima hasilnya
    private final ActivityResultLauncher<Intent> categoryCreationLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Setelah CategoryCreationActivityK selesai (baik create/edit), muat ulang daftar kategori
                if (result.getResultCode() == RESULT_OK) {
                    loadCategoriesFromFirebase();
                    exitSelectionMode(); // Otomatis keluar dari mode seleksi setelah sukses
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_list); // Mengaitkan dengan layout XML

        mDatabase = FirebaseDatabase.getInstance().getReference(); // Inisialisasi Firebase Database

        // Mengambil referensi View dari layout
        rvCategoryList = findViewById(R.id.rvCategoryList);
        tvEmptyCategory = findViewById(R.id.tvEmptyCategory);
        btnSelectDeleteMode = findViewById(R.id.btnSelectDeleteMode);
        btnDeleteSelected = findViewById(R.id.btnDeleteSelected);
        btnCancelSelection = findViewById(R.id.btnCancelSelection);
        tvSelectionCount = findViewById(R.id.tvSelectionCount);
        TextView tvCategoryListTitle = findViewById(R.id.tvCategoryListTitle);

        rvCategoryList.setLayoutManager(new LinearLayoutManager(this)); // Mengatur layout manager untuk RecyclerView

        categoryAdapter = new CategoryAdapterK(categories, this); // Inisialisasi adapter dengan daftar kategori dan listener
        rvCategoryList.setAdapter(categoryAdapter); // Menghubungkan adapter ke RecyclerView

        // Mengatur aksi klik untuk Floating Action Button (FAB)
        FloatingActionButton fabAddCategory = findViewById(R.id.fabAddCategory);
        fabAddCategory.setOnClickListener(v -> {
            Intent intent = new Intent(CategoryListActivityK.this, CategoryCreationActivityK.class); // Intent untuk membuka CategoryCreationActivityK
            categoryCreationLauncher.launch(intent); // Memulai Activity dan menunggu hasilnya
        });

        // Mengatur aksi klik untuk tombol-tombol mode seleksi
        btnSelectDeleteMode.setOnClickListener(v -> enterSelectionMode());
        btnDeleteSelected.setOnClickListener(v -> confirmAndDeleteSelectedCategories());
        btnCancelSelection.setOnClickListener(v -> exitSelectionMode());

        loadCategoriesFromFirebase(); // Memuat daftar kategori dari Firebase saat Activity dibuat
    }

    // Memuat daftar kategori dari Firebase Realtime Database
    private void loadCategoriesFromFirebase() {
        mDatabase.child("categories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                categories.clear(); // Bersihkan daftar lama
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    CategoryK category = postSnapshot.getValue(CategoryK.class); // Deserialisasi data ke objek CategoryK
                    if (category != null) {
                        categories.add(category); // Tambahkan ke daftar
                    }
                }
                categoryAdapter.notifyDataSetChanged(); // Beri tahu adapter bahwa data telah berubah

                // Logika untuk menampilkan/menyembunyikan pesan "Tidak ada kategori" dan tombol "Pilih untuk Hapus"
                if (categories.isEmpty()) {
                    tvEmptyCategory.setVisibility(View.VISIBLE);
                    btnSelectDeleteMode.setVisibility(View.GONE);
                } else {
                    tvEmptyCategory.setVisibility(View.GONE);
                    if (!categoryAdapter.isInSelectionMode()) { // Tampilkan tombol pilih jika tidak dalam mode seleksi
                        btnSelectDeleteMode.setVisibility(View.VISIBLE);
                    }
                }

                // Perbarui jumlah seleksi jika sedang dalam mode seleksi
                if (categoryAdapter.isInSelectionMode()) {
                    updateSelectionCount();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadCategories:onCancelled", databaseError.toException());
                Toast.makeText(CategoryListActivityK.this, "Failed to load categories.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Masuk ke mode multi-seleksi
    private void enterSelectionMode() {
        selectedCategoryIds.clear(); // Bersihkan seleksi sebelumnya
        categoryAdapter.setSelectionMode(true, selectedCategoryIds); // Beri tahu adapter untuk masuk mode seleksi
        updateSelectionUI(true); // Perbarui UI sesuai mode seleksi
    }

    // Keluar dari mode multi-seleksi
    private void exitSelectionMode() {
        selectedCategoryIds.clear(); // Bersihkan semua kategori yang terpilih
        categoryAdapter.setSelectionMode(false, selectedCategoryIds); // Beri tahu adapter untuk keluar mode seleksi
        updateSelectionUI(false); // Perbarui UI kembali ke mode normal
    }

    // Memperbarui tampilan UI berdasarkan mode seleksi
    private void updateSelectionUI(boolean inSelectionMode) {
        FloatingActionButton fabAddCategory = findViewById(R.id.fabAddCategory);
        TextView tvCategoryListTitle = findViewById(R.id.tvCategoryListTitle);

        if (inSelectionMode) {
            btnSelectDeleteMode.setVisibility(View.GONE);
            fabAddCategory.setVisibility(View.GONE); // Sembunyikan FAB tambah
            btnDeleteSelected.setVisibility(View.VISIBLE);
            btnCancelSelection.setVisibility(View.VISIBLE);
            tvSelectionCount.setVisibility(View.VISIBLE);
            tvCategoryListTitle.setText("Select Categories"); // Ubah judul menjadi "Select Categories"
            updateSelectionCount();
        } else {
            // Tampilkan tombol "Pilih untuk Hapus" hanya jika ada kategori
            if (categories.isEmpty()) {
                btnSelectDeleteMode.setVisibility(View.GONE);
            } else {
                btnSelectDeleteMode.setVisibility(View.VISIBLE);
            }
            fabAddCategory.setVisibility(View.VISIBLE);
            btnDeleteSelected.setVisibility(View.GONE);
            btnCancelSelection.setVisibility(View.GONE);
            tvSelectionCount.setVisibility(View.GONE);
            tvCategoryListTitle.setText("Event Categories"); // Kembalikan judul ke "Event Categories"
        }
    }

    // Memperbarui teks yang menunjukkan jumlah kategori yang terpilih
    private void updateSelectionCount() {
        tvSelectionCount.setText(selectedCategoryIds.size() + " selected");
        // Aktifkan/nonaktifkan tombol "Hapus Terpilih" berdasarkan apakah ada yang dipilih
        btnDeleteSelected.setEnabled(!selectedCategoryIds.isEmpty());
    }

    // Konfirmasi penghapusan kategori yang terpilih
    private void confirmAndDeleteSelectedCategories() {
        if (selectedCategoryIds.isEmpty()) {
            Toast.makeText(this, "Please select categories to delete.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Selected Categories")
                .setMessage("Are you sure you want to delete " + selectedCategoryIds.size() + " selected categories? This will NOT delete associated events.")
                .setPositiveButton("Delete", (dialog, which) -> deleteSelectedCategories())
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // Melakukan penghapusan kategori yang terpilih dari Firebase
    private void deleteSelectedCategories() {
        for (String categoryId : selectedCategoryIds) {
            mDatabase.child("categories").child(categoryId).removeValue()
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Category " + categoryId + " deleted."))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to delete category " + categoryId + ": " + e.getMessage()));
        }
        Toast.makeText(this, selectedCategoryIds.size() + " categories deleted.", Toast.LENGTH_SHORT).show();
        exitSelectionMode(); // Keluar dari mode seleksi setelah penghapusan selesai
    }

    // --- Implementasi dari interface CategoryAdapterK.OnCategoryActionListener ---

    // Dipanggil saat kategori diklik di mode normal
    @Override
    public void onCategoryClick(CategoryK category) {
        if (!categoryAdapter.isInSelectionMode()) { // Pastikan bukan di mode seleksi
            Toast.makeText(this, "Opening events for category: " + category.getName(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(CategoryListActivityK.this, CategoryEventListActivityK.class); // Buka CategoryEventListActivityK
            intent.putExtra("categoryId", category.getId()); // Kirim ID kategori
            intent.putExtra("categoryName", category.getName()); // Kirim nama kategori
            startActivity(intent);
        }
    }

    // Dipanggil saat tombol "Edit" di item kategori diklik
    @Override
    public void onEditCategory(CategoryK category) {
        if (!categoryAdapter.isInSelectionMode()) { // Pastikan bukan di mode seleksi
            Intent intent = new Intent(CategoryListActivityK.this, CategoryCreationActivityK.class); // Buka CategoryCreationActivityK
            intent.putExtra("categoryId", category.getId()); // Kirim ID kategori untuk mode edit
            categoryCreationLauncher.launch(intent); // Memulai Activity dan menunggu hasilnya
        }
    }

    // Dipanggil saat checkbox di item kategori diubah (di mode seleksi)
    @Override
    public void onCategorySelected(CategoryK category, boolean isSelected) {
        if (isSelected) {
            selectedCategoryIds.add(category.getId()); // Tambahkan ID ke set jika dipilih
        } else {
            selectedCategoryIds.remove(category.getId()); // Hapus ID dari set jika tidak dipilih
        }
        updateSelectionCount(); // Perbarui tampilan jumlah yang terpilih
    }
}