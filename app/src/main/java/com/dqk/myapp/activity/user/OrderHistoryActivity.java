package com.dqk.myapp.activity.user;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dqk.myapp.R;
import com.dqk.myapp.adapter.OrderAdapter;
import com.dqk.myapp.database.DatabaseHelper;
import com.dqk.myapp.database.OrderDAO;
import com.dqk.myapp.model.Order;
import com.dqk.myapp.utils.SessionManager;

import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Lịch sử đơn hàng");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        SessionManager session = new SessionManager(this);
        OrderDAO orderDAO = new OrderDAO(DatabaseHelper.getInstance(this));

        RecyclerView rvOrders = findViewById(R.id.rvOrders);
        TextView tvEmpty      = findViewById(R.id.tvEmpty);

        // Lấy đơn hàng của user đang đăng nhập
        List<Order> orders = orderDAO.getOrdersByUserId(session.getUserId());

        if (orders.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvOrders.setVisibility(View.GONE);
        } else {
            rvOrders.setLayoutManager(new LinearLayoutManager(this));
            rvOrders.setAdapter(new OrderAdapter(this, orders, orderDAO));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}