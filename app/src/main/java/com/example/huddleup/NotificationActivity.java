package com.example.huddleup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<NotificationItem> notificationList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification);

        recyclerView = findViewById(R.id.recyclerNotifikasi);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Isi data notifikasi
        notificationList = new ArrayList<>();
        // Grup: Hari Ini
        notificationList.add(new NotificationItem(NotificationItem.TYPE_HEADER, "Hari Ini", "", "", false));
        notificationList.add(new NotificationItem(NotificationItem.TYPE_NOTIFICATION, "Event Baru!", "Upcoming Concert dari FISIP!", "1 jam lalu", false));
        notificationList.add(new NotificationItem(NotificationItem.TYPE_NOTIFICATION, "Pameran Startup Mahasiswa", "Datang dan dukung inovasi teman-teman di Samantha Krida!", "1 jam lalu", false));
        notificationList.add(new NotificationItem(NotificationItem.TYPE_NOTIFICATION, "Lomba Fotografi", "Capture UB Moment, hadiah total jutaan rupiah!", "3 jam lalu", false));

        // Grup: Kemarin
        notificationList.add(new NotificationItem(NotificationItem.TYPE_HEADER, "Kemarin", "", "", false));
        notificationList.add(new NotificationItem(NotificationItem.TYPE_NOTIFICATION, "Diskusi Publik", "Tema: 'Masa Depan Demokrasi Digital', di Gedung Widyaloka.", "Kemarin", true));
        notificationList.add(new NotificationItem(NotificationItem.TYPE_NOTIFICATION, "Donor Darah", "PMI hadir di FKUB, sukses kumpulkan 200 kantong darah!", "Kemarin", false));


        // Grup: 7 Hari Terakhir
        notificationList.add(new NotificationItem(NotificationItem.TYPE_HEADER, "7 Hari Terakhir", "", "", false));
        notificationList.add(new NotificationItem(NotificationItem.TYPE_NOTIFICATION, "UB Bersih-bersih", "Aksi peduli lingkungan: bersih kampus bareng!", "2 hari lalu", false));
        notificationList.add(new NotificationItem(NotificationItem.TYPE_NOTIFICATION, "Pemilihan BEM", "Debat kandidat BEM Universitas berlangsung panas!", "4 hari lalu", true));
        notificationList.add(new NotificationItem(NotificationItem.TYPE_NOTIFICATION, "Pemutaran Film", "Film pendek karya mahasiswa FIB tayang di Perpustakaan Pusat.", "6 hari lalu", false));




        Log.d("DEBUG", "Total Notifikasi: " + notificationList.size());

        adapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        // Tambahkan ItemTouchHelper ke RecyclerView setelah adapter disetting
        NotificationAdapter.attachItemTouchHelper(recyclerView, adapter);

        FloatingActionButton fabUpload = findViewById(R.id.fabUpload);
        fabUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 100);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            // Tambahkan notifikasi baru berisi gambar yang diupload
            NotificationItem newItem = new NotificationItem(NotificationItem.TYPE_NOTIFICATION, "Upload Gambar", "Admin menambahkan gambar", "Baru saja", false);
            notificationList.add(1, newItem); // masuk setelah header
            adapter.notifyItemInserted(1);
        }
    }


}
