package com.dqk.myapp.activity.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

public class ProductListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        ProductDAO productDAO = new ProductDAO(DatabaseHelper.getInstance(this));

        // Lấy thông tin danh mục được truyền vào
        int categoryId     = getIntent().getIntExtra("category_id", -1);
        String categoryName = getIntent().getStringExtra("category_name");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(categoryName != null ? categoryName : "Sản phẩm");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView rvProducts = findViewById(R.id.rvProducts);
        TextView tvEmpty = findViewById(R.id.tvEmpty);

        // Grid 2 cột
        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));

        // Load sản phẩm theo danh mục
        List<Product> products = (categoryId != -1)
                ? productDAO.getByCategory(categoryId)
                : productDAO.getAllProducts();

        if (products.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvProducts.setVisibility(View.GONE);
        } else {
            ProductAdapter adapter = new ProductAdapter(this, products);
            adapter.setOnItemClickListener(product -> {
                Intent intent = new Intent(this, ProductDetailActivity.class);
                intent.putExtra("product_id", product.getId());
                startActivity(intent);
            });
            rvProducts.setAdapter(adapter);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}