package com.example.huddleup;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet; // Import yang dibutuhkan

public class Z_EventAdapterP2 extends RecyclerView.Adapter<Z_EventAdapterP2.EventViewHolder> {

    private Context context;
    private ArrayList<Z_EventP2> eventList;
    private OnItemContextClickListener contextClickListener;

    // --- BAGIAN BARU UNTUK FUNGSI WISHLIST ---
    private DatabaseReference wishlistRef;
    private HashSet<String> wishlistEventIds = new HashSet<>();
    // -----------------------------------------

    public interface OnItemContextClickListener {
        void onItemContextClicked(View view, int position);
    }

    public Z_EventAdapterP2(Context context, ArrayList<Z_EventP2> eventList) {
        this.context = context;
        this.eventList = eventList;

        // --- INISIALISASI FIREBASE DAN LISTENER UNTUK WISHLIST ---
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            // Referensi ini akan selalu mendengarkan perubahan pada "folder" wishlist pengguna
            DatabaseReference userWishlistRef = FirebaseDatabase.getInstance().getReference("wishlist").child(currentUserId);
            wishlistRef = userWishlistRef; // Simpan referensi utama

            userWishlistRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    wishlistEventIds.clear(); // Bersihkan daftar ID lama
                    for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                        // Tambahkan semua ID event yang ada di wishlist ke dalam set
                        wishlistEventIds.add(eventSnapshot.getKey());
                    }
                    // Perintahkan RecyclerView untuk menggambar ulang dirinya
                    // Ini akan memperbarui status "love" pada semua item yang terlihat
                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "Gagal sinkronisasi wishlist", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void setOnItemContextClickListener(OnItemContextClickListener listener) {
        this.contextClickListener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.z_item_eventp2, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Z_EventP2 currentEvent = eventList.get(position);
        holder.tvEventName.setText(currentEvent.getName());

        Glide.with(context)
                .load(currentEvent.getImageUrl())
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher_round)
                .into(holder.imgEventPoster);

        // --- UNTUK TOMBOL WISHLIST ---
        if (holder.ibAddToWishlist != null && currentEvent.getId() != null) {
            final String eventId = currentEvent.getId();

            // Cek apakah ID event ini ada di dalam daftar wishlist kita
            if (wishlistEventIds.contains(eventId)) {
                // JIKA YA: Tampilkan hati penuh, dan siapkan fungsi HAPUS
                holder.ibAddToWishlist.setImageResource(R.drawable.ic_favorite_filled);
                holder.ibAddToWishlist.setOnClickListener(v -> {
                    if (wishlistRef != null) {
                        wishlistRef.child(eventId).removeValue(); // Hapus dari Firebase
                        Toast.makeText(context, "Dihapus dari wishlist", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // JIKA TIDAK: Tampilkan hati kosong, dan siapkan fungsi TAMBAH
                holder.ibAddToWishlist.setImageResource(R.drawable.ic_favorite_border);
                holder.ibAddToWishlist.setOnClickListener(v -> {
                    if (wishlistRef != null) {
                        wishlistRef.child(eventId).setValue(currentEvent); // Tambah ke Firebase
                        Toast.makeText(context, "Ditambahkan ke wishlist", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
        // ---------------------------------------------------

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, N_detailEvent.class);
            intent.putExtra("event_key", currentEvent.getId());
            intent.putExtra("title", currentEvent.getName());
            intent.putExtra("date", currentEvent.getDate());
            intent.putExtra("time", currentEvent.getTime());
            intent.putExtra("location", currentEvent.getLocation());
            intent.putExtra("about", currentEvent.getAbout());
            intent.putExtra("image_url", currentEvent.getImageUrl());
            context.startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (contextClickListener != null) {
                contextClickListener.onItemContextClicked(holder.itemView, position);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView imgEventPoster;
        TextView tvEventName;
        ImageButton ibAddToWishlist;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            imgEventPoster = itemView.findViewById(R.id.imgEventPoster);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            ibAddToWishlist = itemView.findViewById(R.id.ib_add_to_wishlist);

            itemView.setLongClickable(true);
            itemView.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                menu.add(0, R.id.action_edit_event, 0, "Edit");
                menu.add(0, R.id.action_delete_event, 0, "Hapus");
            });
        }
    }
}
