package com.example.huddleup;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.text.ParseException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class J_NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private J_NotificationAdapter adapter;
    private List<J_NotificationItem> notificationList;

    private DatabaseReference eventsDbRef;
    private DatabaseReference userRegisteredEventsDbRef;
    private FirebaseAuth mAuth;

    // Tidak lagi menggunakan list-list terpisah ini secara langsung untuk populateRecyclerView
    // Kita akan menggabungkannya ke dalam list kategori waktu.
    // Namun, kita tetap membutuhkannya sebagai tempat penampungan sementara
    private List<J_NotificationItem> tempNewEventsList = new ArrayList<>();
    private List<J_NotificationItem> tempFollowedEventsList = new ArrayList<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.j_notification);

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

        // === Handle long click untuk edit notifikasi ===
        adapter.setOnItemLongClickListener(position -> {
            showEditNotificationDialog(position);
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

            loadRegisteredAndAllEvents();
        } else {
            Log.d("NOTIF_DEBUG", "Tidak ada user yang login. Hanya akan memuat event baru.");
            loadOnlyNewEvents();
        }
        Log.d("CEK_NOTIF", "Jumlah notifikasi: " + notificationList.size());

    }

    private void showAddNotificationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.j_add_notification, null);
        builder.setView(dialogView);

        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);

        builder.setTitle("Tambah Notifikasi");

        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String title = etTitle.getText().toString();
            String description = etDescription.getText().toString();
            String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

            J_NotificationItem newItem = new J_NotificationItem(
                    J_NotificationItem.TYPE_NOTIFICATION,
                    title,
                    description,
                    currentTime,
                    false
            );
            notificationList.add(0, newItem);
            adapter.notifyItemInserted(0);
        });

        builder.setNegativeButton("Batal", null);
        builder.show();
    }


    private void showEditNotificationDialog(int position) {
        J_NotificationItem item = notificationList.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.j_dialog_create_notification, null);
        builder.setView(dialogView);

        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);

        etTitle.setText(item.getTitle());
        etDescription.setText(item.getDescription());

        builder.setTitle("Edit Notifikasi");

        builder.setPositiveButton("Update", (dialog, which) -> {
            item.setTitle(etTitle.getText().toString());
            item.setDescription(etDescription.getText().toString());
            adapter.notifyItemChanged(position);
        });

        builder.setNegativeButton("Batal", null);
        builder.show();
    }



    private void loadRegisteredAndAllEvents() {
        HashMap<String, Long> registeredEventTimestamps = new HashMap<>();

        userRegisteredEventsDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                registeredEventTimestamps.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    // Cek apakah data adalah HashMap (dari setValue(Map))
                    if (dataSnapshot.getValue() instanceof HashMap) {
                        HashMap<String, Object> registrationData = (HashMap<String, Object>) dataSnapshot.getValue();
                        Object timestampObj = registrationData.get("timestamp");
                        if (timestampObj instanceof Long) {
                            registeredEventTimestamps.put(dataSnapshot.getKey(), (Long) timestampObj);
                        } else {
                            // Fallback jika 'timestamp' tidak ada atau tipe data salah
                            registeredEventTimestamps.put(dataSnapshot.getKey(), System.currentTimeMillis());
                        }
                    } else {
                        // Fallback jika node masih 'true' (bukan Map)
                        registeredEventTimestamps.put(dataSnapshot.getKey(), System.currentTimeMillis());
                    }
                }
                Log.d("NOTIF_DEBUG", "Registered Event IDs with Timestamps: " + registeredEventTimestamps.keySet());

                loadAllEventsFromDb(registeredEventTimestamps, true); // True karena user login
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FIREBASE", "Database Error (Registered Events): " + error.getMessage());
                loadAllEventsFromDb(new HashMap<>(), false); // Coba muat event baru saja jika error
            }
        });
    }

    private void loadOnlyNewEvents() {
        loadAllEventsFromDb(new HashMap<>(), false); // False karena tidak ada user login
    }


    private void loadAllEventsFromDb(HashMap<String, Long> registeredEventTimestamps, boolean userIsLoggedIn) {
        eventsDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tempNewEventsList.clear();
                tempFollowedEventsList.clear();
                notificationList.clear(); // Clear list utama

                SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());



                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    N_EventModel event = dataSnapshot.getValue(N_EventModel.class);
                    if (event != null) {
                        event.setKey(dataSnapshot.getKey());

                        if (userIsLoggedIn && registeredEventTimestamps.containsKey(event.getKey())) {
                            // Ini adalah event yang didaftar oleh user
                            Long registrationTimestamp = registeredEventTimestamps.get(event.getKey());
                            String formattedTimestamp = (registrationTimestamp != null) ? timeFormatter.format(new Date(registrationTimestamp)) : "Baru saja";

                            // Notifikasi Pendaftaran Berhasil
                            tempFollowedEventsList.add(new J_NotificationItem(
                                    J_NotificationItem.TYPE_NOTIFICATION,
                                    "🎉 Pendaftaran Berhasil: " + event.getName(),
                                    "Selamat! Kamu berhasil terdaftar di event '" + event.getName() + "'. Siap-siap untuk petualangan seru!",
                                    formattedTimestamp,
                                    false, // isRead
                                    event.getDate(), // eventDateString
                                    (registrationTimestamp != null) ? registrationTimestamp : System.currentTimeMillis() // rawTimestamp
                            ));

                            // Notifikasi Countdown & Hari H & Selesai (berdasarkan eventDate)
                            try {
                                String kategoriWaktu = getKategoriTanggal(event.getDate());
                                String uniqueMessage = ""; // Pesan unik untuk countdown
                                String title = "";
                                long currentNotifTimestamp = System.currentTimeMillis(); // Timestamp saat notifikasi ini dibuat/diproses
                                String currentFormattedTime = timeFormatter.format(new Date(currentNotifTimestamp));


                                switch (kategoriWaktu) {
                                    case "Hari Ini":
                                        title = "🔔 HARI INI: " + event.getName() + " Dimulai!";
                                        uniqueMessage = "Waktunya beraksi! Event '" + event.getName() + "' siap menanti kehadiranmu. Jangan sampai terlewat!";
                                        break;
                                    case "Besok":
                                        title = "⏳ Tinggal 1 Hari Lagi: " + event.getName();
                                        uniqueMessage = "Hitungan mundur terakhir! Besok adalah harinya '" + event.getName() + "'. Persiapkan dirimu!";
                                        break;
                                    case "Mendatang (7 Hari)":
                                        title = "🗓️ " + Math.abs(TimeUnit.DAYS.convert(sdf.parse(event.getDate()).getTime() - System.currentTimeMillis(), TimeUnit.MILLISECONDS)) + " Hari Menuju: " + event.getName();
                                        uniqueMessage = "Sudah tidak sabar? Tinggal beberapa hari lagi menuju event '" + event.getName() + "'. Tetap semangat!";
                                        break;
                                    case "7 Hari Terakhir": // Event yang didaftar dan sudah lewat tapi dalam 7 hari
                                        title = "Event Selesai: " + event.getName();
                                        uniqueMessage = "Event '" + event.getName() + "' sudah selesai. Terima kasih telah berpartisipasi!";
                                        break;
                                    case "Kemarin":
                                        title = "Event Selesai: " + event.getName();
                                        uniqueMessage = "Event '" + event.getName() + "' sudah selesai. Terima kasih telah berpartisipasi!";
                                        break;
                                    case "Lainnya (Lampau)":
                                        title = "Event Selesai: " + event.getName();
                                        uniqueMessage = "Event '" + event.getName() + "' sudah selesai. Terima kasih telah berpartisipasi!";
                                        break;
                                    default: // Termasuk "Mendatang (Jauh)" atau kasus lain yang belum spesifik
                                        // Untuk event mendatang yang belum ada pesan khusus
                                        title = "🗓️ Event Mendatang: " + event.getName();
                                        uniqueMessage = "Event ini akan segera hadir. Tetap ikuti perkembangannya!";
                                        break;
                                }

                                if (!title.isEmpty()) { // Hanya tambahkan jika ada judul
                                    tempFollowedEventsList.add(new J_NotificationItem(
                                            J_NotificationItem.TYPE_NOTIFICATION,
                                            title,
                                            uniqueMessage,
                                            currentFormattedTime, // Waktu saat ini (untuk notif countdown/selesai)
                                            false, // isRead
                                            event.getDate(), // eventDateString
                                            currentNotifTimestamp // rawTimestamp
                                    ));
                                }

                            } catch (ParseException e) {
                                Log.e("NOTIF_DEBUG", "Error parsing date for followed event: " + event.getName() + " - " + e.getMessage());
                            }

                        } else {
                            // Ini adalah event baru (belum didaftar oleh user)
                            Long eventCreationTimestamp = event.getCreationTimestamp();
                            String formattedTimestamp = (eventCreationTimestamp != null) ? timeFormatter.format(new Date(eventCreationTimestamp)) : "Baru saja";

                            tempNewEventsList.add(new J_NotificationItem(
                                    J_NotificationItem.TYPE_NOTIFICATION,
                                    "Event Baru: " + event.getName(),
                                    event.getAbout(),
                                    formattedTimestamp, // Waktu pembuatan event
                                    false, // isRead
                                    event.getDate(), // eventDateString
                                    (eventCreationTimestamp != null) ? eventCreationTimestamp : System.currentTimeMillis() // rawTimestamp
                            ));
                        }
                    }
                }
                populateRecyclerView(); // Panggil setelah semua data dikumpulkan
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FIREBASE", "Database Error (All Events): " + error.getMessage());
                // Jika error, coba populate hanya dengan data yang sudah ada (jika ada)
                populateRecyclerView();
            }
        });
    }


    private void populateRecyclerView() {
        // Gabungkan semua notifikasi ke dalam list per kategori waktu
        List<J_NotificationItem> hariIniNotifications = new ArrayList<>();
        List<J_NotificationItem> besokNotifications = new ArrayList<>();
        List<J_NotificationItem> mendatangTujuhHariNotifications = new ArrayList<>();
        List<J_NotificationItem> mendatangJauhNotifications = new ArrayList<>();
        List<J_NotificationItem> kemarinNotifications = new ArrayList<>();
        List<J_NotificationItem> tujuhHariTerakhirNotifications = new ArrayList<>();
        List<J_NotificationItem> lainnyaLampauNotifications = new ArrayList<>();
        List<J_NotificationItem> registrationNotifications = new ArrayList<>(); // Tetap pisahkan notif pendaftaran

        // Tambahkan notifikasi dari event yang diikuti
        for (J_NotificationItem item : tempFollowedEventsList) {
            if (item.getTitle().startsWith("🎉 Pendaftaran Berhasil")) {
                registrationNotifications.add(item); // Masukkan notif pendaftaran ke list khusus
            } else if (item.getEventDateString() != null && !item.getEventDateString().isEmpty()) {
                String kategoriWaktu = getKategoriTanggal(item.getEventDateString());
                switch (kategoriWaktu) {
                    case "Hari Ini":
                        hariIniNotifications.add(item);
                        break;
                    case "Besok":
                        besokNotifications.add(item);
                        break;
                    case "Mendatang (7 Hari)":
                        mendatangTujuhHariNotifications.add(item);
                        break;
                    case "Mendatang (Jauh)":
                        mendatangJauhNotifications.add(item);
                        break;
                    case "Kemarin":
                        kemarinNotifications.add(item);
                        break;
                    case "7 Hari Terakhir":
                        tujuhHariTerakhirNotifications.add(item);
                        break;
                    case "Lainnya (Lampau)":
                        lainnyaLampauNotifications.add(item);
                        break;
                    default: // Fallback for any other 'Lainnya' from getKategoriTanggal
                        lainnyaLampauNotifications.add(item);
                        break;
                }
            } else {
                lainnyaLampauNotifications.add(item); // Jika tanggal kosong, masukkan ke lainnya
            }
        }

        // Tambahkan notifikasi dari event baru
        for (J_NotificationItem item : tempNewEventsList) {
            if (item.getEventDateString() != null && !item.getEventDateString().isEmpty()) {
                String kategoriWaktu = getKategoriTanggal(item.getEventDateString());
                switch (kategoriWaktu) {
                    case "Hari Ini":
                        hariIniNotifications.add(item);
                        break;
                    case "Besok":
                        besokNotifications.add(item);
                        break;
                    case "Mendatang (7 Hari)":
                        mendatangTujuhHariNotifications.add(item);
                        break;
                    case "Mendatang (Jauh)":
                        mendatangJauhNotifications.add(item);
                        break;
                    case "Kemarin":
                        kemarinNotifications.add(item);
                        break;
                    case "7 Hari Terakhir":
                        tujuhHariTerakhirNotifications.add(item);
                        break;
                    case "Lainnya (Lampau)":
                        lainnyaLampauNotifications.add(item);
                        break;
                    default: // Fallback for any other 'Lainnya' from getKategoriTanggal
                        lainnyaLampauNotifications.add(item);
                        break;
                }
            } else {
                lainnyaLampauNotifications.add(item); // Jika tanggal kosong, masukkan ke lainnya
            }
        }

        // --- Sekarang, populate notificationList dengan header dan item yang sudah digabungkan ---
        // Urutan: Pendaftaran (khusus) -> Mendatang -> Hari Ini -> Kemarin -> 7 Hari Terakhir -> Lampau

        if (!registrationNotifications.isEmpty()) {
            notificationList.add(new J_NotificationItem(J_NotificationItem.TYPE_HEADER, "Pendaftaran Event", "", "", false));
            notificationList.addAll(registrationNotifications);
        }

        if (!besokNotifications.isEmpty()) {
            notificationList.add(new J_NotificationItem(J_NotificationItem.TYPE_HEADER, "Besok", "", "", false));
            notificationList.addAll(besokNotifications);
        }
        if (!mendatangTujuhHariNotifications.isEmpty()) {
            notificationList.add(new J_NotificationItem(J_NotificationItem.TYPE_HEADER, "Mendatang (7 Hari)", "", "", false));
            notificationList.addAll(mendatangTujuhHariNotifications);
        }
        if (!mendatangJauhNotifications.isEmpty()) {
            notificationList.add(new J_NotificationItem(J_NotificationItem.TYPE_HEADER, "Mendatang (Jauh)", "", "", false));
            notificationList.addAll(mendatangJauhNotifications);
        }
        if (!hariIniNotifications.isEmpty()) {
            notificationList.add(new J_NotificationItem(J_NotificationItem.TYPE_HEADER, "Hari Ini", "", "", false));
            notificationList.addAll(hariIniNotifications);
        }
        if (!kemarinNotifications.isEmpty()) {
            notificationList.add(new J_NotificationItem(J_NotificationItem.TYPE_HEADER, "Kemarin", "", "", false));
            notificationList.addAll(kemarinNotifications);
        }
        if (!tujuhHariTerakhirNotifications.isEmpty()) {
            notificationList.add(new J_NotificationItem(J_NotificationItem.TYPE_HEADER, "7 Hari Terakhir", "", "", false));
            notificationList.addAll(tujuhHariTerakhirNotifications);
        }
        if (!lainnyaLampauNotifications.isEmpty()) {
            notificationList.add(new J_NotificationItem(J_NotificationItem.TYPE_HEADER, "Lainnya (Lampau)", "", "", false));
            notificationList.addAll(lainnyaLampauNotifications);
        }

        for (J_NotificationItem item : notificationList) {
            Log.d("NOTIF_ITEM", "Title: " + item.getTitle() + " | Tgl: " + item.getEventDateString());
        }

        adapter.notifyDataSetChanged();
        Log.d("NOTIF_CHECK", "Total item dalam list setelah gabung: " + notificationList.size());
    }

    private void populateRecyclerViewForNewEventsOnly() {
        List<J_NotificationItem> hariIniNotifications = new ArrayList<>();
        List<J_NotificationItem> besokNotifications = new ArrayList<>();
        List<J_NotificationItem> mendatangTujuhHariNotifications = new ArrayList<>();
        List<J_NotificationItem> mendatangJauhNotifications = new ArrayList<>();
        List<J_NotificationItem> kemarinNotifications = new ArrayList<>();
        List<J_NotificationItem> tujuhHariTerakhirNotifications = new ArrayList<>();
        List<J_NotificationItem> lainnyaLampauNotifications = new ArrayList<>();


        for (J_NotificationItem item : tempNewEventsList) { // Menggunakan tempNewEventsList yang sudah diisi
            if (item.getEventDateString() != null && !item.getEventDateString().isEmpty()) {
                String kategoriWaktu = getKategoriTanggal(item.getEventDateString());
                switch (kategoriWaktu) {
                    case "Hari Ini":
                        hariIniNotifications.add(item);
                        break;
                    case "Besok":
                        besokNotifications.add(item);
                        break;
                    case "Mendatang (7 Hari)":
                        mendatangTujuhHariNotifications.add(item);
                        break;
                    case "Mendatang (Jauh)":
                        mendatangJauhNotifications.add(item);
                        break;
                    case "Kemarin":
                        kemarinNotifications.add(item);
                        break;
                    case "7 Hari Terakhir":
                        tujuhHariTerakhirNotifications.add(item);
                        break;
                    case "Lainnya (Lampau)":
                        lainnyaLampauNotifications.add(item);
                        break;
                    default:
                        lainnyaLampauNotifications.add(item);
                        break;
                }
            } else {
                lainnyaLampauNotifications.add(item);
            }
        }

        notificationList.clear(); // Bersihkan list utama sebelum mengisi ulang

        if (!besokNotifications.isEmpty()) {
            notificationList.add(new J_NotificationItem(J_NotificationItem.TYPE_HEADER, "Besok", "", "", false));
            notificationList.addAll(besokNotifications);
        }
        if (!mendatangTujuhHariNotifications.isEmpty()) {
            notificationList.add(new J_NotificationItem(J_NotificationItem.TYPE_HEADER, "Mendatang (7 Hari)", "", "", false));
            notificationList.addAll(mendatangTujuhHariNotifications);
        }
        if (!mendatangJauhNotifications.isEmpty()) {
            notificationList.add(new J_NotificationItem(J_NotificationItem.TYPE_HEADER, "Mendatang (Jauh)", "", "", false));
            notificationList.addAll(mendatangJauhNotifications);
        }
        if (!hariIniNotifications.isEmpty()) {
            notificationList.add(new J_NotificationItem(J_NotificationItem.TYPE_HEADER, "Hari Ini", "", "", false));
            notificationList.addAll(hariIniNotifications);
        }
        if (!kemarinNotifications.isEmpty()) {
            notificationList.add(new J_NotificationItem(J_NotificationItem.TYPE_HEADER, "Kemarin", "", "", false));
            notificationList.addAll(kemarinNotifications);
        }
        if (!tujuhHariTerakhirNotifications.isEmpty()) {
            notificationList.add(new J_NotificationItem(J_NotificationItem.TYPE_HEADER, "7 Hari Terakhir", "", "", false));
            notificationList.addAll(tujuhHariTerakhirNotifications);
        }
        if (!lainnyaLampauNotifications.isEmpty()) {
            notificationList.add(new J_NotificationItem(J_NotificationItem.TYPE_HEADER, "Lainnya (Lampau)", "", "", false));
            notificationList.addAll(lainnyaLampauNotifications);
        }

        adapter.notifyDataSetChanged();
        Log.d("NOTIF_CHECK", "Total item dalam list (hanya event baru): " + notificationList.size());
    }


    private String getKategoriTanggal(String tanggalEvent) {
        // Asumsikan tanggal masuk seperti "2025-06-13"
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());

        try {
            Date eventDate = sdf.parse(tanggalEvent);
            Date today = new Date();

            // Format ulang jadi "13 Juni 2025" (tanpa jam, dengan locale ID)
            SimpleDateFormat dayFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
            Date todayStartOfDay = dayFormat.parse(dayFormat.format(today));
            Date eventDateStartOfDay = dayFormat.parse(dayFormat.format(eventDate));

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
            Log.e("NotificationActivity", "Error parsing date: " + tanggalEvent + " - " + e.getMessage());
            return "Lainnya";
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            long currentTimestamp = System.currentTimeMillis();
            String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(currentTimestamp));

            J_NotificationItem newItem = new J_NotificationItem(J_NotificationItem.TYPE_NOTIFICATION, "Upload Gambar", "Admin menambahkan gambar", currentTime, false, null, currentTimestamp);
            notificationList.add(1, newItem);
            adapter.notifyItemInserted(1);
        }
    }

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

            notificationList.add(new J_NotificationItem(J_NotificationItem.TYPE_NOTIFICATION, title, desc, currentTime, false, null, currentTimestamp));
            adapter.notifyItemInserted(notificationList.size() - 1);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}