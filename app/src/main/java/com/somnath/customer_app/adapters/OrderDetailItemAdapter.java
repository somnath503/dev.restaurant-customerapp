package com.somnath.customer_app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.somnath.customer_app.R;
import com.somnath.customer_app.models.OrderItem;
import java.util.List;
import java.util.Locale;

public class OrderDetailItemAdapter extends RecyclerView.Adapter<OrderDetailItemAdapter.ViewHolder> {

    private final List<OrderItem> items;

    public OrderDetailItemAdapter(List<OrderItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderItem item = items.get(position);
        holder.tvQuantity.setText(String.format(Locale.getDefault(), "%d x", item.getQuantity()));
        holder.tvName.setText(item.getName());
        holder.tvPrice.setText(String.format(Locale.getDefault(), "₹%.2f", item.getPrice()));
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuantity, tvName, tvPrice;
        ViewHolder(View itemView) {
            super(itemView);
            tvQuantity = itemView.findViewById(R.id.text_view_item_quantity); // Updated ID
            tvName = itemView.findViewById(R.id.text_view_item_name);       // Updated ID
            tvPrice = itemView.findViewById(R.id.text_view_item_price);       // Updated ID
        }
    }
}
