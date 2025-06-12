package com.example.huddleup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class N_detailEvent extends AppCompatActivity {

    TextView txtJudul, txtTanggal, txtWaktu, txtLokasi, txtAbout;
    Button btnDaftar;
    private N_EventModel currentEvent;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.n_activity_detail_event);

        database = FirebaseDatabase.getInstance().getReference("registered_events");

        txtJudul = findViewById(R.id.txtJudul);
        txtTanggal = findViewById(R.id.txtTanggal);
        txtWaktu = findViewById(R.id.txtWaktu);
        txtLokasi = findViewById(R.id.txtLokasi);
        txtAbout = findViewById(R.id.txtAbout);
        btnDaftar = findViewById(R.id.btn_daftar);

        Intent i = getIntent();
        if (i != null && i.hasExtra("title")) {
            String title = i.getStringExtra("title");
            String date = i.getStringExtra("date");
            String time = i.getStringExtra("time");
            String location = i.getStringExtra("location");
            String about = i.getStringExtra("about");

            currentEvent = new N_EventModel(title, date, time, location, about);

            txtJudul.setText(currentEvent.getJudul());
            txtTanggal.setText(currentEvent.getTanggal());
            txtWaktu.setText(currentEvent.getWaktu());
            txtLokasi.setText(currentEvent.getLokasi());
            txtAbout.setText(currentEvent.getAbout());

        } else {
            Toast.makeText(this, "Error: Data event tidak ditemukan.", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnDaftar.setOnClickListener(v -> {
            if (currentEvent != null) {
                database.push().setValue(currentEvent)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(N_detailEvent.this, "Berhasil mendaftar!", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(N_detailEvent.this, N_EventCheckedInActivity.class);
                            intent.putExtra("judul", currentEvent.getJudul());
                            intent.putExtra("tanggal", currentEvent.getTanggal());
                            intent.putExtra("waktu", currentEvent.getWaktu());
                            intent.putExtra("lokasi", currentEvent.getLokasi());
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(N_detailEvent.this, "Gagal mendaftar, coba lagi.", Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }
}