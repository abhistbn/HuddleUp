package com.example.huddleup;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<NotificationItem> notificationList;
    private Context context;


    public NotificationAdapter(Context context, List<NotificationItem> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }


    // Menentukan tipe view untuk setiap item (HEADER atau NOTIFICATION)
    @Override
    public int getItemViewType(int position) {
        return notificationList.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == NotificationItem.TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notifikasi, parent, false);
            return new NotificationViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NotificationItem notification = notificationList.get(position);

        if (holder instanceof NotificationViewHolder) {
            ((NotificationViewHolder) holder).tvTitle.setText(notification.getTitle());
            ((NotificationViewHolder) holder).tvMessage.setText(notification.getDescription());
            ((NotificationViewHolder) holder).tvTime.setText(notification.getTime());
        } else if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).tvHeader.setText(notification.getTitle());
        }

        holder.itemView.setOnLongClickListener(v -> {
            showEditNotificationDialog(position);
            return true;
        });

    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public void deleteItem(int position) {
        notificationList.remove(position);
        notifyItemRemoved(position);
    }

    // ViewHolder untuk item notifikasi biasa
    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTime;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvDescription);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }

    // ViewHolder untuk item header
    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeader;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeader = itemView.findViewById(R.id.tvHeaderTitle);
        }
    }

    // Swipe helper
    public static void attachItemTouchHelper(RecyclerView recyclerView, NotificationAdapter adapter) {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                adapter.deleteItem(position);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                if (dX < 0) {
                    Paint paint = new Paint();
                    paint.setColor(Color.RED);
                    c.drawRect((float) viewHolder.itemView.getRight() + dX, (float) viewHolder.itemView.getTop(),
                            (float) viewHolder.itemView.getRight(), (float) viewHolder.itemView.getBottom(), paint);

                    Bitmap trashIcon = BitmapFactory.decodeResource(viewHolder.itemView.getContext().getResources(), R.drawable.ic_trash);
                    c.drawBitmap(trashIcon, viewHolder.itemView.getRight() - trashIcon.getWidth() - 20,
                            viewHolder.itemView.getTop() + (viewHolder.itemView.getHeight() - trashIcon.getHeight()) / 2, null);
                }
            }
        });

        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void showEditNotificationDialog(int position) {
        NotificationItem item = notificationList.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_create_notification, null);
        builder.setView(dialogView);

        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);

        etTitle.setText(item.getTitle());
        etDescription.setText(item.getDescription());

        builder.setPositiveButton("Update", (dialog, which) -> {
            item.setTitle(etTitle.getText().toString());
            item.setDescription(etDescription.getText().toString());
            notifyItemChanged(position);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }


}
