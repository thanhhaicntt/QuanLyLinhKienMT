package com.dqk.myapp.activity.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dqk.myapp.R;
import com.dqk.myapp.adapter.CategoryAdminAdapter;
import com.dqk.myapp.database.CategoryDAO;
import com.dqk.myapp.database.DatabaseHelper;
import com.dqk.myapp.model.Category;

import java.util.List;

public class AdminCategoryActivity extends AppCompatActivity {

    private CategoryDAO categoryDAO;
    private CategoryAdminAdapter adapter;
    private List<Category> categoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_category);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản lý Danh mục");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        categoryDAO = new CategoryDAO(DatabaseHelper.getInstance(this));

        setupRecyclerView();

        Button btnAdd = findViewById(R.id.btnAddCategory);
        btnAdd.setOnClickListener(v -> showCategoryDialog(null));
    }

    private void setupRecyclerView() {
        categoryList = categoryDAO.getAll();
        RecyclerView rvCategories = findViewById(R.id.rvCategories);
        TextView tvEmpty          = findViewById(R.id.tvEmpty);

        if (categoryList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvCategories.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvCategories.setVisibility(View.VISIBLE);
            adapter = new CategoryAdminAdapter(this, categoryList);
            rvCategories.setLayoutManager(new LinearLayoutManager(this));
            rvCategories.setAdapter(adapter);

            adapter.setOnActionListener(new CategoryAdminAdapter.OnActionListener() {
                @Override
                public void onEdit(Category category) {
                    showCategoryDialog(category);
                }

                @Override
                public void onDelete(Category category) {
                    showDeleteDialog(category);
                }
            });
        }
    }

    private void showCategoryDialog(Category existing) {
        boolean isEdit  = (existing != null);
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_category, null);

        EditText etName = dialogView.findViewById(R.id.etCategoryName);
        EditText etDesc = dialogView.findViewById(R.id.etDescription);

        if (isEdit) {
            etName.setText(existing.getName());
            etDesc.setText(existing.getDescription());
        }

        new AlertDialog.Builder(this)
                .setTitle(isEdit ? "Sửa danh mục" : "Thêm danh mục mới")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String desc = etDesc.getText().toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập tên danh mục",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (isEdit) {
                        existing.setName(name);
                        existing.setDescription(desc);
                        int result = categoryDAO.update(existing);
                        if (result > 0) {
                            Toast.makeText(this, "Cập nhật thành công",
                                    Toast.LENGTH_SHORT).show();
                            refreshList();
                        }
                    } else {
                        Category newCat = new Category(name, desc);
                        long result = categoryDAO.insert(newCat);
                        if (result != -1) {
                            Toast.makeText(this, "Thêm danh mục thành công",
                                    Toast.LENGTH_SHORT).show();
                            refreshList();
                        }
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDeleteDialog(Category category) {
        // Kiểm tra còn sản phẩm không trước khi xóa
        if (categoryDAO.hasProducts(category.getId())) {
            new AlertDialog.Builder(this)
                    .setTitle("Không thể xóa")
                    .setMessage("Danh mục \"" + category.getName()
                            + "\" đang có sản phẩm. Vui lòng xóa hoặc chuyển sản phẩm trước.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa danh mục \""
                        + category.getName() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    int result = categoryDAO.delete(category.getId());
                    if (result > 0) {
                        Toast.makeText(this, "Đã xóa danh mục",
                                Toast.LENGTH_SHORT).show();
                        refreshList();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void refreshList() {
        categoryList = categoryDAO.getAll();
        if (adapter != null) {
            adapter.updateList(categoryList);
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