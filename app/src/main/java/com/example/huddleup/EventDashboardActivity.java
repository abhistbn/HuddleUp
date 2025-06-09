package com.example.huddleup;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class EventDashboardActivity extends AppCompatActivity {

    private final List<Event> events = new ArrayList<>();
    private EventAdapter eventAdapter;

    private final ActivityResultLauncher<Intent> addEventLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String name = result.getData().getStringExtra("name");
                    String date = result.getData().getStringExtra("date");
                    String location = result.getData().getStringExtra("location");
                    String category = result.getData().getStringExtra("category");

                    events.add(new Event(name, date, location, category));
                    eventAdapter.notifyItemInserted(events.size() - 1);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_dashboard);

        RecyclerView recyclerView = findViewById(R.id.rvEventList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Data dummy awal
        events.add(new Event("Jazz Night", "10/06/2025", "Taman Kota", "Musik"));
        events.add(new Event("Tech Fair", "15/06/2025", "Jakarta Convention Center", "Teknologi"));

        // Pasang adapter dengan listener hapus event
        eventAdapter = new EventAdapter(events, position -> {
            events.remove(position);
            eventAdapter.notifyItemRemoved(position);
        });
        recyclerView.setAdapter(eventAdapter);

        FloatingActionButton fabAddEvent = findViewById(R.id.fabAddEvent);
        fabAddEvent.setOnClickListener(v -> addEventLauncher.launch(new Intent(this, AddEventActivity.class)));
    }
}
