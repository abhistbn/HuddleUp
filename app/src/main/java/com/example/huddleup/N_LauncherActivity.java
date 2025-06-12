package com.example.huddleup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class N_LauncherActivity extends AppCompatActivity {

    Button btnl_SigIn, btnl_LogIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.n_activity_launcher);

        btnl_SigIn = findViewById(R.id.btnl_SigIn);
        btnl_LogIn = findViewById(R.id.btnl_LogIn);

        btnl_LogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(N_LauncherActivity.this, N_LoginActivity.class);
                startActivity(intent);
            }
        });

        btnl_SigIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(N_LauncherActivity.this, N_SigninActivity.class);
                startActivity(intent);
            }
        });
    }
}