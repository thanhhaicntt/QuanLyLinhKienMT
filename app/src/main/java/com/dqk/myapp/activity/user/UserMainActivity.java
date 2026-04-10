package com.dqk.myapp.activity.user;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dqk.myapp.R;
import com.dqk.myapp.activity.LoginActivity;
import com.dqk.myapp.adapter.CategoryAdapter;
import com.dqk.myapp.adapter.ProductAdapter;
import com.dqk.myapp.database.CategoryDAO;
import com.dqk.myapp.database.DatabaseHelper;
import com.dqk.myapp.database.ProductDAO;
import com.dqk.myapp.model.Category;
import com.dqk.myapp.model.Product;
import com.dqk.myapp.utils.SessionManager;

import java.util.List;

public class UserMainActivity extends AppCompatActivity {

    private ProductDAO productDAO;
    private CategoryDAO categoryDAO;
    private SessionManager session;

    private ProductAdapter newProductAdapter, bestSellerAdapter, searchAdapter;
    private RecyclerView rvNewProducts, rvBestSeller, rvCategories;
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);

        session = new SessionManager(this);
        productDAO = new ProductDAO(DatabaseHelper.getInstance(this));
        categoryDAO = new CategoryDAO(DatabaseHelper.getInstance(this));

        initViews();
        loadData();
        setupSearch();
        setupLogout();
    }

    private void initViews() {
        rvCategories = findViewById(R.id.rvCategories);
        rvNewProducts = findViewById(R.id.rvNewProducts);
        rvBestSeller = findViewById(R.id.rvBestSeller);
        etSearch = findViewById(R.id.etSearch);

        rvCategories.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvNewProducts.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvBestSeller.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    private void loadData() {
        // Load danh mục
        List<Category> categories = categoryDAO.getAll();
        CategoryAdapter categoryAdapter = new CategoryAdapter(this, categories);
        categoryAdapter.setOnItemClickListener(category -> {
            // Mở màn hình danh sách sản phẩm theo danh mục
            Intent intent = new Intent(this, ProductListActivity.class);
            intent.putExtra("category_id", category.getId());
            intent.putExtra("category_name", category.getName());
            startActivity(intent);
        });
        rvCategories.setAdapter(categoryAdapter);

        // Load sản phẩm mới (6 sản phẩm)
        List<Product> newProducts = productDAO.getNewestProducts(6);
        newProductAdapter = new ProductAdapter(this, newProducts);
        newProductAdapter.setOnItemClickListener(this::openProductDetail);
        rvNewProducts.setAdapter(newProductAdapter);

        // Load sản phẩm bán chạy (6 sản phẩm)
        List<Product> bestSellers = productDAO.getBestSellerProducts(6);
        bestSellerAdapter = new ProductAdapter(this, bestSellers);
        bestSellerAdapter.setOnItemClickListener(this::openProductDetail);
        rvBestSeller.setAdapter(bestSellerAdapter);
    }

    private void setupSearch() {
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            String keyword = etSearch.getText().toString().trim();
            if (!keyword.isEmpty()) {
                Intent intent = new Intent(this, SearchActivity.class);
                intent.putExtra("keyword", keyword);
                startActivity(intent);
                etSearch.setText("");
            }
            return true;
        });
    }

    private void setupLogout() {
        // Sửa từ TextView sang LinearLayout
        LinearLayout tvCart = findViewById(R.id.tvCart);
        LinearLayout tvOrderHistory = findViewById(R.id.tvOrderHistory);
        LinearLayout tvLogout = findViewById(R.id.tvLogout);

        tvCart.setOnClickListener(v ->
                startActivity(new Intent(this, CartActivity.class)));

        tvOrderHistory.setOnClickListener(v ->
                startActivity(new Intent(this, OrderHistoryActivity.class)));

        tvLogout.setOnClickListener(v -> {
            session.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void openProductDetail(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        startActivity(intent);
    }
}