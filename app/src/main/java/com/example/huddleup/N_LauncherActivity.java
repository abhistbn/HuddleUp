package com.example.huddleup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class N_LauncherActivity extends AppCompatActivity {

    Button btnl_SigIn, btnl_LogIn;
    ImageButton btnContinueWithGoogle;

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.n_activity_launcher);

        btnl_SigIn = findViewById(R.id.btnl_SigIn);
        btnl_LogIn = findViewById(R.id.btnl_LogIn);
        btnContinueWithGoogle = findViewById(R.id.btnContinueWithGoogle);

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            firebaseAuthWithGoogle(account.getIdToken());
                        } catch (ApiException e) {
                            Toast.makeText(N_LauncherActivity.this, "Google sign in gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        btnl_LogIn.setOnClickListener(v -> {
            Intent intent = new Intent(N_LauncherActivity.this, N_LoginActivity.class);
            startActivity(intent);
        });

        btnl_SigIn.setOnClickListener(v -> {
            Intent intent = new Intent(N_LauncherActivity.this, N_SigninActivity.class);
            startActivity(intent);
        });

        btnContinueWithGoogle.setOnClickListener(v -> signIn());
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(N_LauncherActivity.this, "Login Google Berhasil!", Toast.LENGTH_SHORT).show();
                        navigateToMainActivity();
                    } else {
                        Toast.makeText(N_LauncherActivity.this, "Autentikasi Firebase Gagal.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(N_LauncherActivity.this, Z_MainActivity.class);
        startActivity(intent);
        finish();
    }
}
