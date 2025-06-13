package com.example.huddleup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class N_SigninActivity extends AppCompatActivity {
    Button btns_sigin;
    EditText etsName, etsEmail, etsPassword;
    TextView tvsBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.n_activity_signin);

        btns_sigin = findViewById(R.id.btns_sigin);
        etsName = findViewById(R.id.ets_name);
        etsEmail = findViewById(R.id.ets_email);
        etsPassword = findViewById(R.id.ets_password);
        tvsBack = findViewById(R.id.tvsBack);

        tvsBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(N_SigninActivity.this, N_LauncherActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btns_sigin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etsEmail.getText().toString().trim();
                String password = etsPassword.getText().toString().trim();
                String name = etsName.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
                    Toast.makeText(N_SigninActivity.this, "Harap isi semua field", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(N_SigninActivity.this, "Sign In Berhasil", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(N_SigninActivity.this, N_LoginActivity.class);
                    intent.putExtra("email", email);
                    intent.putExtra("password", password);
                    startActivity(intent);
                    finish();
                }
            }
        });


    }
}