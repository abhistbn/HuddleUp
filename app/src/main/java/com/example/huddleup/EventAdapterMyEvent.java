package com.example.huddleup;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EventAdapterMyEvent extends RecyclerView.Adapter<EventAdapterMyEvent.ViewHolder> {

    List<N_EventModel> list;
    OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(N_EventModel event);
    }

    public EventAdapterMyEvent(List<N_EventModel> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtJudul, txtTanggal, txtWaktu, txtLokasi, txtAbout;
        Button btniv_batal, btniv_detail;

        public ViewHolder(View itemView) {
            super(itemView);
            txtJudul = itemView.findViewById(R.id.txtJudul);
            txtTanggal = itemView.findViewById(R.id.txtTanggal);
            txtWaktu = itemView.findViewById(R.id.txtWaktu);
            txtLokasi = itemView.findViewById(R.id.txtLokasi);
            txtAbout = itemView.findViewById(R.id.txtAbout);
            btniv_batal = itemView.findViewById(R.id.btniv_batal);
            btniv_detail = itemView.findViewById(R.id.btniv_detail);
        }

        public void bind(N_EventModel event, OnItemClickListener listener) {
            if (listener != null) {
                itemView.setOnClickListener(v -> listener.onItemClick(event));
            }
        }
    }

    @Override
    public EventAdapterMyEvent.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        N_EventModel event = list.get(position);
        holder.txtJudul.setText(event.getJudul());
        holder.txtTanggal.setText(event.getTanggal());
        holder.txtWaktu.setText(event.getWaktu());
        holder.txtLokasi.setText(event.getLokasi());
        holder.bind(event, listener);

        holder.btniv_detail.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), N_EventCheckedInActivity.class);
            intent.putExtra("judul", event.getJudul());
            intent.putExtra("tanggal", event.getTanggal());
            intent.putExtra("waktu", event.getWaktu());
            intent.putExtra("lokasi", event.getLokasi());
            holder.itemView.getContext().startActivity(intent);
        });

        holder.btniv_batal.setOnClickListener(v -> {
            list.remove(position);
            notifyItemRemoved(position);
            Toast.makeText(holder.itemView.getContext(), "Berhasil batal mengikuti event!", Toast.LENGTH_SHORT).show();
        });
    }


    @Override
    public int getItemCount() {
        return list.size();
    }
}