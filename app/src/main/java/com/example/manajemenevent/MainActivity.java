package com.example.manajemenevent;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_ADD_EVENT = 1;
    private static final int REQUEST_CODE_EDIT_EVENT = 2;
    private ArrayList<String> eventList;
    private ArrayAdapter<String> adapter;
    private int selectedEventIndex; // Untuk menyimpan index event yang dipilih

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inisialisasi daftar event
        eventList = new ArrayList<>();
        eventList.add("Event 1");
        eventList.add("Event 2");

        // Setup ListView
        ListView eventListView = findViewById(R.id.eventListView);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, eventList);
        eventListView.setAdapter(adapter);

        // Daftarkan menu kontekstual untuk ListView
        registerForContextMenu(eventListView);

        // Setup Tombol Tambah Event
        Button btnAddEvent = findViewById(R.id.btnAddEvent);
        btnAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EventFormActivity.class);
                startActivityForResult(intent, REQUEST_CODE_ADD_EVENT);
            }
        });

        // Handle klik item pada ListView
        eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedEventIndex = position;
                view.showContextMenu(); // Tampilkan menu kontekstual saat item ditekan
            }
        });
    }

    // Buat menu kontekstual untuk ListView
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Aksi Event");
        menu.add(0, 1, 0, "Edit");
        menu.add(0, 2, 0, "Hapus");
    }

    // Tangani aksi pada menu kontekstual
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case 1: // Edit event
                Intent intent = new Intent(MainActivity.this, EventFormActivity.class);
                intent.putExtra("EVENT_NAME", eventList.get(selectedEventIndex));
                startActivityForResult(intent, REQUEST_CODE_EDIT_EVENT);
                return true;
            case 2: // Hapus event
                eventList.remove(selectedEventIndex);
                adapter.notifyDataSetChanged(); // Update ListView
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    // Tangani hasil dari EventFormActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            String eventName = data.getStringExtra("NEW_EVENT");
            if (requestCode == REQUEST_CODE_ADD_EVENT) {
                // Tambah event baru
                eventList.add(eventName);
            } else if (requestCode == REQUEST_CODE_EDIT_EVENT) {
                // Edit event
                eventList.set(selectedEventIndex, eventName);
            }
            adapter.notifyDataSetChanged(); // Update ListView
        }
    }
}
