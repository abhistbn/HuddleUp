package com.example.huddleup;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class D_WishlistDetailActivity extends AppCompatActivity {

    // --- VARIABEL BARU UNTUK PENANDA INISIALISASI ---
    private static boolean isCloudinaryInitialized = false;

    // Komponen UI
    private ImageView ivDetailEventImage, ivPersonalNoteImage;
    private TextView tvDetailEventTitle, tvDetailEventDate, tvDetailEventLocation;
    private EditText etPersonalNote;
    private ImageButton ibAttachPhoto;
    private Button btnSaveChanges;
    private Toolbar toolbar;

    // Data dan Firebase
    private Z_EventP2 currentItem;
    private Uri imageUriToUpload;
    private DatabaseReference wishlistItemRef;

    // Launcher untuk membuka galeri
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imageUriToUpload = result.getData().getData();
                    ivPersonalNoteImage.setVisibility(View.VISIBLE);
                    Glide.with(this).load(imageUriToUpload).into(ivPersonalNoteImage);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.d_activity_wishlist_detail);

        // Panggil method untuk inisialisasi Cloudinary secara aman
        initCloudinary();

        // Lanjutkan dengan inisialisasi lainnya
        initViews();
        setupToolbar();

        currentItem = getIntent().getParcelableExtra("WISHLIST_EVENT_DETAIL");
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentItem != null && currentUser != null) {
            String userId = currentUser.getUid();
            wishlistItemRef = FirebaseDatabase.getInstance().getReference("wishlist").child(userId).child(currentItem.getId());
            displayWishlistData();
        } else {
            Toast.makeText(this, "Data event tidak valid.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ibAttachPhoto.setOnClickListener(v -> openGallery());
        btnSaveChanges.setOnClickListener(v -> saveChanges());
    }

    private void initCloudinary() {
        // --- LOGIKA PENGECEKAN YANG SUDAH DIPERBAIKI ---
        // Cek "penanda" kita, bukan memanggil MediaManager.get()
        if (!isCloudinaryInitialized) {
            try {
                Map config = new HashMap();
                config.put("cloud_name", "dytxgomu8");
                config.put("api_key", "643476478795292");
                config.put("api_secret", "aozu764f2RNEQQJg3ClHfSZHM4E");
                MediaManager.init(getApplicationContext(), config);

                // Jika berhasil, set penanda menjadi true
                isCloudinaryInitialized = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void initViews() {
        ivDetailEventImage = findViewById(R.id.ivDetailEventImage);
        ivPersonalNoteImage = findViewById(R.id.ivPersonalNoteImage);
        tvDetailEventTitle = findViewById(R.id.tvDetailEventTitle);
        tvDetailEventDate = findViewById(R.id.tvDetailEventDate);
        tvDetailEventLocation = findViewById(R.id.tvDetailEventLocation);
        etPersonalNote = findViewById(R.id.etPersonalNote);
        ibAttachPhoto = findViewById(R.id.ibAttachPhoto);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        toolbar = findViewById(R.id.toolbarDetail);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void displayWishlistData() {
        tvDetailEventTitle.setText(currentItem.getName());
        tvDetailEventDate.setText(currentItem.getDate());
        tvDetailEventLocation.setText(currentItem.getLocation());
        Glide.with(this).load(currentItem.getImageUrl()).into(ivDetailEventImage);

        if (currentItem.getPersonalNote() != null) {
            etPersonalNote.setText(currentItem.getPersonalNote());
        }
        if (currentItem.getPersonalNoteImageUrl() != null && !currentItem.getPersonalNoteImageUrl().isEmpty()) {
            ivPersonalNoteImage.setVisibility(View.VISIBLE);
            Glide.with(this).load(currentItem.getPersonalNoteImageUrl()).into(ivPersonalNoteImage);
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }

    private void saveChanges() {
        btnSaveChanges.setEnabled(false);
        Toast.makeText(this, "Menyimpan...", Toast.LENGTH_SHORT).show();

        if (imageUriToUpload != null) {
            // Jika ada gambar baru, upload ke Cloudinary
            MediaManager.get().upload(imageUriToUpload)
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) { }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) { }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            // Jika upload berhasil, dapatkan URL-nya
                            String newImageUrl = (String) resultData.get("secure_url");
                            // Panggil method untuk menyimpan catatan dengan URL baru ini
                            saveNotesToFirebase(newImageUrl);
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            // Jika upload gagal, beri tahu pengguna dan aktifkan lagi tombol
                            Toast.makeText(D_WishlistDetailActivity.this, "Upload gambar gagal: " + error.getDescription(), Toast.LENGTH_LONG).show();
                            btnSaveChanges.setEnabled(true);
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) { }
                    }).dispatch();
        } else {
            // Jika tidak ada gambar baru, langsung simpan teksnya dengan URL gambar yang sudah ada
            saveNotesToFirebase(currentItem.getPersonalNoteImageUrl());
        }
    }

    private void saveNotesToFirebase(String imageUrl) {
        String personalNoteText = etPersonalNote.getText().toString();

        Map<String, Object> noteUpdates = new HashMap<>();
        noteUpdates.put("personalNote", personalNoteText);
        // Pastikan imageUrl tidak null sebelum disimpan
        noteUpdates.put("personalNoteImageUrl", imageUrl != null ? imageUrl : "");

        wishlistItemRef.updateChildren(noteUpdates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Catatan berhasil disimpan!", Toast.LENGTH_SHORT).show();
                    finish(); // Kembali ke halaman sebelumnya
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal menyimpan catatan.", Toast.LENGTH_SHORT).show();
                    btnSaveChanges.setEnabled(true);
                });
    }
}
