package com.example.huddleup;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;

import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.OutputStream;

public class N_EventCheckedInActivity extends AppCompatActivity {

    private ConstraintLayout ticketLayout;
    private Button saveButton;
    TextView txtJudul, txtTanggal, txtWaktu, txtLokasi;
    private static final int STORAGE_PERMISSION_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.n_activity_event_checked_in);

        TextView title = findViewById(R.id.txtJudul);
        TextView date = findViewById(R.id.txtTanggal);
        TextView time = findViewById(R.id.txtWaktu);
        TextView location = findViewById(R.id.txtLokasi);
        ticketLayout = findViewById(R.id.ticketLayout);
        saveButton = findViewById(R.id.btn_saveImg);

        Intent intent = getIntent();
        title.setText(intent.getStringExtra("judul"));
        date.setText(intent.getStringExtra("tanggal"));
        time.setText(intent.getStringExtra("waktu"));
        location.setText(intent.getStringExtra("lokasi"));


        saveButton.setOnClickListener(v -> saveTicketAsImage());
    }

    private void saveTicketAsImage() {
        ticketLayout.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(ticketLayout.getDrawingCache());
        ticketLayout.setDrawingCacheEnabled(false);

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "ticket_" + System.currentTimeMillis() + ".png");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/EventTicket");

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        try {
            OutputStream outStream = getContentResolver().openOutputStream(uri);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            if (outStream != null) outStream.close();
            Toast.makeText(this, "Tiket berhasil disimpan di Galeri 🥳", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal menyimpan tiket 😢", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}

