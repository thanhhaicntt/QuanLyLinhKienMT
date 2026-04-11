package com.dqk.myapp.activity.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dqk.myapp.R;
import com.dqk.myapp.adapter.ProductAdminAdapter;
import com.dqk.myapp.database.CategoryDAO;
import com.dqk.myapp.database.DatabaseHelper;
import com.dqk.myapp.database.ProductDAO;
import com.dqk.myapp.model.Category;
import com.dqk.myapp.model.Product;

import java.util.List;

public class AdminProductActivity extends AppCompatActivity {

    private ProductDAO productDAO;
    private CategoryDAO categoryDAO;
    private ProductAdminAdapter adapter;
    private List<Product> productList;
    private List<Category> categoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_product);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản lý Sản phẩm");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        productDAO  = new ProductDAO(DatabaseHelper.getInstance(this));
        categoryDAO = new CategoryDAO(DatabaseHelper.getInstance(this));
        categoryList = categoryDAO.getAll();

        setupRecyclerView();
        setupSearch();

        Button btnAdd = findViewById(R.id.btnAddProduct);
        btnAdd.setOnClickListener(v -> showProductDialog(null));
    }

    private void setupRecyclerView() {
        productList = productDAO.getAllProducts();
        RecyclerView rvProducts = findViewById(R.id.rvProducts);
        TextView tvEmpty        = findViewById(R.id.tvEmpty);

        if (productList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvProducts.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvProducts.setVisibility(View.VISIBLE);
            adapter = new ProductAdminAdapter(this, productList);
            rvProducts.setLayoutManager(new LinearLayoutManager(this));
            rvProducts.setAdapter(adapter);

            adapter.setOnActionListener(new ProductAdminAdapter.OnActionListener() {
                @Override
                public void onEdit(Product product) {
                    showProductDialog(product);
                }

                @Override
                public void onDelete(Product product) {
                    showDeleteDialog(product);
                }
            });
        }
    }

    private void setupSearch() {
        EditText etSearch = findViewById(R.id.etSearch);
        Button btnSearch  = findViewById(R.id.btnSearch);

        btnSearch.setOnClickListener(v ->
                performSearch(etSearch.getText().toString().trim()));

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(etSearch.getText().toString().trim());
                return true;
            }
            return false;
        });
    }

    private void performSearch(String keyword) {
        List<Product> results = keyword.isEmpty()
                ? productDAO.getAllProducts()
                : productDAO.searchProducts(keyword);
        if (adapter != null) adapter.updateList(results);
    }

    private void showProductDialog(Product existing) {
        boolean isEdit  = (existing != null);
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_product, null);

        EditText etName    = dialogView.findViewById(R.id.etName);
        EditText etDesc    = dialogView.findViewById(R.id.etDescription);
        EditText etPrice   = dialogView.findViewById(R.id.etPrice);
        EditText etStock   = dialogView.findViewById(R.id.etStock);
        EditText etImgUrl  = dialogView.findViewById(R.id.etImageUrl);
        Spinner spinner    = dialogView.findViewById(R.id.spinnerCategory);
        EditText etSpec = dialogView.findViewById(R.id.etSpecification);


        // Setup Spinner danh mục
        // Lấy danh sách tên danh mục để hiển thị trong Spinner
        String[] categoryNames = new String[categoryList.size()];
        for (int i = 0; i < categoryList.size(); i++) {
            categoryNames[i] = categoryList.get(i).getName();
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categoryNames);
        spinnerAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        // Điền sẵn dữ liệu nếu là sửa
        if (isEdit) {
            etName.setText(existing.getName());
            etDesc.setText(existing.getDescription());
            etPrice.setText(String.valueOf((int) existing.getPrice()));
            etStock.setText(String.valueOf(existing.getStock()));
            etImgUrl.setText(existing.getImageUrl());
            etSpec.setText(existing.getSpecification());

            // Tìm vị trí danh mục hiện tại trong Spinner
            for (int i = 0; i < categoryList.size(); i++) {
                if (categoryList.get(i).getId() == existing.getCategoryId()) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(isEdit ? "Sửa sản phẩm" : "Thêm sản phẩm mới")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String name   = etName.getText().toString().trim();
                    String desc   = etDesc.getText().toString().trim();
                    String price  = etPrice.getText().toString().trim();
                    String stock  = etStock.getText().toString().trim();
                    String imgUrl = etImgUrl.getText().toString().trim();
                    String spec = etSpec.getText().toString().trim();

                    // Validate
                    if (name.isEmpty() || price.isEmpty() || stock.isEmpty()) {
                        Toast.makeText(this,
                                "Vui lòng điền tên, giá và số lượng",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Lấy category_id từ vị trí Spinner đang chọn
                    int selectedPos  = spinner.getSelectedItemPosition();
                    int categoryId   = categoryList.get(selectedPos).getId();

                    double priceVal  = Double.parseDouble(price);
                    int stockVal     = Integer.parseInt(stock);

                    if (isEdit) {
                        existing.setName(name);
                        existing.setDescription(desc);
                        existing.setPrice(priceVal);
                        existing.setStock(stockVal);
                        existing.setImageUrl(imgUrl);
                        existing.setCategoryId(categoryId);
                        existing.setSpecification(spec);
                        int result = productDAO.update(existing);
                        if (result > 0) {
                            Toast.makeText(this, "Cập nhật thành công",
                                    Toast.LENGTH_SHORT).show();
                            refreshList();
                        }
                    } else {
                        // Mặc định 2 ảnh nếu không nhập
                        String defaultImages = "https://cdn.phototourl.com/member/2026-04-11-ae2c6c59-d134-41ae-a490-2c80a94d2ce9.jpg,https://cdn.phototourl.com/member/2026-04-11-95688c21-edb3-4f79-9ece-b17aa38a12d8.jpg";
                        String finalImgUrl = (imgUrl.isEmpty()) ? defaultImages : imgUrl;

                        Product newProduct = new Product(
                                categoryId, name, desc, priceVal, stockVal, finalImgUrl);
                        newProduct.setSpecification(spec);
                        long result = productDAO.insert(newProduct);
                        if (result != -1) {
                            Toast.makeText(this, "Thêm sản phẩm thành công",
                                    Toast.LENGTH_SHORT).show();
                            refreshList();
                        }
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDeleteDialog(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Ẩn sản phẩm")
                .setMessage("Sản phẩm \"" + product.getName()
                        + "\" sẽ bị ẩn khỏi danh sách.\nBạn có chắc không?")
                .setPositiveButton("Ẩn", (dialog, which) -> {
                    int result = productDAO.delete(product.getId());
                    if (result > 0) {
                        Toast.makeText(this, "Đã ẩn sản phẩm",
                                Toast.LENGTH_SHORT).show();
                        refreshList();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void refreshList() {
        productList = productDAO.getAllProducts();
        if (adapter != null) {
            adapter.updateList(productList);
        } else {
            setupRecyclerView();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}