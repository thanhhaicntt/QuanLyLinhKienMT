package com.dqk.myapp.adapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dqk.myapp.R;
import com.dqk.myapp.database.DatabaseHelper;
import com.dqk.myapp.database.OrderDAO;
import com.dqk.myapp.model.Order;
import com.dqk.myapp.model.OrderItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private final Context context;
    private final List<Order> orderList;
    private final OrderDAO orderDAO;

    public OrderAdapter(Context context, List<Order> orderList, OrderDAO orderDAO) {
        this.context   = context;
        this.orderList = orderList;
        this.orderDAO  = orderDAO;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orderList.get(position);
        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));

        holder.tvOrderId.setText("Đơn hàng #" + order.getId());
        holder.tvOrderDate.setText("Ngày đặt: " + order.getOrderDate());
        holder.tvTotalPrice.setText(fmt.format(order.getTotalPrice()) + " đ");

        // Hiển thị trạng thái với màu tương ứng
        setStatusStyle(holder.tvStatus, order.getStatus());

        // Hiển thị danh sách sản phẩm trong đơn
        List<OrderItem> items = orderDAO.getOrderItemsByOrderId(order.getId());
        StringBuilder sb = new StringBuilder();
        for (OrderItem item : items) {
            sb.append("• ").append(item.getProductName())
                    .append(" x").append(item.getQuantity()).append("\n");
        }
        if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1);
        holder.tvItems.setText(sb.toString());
    }

    @SuppressLint("SetTextI18n")
    private void setStatusStyle(TextView tvStatus, String status) {
        switch (status) {
            case "pending":
                tvStatus.setText("Chờ xác nhận");
                tvStatus.setBackgroundColor(Color.parseColor("#FF9800"));
                break;
            case "confirmed":
                tvStatus.setText("Đã xác nhận");
                tvStatus.setBackgroundColor(Color.parseColor("#1565C0"));
                break;
            case "delivered":
                tvStatus.setText("Đã giao hàng");
                tvStatus.setBackgroundColor(Color.parseColor("#388E3C"));
                break;
            case "cancelled":
                tvStatus.setText("Đã hủy");
                tvStatus.setBackgroundColor(Color.parseColor("#E53935"));
                break;
            default:
                tvStatus.setText(status);
                tvStatus.setBackgroundColor(Color.parseColor("#888888"));
        }
    }

    @Override
    public int getItemCount() { return orderList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvStatus, tvOrderDate, tvItems, tvTotalPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId   = itemView.findViewById(R.id.tvOrderId);
            tvStatus    = itemView.findViewById(R.id.tvStatus);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvItems     = itemView.findViewById(R.id.tvItems);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
        }
    }
}