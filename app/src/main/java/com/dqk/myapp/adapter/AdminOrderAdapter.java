package com.dqk.myapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dqk.myapp.R;
import com.dqk.myapp.database.DatabaseHelper;
import com.dqk.myapp.database.OrderDAO;
import com.dqk.myapp.model.Order;
import com.dqk.myapp.model.OrderItem;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.ViewHolder> {

    private final Context context;
    private final List<Order> orderList;
    private final OrderDAO orderDAO;

    // Các trạng thái đơn hàng — dùng để hiển thị trong Spinner từng item
    private final String[] statusLabels = {"Chờ xác nhận", "Đã xác nhận", "Đã giao hàng", "Đã hủy"};
    private final String[] statusValues = {"pending", "confirmed", "delivered", "cancelled"};

    public AdminOrderAdapter(Context context, List<Order> orderList, OrderDAO orderDAO) {
        this.context   = context;
        this.orderList = orderList;
        this.orderDAO  = orderDAO;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_order_admin, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orderList.get(position);
        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));

        holder.tvOrderId.setText("Đơn hàng #" + order.getId());
        holder.tvUsername.setText("Khách: " + order.getUsername());
        holder.tvOrderDate.setText(order.getOrderDate());
        holder.tvTotalPrice.setText(fmt.format(order.getTotalPrice()) + " đ");

        // Hiển thị sản phẩm trong đơn
        List<OrderItem> items = orderDAO.getOrderItemsByOrderId(order.getId());
        StringBuilder sb = new StringBuilder();
        for (OrderItem item : items) {
            sb.append("• ").append(item.getProductName())
                    .append(" x").append(item.getQuantity()).append("\n");
        }
        if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1);
        holder.tvItems.setText(sb.toString());

        // Setup Spinner trạng thái — tìm vị trí trạng thái hiện tại
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                context, android.R.layout.simple_spinner_item, statusLabels);
        spinnerAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        holder.spinnerStatus.setAdapter(spinnerAdapter);

        // Tìm vị trí trạng thái hiện tại để set selected
        int currentPos = Arrays.asList(statusValues).indexOf(order.getStatus());
        if (currentPos >= 0) holder.spinnerStatus.setSelection(currentPos);

        // Khi admin đổi trạng thái → lưu vào DB ngay
        holder.spinnerStatus.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    boolean firstTime = true; // tránh trigger khi mới set selection

                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                                               View view, int pos, long id) {
                        if (firstTime) { firstTime = false; return; }
                        String newStatus = statusValues[pos];
                        if (!newStatus.equals(order.getStatus())) {
                            orderDAO.updateStatus(order.getId(), newStatus);
                            order.setStatus(newStatus);
                            Toast.makeText(context,
                                    "Đã cập nhật trạng thái đơn #" + order.getId(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
    }

    @Override
    public int getItemCount() { return orderList.size(); }

    public void updateList(List<Order> newList) {
        orderList.clear();
        orderList.addAll(newList);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvUsername, tvOrderDate, tvItems, tvTotalPrice;
        Spinner spinnerStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId    = itemView.findViewById(R.id.tvOrderId);
            tvUsername   = itemView.findViewById(R.id.tvUsername);
            tvOrderDate  = itemView.findViewById(R.id.tvOrderDate);
            tvItems      = itemView.findViewById(R.id.tvItems);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            spinnerStatus = itemView.findViewById(R.id.spinnerStatus);
        }
    }
}