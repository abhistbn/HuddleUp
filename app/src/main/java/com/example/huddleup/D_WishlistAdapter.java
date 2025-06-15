package com.example.huddleup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
        // Menggunakan layout item wishlist
        View view = LayoutInflater.from(context).inflate(R.layout.d_item_wishlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Mengambil objek Z_EventP2 dari list
        Z_EventP2 item = wishlistItemList.get(position);

        // Mengisi data ke dalam komponen-komponen view dari objek Z_EventP2
        holder.tvEventTitle.setText(item.getName());
        holder.tvEventDate.setText(item.getDate());
        holder.tvEventLocation.setText(item.getLocation());

        // Menggunakan Glide untuk memuat gambar dari URL Firebase
        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.placeholder_event) // Pastikan drawable ini ada
                .error(R.drawable.placeholder_event)
                .into(holder.ivEventImage);

        // Listener untuk seluruh item, akan membuka halaman detail
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });

        // Listener untuk tombol hapus (ikon hati)
        holder.ibRemoveFromWishlist.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(item, holder.getAdapterPosition());
            }
        });

        // Listener untuk tombol daftar
        holder.btnRegister.setOnClickListener(v -> {
            Toast.makeText(context, "Membuka halaman pendaftaran untuk " + item.getName(), Toast.LENGTH_SHORT).show();
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
