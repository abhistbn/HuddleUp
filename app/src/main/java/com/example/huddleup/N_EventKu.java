package com.example.huddleup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class N_EventKu extends AppCompatActivity implements N_EventAdapterMyEvent.OnMyEventListener {

    private RecyclerView recyclerView;
    private N_EventAdapterMyEvent adapter;
    private ArrayList<N_EventModel> eventKuList;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.n_activity_event_ku);

        recyclerView = findViewById(R.id.recyclerEventKu);
        database = FirebaseDatabase.getInstance().getReference("registered_events");

        eventKuList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new N_EventAdapterMyEvent(this, eventKuList, this);
        recyclerView.setAdapter(adapter);

        fetchRegisteredEvents();
    }

    private void fetchRegisteredEvents() {
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                eventKuList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    N_EventModel event = snapshot.getValue(N_EventModel.class);
                    if (event != null) {
                        event.setKey(snapshot.getKey());
                        eventKuList.add(event);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(N_EventKu.this, "Gagal memuat data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBatalClick(int position) {
        String eventKey = eventKuList.get(position).getKey();

        database.child(eventKey).removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(N_EventKu.this, "Pendaftaran dibatalkan", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(N_EventKu.this, "Gagal membatalkan", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onLihatTiketClick(N_EventModel event) {
        Intent intent = new Intent(N_EventKu.this, N_EventCheckedInActivity.class);
        intent.putExtra("judul", event.getJudul());
        intent.putExtra("tanggal", event.getTanggal());
        intent.putExtra("waktu", event.getWaktu());
        intent.putExtra("lokasi", event.getLokasi());
        startActivity(intent);
    }
}