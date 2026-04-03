package com.somnath.customer_app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.somnath.customer_app.R;
import com.somnath.customer_app.config.ApiConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<String> categoryList = new ArrayList<>();
    private Map<String, String> categoryImages = new HashMap<>();
    private OnCategoryClickListener listener;

    private int selectedPosition = 0;

    public interface OnCategoryClickListener {
        void onCategoryClick(String categoryName);
    }

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    public void setCategories(List<String> categories) {
        this.categoryList = categories;
        notifyDataSetChanged();
    }

    public void setCategoryImages(Map<String, String> categoryImages) {
        this.categoryImages = categoryImages;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String category = categoryList.get(position);
        holder.tvCategoryName.setText(category);

        // 1. Set the visual state (background and text color) for the selected item.
        if (position == selectedPosition) {
            holder.itemView.setBackgroundResource(R.drawable.bg_category_selected);
            holder.tvCategoryName.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
        } else {
            holder.itemView.setBackgroundResource(R.drawable.bg_category_unselected);
            holder.tvCategoryName.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_light_secondary));
        }

        // 2. Load the category image with the corrected, robust URL logic.
        String relativeImageUrl = categoryImages.get(category);

        if ("All".equalsIgnoreCase(category) || relativeImageUrl == null || relativeImageUrl.isEmpty()) {
            holder.ivCategoryIcon.setImageResource(R.drawable.ic_category_placeholder);
        } else {
            // --- THIS IS THE RESTORED AND CORRECT URL-BUILDING LOGIC ---
            String fullImageUrl;
            if (relativeImageUrl.startsWith("http://") || relativeImageUrl.startsWith("https://")) {
                fullImageUrl = relativeImageUrl;
            } else {
                String baseUrl = ApiConfig.BASE_URL;
                // This safely handles slashes to prevent issues like "base//path"
                if (baseUrl.endsWith("/") && relativeImageUrl.startsWith("/")) {
                    fullImageUrl = baseUrl + relativeImageUrl.substring(1);
                } else if (!baseUrl.endsWith("/") && !relativeImageUrl.startsWith("/")){
                    fullImageUrl = baseUrl + "/" + relativeImageUrl;
                } else {
                    fullImageUrl = baseUrl + relativeImageUrl;
                }
            }

            Glide.with(holder.itemView.getContext())
                    .load(fullImageUrl)
                    .placeholder(R.drawable.ic_category_placeholder)
                    .error(R.drawable.ic_category_placeholder)
                    .centerCrop()
                    .into(holder.ivCategoryIcon);
        }
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCategoryIcon;
        TextView tvCategoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.iv_category_icon);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onCategoryClick(categoryList.get(position));

                        // Update the selection and refresh the UI efficiently
                        int previousPosition = selectedPosition;
                        selectedPosition = position;
                        notifyItemChanged(previousPosition);
                        notifyItemChanged(selectedPosition);
                    }
                }
            });
        }
    }
}
