package com.example.huddleup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class AddEventActivity extends AppCompatActivity {

    private EditText etName, etDate, etLocation, etCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        etName = findViewById(R.id.etName);
        etDate = findViewById(R.id.etDate);
        etLocation = findViewById(R.id.etLocation);
        etCategory = findViewById(R.id.etCategory);

        Button btnSave = findViewById(R.id.btnSaveEvent);
        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString();
            String date = etDate.getText().toString();
            String location = etLocation.getText().toString();
            String category = etCategory.getText().toString();

            Intent result = new Intent();
            result.putExtra("name", name);
            result.putExtra("date", date);
            result.putExtra("location", location);
            result.putExtra("category", category);

            setResult(Activity.RESULT_OK, result);
            finish();
        });
    }
}
