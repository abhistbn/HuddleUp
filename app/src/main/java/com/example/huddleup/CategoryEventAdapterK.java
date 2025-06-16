// File: app/src/main/java/com/example/huddleup/CategoryEventAdapterK.java
package com.example.huddleup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox; // Tetap perlu karena menggunakan item_event_selectable
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryEventAdapterK extends RecyclerView.Adapter<CategoryEventAdapterK.CategoryEventViewHolder> { // Nama kelas diakhiri 'K'

    private final List<EventK> events; // Daftar EventK yang akan ditampilkan
    private final OnItemRemoveListener listener; // Interface untuk callback penghapusan

    // Interface untuk komunikasi event klik dari adapter ke Activity (untuk menghapus dari kategori)
    public interface OnItemRemoveListener {
        void onRemoveClick(int position);
    }

    public CategoryEventAdapterK(List<EventK> events, OnItemRemoveListener listener) {
        this.events = events;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Menggunakan layout item_event_selectable.xml (yang punya checkbox dan tombol remove)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event_selectable, parent, false);
        return new CategoryEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryEventViewHolder holder, int position) {
        EventK event = events.get(position); // Dapatkan objek EventK
        holder.tvName.setText(event.getName()); // Tampilkan nama event

        // Sembunyikan checkbox karena ini bukan mode seleksi event
        if (holder.cbSelectEvent != null) {
            holder.cbSelectEvent.setVisibility(View.GONE);
        }
        // Pastikan tombol "Remove From Category" terlihat
        if (holder.btnRemoveFromCategory != null) {
            holder.btnRemoveFromCategory.setVisibility(View.VISIBLE);
        }

        // Mengatur aksi klik untuk tombol "Remove From Category"
        holder.btnRemoveFromCategory.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveClick(position); // Panggil metode di Activity
            }
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    // ViewHolder untuk elemen UI di setiap item event dalam kategori
    static class CategoryEventViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        CheckBox cbSelectEvent; // Ada di layout, tapi disembunyikan
        Button btnRemoveFromCategory; // Tombol untuk menghapus dari kategori

        public CategoryEventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvEventName);
            cbSelectEvent = itemView.findViewById(R.id.cbSelectEvent);
            btnRemoveFromCategory = itemView.findViewById(R.id.btnRemoveFromCategory);
        }
    }
}