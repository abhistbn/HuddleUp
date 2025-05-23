package com.example.huddleup;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class EventKu extends AppCompatActivity {

    static ArrayList<EventModel> eventKuList = new ArrayList<>();
    RecyclerView recyclerView;
    EventAdapterMyEvent adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_ku);

        recyclerView = findViewById(R.id.recyclerEventKu);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Dummy data event – kalau tidak ada event dari intent, bisa pakai ini dulu
        eventKuList.clear(); // pastikan tidak menumpuk saat reload
        eventKuList.add(new EventModel("Tech Conference", "13 April 2025", "09.00 - 15.00", "Jakarta", "Konferensi teknologi tahunan"));
        eventKuList.add(new EventModel("UI/UX Workshop", "20 April 2025", "10.00 - 12.00", "Online (Zoom)", "Belajar dasar desain UI/UX"));
        eventKuList.add(new EventModel("Startup Pitch", "27 April 2025", "13.00 - 16.00", "Bandung", "Presentasi ide startup oleh mahasiswa"));

        adapter = new EventAdapterMyEvent(eventKuList, null);
        recyclerView.setAdapter(adapter);
    }
}
