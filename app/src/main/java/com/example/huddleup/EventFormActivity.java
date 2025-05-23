package com.example.huddleup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class EventFormActivity extends AppCompatActivity {
    private EditText edtEventName;
    private Button btnSave, btnChooseImage;
    private ImageView imgPreview;
    private Uri imageUri;
    private boolean isEdit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_form);

        edtEventName = findViewById(R.id.edtEventName);
        btnSave = findViewById(R.id.btnSave);
        btnChooseImage = findViewById(R.id.btnChooseImage);
        imgPreview = findViewById(R.id.imgPreview);

        Intent intent = getIntent();
        if (intent.hasExtra("EVENT_NAME")) {
            String eventName = intent.getStringExtra("EVENT_NAME");
            edtEventName.setText(eventName);
            isEdit = true;
        }

        ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageUri = uri;
                        imgPreview.setImageURI(uri);
                    }
                }
        );

        btnChooseImage.setOnClickListener(v -> {
            pickImageLauncher.launch("image/*");
        });

        btnSave.setOnClickListener(v -> {
            String eventName = edtEventName.getText().toString().trim();

            if (eventName.isEmpty()) {
                Toast.makeText(EventFormActivity.this, "Nama event tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }

            if (imageUri == null) {
                Toast.makeText(EventFormActivity.this, "Pilih poster dulu sebelum simpan", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent resultIntent = new Intent();
            resultIntent.putExtra("NEW_EVENT", eventName);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }
}
