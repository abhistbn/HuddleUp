package com.example.huddleup;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.io.OutputStream;
import java.util.Objects;

public class N_EventCheckedInActivity extends AppCompatActivity {

    private ConstraintLayout ticketLayout;
    private Button btnMyEvents;
    private TextView tvBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.n_activity_event_checked_in);

        TextView txtJudul = findViewById(R.id.txtJudul);
        TextView txtTanggal = findViewById(R.id.txtTanggal);
        TextView txtWaktu = findViewById(R.id.txtWaktu);
        TextView txtLokasi = findViewById(R.id.txtLokasi);
        ticketLayout = findViewById(R.id.ticketLayout);
        Button saveButton = findViewById(R.id.btn_saveImg);
        btnMyEvents = findViewById(R.id.btn_myEvents);
        tvBack = findViewById(R.id.tvde_Back);

        Intent intent = getIntent();
        if (intent != null) {
            txtJudul.setText(intent.getStringExtra("judul"));
            txtTanggal.setText(intent.getStringExtra("tanggal"));
            txtWaktu.setText(intent.getStringExtra("waktu"));
            txtLokasi.setText(intent.getStringExtra("lokasi"));
        }

        saveButton.setOnClickListener(v -> saveTicketAsImage());

        btnMyEvents.setOnClickListener(v -> {
            Intent intentToMyEvents = new Intent(N_EventCheckedInActivity.this, N_EventKu.class);
            intentToMyEvents.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentToMyEvents);
            finish();
        });

        tvBack.setOnClickListener(v -> {
            Intent intent2 = new Intent(N_EventCheckedInActivity.this, Z_MainActivity.class);
            startActivity(intent2);
        });
    }

    private void saveTicketAsImage() {
        Bitmap bitmap = Bitmap.createBitmap(ticketLayout.getWidth(), ticketLayout.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Drawable bgDrawable = ticketLayout.getBackground();
        if (bgDrawable != null) {
            bgDrawable.draw(canvas);
        } else {
            canvas.drawColor(Color.WHITE);
        }
        ticketLayout.draw(canvas);

        OutputStream fos = null;
        Uri imageUri = null;
        String fileName = "ticket_" + System.currentTimeMillis() + ".png";

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/HuddleUpTickets");

        try {
            imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            fos = getContentResolver().openOutputStream(Objects.requireNonNull(imageUri));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            Toast.makeText(this, "Tiket berhasil disimpan di Galeri! 🥳", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal menyimpan tiket. 😢", Toast.LENGTH_SHORT).show();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
