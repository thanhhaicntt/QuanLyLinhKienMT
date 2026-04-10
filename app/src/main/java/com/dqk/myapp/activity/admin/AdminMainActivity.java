package com.dqk.myapp.activity.admin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.dqk.myapp.R;
import com.dqk.myapp.activity.LoginActivity;
import com.dqk.myapp.database.CategoryDAO;
import com.dqk.myapp.database.DatabaseHelper;
import com.dqk.myapp.database.OrderDAO;
import com.dqk.myapp.database.ProductDAO;
import com.dqk.myapp.database.UserDAO;
import com.dqk.myapp.utils.SessionManager;

public class AdminMainActivity extends AppCompatActivity {

    private SessionManager session;
    private UserDAO userDAO;
    private CategoryDAO categoryDAO;
    private ProductDAO productDAO;
    private OrderDAO orderDAO;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        session = new SessionManager(this);
        userDAO = new UserDAO(DatabaseHelper.getInstance(this));
        categoryDAO = new CategoryDAO(DatabaseHelper.getInstance(this));
        productDAO = new ProductDAO(DatabaseHelper.getInstance(this));
        orderDAO = new OrderDAO(DatabaseHelper.getInstance(this));

        // Hiển thị tên admin
        TextView tvAdminName = findViewById(R.id.tvAdminName);
        tvAdminName.setText("Xin chào, " + session.getFullName());

        loadStats();
        setupMenuCards();
        setupLogout();
    }

    // Hiển thị số lượng thống kê trên từng card
    @SuppressLint("SetTextI18n")
    private void loadStats() {
        TextView tvUserCount = findViewById(R.id.tvUserCount);
        TextView tvCategoryCount = findViewById(R.id.tvCategoryCount);
        TextView tvProductCount = findViewById(R.id.tvProductCount);
        TextView tvOrderCount = findViewById(R.id.tvOrderCount);

        tvUserCount.setText(userDAO.getAllUsers().size() + " người dùng");
        tvCategoryCount.setText(categoryDAO.getAll().size() + " danh mục");
        tvProductCount.setText(productDAO.getAllProducts().size() + " sản phẩm");
        tvOrderCount.setText(orderDAO.getAllOrders().size() + " đơn hàng");
    }

    private void setupMenuCards() {
        CardView cardUsers = findViewById(R.id.cardUsers);
        CardView cardCategories = findViewById(R.id.cardCategories);
        CardView cardProducts = findViewById(R.id.cardProducts);
        CardView cardOrders = findViewById(R.id.cardOrders);

        cardUsers.setOnClickListener(v ->
                startActivity(new Intent(this, AdminUserActivity.class))); // ← Chuyển sang AdminUserActivity


        cardCategories.setOnClickListener(v ->
                startActivity(new Intent(this, AdminCategoryActivity.class))); // ← Chuyển sang AdminCategoryActivity


        cardProducts.setOnClickListener(v ->
                startActivity(new Intent(this, AdminProductActivity.class))); // ← Chuyển sang AdminProductActivity


        cardOrders.setOnClickListener(v ->
                startActivity(new Intent(this, AdminOrderActivity.class))); // ← Chuyển sang AdminOrderActivity
    }

    private void setupLogout() {
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            session.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    // Reload stats khi quay lại từ màn hình con
    @Override
    protected void onResume() {
        super.onResume();
        loadStats();
    }
}