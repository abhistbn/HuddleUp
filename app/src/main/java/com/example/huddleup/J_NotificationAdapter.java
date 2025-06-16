package com.example.huddleup;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
// import android.net.Uri; // Tidak digunakan di sini, bisa dihapus jika tidak ada error
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class J_NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<J_NotificationItem> notificationList;
    private OnItemLongClickListener onItemLongClickListener;
    private OnItemSwipeListener onItemSwipeListener;

    public J_NotificationAdapter(Context context, List<J_NotificationItem> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.onItemLongClickListener = listener;
    }

    public void setOnItemSwipeListener(OnItemSwipeListener listener) {
        this.onItemSwipeListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return notificationList.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == J_NotificationItem.TYPE_HEADER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_header, parent, false);
            return new HeaderViewHolder(view);
        } else { // TYPE_NOTIFICATION
            View view = LayoutInflater.from(context).inflate(R.layout.j_item_notifikasi, parent, false);
            return new NotificationViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        J_NotificationItem item = notificationList.get(position);

        if (holder.getItemViewType() == J_NotificationItem.TYPE_HEADER) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.tvHeaderTitle.setText(item.getTitle());
        } else { // TYPE_NOTIFICATION
            NotificationViewHolder notificationHolder = (NotificationViewHolder) holder;
            notificationHolder.tvTitle.setText(item.getTitle());
            notificationHolder.tvDescription.setText(item.getDescription());
            notificationHolder.tvTime.setText(item.getTime());

            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                Picasso.get().load(item.getImageUrl())
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_error_image)
                        .into(notificationHolder.ivNotificationImage);
                notificationHolder.ivNotificationImage.setVisibility(View.VISIBLE);
                Log.d("ADAPTER_IMAGE", "Loading image: " + item.getImageUrl() + " for " + item.getTitle());
            } else {
                notificationHolder.ivNotificationImage.setVisibility(View.GONE);
                notificationHolder.ivNotificationImage.setImageDrawable(null);
                Log.d("ADAPTER_IMAGE", "No image for " + item.getTitle());
            }

            notificationHolder.itemView.setOnLongClickListener(v -> {
                if (onItemLongClickListener != null) {
                    onItemLongClickListener.onItemLongClick(position);
                    return true;
                }
                return false;
            });
        }
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public void deleteItem(int position) {
        // TODO: Jika Anda menyimpan notifikasi manual di Firebase, Anda juga harus menghapusnya dari Firebase di sini
        // Anda perlu mendapatkan key Firebase dari item tersebut untuk menghapusnya
        notificationList.remove(position);
        notifyItemRemoved(position);
        Log.d("NOTIF_DELETE", "Item dihapus dari list lokal di posisi: " + position);
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeaderTitle;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeaderTitle = itemView.findViewById(R.id.tvHeaderTitle);
        }
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvTime;
        ImageView ivNotificationImage;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTime = itemView.findViewById(R.id.tvTime);
            ivNotificationImage = itemView.findViewById(R.id.ivNotificationImage);
        }
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    public interface OnItemSwipeListener {
        void onItemSwipe(int position);
    }

    public static void attachItemTouchHelper(RecyclerView recyclerView, J_NotificationAdapter adapter) {
        // --- Deklarasi simpleItemTouchCallback di sini ---
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            private final Drawable deleteIcon = ContextCompat.getDrawable(adapter.context, R.drawable.ic_trash);
            private final Paint paint = new Paint();
            private final int intrinsicWidth;
            private final int intrinsicHeight;

            {
                if (deleteIcon != null) {
                    intrinsicWidth = deleteIcon.getIntrinsicWidth();
                    intrinsicHeight = deleteIcon.getIntrinsicHeight();
                } else {
                    intrinsicWidth = 0;
                    intrinsicHeight = 0;
                    Log.e("SWIPE_DRAW", "Delete icon is null! Check R.drawable.ic_delete");
                }
                paint.setColor(Color.RED);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (adapter.onItemSwipeListener != null) {
                    adapter.onItemSwipeListener.onItemSwipe(viewHolder.getAdapterPosition());
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                View itemView = viewHolder.itemView;
                int itemHeight = itemView.getBottom() - itemView.getTop();
                int itemWidth = itemView.getRight() - itemView.getLeft();

                if (viewHolder.getItemViewType() == J_NotificationItem.TYPE_HEADER || dX == 0) {
                    return;
                }

                RectF background;
                if (dX > 0) { // Swipe ke kanan
                    background = new RectF(itemView.getLeft(), itemView.getTop(), dX, itemView.getBottom());
                } else { // Swipe ke kiri
                    background = new RectF(itemView.getRight() + dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                }
                c.drawRect(background, paint);

                if (deleteIcon != null) {
                    int iconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                    int iconBottom = iconTop + intrinsicHeight;
                    int iconLeft;
                    int iconRight;
                    int iconMargin = (itemHeight - intrinsicHeight) / 2;

                    if (dX > 0) { // Swipe ke kanan (ikon di kiri)
                        iconLeft = itemView.getLeft() + iconMargin;
                        iconRight = itemView.getLeft() + iconMargin + intrinsicWidth;
                    } else { // Swipe ke kiri (ikon di kanan)
                        iconLeft = itemView.getRight() - iconMargin - intrinsicWidth;
                        iconRight = itemView.getRight() - iconMargin;
                    }

                    if (iconRight > itemView.getLeft() && iconLeft < itemView.getRight()) {
                        deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                        deleteIcon.draw(c);
                    }
                }
            }
        }; // Akhir dari deklarasi simpleItemTouchCallback

        // --- Penggunaan simpleItemTouchCallback di sini ---
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
}