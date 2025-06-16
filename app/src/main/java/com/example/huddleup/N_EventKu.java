package com.example.huddleup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    private TextView tvEmptyMessage;
    private DatabaseReference dbEvents;
    private TextView tvde_Back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.n_activity_event_ku);

        recyclerView = findViewById(R.id.recyclerEventKu);
        tvEmptyMessage = findViewById(R.id.tv_empty_my_events);
        dbEvents = FirebaseDatabase.getInstance().getReference("events");
        tvde_Back = findViewById(R.id.tvde_Back);

        eventKuList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new N_EventAdapterMyEvent(this, eventKuList, this);
        recyclerView.setAdapter(adapter);

        fetchRegisteredEvents();

        tvde_Back.setOnClickListener(v -> {
            Intent intent = new Intent(N_EventKu.this, Z_MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void fetchRegisteredEvents() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            updateUI(true);
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference userRegisteredRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("registered_events");

        userRegisteredRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                eventKuList.clear();
                if (!dataSnapshot.exists()) {
                    updateUI(true);
                } else {
                    updateUI(false);
                    for (DataSnapshot regSnapshot : dataSnapshot.getChildren()) {
                        String eventKey = regSnapshot.getKey();
                        if (eventKey != null) {
                            fetchEventDetails(eventKey, regSnapshot);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(N_EventKu.this, "Gagal memuat data event.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchEventDetails(String eventKey, DataSnapshot regSnapshot) {
        dbEvents.child(eventKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot eventDetailsSnapshot) {
                if (eventDetailsSnapshot.exists()){
                    N_EventModel event = eventDetailsSnapshot.getValue(N_EventModel.class);
                    if (event != null) {
                        event.setKey(eventDetailsSnapshot.getKey());
                        N_RegistrationInfo info = regSnapshot.getValue(N_RegistrationInfo.class);
                        event.setRegistrationInfo(info);

                        int existingIndex = -1;
                        for (int i = 0; i < eventKuList.size(); i++) {
                            if (eventKuList.get(i).getKey().equals(event.getKey())) {
                                existingIndex = i;
                                break;
                            }
                        }

                        if (existingIndex != -1) {
                            eventKuList.set(existingIndex, event);
                        } else {
                            eventKuList.add(event);
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(N_EventKu.this, "Gagal mengambil detail event: " + eventKey, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBatalClick(String eventKey) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("registered_events")
                .child(eventKey)
                .removeValue()
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

    @Override
    public void onKonfirmasiClick(String eventKey) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Harap login untuk mengonfirmasi.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference registrationRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("registered_events")
                .child(eventKey)
                .child("status");

        registrationRef.setValue("Terkonfirmasi")
                .addOnSuccessListener(aVoid -> Toast.makeText(N_EventKu.this, "Kehadiran dikonfirmasi!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(N_EventKu.this, "Gagal mengonfirmasi.", Toast.LENGTH_SHORT).show());
    }

    private void updateUI(boolean isEmpty) {
        if (isEmpty) {
            recyclerView.setVisibility(View.GONE);
            tvEmptyMessage.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmptyMessage.setVisibility(View.GONE);
        }
    }
}