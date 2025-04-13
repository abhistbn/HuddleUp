package com.example.huddleup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class detailEvent extends AppCompatActivity {

    TextView txtJudul, txtTanggal, txtWaktu, txtLokasi, txtAbout;
    Button btnDaftar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_event);

        txtJudul = findViewById(R.id.txtJudul);
        txtTanggal = findViewById(R.id.txtTanggal);
        txtWaktu = findViewById(R.id.txtWaktu);
        txtLokasi = findViewById(R.id.txtLokasi);
        txtAbout = findViewById(R.id.txtAbout);
        btnDaftar = findViewById(R.id.btn_daftar);

//        Intent i = getIntent();
//        String title = i.getStringExtra("title");
//        String date = i.getStringExtra("date");
//        String time = i.getStringExtra("time");
//        String location = i.getStringExtra("location");
//        String about = i.getStringExtra("about");

        String title = "Tech Conference";
        String date = "13 April 2025";
        String time = "09.00 - 15:00";
        String location = "Jakarta";
        String about = "Acara kumpul bareng komunitas teknologi untuk sharing, networking, dan belajar bareng.";


        txtJudul.setText(title);
        txtTanggal.setText(date);
        txtWaktu.setText(time);
        txtLokasi.setText(location);
        txtAbout.setText(about);

        btnDaftar.setOnClickListener(v -> {
            Intent intent = new Intent(detailEvent.this, EventKu.class);
            intent.putExtra("title", title);
            intent.putExtra("date", date);
            intent.putExtra("time", time);
            intent.putExtra("location", location);
            intent.putExtra("about", about);
            startActivity(intent);
            finish();
        });

    }
}