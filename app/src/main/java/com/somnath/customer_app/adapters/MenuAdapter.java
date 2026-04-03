package com.somnath.customer_app.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.somnath.customer_app.R;
import com.somnath.customer_app.config.ApiConfig;
import com.somnath.customer_app.models.MenuItem;

import java.util.List;
import com.bumptech.glide.request.RequestOptions;
import jp.wasabeef.glide.transformations.CropCircleWithBorderTransformation;
import androidx.core.content.ContextCompat;
public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {
    private List<MenuItem> menuItemList;
    private OnMenuItemClickListener onMenuItemClickListener;
    public interface OnMenuItemClickListener {
        void onMenuItemClick(MenuItem menuItem);
    }
    public void setMenuItems(List<MenuItem> menuItemList) {
        this.menuItemList = menuItemList;
        notifyDataSetChanged();
    }
    public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
        this.onMenuItemClickListener = listener;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_menu, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        final MenuItem menuItem = menuItemList.get(position);

        holder.itemNameTextView.setText(menuItem.getName());
        holder.itemDescriptionTextView.setText(menuItem.getDescription());
        holder.itemPriceTextView.setText(String.format("₹%.2f", menuItem.getPrice()));

        String relativeImageUrl = menuItem.getImageUrl();
        if (relativeImageUrl != null && !relativeImageUrl.isEmpty()) {
            String fullImageUrl;

            if (relativeImageUrl.startsWith("http://") || relativeImageUrl.startsWith("https://")) {
                fullImageUrl = relativeImageUrl;
            } else {
                String baseUrl = ApiConfig.BASE_URL;
                if (baseUrl.endsWith("/") && relativeImageUrl.startsWith("/")) {
                    fullImageUrl = baseUrl + relativeImageUrl.substring(1);
                } else if (!baseUrl.endsWith("/") && !relativeImageUrl.startsWith("/")) {
                    fullImageUrl = baseUrl + "/" + relativeImageUrl;
                } else {
                    fullImageUrl = baseUrl + relativeImageUrl;
                }
            }
            int borderWidth = 2; // in pixels
            int borderColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.menu_item_image_border);

            Glide.with(holder.itemView.getContext())
                    .load(fullImageUrl)
                    .apply(RequestOptions.bitmapTransform(new CropCircleWithBorderTransformation(borderWidth, borderColor)))
                    .placeholder(R.drawable.placeholder_menu_item)
                    .error(R.drawable.error_menu_item)
                    .into(holder.itemImageView);

        } else {
            holder.itemImageView.setImageResource(R.drawable.default_menu_item_image);
        }

        if (menuItem.isAvailable()) {
            holder.addItemButton.setVisibility(View.VISIBLE);
            holder.outOfStockTextView.setVisibility(View.GONE);
        } else {
            holder.addItemButton.setVisibility(View.GONE);
            holder.outOfStockTextView.setVisibility(View.VISIBLE);
        }

        holder.addItemButton.setOnClickListener(view -> {
            if (onMenuItemClickListener != null) {
                onMenuItemClickListener.onMenuItemClick(menuItem);
            }
        });
    }


    @Override
    public int getItemCount() {
        return menuItemList != null ? menuItemList.size() : 0;
    }

    static class MenuViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImageView;
        TextView itemNameTextView;
        TextView itemDescriptionTextView;
        TextView itemPriceTextView;
        TextView outOfStockTextView; // named for the "Out of Stock" label
        Button addItemButton;        //  named for the "ADD" button

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);

            // --- CORRECTED findViewById CALLS TO MATCH XML IDs ---
            itemImageView = itemView.findViewById(R.id.ivMenuItemImage);
            itemNameTextView = itemView.findViewById(R.id.tvMenuItemName);
            itemDescriptionTextView = itemView.findViewById(R.id.tvMenuItemDescription);
            itemPriceTextView = itemView.findViewById(R.id.tvMenuItemPrice);
            outOfStockTextView = itemView.findViewById(R.id.tvOutOfStock); // Matches id: @+id/tvOutOfStock
            addItemButton = itemView.findViewById(R.id.btnAddToCart);   // Matches id: @+id/btnAddToCart
        }
    }

}
