//package com.example.huddleup;
//
//import android.os.Bundle;
//import android.widget.TextView;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class eventdashboard extends AppCompatActivity {
//
//    private RecyclerView recyclerView;
//    private EventAdapter eventAdapter;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_event_dashboard);
//
//        // Inisialisasi RecyclerView
//        recyclerView = findViewById(R.id.rvEventList);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//
//        // Set nama event dan kategori
//        TextView tvEventName = findViewById(R.id.tvEventName);
//        TextView tvEventCategory = findViewById(R.id.tvEventCategory);
//        tvEventName.setText("Festival");
//        tvEventCategory.setText("Kategori: Musik");
//
//        // Ambil data event (data statis)
//        List<Event> events = getEventData();
//
//        // Inisialisasi adapter dan set ke RecyclerView
//        eventAdapter = new EventAdapter(events);
//        recyclerView.setAdapter(eventAdapter);
//    }
//
//    // Method untuk mengambil data event (data statis)
//    private List<Event> getEventData() {
//        List<Event> events = new ArrayList<>();
//        events.add(new Event("Concert", 120));
//        events.add(new Event("DJ Performance", 200));
//        events.add(new Event("Music Workshop", 80));
//        events.add(new Event("Rock Band Show", 150));
//        return events;
//    }
//}
