package com.example.huddleup;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class J_NotificationActivity extends BaseActivity {

    private static final String CLOUDINARY_CLOUD_NAME = "dogwmbaw4";
    private static final String CLOUDINARY_API_KEY = "492621155953182";
    private static final String CLOUDINARY_API_SECRET = "CySQXQNV7UKNv8pCkURttqO8XPo";
    private RecyclerView recyclerView;
    private J_NotificationAdapter adapter;
    private List<J_NotificationItem> notificationList;

    private DatabaseReference eventsDbRef;
    private DatabaseReference userRegisteredEventsDbRef;
    private FirebaseAuth mAuth;
    private List<J_NotificationItem> tempNewEventsList;
    private List<J_NotificationItem> tempFollowedEventsList;
    private List<J_NotificationItem> tempManualNotificationsList;

    private static final int PICK_IMAGE_REQUEST = 100;
    private Uri selectedImageUri;
    private AlertDialog currentAddNotificationDialog;
    private ImageView ivDialogPreviewGlobal;
    private EditText etTitleGlobal;
    private EditText etDescriptionGlobal;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.j_notification);

        Map<String, String> config = new HashMap<>();
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

        tempFollowedEventsList = new ArrayList<>();
        tempNewEventsList = new ArrayList<>();
        tempManualNotificationsList = new ArrayList<>();

        J_NotificationAdapter.attachItemTouchHelper(recyclerView, adapter);
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 3. Buat Intent untuk pindah ke Z_MainActivity
                Intent intent = new Intent(J_NotificationActivity.this, Z_MainActivity.class);

                // 4. Jalankan Intent untuk membuka activity baru
                startActivity(intent);

                // 5. (Opsional tapi disarankan) Tutup activity saat ini agar tidak menumpuk
                finish();
            }
        });


        FloatingActionButton fabUpload = findViewById(R.id.fabUpload);
        fabUpload.setOnClickListener(v -> showAddNotificationDialog());
        fabUpload.setOnLongClickListener(v -> {
            showAddNotificationDialog();
            return true;
        });

// Di dalam onCreate() di J_NotificationActivity.java
        adapter.setOnItemLongClickListener(position -> {
            final J_NotificationItem item = notificationList.get(position);

            // HANYA izinkan edit jika itu notifikasi dan punya key (artinya notifikasi manual)
            if (item.getType() == J_NotificationItem.TYPE_NOTIFICATION && item.getKey() != null && !item.getKey().isEmpty()) {
                showEditNotificationDialog(position);
            } else {
                // Jangan lakukan apa-apa, atau tampilkan pesan singkat jika perlu
                Log.d("NOTIF_ACTION", "This item cannot be edited.");
                Toast.makeText(this, "This notification cannot be edited", Toast.LENGTH_SHORT).show();
            }
        });

