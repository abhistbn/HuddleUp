package com.example.huddleup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryEventAdapterK extends RecyclerView.Adapter<CategoryEventAdapterK.CategoryEventViewHolder> {

    private final List<EventK> events;
    private final OnItemRemoveListener listener;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event_selectable, parent, false);
        return new CategoryEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryEventViewHolder holder, int position) {
        EventK event = events.get(position);
        holder.tvName.setText(event.getName());

        if (holder.cbSelectEvent != null) {
            holder.cbSelectEvent.setVisibility(View.GONE);
        }
        if (holder.btnRemoveFromCategory != null) {
            holder.btnRemoveFromCategory.setVisibility(View.VISIBLE);
        }

        holder.btnRemoveFromCategory.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class CategoryEventViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        CheckBox cbSelectEvent;
        Button btnRemoveFromCategory;

        public CategoryEventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvEventName);
            cbSelectEvent = itemView.findViewById(R.id.cbSelectEvent);
            btnRemoveFromCategory = itemView.findViewById(R.id.btnRemoveFromCategory);
        }
    }
}