package com.dqk.myapp.activity.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dqk.myapp.R;
import com.dqk.myapp.adapter.AdminOrderAdapter;
import com.dqk.myapp.database.CategoryDAO;
import com.dqk.myapp.database.DatabaseHelper;
import com.dqk.myapp.database.OrderDAO;
import com.dqk.myapp.model.Category;
import com.dqk.myapp.model.Order;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminOrderActivity extends AppCompatActivity {

    private OrderDAO orderDAO;
    private CategoryDAO categoryDAO;
    private AdminOrderAdapter adapter;
    private List<Order> orderList;
    private List<Category> categoryList;

    private Spinner spinnerStatus, spinnerCategory;
    private String currentStatus = "all";
    private int currentCategoryId = -1; // -1 = tất cả danh mục

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản lý Đơn hàng");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        orderDAO    = new OrderDAO(DatabaseHelper.getInstance(this));
        categoryDAO = new CategoryDAO(DatabaseHelper.getInstance(this));
        categoryList = categoryDAO.getAll();

        loadStats();
        setupFilters();
        loadOrders();
    }

    // Hiển thị số liệu thống kê trên đầu màn hình
    private void loadStats() {
        TextView tvTotal   = findViewById(R.id.tvTotalOrders);
        TextView tvRevenue = findViewById(R.id.tvTotalRevenue);
        TextView tvPending = findViewById(R.id.tvPendingOrders);

        int totalOrders    = orderDAO.getAllOrders().size();
        double revenue     = orderDAO.getTotalRevenue();
        int pendingOrders  = orderDAO.countPendingOrders();

        tvTotal.setText(String.valueOf(totalOrders));
        tvPending.setText(String.valueOf(pendingOrders));

        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
        tvRevenue.setText(fmt.format(revenue) + "đ");
    }

    private void setupFilters() {
        spinnerStatus   = findViewById(R.id.spinnerStatus);
        spinnerCategory = findViewById(R.id.spinnerCategory);

        // Filter theo trạng thái
        String[] statusLabels = {
                "Tất cả", "Chờ xác nhận", "Đã xác nhận",
                "Đã giao hàng", "Đã hủy"};
        final String[] statusValues = {
                "all", "pending", "confirmed", "delivered", "cancelled"};

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, statusLabels);
        statusAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        spinnerStatus.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                                               View view, int pos, long id) {
                        currentStatus = statusValues[pos];
                        loadOrders();
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });

        // Filter theo danh mục
        List<String> catLabels = new ArrayList<>();
        catLabels.add("Tất cả danh mục");
        for (Category c : categoryList) catLabels.add(c.getName());

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, catLabels);
        catAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);

        spinnerCategory.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                                               View view, int pos, long id) {
                        // pos=0 là "Tất cả", pos>0 thì lấy category index (pos-1)
                        currentCategoryId = (pos == 0)
                                ? -1 : categoryList.get(pos - 1).getId();
                        loadOrders();
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
    }

    private void loadOrders() {
        // Ưu tiên lọc theo danh mục nếu có chọn
        if (currentCategoryId != -1) {
            orderList = orderDAO.getOrdersByCategory(currentCategoryId);
        } else {
            orderList = orderDAO.getOrdersByStatus(currentStatus);
        }

        RecyclerView rvOrders = findViewById(R.id.rvOrders);
        TextView tvEmpty      = findViewById(R.id.tvEmpty);

        if (orderList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvOrders.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvOrders.setVisibility(View.VISIBLE);

            if (adapter == null) {
                adapter = new AdminOrderAdapter(this, orderList, orderDAO);
                rvOrders.setLayoutManager(new LinearLayoutManager(this));
                rvOrders.setAdapter(adapter);
            } else {
                adapter.updateList(orderList);
            }
        }
    }

    // Reload stats khi quay lại màn hình
    @Override
    protected void onResume() {
        super.onResume();
        loadStats();
        loadOrders();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}