// Di dalam onCreate() di J_NotificationActivity.java
        adapter.setOnItemSwipeListener(position -> {
            if (position < 0 || position >= notificationList.size()) {
                return;
            }

            final J_NotificationItem swipedItem = notificationList.get(position);

            // KONDISI 1: Jika ini notifikasi MANUAL (punya key), hapus dari Firebase.
            if (swipedItem.getKey() != null && !swipedItem.getKey().isEmpty()) {
                DatabaseReference notificationRef = FirebaseDatabase.getInstance()
                        .getReference("notifications")
                        .child(swipedItem.getKey());

                notificationRef.removeValue().addOnSuccessListener(aVoid -> {
                    Log.d("FIREBASE_DELETE", "Manual notification deleted: " + swipedItem.getKey());
                    Toast.makeText(J_NotificationActivity.this, "Notification deleted", Toast.LENGTH_SHORT).show();
                    // Hapus dari tampilan setelah sukses dari DB
                    // (Adapter akan menangani penghapusan dari list)
                }).addOnFailureListener(e -> {
                    Log.e("FIREBASE_DELETE", "Failed to delete notification", e);
                    Toast.makeText(J_NotificationActivity.this, "Failed to delete", Toast.LENGTH_SHORT).show();
                    // Jika gagal, kembalikan item ke posisi semula
                    adapter.notifyItemChanged(position);
                });
            }
            // KONDISI 2: Jika ini notifikasi EVENT (tidak punya key), hapus dari TAMPILAN SAJA.
            else if (swipedItem.getEventDateString() != null) {
                notificationList.remove(position);
                adapter.notifyItemRemoved(position);
                Toast.makeText(J_NotificationActivity.this, "Notification hidden", Toast.LENGTH_SHORT).show();
            }
            // KONDISI 3: Jika bukan keduanya (untuk keamanan), batalkan swipe.
            else {
                adapter.notifyItemChanged(position);
            }
        });

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        eventsDbRef = FirebaseDatabase.getInstance().getReference("events");

        if (currentUser != null) {
            Log.d("NOTIF_DEBUG", "User logged in: " + currentUser.getUid());
            userRegisteredEventsDbRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(currentUser.getUid())
                    .child("registered_events");
            loadRegisteredAndAllEvents();
        } else {
            Log.d("NOTIF_DEBUG", "No user logged in. Loading only new events.");
            loadOnlyNewEvents();
        }
    }

    private void showAddNotificationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.j_add_notification, null);
        builder.setView(dialogView);

        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);
        Button btnChooseImage = dialogView.findViewById(R.id.btnChooseImage);
        ImageView ivDialogPreview = dialogView.findViewById(R.id.ivDialogPreview);

        selectedImageUri = null;
        ivDialogPreview.setVisibility(View.GONE);
        ivDialogPreview.setImageDrawable(null);

        btnChooseImage.setOnClickListener(v -> openImageChooser());

        builder.setPositiveButton("Save", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (title.isEmpty()) {
                etTitle.setError("Title cannot be empty");
                return;
            }

            if (selectedImageUri != null) {
                uploadImageToCloudinary(selectedImageUri, title, description);
            } else {
                saveNotificationToFirebase(title, description, null);
            }
            if (currentAddNotificationDialog != null) {
                currentAddNotificationDialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            selectedImageUri = null;
            ivDialogPreviewGlobal = null;
            etTitleGlobal = null;
            etDescriptionGlobal = null;
        });

        currentAddNotificationDialog = builder.create();
        currentAddNotificationDialog.show();
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
            ImageView ivDialogPreview = findViewById(R.id.ivDialogPreview);
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
        progressDialog.setMessage("Uploading image...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        MediaManager.get().upload(imageUri)
                .option("folder", "notification_images")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d("CLOUDINARY_UPLOAD", "Upload started: " + requestId);
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        double progress = (double) bytes / totalBytes * 100;
                        progressDialog.setMessage("Uploading image... " + (int) progress + "%");
                        Log.d("CLOUDINARY_UPLOAD", "Progress: " + (int) progress + "%");
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        progressDialog.dismiss();
                        String imageUrl = (String) resultData.get("url");

                        if (imageUrl != null && imageUrl.startsWith("http://")) {
                            imageUrl = imageUrl.replace("http://", "https://");
                        }

                        Log.d("CLOUDINARY_UPLOAD", "Upload successful. Final URL: " + imageUrl);
                        saveNotificationToFirebase(title, description, imageUrl);
                        Toast.makeText(J_NotificationActivity.this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        progressDialog.dismiss();
                        Log.e("CLOUDINARY_UPLOAD", "Upload error: " + error.getDescription());
                        Toast.makeText(J_NotificationActivity.this, "Failed to upload image: " + error.getDescription(), Toast.LENGTH_LONG).show();
                        saveNotificationToFirebase(title, description, null);
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        Log.w("CLOUDINARY_UPLOAD", "Upload rescheduled: " + error.getDescription());
                    }
                }).dispatch();
    }

    private void saveNotificationToFirebase(String title, String description, @Nullable String imageUrl) {
        DatabaseReference notificationsDbRef = FirebaseDatabase.getInstance().getReference("notifications");
        String notificationId = notificationsDbRef.push().getKey();

        long currentTimestamp = System.currentTimeMillis();
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(currentTimestamp));

        J_NotificationItem newNotification = new J_NotificationItem(
                J_NotificationItem.TYPE_NOTIFICATION,
                title,
                description,
                currentTime,
                false,
                null,
                currentTimestamp,
                imageUrl
        );

        if (notificationId != null) {
            notificationsDbRef.child(notificationId).setValue(newNotification)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(J_NotificationActivity.this, "Notification added successfully!", Toast.LENGTH_SHORT).show();
                        Log.d("FIREBASE_SAVE", "Notification saved: " + title);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(J_NotificationActivity.this, "Failed to add notification: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("FIREBASE_SAVE", "Failed to save notification: " + e.getMessage());
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


    private void loadAllEventsFromDb(HashMap<String, Long> registeredEventTimestamps, boolean userIsLoggedIn) {
        eventsDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tempFollowedEventsList.clear();
                tempNewEventsList.clear();

                SimpleDateFormat eventDateParser = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    // Asumsi N_EventModel sudah memiliki getImageUrl()
                    N_EventModel event = dataSnapshot.getValue(N_EventModel.class);
                    if (event != null) {
                        event.setKey(dataSnapshot.getKey());

                        // --- Logika untuk event yang diikuti user ---
                        if (userIsLoggedIn && registeredEventTimestamps.containsKey(event.getKey())) {

                            String finalTitle = "🎉 Registration Successful: " + event.getName();
                            String finalMessage = "Congratulations! You have successfully registered for the event '" + event.getName() + "'.";
                            long finalTimestampForSorting = System.currentTimeMillis();

                            try {
                                Date eventActualDate = eventDateParser.parse(event.getDate());
                                long eventTimestamp = eventActualDate.getTime(); // Ambil timestamp long dari tanggal event

                                long diffMillisToEvent = eventTimestamp - System.currentTimeMillis();
                                long diffDaysToEvent = TimeUnit.DAYS.convert(diffMillisToEvent, TimeUnit.MILLISECONDS);

                                // Panggil dengan long timestamp yang benar
                                String kategoriWaktu = getKategoriTanggal(eventTimestamp);
                                String timeSensitiveTitle = "";
                                String timeSensitiveMessage = "";

                                switch (kategoriWaktu) {
                                    case "Hari Ini":
                                        timeSensitiveTitle = "🔔 TODAY: " + event.getName() + " Starts!";
                                        timeSensitiveMessage = "Time for action! The event '" + event.getName() + "' awaits your presence.";
                                        break;
                                    case "Besok":
                                        timeSensitiveTitle = "⏳ Only 1 Day Left: " + event.getName();
                                        timeSensitiveMessage = "Final countdown! Tomorrow is the day for '" + event.getName() + "'.";
                                        break;
                                    case "Mendatang (7 Hari)":
                                        timeSensitiveTitle = "🗓 " + (diffDaysToEvent + 1) + " Days Until: " + event.getName();
                                        timeSensitiveMessage = "Can't wait? Only a few days left until '" + event.getName() + "'.";
                                        break;
                                    case "7 Hari Terakhir": case "Kemarin": case "Lainnya (Lampau)":
                                        timeSensitiveTitle = "Event Finished: " + event.getName();
                                        timeSensitiveMessage = "The event '" + event.getName() + "' has concluded. Thank you for participating!";
                                        break;
                                }

                                if (!timeSensitiveTitle.isEmpty()) {
                                    finalTitle = timeSensitiveTitle;
                                    finalMessage = timeSensitiveMessage;
                                }

                            } catch (ParseException e) {
                                Log.e("NOTIF_DEBUG", "Error parsing date for followed event: " + event.getName(), e);
                            }

                            tempFollowedEventsList.add(new J_NotificationItem(
                                    J_NotificationItem.TYPE_NOTIFICATION,
                                    finalTitle,
                                    finalMessage,
                                    event.getDate(), // Menampilkan TANGGAL event
                                    false,
                                    event.getDate(),
                                    finalTimestampForSorting,
                                    event.getImageUrl() // Asumsi ada method getImageUrl()
                            ));

                        } else { // --- Logika untuk event baru yang belum diikuti ---
                            long notificationTimestamp;
                            try {
                                Date eventDateObj = eventDateParser.parse(event.getDate());
                                notificationTimestamp = eventDateObj.getTime();
                            } catch (ParseException e) {
                                notificationTimestamp = System.currentTimeMillis();
                            }

                            tempNewEventsList.add(new J_NotificationItem(
                                    J_NotificationItem.TYPE_NOTIFICATION,
                                    "New Event: " + event.getName(),
                                    event.getAbout(),
                                    event.getDate(), // Menampilkan TANGGAL event
                                    false,
                                    event.getDate(),
                                    notificationTimestamp,
                                    event.getImageUrl() // Asumsi ada method getImageUrl()
                            ));
                        }
                    }
                }
                Log.d("FIREBASE_DATA_LOADED", "Events data loaded. Followed: " + tempFollowedEventsList.size() + ", New: " + tempNewEventsList.size());
                loadManualNotifications();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FIREBASE", "Database Error (All Events): " + error.getMessage());
                loadManualNotifications();
            }
        });
    }
    private void loadManualNotifications() {
        DatabaseReference notificationsDbRef = FirebaseDatabase.getInstance().getReference("notifications");

        notificationsDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tempManualNotificationsList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    J_NotificationItem manualNotif = dataSnapshot.getValue(J_NotificationItem.class);
                    if (manualNotif != null) {
                        manualNotif.setKey(dataSnapshot.getKey());
                        if (manualNotif.getType() == J_NotificationItem.TYPE_NOTIFICATION && manualNotif.getEventDateString() == null) {
                            tempManualNotificationsList.add(manualNotif);
                        }
                    }
                }
                Log.d("FIREBASE_DATA_LOADED", "Manual notifications data loaded. Total manual notifications: " + tempManualNotificationsList.size());

                populateRecyclerView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FIREBASE", "Database Error (Manual Notifications): " + error.getMessage());
                populateRecyclerView();
            }
        });
    }

    private void populateRecyclerView() {
        // --- BAGIAN 1: PROSES NOTIFIKASI EVENT ---
        List<J_NotificationItem> hariIniEvents = new ArrayList<>();
        List<J_NotificationItem> besokEvents = new ArrayList<>();
        List<J_NotificationItem> mendatangEvents = new ArrayList<>();
        List<J_NotificationItem> kemarinEvents = new ArrayList<>();
        List<J_NotificationItem> eventLampau = new ArrayList<>();

        List<J_NotificationItem> allEventNotifications = new ArrayList<>();
        allEventNotifications.addAll(tempFollowedEventsList);
        allEventNotifications.addAll(tempNewEventsList);

        // Urutkan dan kategorikan HANYA notifikasi event
        Collections.sort(allEventNotifications, (item1, item2) -> Long.compare(item2.getRawTimestamp(), item1.getRawTimestamp()));

        for (J_NotificationItem item : allEventNotifications) {
            String kategori = getKategoriTanggal(item.getRawTimestamp());
            switch (kategori) {
                case "Hari Ini":
                    hariIniEvents.add(item);
                    break;
                case "Besok":
                    besokEvents.add(item);
                    break;
                case "Mendatang (7 Hari)":
                case "Mendatang (Jauh)":
                    mendatangEvents.add(item);
                    break;
                case "Kemarin":
                    kemarinEvents.add(item);
                    break;
                default: // Menangkap "Hari-Hari Lalu"
                    eventLampau.add(item);
                    break;
            }
        }

        // --- BAGIAN 2: SUSUN ULANG TAMPILAN AKHIR ---
        notificationList.clear();

        // Prioritas 1: Tampilkan Notifikasi Manual di paling atas
        if (!tempManualNotificationsList.isEmpty()) {
            // Tambahkan header khusus untuk notifikasi manual
            notificationList.add(new J_NotificationItem(J_NotificationItem.TYPE_HEADER, "Notifikasi Manual", "", "", false, null, 0, null));

            // Urutkan notifikasi manual berdasarkan waktu pembuatannya (terbaru di atas)
            Collections.sort(tempManualNotificationsList, (item1, item2) -> Long.compare(item2.getRawTimestamp(), item1.getRawTimestamp()));

            notificationList.addAll(tempManualNotificationsList);
        }

        // Prioritas 2: Tampilkan notifikasi event yang sudah dikategorikan
        if (!hariIniEvents.isEmpty()) {
            notificationList.add(new J_NotificationItem(J_NotificationItem.TYPE_HEADER, "Today", "", "", false, null, 0, null));
            notificationList.addAll(hariIniEvents);
        }
        if (!besokEvents.isEmpty()) {
            notificationList.add(new J_NotificationItem(J_NotificationItem.TYPE_HEADER, "Tomorrow", "", "", false, null, 0, null));
            notificationList.addAll(besokEvents);
        }
        if (!mendatangEvents.isEmpty()) {
            Collections.sort(mendatangEvents, (item1, item2) -> Long.compare(item1.getRawTimestamp(), item2.getRawTimestamp()));
            notificationList.add(new J_NotificationItem(J_NotificationItem.TYPE_HEADER, "Upcoming", "", "", false, null, 0, null));
            notificationList.addAll(mendatangEvents);
        }
        if (!kemarinEvents.isEmpty()) {
            notificationList.add(new J_NotificationItem(J_NotificationItem.TYPE_HEADER, "Yesterday", "", "", false, null, 0, null));
            notificationList.addAll(kemarinEvents);
        }
        if (!eventLampau.isEmpty()) {
            notificationList.add(new J_NotificationItem(J_NotificationItem.TYPE_HEADER, "Past Events", "", "", false, null, 0, null));
            notificationList.addAll(eventLampau);
        }

        adapter.notifyDataSetChanged();
        Log.d("NOTIF_CHECK", "Total notifications displayed: " + notificationList.size());
    }

    // GANTI method getKategoriTanggal yang lama dengan versi baru ini
    private String getKategoriTanggal(long timestamp) { // Sekarang menerima long timestamp
        if (timestamp == 0) {
            // Jika timestamp tidak valid, anggap saja sudah lampau
            return "Hari-Hari Lalu";
        }

        Date notifDate = new Date(timestamp);
        Date today = new Date();
        SimpleDateFormat dayOnlySdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        try {
            Date todayStartOfDay = dayOnlySdf.parse(dayOnlySdf.format(today));
            Date notifDateStartOfDay = dayOnlySdf.parse(dayOnlySdf.format(notifDate));

            long diffMillis = notifDateStartOfDay.getTime() - todayStartOfDay.getTime();
            long diffDays = TimeUnit.DAYS.convert(diffMillis, TimeUnit.MILLISECONDS);

            if (diffDays > 7) {
                return "Mendatang (Jauh)";
            } else if (diffDays > 1 && diffDays <= 7) {
                return "Mendatang (7 Hari)";
            } else if (diffDays == 1) {
                return "Besok";
            } else if (diffDays == 0) {
                return "Hari Ini";
            } else if (diffDays == -1) {
                return "Kemarin";
            } else { // Semua hari yang sudah lewat lainnya
                return "Hari-Hari Lalu";
            }
        } catch (ParseException e) {
            Log.e("DATE_COMPARE_ERROR", "Error comparing dates: " + e.getMessage());
            return "Hari-Hari Lalu";
        }
    }

    private void showEditNotificationDialog(int position) {
        if (position < 0 || position >= notificationList.size()) {
            Log.e("EDIT_NOTIF", "Invalid position: " + position);
            return;
        }

        J_NotificationItem item = notificationList.get(position);

        if (item.getType() != J_NotificationItem.TYPE_NOTIFICATION) {
            Log.d("EDIT_NOTIF", "Cannot edit non-notification item (header).");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.j_dialog_create_notification, null);
        builder.setView(dialogView);

        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);
        Button btnChooseImage = dialogView.findViewById(R.id.btnChooseImage);
        ImageView ivDialogPreview = dialogView.findViewById(R.id.ivDialogPreview);

        etTitle.setText(item.getTitle());
        etDescription.setText(item.getDescription());

        Uri initialImageUri = null;
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Picasso.get().load(item.getImageUrl()).into(ivDialogPreview);
            ivDialogPreview.setVisibility(View.VISIBLE);
            initialImageUri = Uri.parse(item.getImageUrl());
        } else {
            ivDialogPreview.setVisibility(View.GONE);
        }
        selectedImageUri = initialImageUri;

        btnChooseImage.setOnClickListener(v -> openImageChooser());

        builder.setPositiveButton("Update", (dialog, which) -> {
            String updatedTitle = etTitle.getText().toString();
            String updatedDescription = etDescription.getText().toString();
            boolean isImageChanged = (selectedImageUri != null && !selectedImageUri.toString().equals(item.getImageUrl())) ||
                    (selectedImageUri == null && item.getImageUrl() != null);
            boolean isTextChanged = !updatedTitle.equals(item.getTitle()) || !updatedDescription.equals(item.getDescription());

            if (isImageChanged) {
                uploadImageToCloudinaryForEdit(selectedImageUri, item.getKey(), updatedTitle, updatedDescription, position);
            } else if (isTextChanged) {
                DatabaseReference notificationsDbRef = FirebaseDatabase.getInstance().getReference("notifications");
                notificationsDbRef.child(item.getKey()).child("title").setValue(updatedTitle);
                notificationsDbRef.child(item.getKey()).child("description").setValue(updatedDescription)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(J_NotificationActivity.this, "Notification updated (text)!", Toast.LENGTH_SHORT).show();
                            item.setTitle(updatedTitle);
                            item.setDescription(updatedDescription);
                            adapter.notifyItemChanged(position);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(J_NotificationActivity.this, "Failed to update notification (text): " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            } else {
                Toast.makeText(J_NotificationActivity.this, "No changes made.", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void uploadImageToCloudinaryForEdit(Uri imageUri, String notificationKey, String newTitle, String newDescription, int position) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading new image...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        MediaManager.get().upload(imageUri)
                .option("folder", "notification_images")
                .callback(new UploadCallback() {
                    @Override public void onStart(String requestId) { Log.d("CLOUDINARY_EDIT", "Upload started: " + requestId); }
                    @Override public void onProgress(String requestId, long bytes, long totalBytes) {
                        double progress = (double) bytes / totalBytes * 100;
                        progressDialog.setMessage("Uploading image... " + (int) progress + "%");
                    }
                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        progressDialog.dismiss();
                        String newImageUrl = (String) resultData.get("url");

                        if (newImageUrl != null && newImageUrl.startsWith("http://")) {
                            newImageUrl = newImageUrl.replace("http://", "https://");
                        }

                        Log.d("CLOUDINARY_EDIT", "Upload successful. Final URL: " + newImageUrl);
                        DatabaseReference notificationsDbRef = FirebaseDatabase.getInstance().getReference("notifications");
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("title", newTitle);
                        updates.put("description", newDescription);
                        updates.put("imageUrl", newImageUrl);

                        notificationsDbRef.child(notificationKey).updateChildren(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(J_NotificationActivity.this, "Notification and image updated successfully!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(J_NotificationActivity.this, "Failed to update notification and image: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    }
                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        progressDialog.dismiss();
                        Log.e("CLOUDINARY_EDIT", "Upload error: " + error.getDescription());
                        Toast.makeText(J_NotificationActivity.this, "Failed to upload image: " + error.getDescription() + ". Notification saved without new image.", Toast.LENGTH_LONG).show();
                        DatabaseReference notificationsDbRef = FirebaseDatabase.getInstance().getReference("notifications");
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("title", newTitle);
                        updates.put("description", newDescription);
                        notificationsDbRef.child(notificationKey).updateChildren(updates)
                                .addOnSuccessListener(aVoid -> Toast.makeText(J_NotificationActivity.this, "Notification updated (without new image).", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(J_NotificationActivity.this, "Failed to update notification (without new image): " + e.getMessage(), Toast.LENGTH_LONG).show());
                    }
                    @Override public void onReschedule(String requestId, ErrorInfo error) { Log.w("CLOUDINARY_EDIT", "Upload rescheduled: " + error.getDescription()); }
                }).dispatch();

        }
    @Override
    protected int getNavigationMenuItemId() {
        return R.id.nav_notifications;
    }
}