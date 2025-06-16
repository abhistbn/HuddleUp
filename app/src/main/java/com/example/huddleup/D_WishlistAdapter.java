package com.example.huddleup;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;

public class D_WishlistAdapter extends RecyclerView.Adapter<D_WishlistAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<Z_EventP2> wishlistItemList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Z_EventP2 event);
        void onDeleteClick(Z_EventP2 event, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public D_WishlistAdapter(Context context, ArrayList<Z_EventP2> wishlistItemList) {
        this.context = context;
        this.wishlistItemList = wishlistItemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.d_item_wishlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Z_EventP2 item = wishlistItemList.get(position);

        holder.tvEventTitle.setText(item.getName());
        holder.tvEventDate.setText(item.getDate());
        holder.tvEventLocation.setText(item.getLocation());

        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.placeholder_event)
                .error(R.drawable.placeholder_event)
                .into(holder.ivEventImage);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });

        holder.ibRemoveFromWishlist.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(item, holder.getAdapterPosition());
            }
        });

        // --- BAGIAN YANG DIPERBAIKI ---
        // Listener untuk tombol daftar sekarang mengarah ke N_detailEvent
        holder.btnRegister.setOnClickListener(v -> {
            // Membuat Intent untuk membuka N_detailEvent
            Intent intent = new Intent(context, N_detailEvent.class);

            // Mengirim semua data event yang relevan menggunakan putExtra
            // Pastikan key-nya sama dengan yang diterima di N_detailEvent
            intent.putExtra("event_key", item.getId());
            intent.putExtra("title", item.getName());
            intent.putExtra("date", item.getDate());
            intent.putExtra("time", item.getTime());
            intent.putExtra("location", item.getLocation());
            intent.putExtra("about", item.getAbout());
            intent.putExtra("image_url", item.getImageUrl());

            // Memulai Activity
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return wishlistItemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivEventImage;
        TextView tvEventTitle, tvEventDate, tvEventLocation;
        ImageButton ibRemoveFromWishlist;
        Button btnRegister;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivEventImage = itemView.findViewById(R.id.ivEventImage);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvEventLocation = itemView.findViewById(R.id.tvEventLocation);
            ibRemoveFromWishlist = itemView.findViewById(R.id.ibRemoveFromWishlist);
            btnRegister = itemView.findViewById(R.id.btnRegister);
        }
    }
}
