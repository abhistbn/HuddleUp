package com.example.huddleup;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class EventKu extends AppCompatActivity {

    static ArrayList<EventModel> eventKuList = new ArrayList<>();
    RecyclerView recyclerView;
    EventAdapter adapter;
    List<EventModel> eventList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_ku);

        recyclerView = findViewById(R.id.recyclerEventKu);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Intent i = getIntent();
//        if (i != null && i.hasExtra("title")) {
//            EventModel event = new EventModel(
//                    i.getStringExtra("title"),
//                    i.getStringExtra("date"),
//                    i.getStringExtra("time"),
//                    i.getStringExtra("location"),
//                    i.getStringExtra("about")
//            );
//            eventKuList.add(event);
//        }

        eventKuList.add(new EventModel("Tech Conference", "13 April 2025", "09.00 - 15.00", "Jakarta", "Konferensi teknologi tahunan"));
        eventKuList.add(new EventModel("UI/UX Workshop", "20 April 2025", "10.00 - 12.00", "Online (Zoom)", "Belajar dasar desain UI/UX"));
        eventKuList.add(new EventModel("Startup Pitch", "27 April 2025", "13.00 - 16.00", "Bandung", "Presentasi ide startup oleh mahasiswa"));


        adapter = new EventAdapter(eventKuList, null);
        recyclerView.setAdapter(adapter);
    }
}