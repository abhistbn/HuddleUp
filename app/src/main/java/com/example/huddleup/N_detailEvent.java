package com.example.huddleup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class N_detailEvent extends AppCompatActivity {

    TextView txtJudul, txtTanggal, txtWaktu, txtLokasi, txtAbout, tvde_Back;
    Button btnDaftar;
    ImageView imgEvent;
    private N_EventModel currentEvent;
    private String eventKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.n_activity_detail_event);

        txtJudul = findViewById(R.id.txtJudul);
        txtTanggal = findViewById(R.id.txtTanggal);
        txtWaktu = findViewById(R.id.txtWaktu);
        txtLokasi = findViewById(R.id.txtLokasi);
        txtAbout = findViewById(R.id.txtAbout);
        btnDaftar = findViewById(R.id.btn_daftar);
        imgEvent = findViewById(R.id.imgEvent);
        tvde_Back = findViewById(R.id.tvde_Back);

        Intent i = getIntent();
        if (i != null && i.hasExtra("event_key")) {
            eventKey = i.getStringExtra("event_key");
            String title = i.getStringExtra("title");
            String date = i.getStringExtra("date");
            String time = i.getStringExtra("time");
            String location = i.getStringExtra("location");
            String about = i.getStringExtra("about");
            String imageUrl = i.getStringExtra("image_url");

            currentEvent = new N_EventModel(title, date, time, location, about, imageUrl);
            currentEvent.setKey(eventKey);

            txtJudul.setText(currentEvent.getJudul());
            txtTanggal.setText(currentEvent.getTanggal());
            txtWaktu.setText(currentEvent.getWaktu());
            txtLokasi.setText(currentEvent.getLokasi());
            txtAbout.setText(currentEvent.getAbout());

            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.event)
                        .error(R.drawable.event)
                        .into(imgEvent);
            }
            checkRegistrationStatus();
        } else {
            Toast.makeText(this, "Error: Data event tidak lengkap.", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnDaftar.setOnClickListener(v -> registerEvent());
        tvde_Back.setOnClickListener(v -> {
            onBackPressed();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (eventKey != null) {
            checkRegistrationStatus();
        }
    }

    private void checkRegistrationStatus() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || eventKey == null || eventKey.isEmpty()) {
            btnDaftar.setVisibility(View.GONE);
            return;
        }
        String userId = currentUser.getUid();
        DatabaseReference userEventRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("registered_events")
                .child(eventKey);

        userEventRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    btnDaftar.setVisibility(View.GONE);
                } else {
                    btnDaftar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                btnDaftar.setVisibility(View.VISIBLE);
                Toast.makeText(N_detailEvent.this, "Gagal memeriksa status registrasi.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerEvent() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Anda harus login untuk mendaftar", Toast.LENGTH_SHORT).show();
            return;
        }
        if (eventKey == null || eventKey.isEmpty()) {
            Toast.makeText(this, "Gagal mendapatkan ID event.", Toast.LENGTH_SHORT).show();
            return;
        }
        btnDaftar.setEnabled(false);

        String userId = currentUser.getUid();
        DatabaseReference userEventRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("registered_events")
                .child(eventKey);

        N_RegistrationInfo registrationData = new N_RegistrationInfo("Telah Terdaftar", System.currentTimeMillis());

        userEventRef.setValue(registrationData).addOnSuccessListener(aVoid -> {
            Toast.makeText(N_detailEvent.this, "Berhasil mendaftar!", Toast.LENGTH_SHORT).show();
            btnDaftar.setVisibility(View.GONE);

            Intent intent = new Intent(N_detailEvent.this, N_EventCheckedInActivity.class);
            intent.putExtra("judul", currentEvent.getJudul());
            intent.putExtra("tanggal", currentEvent.getTanggal());
            intent.putExtra("waktu", currentEvent.getWaktu());
            intent.putExtra("lokasi", currentEvent.getLokasi());
            startActivity(intent);

        }).addOnFailureListener(e -> {
            Toast.makeText(N_detailEvent.this, "Gagal mendaftar.", Toast.LENGTH_SHORT).show();
            btnDaftar.setEnabled(true);
        });
    }
}