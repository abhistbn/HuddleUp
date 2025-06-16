package com.example.huddleup;



import android.app.ProgressDialog; // Untuk dialog loading
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog; // Pastikan ini androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.cloudinary.android.MediaManager; // Import Cloudinary
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.squareup.picasso.Picasso; // Import Picasso

import java.text.ParseException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map; // Import Map untuk Cloudinary config
import java.util.concurrent.TimeUnit;



public class J_NotificationActivity extends AppCompatActivity {

    private static final String CLOUDINARY_CLOUD_NAME = "dogwmbaw4";
    private static final String CLOUDINARY_API_KEY = "492621155953182";
    private static final String CLOUDINARY_API_SECRET = "CySQXQNV7UKNv8pCkURttqO8XPo";
    private static final String CLOUDINARY_UPLOAD_PRESET = "HuddleUp";
    private RecyclerView recyclerView;
    private J_NotificationAdapter adapter;
    private List<J_NotificationItem> notificationList;

    private DatabaseReference eventsDbRef;
    private DatabaseReference userRegisteredEventsDbRef;
    private FirebaseAuth mAuth;
    private List<J_NotificationItem> tempNewEventsList = new ArrayList<>();
    private List<J_NotificationItem> tempFollowedEventsList = new ArrayList<>();

