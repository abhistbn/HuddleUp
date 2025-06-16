package com.example.huddleup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class EventSelectionAdapterK extends RecyclerView.Adapter<EventSelectionAdapterK.EventSelectViewHolder> {

    private final List<EventK> events;
    private final Set<String> selectedEventIds;

    public EventSelectionAdapterK(List<EventK> events, Set<String> currentSelectedIds) {
        this.events = events;
        this.selectedEventIds = new HashSet<>(currentSelectedIds);
    }

    @NonNull
    @Override
    public EventSelectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_selectable, parent, false);
        return new EventSelectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventSelectViewHolder holder, int position) {
        EventK event = events.get(position);
        holder.tvName.setText(event.getName());

        holder.cbSelectEvent.setVisibility(View.VISIBLE);
        if (holder.btnRemoveFromCategory != null) {
            holder.btnRemoveFromCategory.setVisibility(View.GONE);
        }

        holder.cbSelectEvent.setChecked(selectedEventIds.contains(event.getId()));

        holder.cbSelectEvent.setOnCheckedChangeListener(null);
        holder.cbSelectEvent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedEventIds.add(event.getId());
            } else {
                selectedEventIds.remove(event.getId());
            }
        });

        holder.itemView.setOnClickListener(v -> {
            holder.cbSelectEvent.setChecked(!holder.cbSelectEvent.isChecked());
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public Set<String> getSelectedEventIds() {
        return selectedEventIds;
    }

    static class EventSelectViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        CheckBox cbSelectEvent;
        Button btnRemoveFromCategory;

        public EventSelectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvEventName);
            cbSelectEvent = itemView.findViewById(R.id.cbSelectEvent);
            btnRemoveFromCategory = itemView.findViewById(R.id.btnRemoveFromCategory);
        }
    }
}