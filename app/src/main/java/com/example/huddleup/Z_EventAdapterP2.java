package com.example.huddleup;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class Z_EventAdapterP2 extends RecyclerView.Adapter<Z_EventAdapterP2.EventViewHolder> {

    private Context context;
    private ArrayList<Z_EventP2> eventList;
    private OnItemContextClickListener contextClickListener;

    public interface OnItemContextClickListener {
        void onItemContextClicked(View view, int position);
    }

    public Z_EventAdapterP2(Context context, ArrayList<Z_EventP2> eventList) {
        this.context = context;
        this.eventList = eventList;
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

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            imgEventPoster = itemView.findViewById(R.id.imgEventPoster);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            itemView.setLongClickable(true);
            itemView.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                menu.add(0, R.id.action_edit_event, 0, "Edit");
                menu.add(0, R.id.action_delete_event, 0, "Hapus");
            });
        }
    }
}
