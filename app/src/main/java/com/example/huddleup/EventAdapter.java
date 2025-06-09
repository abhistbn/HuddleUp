package com.example.huddleup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final List<Event> events;
    private final OnItemDeleteListener onItemDeleteListener;

    // Interface untuk callback hapus
    public interface OnItemDeleteListener {
        void onItemDelete(int position);
    }

    public EventAdapter(List<Event> events, OnItemDeleteListener listener) {
        this.events = events;
        this.onItemDeleteListener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.tvName.setText(event.getName());
        holder.tvDate.setText(event.getDate());
        holder.tvLocation.setText(event.getLocation());
        holder.tvCategory.setText(event.getCategory());

        holder.btnDelete.setOnClickListener(v -> {
            if (onItemDeleteListener != null) {
                onItemDeleteListener.onItemDelete(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDate, tvLocation, tvCategory;
        View btnDelete;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvEventName);
            tvDate = itemView.findViewById(R.id.tvEventDate);
            tvLocation = itemView.findViewById(R.id.tvEventLocation);
            tvCategory = itemView.findViewById(R.id.tvEventCategory);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
