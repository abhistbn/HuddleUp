package com.example.huddleup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class D_WishlistActivity extends AppCompatActivity implements D_WishlistAdapter.OnItemClickListener {

    private RecyclerView rvWishlist;
    private D_WishlistAdapter adapter;
    private ArrayList<Z_EventP2> wishlistItemList; // Menggunakan model Z_EventP2
    private TextView tvEmptyWishlist;
    private Toolbar toolbar;
    private DatabaseReference wishlistRef;
    private ValueEventListener eventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.d_activity_wishlist);

        initViews();
        setupToolbar();
        setupRecyclerView();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            wishlistRef = FirebaseDatabase.getInstance().getReference("wishlist").child(userId);
            fetchWishlistData();
        } else {
            Toast.makeText(this, "Silakan login untuk melihat wishlist", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        rvWishlist = findViewById(R.id.rvWishlist);
        tvEmptyWishlist = findViewById(R.id.tvEmptyWishlist);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        wishlistItemList = new ArrayList<>();
        adapter = new D_WishlistAdapter(this, wishlistItemList);
        adapter.setOnItemClickListener(this);
        rvWishlist.setLayoutManager(new LinearLayoutManager(this));
        rvWishlist.setAdapter(adapter);
    }

    private void fetchWishlistData() {
        eventListener = wishlistRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                wishlistItemList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    // Konversi data dari Firebase ke objek Z_EventP2
                    Z_EventP2 event = dataSnapshot.getValue(Z_EventP2.class);
                    if (event != null) {
                        wishlistItemList.add(event);
                    }
                }
                adapter.notifyDataSetChanged();
                checkIfListEmpty();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(D_WishlistActivity.this, "Gagal memuat wishlist.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfListEmpty() {
        if (wishlistItemList.isEmpty()) {
            rvWishlist.setVisibility(View.GONE);
            tvEmptyWishlist.setVisibility(View.VISIBLE);
        } else {
            rvWishlist.setVisibility(View.VISIBLE);
            tvEmptyWishlist.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(Z_EventP2 event) {
        Intent intent = new Intent(this, D_WishlistDetailActivity.class);
        intent.putExtra("WISHLIST_EVENT_DETAIL", event); // Kirim seluruh objek
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Z_EventP2 event, int position) {
        // Tampilkan dialog konfirmasi sebelum menghapus
        new AlertDialog.Builder(this)
                .setTitle("Hapus dari Wishlist")
                .setMessage("Yakin ingin menghapus '" + event.getName() + "' dari wishlist?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    // Hapus data dari Firebase
                    if (wishlistRef != null && event.getId() != null) {
                        wishlistRef.child(event.getId()).removeValue();
                        Toast.makeText(this, "Berhasil dihapus", Toast.LENGTH_SHORT).show();
                        // Data akan otomatis ter-refresh karena kita menggunakan addValueEventListener
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Penting: Hapus listener untuk menghindari memory leak saat activity ditutup
        if (wishlistRef != null && eventListener != null) {
            wishlistRef.removeEventListener(eventListener);
        }
    }
}
