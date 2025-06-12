package com.example.huddleup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class N_EventAdapterMyEvent extends RecyclerView.Adapter<N_EventAdapterMyEvent.MyEventViewHolder> {

    private final ArrayList<N_EventModel> eventList;
    private final OnMyEventListener listener;
    private final Context context;

    public interface OnMyEventListener {
        void onBatalClick(int position);
        void onLihatTiketClick(N_EventModel event);
    }

    public N_EventAdapterMyEvent(Context context, ArrayList<N_EventModel> eventList, OnMyEventListener listener) {
        this.context = context;
        this.eventList = eventList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.n_item_event, parent, false);
        return new MyEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyEventViewHolder holder, int position) {
        N_EventModel event = eventList.get(position);

        holder.txtJudul.setText(event.getJudul());
        holder.txtTanggal.setText(event.getTanggal());
        holder.txtWaktu.setText(event.getWaktu());
        holder.txtLokasi.setText(event.getLokasi());

        holder.btnBatal.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBatalClick(holder.getAdapterPosition());
            }
        });

        holder.btnLihatTiket.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLihatTiketClick(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class MyEventViewHolder extends RecyclerView.ViewHolder {
        TextView txtJudul, txtTanggal, txtWaktu, txtLokasi;
        Button btnBatal, btnLihatTiket;

        public MyEventViewHolder(@NonNull View itemView) {
            super(itemView);
            txtJudul = itemView.findViewById(R.id.txtJudul);
            txtTanggal = itemView.findViewById(R.id.txtTanggal);
            txtWaktu = itemView.findViewById(R.id.txtWaktu);
            txtLokasi = itemView.findViewById(R.id.txtLokasi);
            btnBatal = itemView.findViewById(R.id.btniv_batal);
            btnLihatTiket = itemView.findViewById(R.id.btniv_detail);
        }
    }
}