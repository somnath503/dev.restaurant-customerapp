package com.somnath.customer_app.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.somnath.customer_app.R;
import com.somnath.customer_app.activities.OrderDetailActivity;
import com.somnath.customer_app.models.Order;
import com.somnath.customer_app.models.OrderItem;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private final Context context;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
        void onCancelOrderClick(Order order);
    }

    public void setOnOrderClickListener(OnOrderClickListener listener) {
        this.listener = listener;
    }

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.bind(order, listener);
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    public void setOrders(List<Order> orders) {
        this.orderList = orders;
        notifyDataSetChanged();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvStatusAndDate, tvProductName, tvOrderTotal;
        Button btnCancelOrder;

        OrderViewHolder(View itemView) {
            super(itemView);
            tvStatusAndDate = itemView.findViewById(R.id.tv_order_status_and_date);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvOrderTotal = itemView.findViewById(R.id.tv_order_total);
            btnCancelOrder = itemView.findViewById(R.id.btn_cancel_order);
        }

        void bind(Order order, OnOrderClickListener listener) {
            // Determine Status and Date Text
            String status = order.getStatus() != null ? order.getStatus() : "Unknown";
            LocalDateTime orderDate = order.getOrderDate();
            String formattedDate = "";
            if (orderDate != null) {
                formattedDate = orderDate.format(DateTimeFormatter.ofPattern("MMM dd"));
            }

            String statusLine;
            int statusColor;
            boolean showCancelButton = false;

            switch (status.toUpperCase(Locale.ROOT)) {
                case "DELIVERED":
                    statusLine = "Delivered on " + formattedDate;
                    statusColor = ContextCompat.getColor(context, R.color.status_delivered);
                    break;
                case "CANCELLED":
                case "REJECTED":
                case "REFUNDED":
                    statusLine = status.toUpperCase(Locale.ROOT) + " on " + formattedDate;
                    statusColor = ContextCompat.getColor(context, R.color.status_cancelled);
                    break;
                case "PENDING":
                    statusLine = "Pending on " + formattedDate;
                    statusColor = ContextCompat.getColor(context, R.color.status_pending_orange);
                    showCancelButton = true;
                    break;
                default: // Covers PREPARING, ACCEPTED, OUT_FOR_DELIVERY, etc.
                    statusLine = status.toUpperCase(Locale.ROOT).replace("_", " ") + " on " + formattedDate;
                    statusColor = ContextCompat.getColor(context, R.color.status_preparing_blue);
                    break;
            }

            tvStatusAndDate.setText(statusLine);
            tvStatusAndDate.setTextColor(statusColor);
            btnCancelOrder.setVisibility(showCancelButton ? View.VISIBLE : View.GONE);

            // Special handling for "ACCEPTED" status to show a disabled button
            if (order.getStatus().equalsIgnoreCase("ACCEPTED")) {
                btnCancelOrder.setVisibility(View.VISIBLE);
                btnCancelOrder.setEnabled(false);
                btnCancelOrder.setAlpha(0.5f);
            }

            // Build Item Summary String - This will now work correctly
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                int itemsCount = order.getItems().size();
                StringBuilder summary = new StringBuilder();
                for (int i = 0; i < Math.min(itemsCount, 2); i++) {
                    OrderItem item = order.getItems().get(i);
                    if (item != null && item.getName() != null) {
                        if (i > 0) summary.append(", ");
                        summary.append(item.getName());
                    }
                }
                if (itemsCount > 2) {
                    summary.append(" & ").append(itemsCount - 2).append(" more");
                }
                tvProductName.setText(summary.toString());
            } else {
                tvProductName.setText("No items in this order");
            }

            // Set Total Amount
            if (order.getTotalAmount() != null) {
                tvOrderTotal.setText(String.format(Locale.getDefault(), "₹%s", order.getTotalAmount()));
            } else {
                tvOrderTotal.setText("₹0.00");
            }

            // Set Click Listeners
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, OrderDetailActivity.class);
                Gson gson = new Gson();
                intent.putExtra("ORDER_DETAILS_JSON", gson.toJson(order));
                context.startActivity(intent);
            });

            btnCancelOrder.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCancelOrderClick(order);
                }
            });
        }
    }
}
