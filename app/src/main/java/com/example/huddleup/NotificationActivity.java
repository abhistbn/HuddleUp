package com.example.huddleup;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.huddleup.NotificationItem;
import com.example.huddleup.R;

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
        notificationList.add(new NotificationItem("Event Baru!", "Upcoming Concert dari FISIP! penasaran siapa guest starnya?", "1 jam lalu"));
        notificationList.add(new NotificationItem("Art Exhibition Date", "Buat kamu yang mau modus sama doi", "2 jam lalu"));
        notificationList.add(new NotificationItem("Update!", "Acara Marathon Brawijaya diundur", "5 jam lalu"));

        Log.d("DEBUG", "Total Notifikasi: " + notificationList.size());

        adapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }
}
