package com.example.huddleup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class N_LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    TextView tvBack;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.n_activity_login);

        etEmail = findViewById(R.id.etlo_email);
        etPassword = findViewById(R.id.etlo_password);
        btnLogin = findViewById(R.id.btnlo_login);
        tvBack = findViewById(R.id.tvlo_Back);

        tvBack.setOnClickListener(v -> {
            Intent intent = new Intent(N_LoginActivity.this, N_LauncherActivity.class);
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(N_LoginActivity.this, "Email dan Password tidak boleh kosong", Toast.LENGTH_SHORT).show();
            } else {
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(N_LoginActivity.this, "Berhasil Log In", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(N_LoginActivity.this, Z_MainActivity.class); // <-- FIXED
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(N_LoginActivity.this, "Email atau Password salah", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }
}

