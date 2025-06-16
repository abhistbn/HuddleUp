package com.example.huddleup;

import android.content.Intent;
import android.os.Bundle;
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

public class N_SigninActivity extends AppCompatActivity {
    Button btns_sigin;
    EditText etsEmail, etsPassword, etsConfirmPassword;
    TextView tvsBack;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.n_activity_signin);

        mAuth = FirebaseAuth.getInstance();

        btns_sigin = findViewById(R.id.btns_sigin);
        etsEmail = findViewById(R.id.ets_email);
        etsPassword = findViewById(R.id.ets_password);
        etsConfirmPassword = findViewById(R.id.ets_confirm_password);
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
                String confirmPassword = etsConfirmPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(N_SigninActivity.this, "Harap isi semua field", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    Toast.makeText(N_SigninActivity.this, "Password tidak cocok", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(N_SigninActivity.this, "Pendaftaran Berhasil", Toast.LENGTH_SHORT).show();

                                    FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
                                        @Override
                                        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                                            if (firebaseAuth.getCurrentUser() == null) {
                                                firebaseAuth.removeAuthStateListener(this);

                                                Intent intent = new Intent(N_SigninActivity.this, N_LoginActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }
                                    };

                                    FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
                                    FirebaseAuth.getInstance().signOut();

                                } else {
                                    Toast.makeText(N_SigninActivity.this, "Pendaftaran Gagal: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });
    }
}