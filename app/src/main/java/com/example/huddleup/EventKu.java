package com.example.huddleup;

import android.content.Intent;
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
    List<EventModel> eventList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_ku);

        recyclerView = findViewById(R.id.recyclerEventKu);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Intent i = getIntent();
        if (i != null && i.hasExtra("title")) {
            EventModel event = new EventModel(
                    i.getStringExtra("title"),
                    i.getStringExtra("date"),
                    i.getStringExtra("time"),
                    i.getStringExtra("location"),
                    i.getStringExtra("about")
            );
            eventKuList.add(event);
        }


        adapter = new EventAdapterMyEvent (eventKuList, null);
        recyclerView.setAdapter(adapter);
    }
}