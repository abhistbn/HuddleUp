package com.example.huddleup;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<NotificationItem> notificationList;
    public Uri selectedImageUri = null;
    public static final int PICK_IMAGE_REQUEST = 100;

    // Dialog references - PENTING untuk edit notification
    public ImageView dialogIvPreview = null;
    public Button dialogBtnRemoveImage = null;
    public LinearLayout dialogLlPlaceholder = null;
    public TextView dialogTvImageStatus = null;
    public View dialogVOverlay = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification);

        recyclerView = findViewById(R.id.recyclerNotifikasi);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Isi data notifikasi (data dummy)
        notificationList = new ArrayList<>();
        // Grup: Hari Ini
        notificationList.add(new NotificationItem(NotificationItem.TYPE_HEADER, "Hari Ini", "", "", false, null));
        notificationList.add(new NotificationItem(NotificationItem.TYPE_NOTIFICATION, "Event Baru!", "Upcoming Concert dari FISIP!", "1 jam lalu", false, null));
        notificationList.add(new NotificationItem(NotificationItem.TYPE_NOTIFICATION, "Pameran Startup Mahasiswa", "Datang dan dukung inovasi teman-teman di Samantha Krida!", "1 jam lalu", false, null));
        notificationList.add(new NotificationItem(NotificationItem.TYPE_NOTIFICATION, "Lomba Fotografi", "Capture UB Moment, hadiah total jutaan rupiah!", "3 jam lalu", false, null));

        // Grup: Kemarin
        notificationList.add(new NotificationItem(NotificationItem.TYPE_HEADER, "Kemarin", "", "", false, null));
        notificationList.add(new NotificationItem(NotificationItem.TYPE_NOTIFICATION, "Diskusi Publik", "Tema: 'Masa Depan Demokrasi Digital', di Gedung Widyaloka.", "Kemarin", true, null));
        notificationList.add(new NotificationItem(NotificationItem.TYPE_NOTIFICATION, "Donor Darah", "PMI hadir di FKUB, sukses kumpulkan 200 kantong darah!", "Kemarin", false, null));

        // Grup: 7 Hari Terakhir
        notificationList.add(new NotificationItem(NotificationItem.TYPE_HEADER, "7 Hari Terakhir", "", "", false, null));
        notificationList.add(new NotificationItem(NotificationItem.TYPE_NOTIFICATION, "UB Bersih-bersih", "Aksi peduli lingkungan: bersih kampus bareng!", "2 hari lalu", false, null));
        notificationList.add(new NotificationItem(NotificationItem.TYPE_NOTIFICATION, "Pemilihan BEM", "Debat kandidat BEM Universitas berlangsung panas!", "4 hari lalu", true, null));
        notificationList.add(new NotificationItem(NotificationItem.TYPE_NOTIFICATION, "Pemutaran Film", "Film pendek karya mahasiswa FIB tayang di Perpustakaan Pusat.", "6 hari lalu", false, null));

        Log.d("DEBUG", "Total Notifikasi: " + notificationList.size());

        adapter = new NotificationAdapter(this, notificationList);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        NotificationAdapter.attachItemTouchHelper(recyclerView, adapter);

        FloatingActionButton fabAddNotification = findViewById(R.id.fabUpload);
        fabAddNotification.setOnClickListener(v -> {
            showCreateNotificationDialog();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {

                // PENTING: Cek apakah sedang edit notification atau create baru
                if (dialogIvPreview != null && dialogLlPlaceholder != null &&
                        dialogTvImageStatus != null && dialogBtnRemoveImage != null && dialogVOverlay != null) {

                    // Case 1: Sedang edit notification - update melalui adapter
                    selectedImageUri = imageUri;
                    updateDialogImagePreview(imageUri);

                } else {
                    // Case 2: Create notification baru
                    selectedImageUri = imageUri;
                    updateImagePreview();
                }
            }
        }
    }

    private void showCreateNotificationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_notification, null);
        builder.setView(dialogView);

        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);
        Button btnChooseImage = dialogView.findViewById(R.id.btnChooseImage);

        // PENTING: Inisialisasi SEMUA dialog references untuk create
        dialogIvPreview = dialogView.findViewById(R.id.ivDialogPreview);
        dialogBtnRemoveImage = dialogView.findViewById(R.id.btnRemoveImage);
        dialogLlPlaceholder = dialogView.findViewById(R.id.llImagePlaceholder);
        dialogTvImageStatus = dialogView.findViewById(R.id.tvImageStatus);
        dialogVOverlay = dialogView.findViewById(R.id.vImageOverlay);

        // Reset selectedImageUri setiap kali dialog dibuka untuk "Add"
        selectedImageUri = null;

        // Set initial state
        updateDialogImagePreview(null); // Reset ke state kosong

        btnChooseImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        // Listener untuk tombol hapus gambar di dialog
        dialogBtnRemoveImage.setOnClickListener(v -> {
            selectedImageUri = null;
            updateDialogImagePreview(null); // Reset preview
            Toast.makeText(NotificationActivity.this, "🗑️ Gambar berhasil dihapus", Toast.LENGTH_SHORT).show();
        });

        builder.setPositiveButton("Add", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String desc = etDescription.getText().toString().trim();

            if (title.isEmpty() || desc.isEmpty()) {
                Toast.makeText(NotificationActivity.this, "⚠️ Judul dan Deskripsi tidak boleh kosong!", Toast.LENGTH_SHORT).show();
                return;
            }

            String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

            int insertIndex = 1;
            if (notificationList.isEmpty() || notificationList.get(0).getType() != NotificationItem.TYPE_HEADER || !notificationList.get(0).getTitle().equals("Hari Ini")) {
                notificationList.add(0, new NotificationItem(NotificationItem.TYPE_HEADER, "Hari Ini", "", "", false, null));
                insertIndex = 1;
            } else {
                for (int i = 1; i < notificationList.size(); i++) {
                    if (notificationList.get(i).getType() == NotificationItem.TYPE_HEADER) {
                        insertIndex = i;
                        break;
                    }
                    insertIndex = i + 1;
                }
            }

            notificationList.add(insertIndex, new NotificationItem(NotificationItem.TYPE_NOTIFICATION, title, desc, currentTime, false, selectedImageUri));
            adapter.notifyItemInserted(insertIndex);
            recyclerView.scrollToPosition(insertIndex);
            Toast.makeText(this, "✅ Notifikasi berhasil ditambahkan!", Toast.LENGTH_SHORT).show();

            resetDialogReferences(); // Clear references
            dialog.dismiss();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            resetDialogReferences(); // Clear references
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();

        // Set dismiss listener untuk clear references
        dialog.setOnDismissListener(dialogInterface -> {
            resetDialogReferences();
        });

        dialog.show();
    }

    // METHOD PENTING: Update preview gambar di dialog
    private void updateDialogImagePreview(Uri imageUri) {
        if (dialogIvPreview == null || dialogLlPlaceholder == null ||
                dialogTvImageStatus == null || dialogBtnRemoveImage == null || dialogVOverlay == null) {
            return; // Safety check
        }

        if (imageUri != null) {
            // Ada gambar - tampilkan preview
            dialogIvPreview.setImageURI(imageUri);
            dialogIvPreview.setVisibility(View.VISIBLE);
            dialogVOverlay.setVisibility(View.VISIBLE);
            dialogLlPlaceholder.setVisibility(View.GONE);
            dialogBtnRemoveImage.setVisibility(View.VISIBLE);

            // Update status
            dialogTvImageStatus.setText("✅ Gambar dipilih");
            dialogTvImageStatus.setTextColor(Color.parseColor("#27AE60"));

            // Update status badge background
            GradientDrawable statusBg = new GradientDrawable();
            statusBg.setColor(Color.parseColor("#D5F4E6"));
            statusBg.setCornerRadius(12f);
            statusBg.setStroke(1, Color.parseColor("#27AE60"));
            dialogTvImageStatus.setBackground(statusBg);

            Toast.makeText(this, "📷 Gambar berhasil dipilih!", Toast.LENGTH_SHORT).show();

        } else {
            // Tidak ada gambar - tampilkan placeholder
            dialogIvPreview.setVisibility(View.GONE);
            dialogVOverlay.setVisibility(View.GONE);
            dialogLlPlaceholder.setVisibility(View.VISIBLE);
            dialogBtnRemoveImage.setVisibility(View.GONE);

            // Update status
            dialogTvImageStatus.setText("📷 Tidak ada gambar");
            dialogTvImageStatus.setTextColor(Color.parseColor("#95A5A6"));

            // Reset status badge background
            GradientDrawable statusBg = new GradientDrawable();
            statusBg.setColor(Color.parseColor("#ECF0F1"));
            statusBg.setCornerRadius(12f);
            statusBg.setStroke(1, Color.parseColor("#D5DBDB"));
            dialogTvImageStatus.setBackground(statusBg);
        }
    }

    private void updateImagePreview() {
        Toast.makeText(this, "📷 Gambar berhasil dipilih!", Toast.LENGTH_SHORT).show();
    }

    // Method untuk reset dialog references
    public void resetDialogReferences() {
        selectedImageUri = null;
        dialogIvPreview = null;
        dialogBtnRemoveImage = null;
        dialogLlPlaceholder = null;
        dialogTvImageStatus = null;
        dialogVOverlay = null;
    }
}