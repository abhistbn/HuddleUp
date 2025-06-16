package com.example.huddleup;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Set;

public class CategoryAdapterK extends RecyclerView.Adapter<CategoryAdapterK.CategoryViewHolder> {

    private final List<CategoryK> categories;
    private final OnCategoryActionListener listener;
    private boolean isInSelectionMode = false;
    private Set<String> selectedCategoryIds;

    public interface OnCategoryActionListener {
        void onCategoryClick(CategoryK category);
        void onEditCategory(CategoryK category);
        void onCategorySelected(CategoryK category, boolean isSelected);
    }

    public CategoryAdapterK(List<CategoryK> categories, OnCategoryActionListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setSelectionMode(boolean isInSelectionMode, Set<String> selectedCategoryIds) {
        this.isInSelectionMode = isInSelectionMode;
        this.selectedCategoryIds = selectedCategoryIds;
        notifyDataSetChanged();
    }

    public boolean isInSelectionMode() {
        return isInSelectionMode;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryK category = categories.get(position);
        holder.tvCategoryName.setText(category.getName());

        if (isInSelectionMode) {
            holder.cbSelectCategory.setVisibility(View.VISIBLE);
            holder.btnEditCategory.setVisibility(View.GONE);

            holder.cbSelectCategory.setChecked(selectedCategoryIds.contains(category.getId()));

            holder.cbSelectCategory.setOnCheckedChangeListener(null);
            holder.cbSelectCategory.setOnCheckedChangeListener((buttonView, isChecked) -> listener.onCategorySelected(category, isChecked));

            holder.itemView.setOnClickListener(v -> holder.cbSelectCategory.setChecked(!holder.cbSelectCategory.isChecked()));

        } else {
            holder.cbSelectCategory.setVisibility(View.GONE);
            holder.btnEditCategory.setVisibility(View.VISIBLE);
            holder.cbSelectCategory.setChecked(false);

            holder.cbSelectCategory.setOnCheckedChangeListener(null);

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategoryClick(category);
                }
            });

            holder.btnEditCategory.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditCategory(category);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName;
        Button btnEditCategory;
        CheckBox cbSelectCategory;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            btnEditCategory = itemView.findViewById(R.id.btnEditCategory);
            cbSelectCategory = itemView.findViewById(R.id.cbSelectCategory);
        }
    }
}