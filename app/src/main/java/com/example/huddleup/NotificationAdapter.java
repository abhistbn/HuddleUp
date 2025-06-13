package com.example.huddleup;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface; // Import DialogInterface
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<NotificationItem> notificationList;
    private Context context;

    public NotificationAdapter(Context context, List<NotificationItem> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

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
            NotificationViewHolder notificationHolder = (NotificationViewHolder) holder;
            notificationHolder.tvTitle.setText(notification.getTitle());
            notificationHolder.tvMessage.setText(notification.getDescription());
            notificationHolder.tvTime.setText(notification.getTime());

            if (notification.getImageUri() != null) {
                notificationHolder.ivNotificationImage.setVisibility(View.VISIBLE);
                notificationHolder.ivNotificationImage.setImageURI(notification.getImageUri());
            } else {
                notificationHolder.ivNotificationImage.setVisibility(View.GONE);
            }
        } else if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).tvHeader.setText(notification.getTitle());
        }

        if (notification.getType() == NotificationItem.TYPE_NOTIFICATION) {
            holder.itemView.setOnLongClickListener(v -> {
                showEditNotificationDialog(position);
                return true;
            });
        } else {
            holder.itemView.setOnLongClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public void deleteItem(int position) {
        if (notificationList.get(position).getType() == NotificationItem.TYPE_NOTIFICATION) {
            notificationList.remove(position);
            notifyItemRemoved(position);
            Toast.makeText(context, "Notifikasi berhasil dihapus!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Header tidak bisa dihapus!", Toast.LENGTH_SHORT).show();
            notifyItemChanged(position);
        }
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTime;
        ImageView ivNotificationImage;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvDescription);
            tvTime = itemView.findViewById(R.id.tvTime);
            ivNotificationImage = itemView.findViewById(R.id.ivNotificationImage);
        }
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeader;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeader = itemView.findViewById(R.id.tvHeaderTitle);
        }
    }

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
                if (viewHolder.getItemViewType() == NotificationItem.TYPE_HEADER) {
                    super.onChildDraw(c, recyclerView, viewHolder, 0, dY, actionState, isCurrentlyActive);
                    return;
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                if (dX < 0) {
                    Paint paint = new Paint();
                    paint.setColor(Color.RED);
                    c.drawRect((float) viewHolder.itemView.getRight() + dX, (float) viewHolder.itemView.getTop(),
                            (float) viewHolder.itemView.getRight(), (float) viewHolder.itemView.getBottom(), paint);

                    Bitmap trashIcon = BitmapFactory.decodeResource(viewHolder.itemView.getContext().getResources(), R.drawable.ic_trash);
                    float iconMargin = (viewHolder.itemView.getHeight() - trashIcon.getHeight()) / 2;
                    float iconLeft = viewHolder.itemView.getRight() - trashIcon.getWidth() - iconMargin;
                    float iconTop = viewHolder.itemView.getTop() + iconMargin;
                    c.drawBitmap(trashIcon, iconLeft, iconTop, null);
                }
            }
        });

        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void showEditNotificationDialog(int position) {
        NotificationItem item = notificationList.get(position);

        if (item.getType() == NotificationItem.TYPE_HEADER) {
            Toast.makeText(context, "Header tidak bisa diedit!", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Light_Dialog);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_create_notification, null);
        builder.setView(dialogView);

        // Find all views
        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);
        Button btnChooseImage = dialogView.findViewById(R.id.btnChooseImage);
        Button btnRemoveImage = dialogView.findViewById(R.id.btnRemoveImage);

        // Image preview elements
        ImageView ivDialogPreview = dialogView.findViewById(R.id.ivDialogPreview);
        LinearLayout llImagePlaceholder = dialogView.findViewById(R.id.llImagePlaceholder);
        TextView tvImageStatus = dialogView.findViewById(R.id.tvImageStatus);
        View vImageOverlay = dialogView.findViewById(R.id.vImageOverlay);

        // Set initial values
        etTitle.setText(item.getTitle());
        etDescription.setText(item.getDescription());

        // Handle image display
        updateImagePreview(item.getImageUri(), ivDialogPreview, llImagePlaceholder,
                btnRemoveImage, tvImageStatus, vImageOverlay);

        // Choose image button listener
        btnChooseImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            if (context instanceof NotificationActivity) {
                ((NotificationActivity) context).dialogIvPreview = ivDialogPreview;
                ((NotificationActivity) context).dialogBtnRemoveImage = btnRemoveImage;
                ((NotificationActivity) context).dialogLlPlaceholder = llImagePlaceholder;
                ((NotificationActivity) context).dialogTvImageStatus = tvImageStatus;
                ((NotificationActivity) context).dialogVOverlay = vImageOverlay;
                ((NotificationActivity) context).startActivityForResult(intent, NotificationActivity.PICK_IMAGE_REQUEST);
            }
        });

        // Remove image button listener
        btnRemoveImage.setOnClickListener(v -> {
            item.setImageUri(null);
            updateImagePreview(null, ivDialogPreview, llImagePlaceholder,
                    btnRemoveImage, tvImageStatus, vImageOverlay);
            showStyledToast("🗑️ Gambar berhasil dihapus");
        });

        AlertDialog dialog = builder.create();

        // Style dialog window
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            dialogView.setPadding(24, 24, 24, 24);
        }

        // Set custom buttons
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "✓ Update", (DialogInterface.OnClickListener) null);
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "✕ Cancel", (DialogInterface.OnClickListener) null);

        dialog.show();

        // Style and set button listeners
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        if (positiveButton != null) {
            styleDialogButton(positiveButton, "#27AE60", "#FFFFFF");
            positiveButton.setOnClickListener(v -> {
                String newTitle = etTitle.getText().toString().trim();
                String newDesc = etDescription.getText().toString().trim();

                if (newTitle.isEmpty() || newDesc.isEmpty()) {
                    showStyledToast("⚠️ Judul dan Deskripsi tidak boleh kosong!");
                    return;
                }

                // Update item
                item.setTitle(newTitle);
                item.setDescription(newDesc);

                if (context instanceof NotificationActivity) {
                    NotificationActivity activity = (NotificationActivity) context;
                    if (activity.selectedImageUri != null) {
                        item.setImageUri(activity.selectedImageUri);
                    }

                    // Reset dialog references
                    activity.selectedImageUri = null;
                    activity.dialogIvPreview = null;
                    activity.dialogBtnRemoveImage = null;
                    activity.dialogLlPlaceholder = null;
                    activity.dialogTvImageStatus = null;
                    activity.dialogVOverlay = null;
                }

                item.setTime(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
                notifyItemChanged(position);
                showStyledToast("✅ Notifikasi berhasil diperbarui!");
                dialog.dismiss();
            });
        }

        if (negativeButton != null) {
            styleDialogButton(negativeButton, "#95A5A6", "#FFFFFF");
            negativeButton.setOnClickListener(v -> {
                if (context instanceof NotificationActivity) {
                    NotificationActivity activity = (NotificationActivity) context;
                    activity.selectedImageUri = null;
                    activity.dialogIvPreview = null;
                    activity.dialogBtnRemoveImage = null;
                    activity.dialogLlPlaceholder = null;
                    activity.dialogTvImageStatus = null;
                    activity.dialogVOverlay = null;
                }
                dialog.dismiss();
            });
        }
    }

    // Helper method untuk update preview gambar
    private void updateImagePreview(Uri imageUri, ImageView ivPreview, LinearLayout llPlaceholder,
                                    Button btnRemove, TextView tvStatus, View vOverlay) {
        if (imageUri != null) {
            // Ada gambar - tampilkan preview
            ivPreview.setImageURI(imageUri);
            ivPreview.setVisibility(View.VISIBLE);
            vOverlay.setVisibility(View.VISIBLE);
            llPlaceholder.setVisibility(View.GONE);
            btnRemove.setVisibility(View.VISIBLE);
            tvStatus.setText("✅ Gambar dipilih");
            tvStatus.setTextColor(Color.parseColor("#27AE60"));

            // Update status badge background
            GradientDrawable statusBg = new GradientDrawable();
            statusBg.setColor(Color.parseColor("#D5F4E6"));
            statusBg.setCornerRadius(12f);
            statusBg.setStroke(1, Color.parseColor("#27AE60"));
            tvStatus.setBackground(statusBg);

        } else {
            // Tidak ada gambar - tampilkan placeholder
            ivPreview.setVisibility(View.GONE);
            vOverlay.setVisibility(View.GONE);
            llPlaceholder.setVisibility(View.VISIBLE);
            btnRemove.setVisibility(View.GONE);
            tvStatus.setText("📷 Tidak ada gambar");
            tvStatus.setTextColor(Color.parseColor("#95A5A6"));

            // Reset status badge background
            GradientDrawable statusBg = new GradientDrawable();
            statusBg.setColor(Color.parseColor("#ECF0F1"));
            statusBg.setCornerRadius(12f);
            statusBg.setStroke(1, Color.parseColor("#D5DBDB"));
            tvStatus.setBackground(statusBg);
        }
    }

    private void styleDialogButton(Button button, String backgroundColor, String textColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.parseColor(backgroundColor));
        drawable.setCornerRadius(16f);
        button.setBackground(drawable);
        button.setTextColor(Color.parseColor(textColor));
        button.setPadding(32, 12, 32, 12);
        button.setAllCaps(false);
        button.setTextSize(14f);

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) button.getLayoutParams();
        params.setMargins(8, 0, 8, 0);
        button.setLayoutParams(params);
    }

    private void showStyledToast(String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        View toastView = toast.getView();
        if (toastView != null) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(Color.parseColor("#2C3E50"));
            drawable.setCornerRadius(16f);
            toastView.setBackground(drawable);
        }
        toast.show();
    }


    // Method untuk update preview gambar di dialog edit
    public void updateDialogImagePreview(Uri imageUri) {
        if (context instanceof NotificationActivity) {
            NotificationActivity activity = (NotificationActivity) context;

            // Update selectedImageUri
            activity.selectedImageUri = imageUri;

            // Update preview UI jika referensi masih ada
            if (activity.dialogIvPreview != null &&
                    activity.dialogLlPlaceholder != null &&
                    activity.dialogBtnRemoveImage != null &&
                    activity.dialogTvImageStatus != null &&
                    activity.dialogVOverlay != null) {

                updateImagePreview(imageUri,
                        activity.dialogIvPreview,
                        activity.dialogLlPlaceholder,
                        activity.dialogBtnRemoveImage,
                        activity.dialogTvImageStatus,
                        activity.dialogVOverlay);
            }
        }
    }

    // Method untuk clear references ketika dialog ditutup
    public void clearDialogReferences() {
        if (context instanceof NotificationActivity) {
            NotificationActivity activity = (NotificationActivity) context;
            activity.resetDialogReferences();
        }
    }
}