package com.example.huddleup;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SiginActivity extends AppCompatActivity {

    private static final String TAG = "SiginActivity";

    Button btns_sigin;
    EditText etsName, etsEmail, etsPassword;
    TextView tvsBack;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sigin);

        mAuth = FirebaseAuth.getInstance();

        btns_sigin = findViewById(R.id.btns_sigin);
        etsName = findViewById(R.id.ets_name);
        etsEmail = findViewById(R.id.ets_email);
        etsPassword = findViewById(R.id.ets_password);
        tvsBack = findViewById(R.id.tvsBack);

        tvsBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SiginActivity.this, LauncherActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btns_sigin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etsName.getText().toString().trim();
                String email = etsEmail.getText().toString().trim();
                String password = etsPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
                    Toast.makeText(SiginActivity.this, "Harap isi semua field", Toast.LENGTH_SHORT).show();
                } else {
                    signup(email, password);
                }
            }
        });
    }

    private void signup(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Registrasi berhasil
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(SiginActivity.this, "Registrasi berhasil", Toast.LENGTH_SHORT).show();
                            updateUI(user);
                        } else {
                            // Gagal registrasi
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SiginActivity.this, "Registrasi gagal: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private boolean validateForm() {
        boolean result = true;

        if (TextUtils.isEmpty(etsEmail.getText().toString())) {
            etsEmail.setError("Email wajib diisi");
            result = false;
        } else {
            etsEmail.setError(null);
        }

        if (TextUtils.isEmpty(etsPassword.getText().toString())) {
            etsPassword.setError("Password wajib diisi");
            result = false;
        } else {
            etsPassword.setError(null);
        }

        if (TextUtils.isEmpty(etsName.getText().toString())) {
            etsName.setError("Nama wajib diisi");
            result = false;
        } else {
            etsName.setError(null);
        }

        return result;
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(SiginActivity.this, LoginActivity.class); // atau InsertNoteActivity
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(SiginActivity.this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
        }
    }


}
