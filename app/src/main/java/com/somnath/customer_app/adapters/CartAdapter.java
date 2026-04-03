package com.somnath.customer_app.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.somnath.customer_app.R;
import com.somnath.customer_app.config.ApiConfig;
import com.somnath.customer_app.databinding.ItemCartBinding; // --- NEW: Import ViewBinding
import com.somnath.customer_app.models.CartItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItemList = new ArrayList<>();
    private OnCartItemChangeListener listener;

    public interface OnCartItemChangeListener {
        void onQuantityChanged(CartItem item, int newQuantity);
        void onRemoveItem(CartItem item);
    }

    public void setCartItems(List<CartItem> cartItemList) {
        this.cartItemList = cartItemList;
        notifyDataSetChanged();
    }

    public void setOnCartItemChangeListener(OnCartItemChangeListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // --- NEW: Inflate using ViewBinding ---
        ItemCartBinding binding = ItemCartBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CartViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem cartItem = cartItemList.get(position);
        holder.bind(cartItem, listener);
    }

    @Override
    public int getItemCount() {
        return cartItemList != null ? cartItemList.size() : 0;
    }

    // --- UPDATED: ViewHolder now uses ViewBinding ---
    static class CartViewHolder extends RecyclerView.ViewHolder {
        private final ItemCartBinding binding;

        public CartViewHolder(ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final CartItem cartItem, final OnCartItemChangeListener listener) {
            binding.tvCartItemName.setText(cartItem.getMenuItem().getName());
            binding.tvCartItemQuantity.setText(String.valueOf(cartItem.getQuantity()));
            binding.tvCartItemPrice.setText(String.format(Locale.getDefault(), "₹%.2f", cartItem.getMenuItem().getPrice()));

            String imageUrl = buildFullImageUrl(cartItem.getMenuItem().getImageUrl());
            Glide.with(itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_menu_item)
                    .into(binding.ivCartItemImage);

            // --- NEW: Set click listeners for interactive controls ---
            binding.ivIncreaseQuantity.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onQuantityChanged(cartItem, cartItem.getQuantity() + 1);
                }
            });

            binding.ivDecreaseQuantity.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onQuantityChanged(cartItem, cartItem.getQuantity() - 1);
                }
            });

            binding.ivRemoveItem.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveItem(cartItem);
                }
            });
        }

        private String buildFullImageUrl(String relativeImageUrl) {
            if (relativeImageUrl == null || relativeImageUrl.isEmpty()) return null;
            if (relativeImageUrl.startsWith("http")) return relativeImageUrl;
            String baseUrl = ApiConfig.BASE_URL;
            if (baseUrl.endsWith("/") && relativeImageUrl.startsWith("/")) return baseUrl + relativeImageUrl.substring(1);
            if (!baseUrl.endsWith("/") && !relativeImageUrl.startsWith("/")) return baseUrl + "/" + relativeImageUrl;
            return baseUrl + relativeImageUrl;
        }
    }
}