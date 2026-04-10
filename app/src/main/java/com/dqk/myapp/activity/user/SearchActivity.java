package com.dqk.myapp.activity.user;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dqk.myapp.R;
import com.dqk.myapp.adapter.ProductAdapter;
import com.dqk.myapp.database.DatabaseHelper;
import com.dqk.myapp.database.ProductDAO;
import com.dqk.myapp.model.Product;

import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private ProductDAO productDAO;
    private ProductAdapter adapter;
    private EditText etSearch;
    private TextView tvEmpty, tvResultCount;
    private RecyclerView rvResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Tìm kiếm");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        productDAO    = new ProductDAO(DatabaseHelper.getInstance(this));
        etSearch      = findViewById(R.id.etSearch);
        tvEmpty       = findViewById(R.id.tvEmpty);
        tvResultCount = findViewById(R.id.tvResultCount);
        rvResults     = findViewById(R.id.rvResults);
        Button btnSearch = findViewById(R.id.btnSearch);

        rvResults.setLayoutManager(new GridLayoutManager(this, 2));

        // Nhận keyword từ trang chủ nếu có
        String keyword = getIntent().getStringExtra("keyword");
        if (keyword != null && !keyword.isEmpty()) {
            etSearch.setText(keyword);
            performSearch(keyword);
        }

        // Nhấn nút Tìm
        btnSearch.setOnClickListener(v ->
                performSearch(etSearch.getText().toString().trim()));

        // Nhấn Enter trên bàn phím
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(etSearch.getText().toString().trim());
                return true;
            }
            return false;
        });
    }

    @SuppressLint("SetTextI18n")
    private void performSearch(String keyword) {
        if (keyword.isEmpty()) {
            tvEmpty.setText("Nhập từ khóa để tìm kiếm");
            tvEmpty.setVisibility(View.VISIBLE);
            rvResults.setVisibility(View.GONE);
            tvResultCount.setVisibility(View.GONE);
            return;
        }

        List<Product> results = productDAO.searchProducts(keyword);

        if (results.isEmpty()) {
            tvEmpty.setText("Không tìm thấy sản phẩm nào với từ khóa \"" + keyword + "\"");
            tvEmpty.setVisibility(View.VISIBLE);
            rvResults.setVisibility(View.GONE);
            tvResultCount.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvResults.setVisibility(View.VISIBLE);
            tvResultCount.setVisibility(View.VISIBLE);
            tvResultCount.setText("Tìm thấy " + results.size() + " sản phẩm");

            if (adapter == null) {
                adapter = new ProductAdapter(this, results);
                adapter.setOnItemClickListener(product -> {
                    Intent intent = new Intent(this, ProductDetailActivity.class);
                    intent.putExtra("product_id", product.getId());
                    startActivity(intent);
                });
                rvResults.setAdapter(adapter);
            } else {
                adapter.updateList(results);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}