package com.example.huddleup;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EventFormActivity extends AppCompatActivity {
    private EditText edtEventName;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_form);

        edtEventName = findViewById(R.id.edtEventName);
        btnSave = findViewById(R.id.btnSave);

        // Tangkap data event jika dalam mode edit
        Intent intent = getIntent();
        if (intent.hasExtra("EVENT_NAME")) {
            edtEventName.setText(intent.getStringExtra("EVENT_NAME"));
        }

        btnSave.setOnClickListener(v -> {
            String eventName = edtEventName.getText().toString();
            if (eventName.isEmpty()) {
                Toast.makeText(EventFormActivity.this, "Nama event tidak boleh kosong", Toast.LENGTH_SHORT).show();
            } else {
                // Kirim event kembali ke MainActivity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("NEW_EVENT", eventName);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }
}