    private static final int PICK_IMAGE_REQUEST = 100;
    private Uri selectedImageUri; // Untuk menyimpan URI gambar yang dipilih
    private AlertDialog currentAddNotificationDialog; // Untuk menyimpan referensi dialog
    private ImageView ivDialogPreviewGlobal; // Untuk menyimpan referensi ImageView dari dialog
    private EditText etTitleGlobal; // Tambahkan ini
    private EditText etDescriptionGlobal; // Tambahkan ini

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.j_notification);

        // Inisialisasi Cloudinary (hanya sekali)
        // Ganti dengan konfigurasi Cloudinary Anda
        Map config = new HashMap();
        config.put("cloud_name", CLOUDINARY_CLOUD_NAME);
        config.put("api_key", CLOUDINARY_API_KEY);
        config.put("api_secret", CLOUDINARY_API_SECRET);
        MediaManager.init(this, config);
        Log.d("CLOUDINARY", "Cloudinary initialized.");

        recyclerView = findViewById(R.id.recyclerNotifikasi);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        notificationList = new ArrayList<>();
        adapter = new J_NotificationAdapter(this, notificationList);
        recyclerView.setAdapter(adapter);

        // === Swipe to delete ===
        J_NotificationAdapter.attachItemTouchHelper(recyclerView, adapter);

        // === FAB: klik untuk upload gambar, long click untuk tambah notifikasi ===
        FloatingActionButton fabUpload = findViewById(R.id.fabUpload);

        fabUpload.setOnClickListener(v -> showAddNotificationDialog());
        fabUpload.setOnLongClickListener(v -> {
            showAddNotificationDialog();
            return true;
        });

        adapter.setOnItemLongClickListener(position -> {
            // Panggil metode yang sudah dipindahkan
            // Pastikan item yang diedit adalah TYPE_NOTIFICATION, bukan header
            if (notificationList.get(position).getType() == J_NotificationItem.TYPE_NOTIFICATION) {
                showEditNotificationDialog(position);
            } else {
                Log.d("NOTIF_ACTION", "Tidak bisa edit header.");
            }
        });

        adapter.setOnItemSwipeListener(position -> {
            // Ambil item yang di-swipe
            J_NotificationItem swipedItem = notificationList.get(position);

            // Logging (untuk debugging)
            Log.d("SWIPE_DEBUG", "Item di posisi " + position + " di-swipe. Judul: " + swipedItem.getTitle());

            // Cek jenis item: hanya hapus kalau TYPE_NOTIFICATION
            if (swipedItem.getType() == J_NotificationItem.TYPE_NOTIFICATION) {
                adapter.deleteItem(position); // Hapus dari list adapter
                // TODO: Pertimbangkan menghapus gambar dari Cloudinary jika notifikasi dihapus
            } else {
                // Jika bukan notifikasi (misal header), batalkan swipe
                adapter.notifyItemChanged(position);
                Log.d("SWIPE_DEBUG", "Swipe dibatalkan: bukan tipe notifikasi");
            }
        });

        // === Firebase setup ===
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        eventsDbRef = FirebaseDatabase.getInstance().getReference("events");

        if (currentUser != null) {
            Log.d("NOTIF_DEBUG", "User logged in: " + currentUser.getUid());
            userRegisteredEventsDbRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(currentUser.getUid())
                    .child("registered_events");
            loadRegisteredAndAllEvents(); // Memuat data saat aplikasi dimulai
        } else {
            Log.d("NOTIF_DEBUG", "Tidak ada user yang login. Hanya akan memuat event baru.");
            loadOnlyNewEvents(); // Memuat data saat aplikasi dimulai
        }
    }

    private void showAddNotificationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.j_add_notification, null); // Pastikan ini layout yang benar
        builder.setView(dialogView);

        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);
        Button btnChooseImage = dialogView.findViewById(R.id.btnChooseImage);
        ImageView ivDialogPreview = dialogView.findViewById(R.id.ivDialogPreview);

        // Reset selectedImageUri setiap kali dialog dibuka
        selectedImageUri = null;
        ivDialogPreview.setVisibility(View.GONE);
        ivDialogPreview.setImageDrawable(null);

        btnChooseImage.setOnClickListener(v -> {
            openImageChooser();
        });

        builder.setTitle("Tambah Notifikasi");
        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (title.isEmpty()) {
                etTitle.setError("Judul tidak boleh kosong");
                return;
            }

            // Jika ada gambar dipilih, upload dulu ke Cloudinary
            if (selectedImageUri != null) {
                uploadImageToCloudinary(selectedImageUri, title, description);
            } else {
                // Jika tidak ada gambar, langsung simpan notifikasi ke Firebase
                saveNotificationToFirebase(title, description, null); // null untuk imageUrl
            }
            if (currentAddNotificationDialog != null) {
                currentAddNotificationDialog.dismiss();
            }
        });

        builder.setNegativeButton("Batal", (dialog, which) -> {
            // Ketika dialog dibatalkan, pastikan untuk membersihkan referensi jika perlu
            selectedImageUri = null;
            ivDialogPreviewGlobal = null;
            etTitleGlobal = null;
            etDescriptionGlobal = null;
        });

        currentAddNotificationDialog = builder.create(); // Buat dialog dan simpan referensinya
        currentAddNotificationDialog.show(); // Tampilkan dialog
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            ImageView ivDialogPreview = findViewById(R.id.ivDialogPreview); // Ambil ImageView dari dialog yang sedang aktif
            if (ivDialogPreview != null) {
                Picasso.get().load(selectedImageUri).into(ivDialogPreview);
                ivDialogPreview.setVisibility(View.VISIBLE);
                Log.d("IMAGE_CHOOSER", "Image selected and previewed: " + selectedImageUri.toString());
            } else {
                Log.e("IMAGE_CHOOSER", "ivDialogPreview is null. Dialog might not be active or view not found.");
            }
        }
    }


    private void uploadImageToCloudinary(Uri imageUri, String title, String description) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Mengunggah gambar...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        MediaManager.get().upload(imageUri)
                .option("folder", "notification_images") // Folder di Cloudinary
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d("CLOUDINARY_UPLOAD", "Upload started: " + requestId);
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        double progress = (double) bytes / totalBytes * 100;
                        progressDialog.setMessage("Mengunggah gambar... " + (int) progress + "%");
                        Log.d("CLOUDINARY_UPLOAD", "Progress: " + (int) progress + "%");
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        progressDialog.dismiss();
                        String imageUrl = (String) resultData.get("url");

                        // --- TAMBAHKAN KODE INI ---
                        // Pastikan URL menggunakan HTTPS
                        if (imageUrl != null && imageUrl.startsWith("http://")) {
                            imageUrl = imageUrl.replace("http://", "https://");
                        }
                        // --- AKHIR KODE TAMBAHAN ---

                        Log.d("CLOUDINARY_UPLOAD", "Upload successful. Final URL: " + imageUrl); // Perbarui log ini
                        saveNotificationToFirebase(title, description, imageUrl); // Gunakan 'imageUrl' yang sudah HTTPS
                        Toast.makeText(J_NotificationActivity.this, "Gambar berhasil diunggah!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        progressDialog.dismiss();
                        Log.e("CLOUDINARY_UPLOAD", "Upload error: " + error.getDescription());
                        Toast.makeText(J_NotificationActivity.this, "Gagal mengunggah gambar: " + error.getDescription(), Toast.LENGTH_LONG).show();
                        // Tetap simpan notifikasi tanpa gambar jika upload gagal
                        saveNotificationToFirebase(title, description, null);
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        Log.w("CLOUDINARY_UPLOAD", "Upload rescheduled: " + error.getDescription());
                    }
                }).dispatch();
    }

    private void saveNotificationToFirebase(String title, String description, @Nullable String imageUrl) {
        DatabaseReference notificationsDbRef = FirebaseDatabase.getInstance().getReference("notifications"); // <-- Buat node baru "notifications"
        String notificationId = notificationsDbRef.push().getKey(); // Generate unique key

        long currentTimestamp = System.currentTimeMillis();
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(currentTimestamp));

        J_NotificationItem newNotification = new J_NotificationItem(
                J_NotificationItem.TYPE_NOTIFICATION,
                title,
                description,
                currentTime,
                false, // isRead
                null, // eventDateString tidak relevan untuk notif manual
                currentTimestamp, // rawTimestamp
                imageUrl // <-- Simpan URL gambar di sini
        );

        if (notificationId != null) {
            notificationsDbRef.child(notificationId).setValue(newNotification)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(J_NotificationActivity.this, "Notifikasi berhasil ditambahkan!", Toast.LENGTH_SHORT).show();
                        Log.d("FIREBASE_SAVE", "Notifikasi berhasil disimpan: " + title);
                        // Data akan otomatis dimuat ulang oleh loadAllEventsFromDb (karena addValueEventListener)
                        // atau panggil populateRecyclerView() jika Anda ingin segera melihat perubahan
                        // populateRecyclerView(); // Ini akan dipanggil dari listener eventsDbRef di onDataChange
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(J_NotificationActivity.this, "Gagal menambahkan notifikasi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("FIREBASE_SAVE", "Gagal menyimpan notifikasi: " + e.getMessage());
                    });
        }
    }


    private void loadRegisteredAndAllEvents() {
        HashMap<String, Long> registeredEventTimestamps = new HashMap<>();

        userRegisteredEventsDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                registeredEventTimestamps.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.getValue() instanceof HashMap) {
                        HashMap<String, Object> registrationData = (HashMap<String, Object>) dataSnapshot.getValue();
                        Object timestampObj = registrationData.get("timestamp");
                        if (timestampObj instanceof Long) {
                            registeredEventTimestamps.put(dataSnapshot.getKey(), (Long) timestampObj);
                        } else {
                            registeredEventTimestamps.put(dataSnapshot.getKey(), System.currentTimeMillis());
                        }
                    } else {
                        registeredEventTimestamps.put(dataSnapshot.getKey(), System.currentTimeMillis());
                    }
                }
                Log.d("NOTIF_DEBUG", "Registered Event IDs with Timestamps: " + registeredEventTimestamps.keySet().size() + " items");

                loadAllEventsFromDb(registeredEventTimestamps, true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FIREBASE", "Database Error (Registered Events): " + error.getMessage());
                loadAllEventsFromDb(new HashMap<>(), false);
            }
        });
    }

    private void loadOnlyNewEvents() {
        loadAllEventsFromDb(new HashMap<>(), false);
    }

    private String getKategoriWaktuNotifikasi(long timestampNotifikasi) {
        SimpleDateFormat dayOnlySdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date today = new Date(System.currentTimeMillis());
        Date notifDate = new Date(timestampNotifikasi);

        try {
            Date todayStartOfDay = dayOnlySdf.parse(dayOnlySdf.format(today));
            Date notifDateStartOfDay = dayOnlySdf.parse(dayOnlySdf.format(notifDate));

            long diffMillis = notifDateStartOfDay.getTime() - todayStartOfDay.getTime();
            long diffDays = TimeUnit.DAYS.convert(diffMillis, TimeUnit.MILLISECONDS);

            if (diffDays == 0) {
                return "Hari Ini";
            } else if (diffDays == -1) {
                return "Kemarin";
            } else {
                return "Hari-Hari Lalu";
            }
        } catch (ParseException e) {
            Log.e("NotificationActivity", "Error parsing date for notification timestamp: " + timestampNotifikasi + " - " + e.getMessage());
            return "Hari-Hari Lalu";
        }
    }


    private void loadAllEventsFromDb(HashMap<String, Long> registeredEventTimestamps, boolean userIsLoggedIn) {
        eventsDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tempFollowedEventsList.clear();
                SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
                SimpleDateFormat eventDateParser = new SimpleDateFormat("dd MMMM यथा", new Locale("id", "ID"));

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    N_EventModel event = dataSnapshot.getValue(N_EventModel.class);
                    if (event != null) {
                        event.setKey(dataSnapshot.getKey());

                        if (userIsLoggedIn && registeredEventTimestamps.containsKey(event.getKey())) {
                            Long registrationTimestamp = registeredEventTimestamps.get(event.getKey());
                            String formattedTimestamp = (registrationTimestamp != null) ? timeFormatter.format(new Date(registrationTimestamp)) : "Baru saja";

                            tempFollowedEventsList.add(new J_NotificationItem(
                                    J_NotificationItem.TYPE_NOTIFICATION,
                                    "🎉 Pendaftaran Berhasil: " + event.getName(),
                                    "Selamat! Kamu berhasil terdaftar di event '" + event.getName() + "'. Siap-siap untuk petualangan seru!",
                                    formattedTimestamp,
                                    false,
                                    event.getDate(),
                                    (registrationTimestamp != null) ? registrationTimestamp : System.currentTimeMillis(),
                                    null
                            ));

                            try {
                                String kategoriWaktu = getKategoriTanggal(event.getDate());
                                String uniqueMessage = "";
                                String title = "";
                                long currentNotifTimestamp = System.currentTimeMillis();
                                String currentFormattedTime = timeFormatter.format(new Date(currentNotifTimestamp));

                                Date eventActualDate = eventDateParser.parse(event.getDate());
                                long diffMillisToEvent = eventActualDate.getTime() - System.currentTimeMillis();
                                long diffDaysToEvent = TimeUnit.DAYS.convert(diffMillisToEvent, TimeUnit.MILLISECONDS);

                                switch (kategoriWaktu) {
                                    case "Hari Ini": title = "🔔 HARI INI: " + event.getName() + " Dimulai!"; uniqueMessage = "Waktunya beraksi! Event '" + event.getName() + "' siap menanti kehadiranmu. Jangan sampai terlewat!"; break;
                                    case "Besok": title = "⏳ Tinggal 1 Hari Lagi: " + event.getName(); uniqueMessage = "Hitungan mundur terakhir! Besok adalah harinya '" + event.getName() + "'. Persiapkan dirimu!"; break;
                                    case "Mendatang (7 Hari)": title = "🗓️ " + (diffDaysToEvent + 1) + " Hari Menuju: " + event.getName(); uniqueMessage = "Sudah tidak sabar? Tinggal beberapa hari lagi menuju event '" + event.getName() + "'. Tetap semangat!"; break;
                                    case "7 Hari Terakhir":
                                    case "Kemarin":
                                    case "Lainnya (Lampau)": title = "Event Selesai: " + event.getName(); uniqueMessage = "Event '" + event.getName() + "' sudah selesai. Terima kasih telah berpartisipasi!"; break;
                                    case "Mendatang (Jauh)":
                                    default: title = "🗓️ Event Mendatang: " + event.getName(); uniqueMessage = "Event ini akan segera hadir. Tetap ikuti perkembangannya!"; break;
                                }

                                if (!title.isEmpty()) {
                                    tempFollowedEventsList.add(new J_NotificationItem(
                                            J_NotificationItem.TYPE_NOTIFICATION,
                                            title,
                                            uniqueMessage,
                                            currentFormattedTime,
                                            false,
                                            event.getDate(),
                                            currentNotifTimestamp,
                                            null
                                    ));
                                }

                            } catch (ParseException e) {
                                Log.e("NOTIF_DEBUG", "Error parsing date for followed event: " + event.getName() + " - " + e.getMessage());
                            }
                        } else {
                            long notificationTimestamp;
                            try {
                                Date eventDateObj = eventDateParser.parse(event.getDate());
                                notificationTimestamp = eventDateObj.getTime();
                            } catch (ParseException e) {
                                Log.e("NOTIF_DEBUG", "Error parsing event date for new event timestamp: " + event.getDate() + " - " + e.getMessage());
                                notificationTimestamp = System.currentTimeMillis();
                            }
                            String formattedTimestamp = timeFormatter.format(new Date(notificationTimestamp));

                            tempNewEventsList.add(new J_NotificationItem(
                                    J_NotificationItem.TYPE_NOTIFICATION,
                                    "Event Baru: " + event.getName(),
                                    event.getAbout(),
                                    formattedTimestamp,
                                    false,
                                    event.getDate(),
                                    notificationTimestamp,
                                    null
                            ));
                        }
                    }
                }// Panggil metode baru ini
                Log.d("FIREBASE_DATA_LOADED", "Events data loaded. Total followed events: " + tempFollowedEventsList.size() + ", Total new events from events node: " + tempNewEventsList.size());
                loadManualNotifications();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FIREBASE", "Database Error (All Events): " + error.getMessage());
                loadManualNotifications(); // Tetap coba muat notifikasi manual jika event error
            }

        });
    }

    private void loadManualNotifications() {
        DatabaseReference notificationsDbRef = FirebaseDatabase.getInstance().getReference("notifications");

        notificationsDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tempNewEventsList.removeIf(item -> item.getEventDateString() == null); // Asumsi notif manual eventDateString == null

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    J_NotificationItem manualNotif = dataSnapshot.getValue(J_NotificationItem.class);
                    if (manualNotif != null) {
                        if (manualNotif.getType() == J_NotificationItem.TYPE_NOTIFICATION && manualNotif.getEventDateString() == null) {
                            manualNotif.setKey(dataSnapshot.getKey()); // Tambahkan ke tempNewEventsList
                            tempNewEventsList.add(manualNotif);
                        }
                    }
                }
                Log.d("FIREBASE_DATA_LOADED", "Manual notifications data loaded. Total manual notifications: " + (snapshot.getChildrenCount()));
                populateRecyclerView(); // Panggil populateRecyclerView setelah kedua data dimuat// <-- Pastikan ini dipanggil di akhir onDataChange
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FIREBASE", "Database Error (Manual Notifications): " + error.getMessage());
                populateRecyclerView(); // Tetap populate RecyclerView jika error
            }
        });
    }


    // Di dalam J_NotificationActivity class
    private void populateRecyclerView() {
        // List baru untuk notifikasi manual (penting)
        List<J_NotificationItem> manualNotifications = new ArrayList<>();
        List<J_NotificationItem> hariIniNotifications = new ArrayList<>();
        List<J_NotificationItem> kemarinNotifications = new ArrayList<>();
        List<J_NotificationItem> hariHariLaluNotifications = new ArrayList<>();

        // Gabungkan tempNewEventsList dan tempFollowedEventsList
        List<J_NotificationItem> allNotifications = new ArrayList<>();
        allNotifications.addAll(tempFollowedEventsList);
        allNotifications.addAll(tempNewEventsList); // Sekarang tempNewEventsList berisi event baru dari 'events' DAN notifikasi manual

        // Urutkan semua notifikasi berdasarkan timestamp (terbaru di atas)
        Collections.sort(allNotifications, (item1, item2) -> Long.compare(item2.getRawTimestamp(), item1.getRawTimestamp()));

        // Pisahkan notifikasi manual dari event-driven notifications
        for (J_NotificationItem item : allNotifications) {
            // Asumsi: notifikasi manual memiliki eventDateString == null
            if (item.getType() == J_NotificationItem.TYPE_NOTIFICATION && item.getEventDateString() == null) {
                manualNotifications.add(item);
            } else {
                // Notifikasi yang berhubungan dengan event
                long timestamp = item.getRawTimestamp();
                if (timestamp == 0) {
                    timestamp = System.currentTimeMillis(); // Fallback
                }
                String kategori = getKategoriWaktuNotifikasi(timestamp);

                switch (kategori) {
                    case "Hari Ini":
                        hariIniNotifications.add(item);
                        break;
                    case "Kemarin":
                        kemarinNotifications.add(item);
                        break;
                    default:
                        hariHariLaluNotifications.add(item);
                        break;
                }
            }
        }

        // --- Sekarang susun notificationList utama dengan urutan baru ---
        notificationList.clear();

        // 1. Notifikasi Manual (Penting)
        if (!manualNotifications.isEmpty()) {
            notificationList.add(new J_NotificationItem(J_NotificationItem.TYPE_HEADER, "Notifikasi Penting", "", "", false, null, 0, null));
            // Urutkan notifikasi manual berdasarkan waktu dibuat (terbaru di atas)
            Collections.sort(manualNotifications, (item1, item2) -> Long.compare(item2.getRawTimestamp(), item1.getRawTimestamp()));
            notificationList.addAll(manualNotifications);
        }

        // 2. Notifikasi Hari Ini
        if (!hariIniNotifications.isEmpty()) {
            notificationList.add(new J_NotificationItem(J_NotificationItem.TYPE_HEADER, "Hari Ini", "", "", false, null, 0, null));
            notificationList.addAll(hariIniNotifications);
        }

        // 3. Notifikasi Kemarin
        if (!kemarinNotifications.isEmpty()) {
            notificationList.add(new J_NotificationItem(J_NotificationItem.TYPE_HEADER, "Kemarin", "", "", false, null, 0, null));
            notificationList.addAll(kemarinNotifications);
        }

        // 4. Notifikasi Hari-Hari Lalu
        if (!hariHariLaluNotifications.isEmpty()) {
            notificationList.add(new J_NotificationItem(J_NotificationItem.TYPE_HEADER, "Hari-Hari Lalu", "", "", false, null, 0, null));
            notificationList.addAll(hariHariLaluNotifications);
        }

        adapter.notifyDataSetChanged();
        Log.d("NOTIF_CHECK", "Total notifikasi yang ditampilkan di RecyclerView: " + notificationList.size());

        for (int i = 0; i < notificationList.size(); i++) {
            J_NotificationItem item = notificationList.get(i);
            Log.d("DEBUG_FINAL_LIST", i + ": Type=" + item.getType() + ", Title='" + item.getTitle() + "', RawTimestamp=" + item.getRawTimestamp() + ", ImageUrl=" + item.getImageUrl());
        }
    }


    private String getKategoriTanggal(String tanggalEvent) {
        SimpleDateFormat inputSdfFirebaseID = new SimpleDateFormat("dd MMMM यथा", new Locale("id", "ID"));
        Date eventDate;
        try {
            eventDate = inputSdfFirebaseID.parse(tanggalEvent);
        } catch (ParseException e) {
            Log.e("NotificationActivity", "Error parsing event date string (dd MMMM यथा): " + tanggalEvent + " - " + e.getMessage());
            return "Lainnya";
        }

        Date today = new Date();
        SimpleDateFormat dayOnlySdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date todayStartOfDay = dayOnlySdf.parse(dayOnlySdf.format(today));
            Date eventDateStartOfDay = dayOnlySdf.parse(dayOnlySdf.format(eventDate));

            long diffMillis = eventDateStartOfDay.getTime() - todayStartOfDay.getTime();
            long diffDays = TimeUnit.DAYS.convert(diffMillis, TimeUnit.MILLISECONDS);

            if (diffDays == 0) {
                return "Hari Ini";
            } else if (diffDays == 1) {
                return "Besok";
            } else if (diffDays > 1 && diffDays <= 7) {
                return "Mendatang (7 Hari)";
            } else if (diffDays > 7) {
                return "Mendatang (Jauh)";
            } else if (diffDays == -1) {
                return "Kemarin";
            } else if (diffDays < -1 && diffDays >= -7) {
                return "7 Hari Terakhir";
            } else {
                return "Lainnya (Lampau)";
            }
        } catch (ParseException e) {
            Log.e("NotificationActivity", "Error comparing dates after initial parse: " + e.getMessage());
            return "Lainnya";
        }
    }

    // Metode ini dihapus atau diubah karena logikanya sudah dipindahkan
    // ke showAddNotificationDialog() dan saveNotificationToFirebase()
    // @Override
    // protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    //     super.onActivityResult(requestCode, resultCode, data);
    //     if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
    //         Uri selectedImage = data.getData();
    //         long currentTimestamp = System.currentTimeMillis();
    //         String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(currentTimestamp));
    //         J_NotificationItem newItem = new J_NotificationItem(J_NotificationItem.TYPE_NOTIFICATION, "Upload Gambar", "Admin menambahkan gambar", currentTime, false, null, currentTimestamp);
    //         notificationList.add(1, newItem);
    //         adapter.notifyItemInserted(1);
    //     }
    // }

    // showCreateNotificationDialog ini juga bisa dihapus jika tidak lagi digunakan
    private void showCreateNotificationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.j_dialog_create_notification, null);
        builder.setView(dialogView);

        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);
        Button btnChooseImage = dialogView.findViewById(R.id.btnChooseImage);
        ImageView ivPreview = dialogView.findViewById(R.id.ivDialogPreview);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String title = etTitle.getText().toString();
            String desc = etDescription.getText().toString();
            long currentTimestamp = System.currentTimeMillis();
            String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(currentTimestamp));

            // Jika masih ingin pakai ini, Anda juga perlu menambahkan logika gambar di sini
            // dan menyimpannya ke Firebase
            notificationList.add(new J_NotificationItem(J_NotificationItem.TYPE_NOTIFICATION, title, desc, currentTime, false, null, currentTimestamp));
            adapter.notifyItemInserted(notificationList.size() - 1);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    private void showEditNotificationDialog(int position) {
        if (position < 0 || position >= notificationList.size()) {
            Log.e("EDIT_NOTIF", "Posisi tidak valid: " + position);
            return;
        }

        J_NotificationItem item = notificationList.get(position);

        if (item.getType() != J_NotificationItem.TYPE_NOTIFICATION) {
            Log.d("EDIT_NOTIF", "Tidak bisa mengedit item non-notifikasi (header).");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.j_dialog_create_notification, null);
        builder.setView(dialogView);

        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);
        Button btnChooseImage = dialogView.findViewById(R.id.btnChooseImage); // Ambil tombol gambar
        ImageView ivDialogPreview = dialogView.findViewById(R.id.ivDialogPreview); // Ambil preview gambar

        // Set data yang sudah ada
        etTitle.setText(item.getTitle());
        etDescription.setText(item.getDescription());

        // Jika ada gambar, tampilkan di preview
        Uri initialImageUri = null;
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Picasso.get().load(item.getImageUrl()).into(ivDialogPreview);
            ivDialogPreview.setVisibility(View.VISIBLE);
            initialImageUri = Uri.parse(item.getImageUrl()); // Set URI untuk perbandingan
        } else {
            ivDialogPreview.setVisibility(View.GONE);
        }
        selectedImageUri = initialImageUri;

        // Listener untuk memilih gambar baru saat edit
        btnChooseImage.setOnClickListener(v -> {
            openImageChooser();
        });


        builder.setTitle("Edit Notifikasi");
        builder.setPositiveButton("Update", (dialog, which) -> {
            String updatedTitle = etTitle.getText().toString();
            String updatedDescription = etDescription.getText().toString();
            boolean isImageChanged = (selectedImageUri != null && !selectedImageUri.toString().equals(item.getImageUrl())) ||
                    (selectedImageUri == null && item.getImageUrl() != null);
            boolean isTextChanged = !updatedTitle.equals(item.getTitle()) || !updatedDescription.equals(item.getDescription());

            if (isImageChanged) {
                // Upload gambar baru dan kemudian update Firebase
                uploadImageToCloudinaryForEdit(selectedImageUri, item.getKey(), updatedTitle, updatedDescription, position);
            } else if (isTextChanged) {
                // Hanya update teks di Firebase jika tidak ada perubahan gambar
                DatabaseReference notificationsDbRef = FirebaseDatabase.getInstance().getReference("notifications");
                notificationsDbRef.child(item.getKey()).child("title").setValue(updatedTitle);
                notificationsDbRef.child(item.getKey()).child("description").setValue(updatedDescription)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(J_NotificationActivity.this, "Notifikasi diperbarui (teks)!", Toast.LENGTH_SHORT).show();
                            item.setTitle(updatedTitle);
                            item.setDescription(updatedDescription);
                            adapter.notifyItemChanged(position);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(J_NotificationActivity.this, "Gagal memperbarui notifikasi (teks): " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            } else {
                Toast.makeText(J_NotificationActivity.this, "Tidak ada perubahan.", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss(); // Tutup dialog setelah update
        });

        builder.setNegativeButton("Batal", null);
        builder.show();
    }
    // Metode baru untuk upload gambar saat edit
    private void uploadImageToCloudinaryForEdit(Uri imageUri, String notificationKey, String newTitle, String newDescription, int position) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Mengunggah gambar baru...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        MediaManager.get().upload(imageUri)
                .option("folder", "notification_images")
                .callback(new UploadCallback() {
                    @Override public void onStart(String requestId) { Log.d("CLOUDINARY_EDIT", "Upload started: " + requestId); }
                    @Override public void onProgress(String requestId, long bytes, long totalBytes) {
                        double progress = (double) bytes / totalBytes * 100;
                        progressDialog.setMessage("Mengunggah gambar... " + (int) progress + "%");
                    }
                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        progressDialog.dismiss();
                        String newImageUrl = (String) resultData.get("url");

                        // --- TAMBAHKAN KODE INI ---
                        // Pastikan URL menggunakan HTTPS
                        if (newImageUrl != null && newImageUrl.startsWith("http://")) {
                            newImageUrl = newImageUrl.replace("http://", "https://");
                        }
                        // --- AKHIR KODE TAMBAHAN ---

                        Log.d("CLOUDINARY_EDIT", "Upload successful. Final URL: " + newImageUrl); // Perbarui log ini
                        // Update di Firebase
                        DatabaseReference notificationsDbRef = FirebaseDatabase.getInstance().getReference("notifications");
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("title", newTitle);
                        updates.put("description", newDescription);
                        updates.put("imageUrl", newImageUrl); // Gunakan newImageUrl yang sudah HTTPS

                        notificationsDbRef.child(notificationKey).updateChildren(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(J_NotificationActivity.this, "Notifikasi dan gambar berhasil diperbarui!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(J_NotificationActivity.this, "Gagal memperbarui notifikasi dan gambar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    }
                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        progressDialog.dismiss();
                        Log.e("CLOUDINARY_EDIT", "Upload error: " + error.getDescription());
                        Toast.makeText(J_NotificationActivity.this, "Gagal mengunggah gambar: " + error.getDescription() + ". Notifikasi disimpan tanpa gambar baru.", Toast.LENGTH_LONG).show();
                        DatabaseReference notificationsDbRef = FirebaseDatabase.getInstance().getReference("notifications");
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("title", newTitle);
                        updates.put("description", newDescription);
                        notificationsDbRef.child(notificationKey).updateChildren(updates)
                                .addOnSuccessListener(aVoid -> Toast.makeText(J_NotificationActivity.this, "Notifikasi diperbarui (tanpa gambar baru).", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(J_NotificationActivity.this, "Gagal memperbarui notifikasi (tanpa gambar baru): " + e.getMessage(), Toast.LENGTH_LONG).show());
                    }
                    @Override public void onReschedule(String requestId, ErrorInfo error) { Log.w("CLOUDINARY_EDIT", "Upload rescheduled: " + error.getDescription()); }
                }).dispatch();
    }
}