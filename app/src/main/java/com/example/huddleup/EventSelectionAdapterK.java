// File: app/src/main/java/com/example/huddleup/EventSelectionAdapterK.java
package com.example.huddleup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class EventSelectionAdapterK extends RecyclerView.Adapter<EventSelectionAdapterK.EventSelectViewHolder> { // Nama kelas diakhiri 'K'

    private final List<EventK> events; // Daftar semua event yang tersedia (dari EventK)
    private final Set<String> selectedEventIds; // Menyimpan ID event yang saat ini terpilih

    public EventSelectionAdapterK(List<EventK> events, Set<String> currentSelectedIds) {
        this.events = events;
        // Salin ID event yang sudah terpilih sebelumnya (untuk mode edit kategori)
        this.selectedEventIds = new HashSet<>(currentSelectedIds);
    }

    @NonNull
    @Override
    public EventSelectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Menggunakan layout item_event_selectable.xml
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_selectable, parent, false);
        return new EventSelectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventSelectViewHolder holder, int position) {
        EventK event = events.get(position); // Dapatkan objek EventK
        holder.tvName.setText(event.getName()); // Tampilkan nama event

        // Pastikan checkbox terlihat dan tombol "Remove From Category" disembunyikan
        holder.cbSelectEvent.setVisibility(View.VISIBLE);
        if (holder.btnRemoveFromCategory != null) {
            holder.btnRemoveFromCategory.setVisibility(View.GONE);
        }

        // Atur status CheckBox berdasarkan apakah event ini sudah ada di daftar terpilih
        holder.cbSelectEvent.setChecked(selectedEventIds.contains(event.getId()));

        // Hapus listener sebelumnya untuk mencegah kesalahan (re-trigger)
        holder.cbSelectEvent.setOnCheckedChangeListener(null);
        // Set listener baru untuk memperbarui daftar selectedEventIds saat checkbox berubah
        holder.cbSelectEvent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedEventIds.add(event.getId());
            } else {
                selectedEventIds.remove(event.getId());
            }
        });

        // Juga tambahkan OnClickListener untuk seluruh item, agar klik pada baris juga bisa toggle checkbox
        holder.itemView.setOnClickListener(v -> {
            holder.cbSelectEvent.setChecked(!holder.cbSelectEvent.isChecked());
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    // Metode untuk mendapatkan semua ID event yang saat ini terpilih
    public Set<String> getSelectedEventIds() {
        return selectedEventIds;
    }

    // ViewHolder untuk elemen UI di setiap item event yang dapat dipilih
    static class EventSelectViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        CheckBox cbSelectEvent;
        Button btnRemoveFromCategory; // Ada di layout, tapi disembunyikan di adapter ini

        public EventSelectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvEventName);
            cbSelectEvent = itemView.findViewById(R.id.cbSelectEvent);
            btnRemoveFromCategory = itemView.findViewById(R.id.btnRemoveFromCategory);
        }
    }
}