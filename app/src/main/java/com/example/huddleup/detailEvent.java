package com.example.huddleup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class detailEvent extends AppCompatActivity {

    Button btnDaftar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_event);

        btnDaftar = findViewById(R.id.btn_daftar);
        btnDaftar.setOnClickListener(v -> {
            Intent intent = new Intent(detailEvent.this, EventCheckedInActivity.class);
            intent.putExtra("event_title", "Workshop Penulisan Artikel Ilmiah");
            startActivity(intent);
            finish();
        });
    }
}