package com.example.huddleup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EventCheckedInActivity extends AppCompatActivity {
    TextView tvdBack, textViewEventTitle, textViewStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_checked_in);

        textViewEventTitle = findViewById(R.id.textViewEventTitle);
        textViewStatus = findViewById(R.id.textViewStatus);
        tvdBack = findViewById(R.id.tvdBack);

        Intent intent = getIntent();
        String eventTitle = intent.getStringExtra("event_title");

        textViewEventTitle.setText(eventTitle);
        textViewStatus.setText("✅ Anda sudah terdaftar dalam event ini");


    }
}