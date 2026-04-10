package com.dqk.myapp.activity.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dqk.myapp.R;
import com.dqk.myapp.adapter.UserAdapter;
import com.dqk.myapp.database.DatabaseHelper;
import com.dqk.myapp.database.UserDAO;
import com.dqk.myapp.model.User;
import com.dqk.myapp.utils.SessionManager;

import java.util.List;

public class AdminUserActivity extends AppCompatActivity {

    private UserDAO userDAO;
    private UserAdapter adapter;
    private List<User> userList;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản lý User");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        userDAO = new UserDAO(DatabaseHelper.getInstance(this));
        session = new SessionManager(this);

        setupRecyclerView();
        setupSearch();

        Button btnAdd = findViewById(R.id.btnAddUser);
        btnAdd.setOnClickListener(v -> showUserDialog(null));
    }

    private void setupRecyclerView() {
        userList = userDAO.getAllUsers();
        RecyclerView rvUsers = findViewById(R.id.rvUsers);
        TextView tvEmpty     = findViewById(R.id.tvEmpty);

        if (userList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvUsers.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvUsers.setVisibility(View.VISIBLE);
            adapter = new UserAdapter(this, userList);
            rvUsers.setLayoutManager(new LinearLayoutManager(this));
            rvUsers.setAdapter(adapter);

            adapter.setOnActionListener(new UserAdapter.OnActionListener() {
                @Override
                public void onEdit(User user) {
                    showUserDialog(user);
                }

                @Override
                public void onDelete(User user) {
                    showDeleteDialog(user);
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
        List<User> results = keyword.isEmpty()
                ? userDAO.getAllUsers()
                : userDAO.searchUsers(keyword);
        if (adapter != null) adapter.updateList(results);
    }

    // Dialog dùng chung cho cả thêm và sửa
    private void showUserDialog(User existingUser) {
        boolean isEdit = (existingUser != null);
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_user, null);

        EditText etFullName = dialogView.findViewById(R.id.etFullName);
        EditText etUsername = dialogView.findViewById(R.id.etUsername);
        EditText etEmail    = dialogView.findViewById(R.id.etEmail);
        EditText etPhone    = dialogView.findViewById(R.id.etPhone);
        EditText etPassword = dialogView.findViewById(R.id.etPassword);
        RadioGroup rgRole   = dialogView.findViewById(R.id.rgRole);
        RadioButton rbAdmin = dialogView.findViewById(R.id.rbAdmin);
        RadioButton rbUser  = dialogView.findViewById(R.id.rbUser);

        // Nếu là sửa thì điền sẵn dữ liệu
        if (isEdit) {
            etFullName.setText(existingUser.getFullname());
            etUsername.setText(existingUser.getUsername());
            etEmail.setText(existingUser.getEmail());
            etPhone.setText(existingUser.getPhone());
            etPassword.setText(existingUser.getPassword());
            if ("admin".equals(existingUser.getRole())) {
                rbAdmin.setChecked(true);
            } else {
                rbUser.setChecked(true);
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(isEdit ? "Sửa thông tin User" : "Thêm User mới")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String fullName = etFullName.getText().toString().trim();
                    String username = etUsername.getText().toString().trim();
                    String email    = etEmail.getText().toString().trim();
                    String phone    = etPhone.getText().toString().trim();
                    String password = etPassword.getText().toString().trim();
                    String role     = (rgRole.getCheckedRadioButtonId() == R.id.rbAdmin)
                            ? "admin" : "user";

                    // Validate
                    if (fullName.isEmpty() || username.isEmpty()
                            || email.isEmpty() || password.isEmpty()) {
                        Toast.makeText(this, "Vui lòng điền đầy đủ thông tin",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (isEdit) {
                        // Không cho sửa role của chính mình
                        if (existingUser.getId() == session.getUserId() && !"admin".equals(role)) {
                            Toast.makeText(this,
                                    "Không thể tự hạ quyền Admin của chính mình",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        existingUser.setFullName(fullName);
                        existingUser.setUsername(username);
                        existingUser.setEmail(email);
                        existingUser.setPhone(phone);
                        existingUser.setPassword(password);
                        existingUser.setRole(role);
                        int result = userDAO.update(existingUser);
                        if (result > 0) {
                            Toast.makeText(this, "Cập nhật thành công",
                                    Toast.LENGTH_SHORT).show();
                            refreshList();
                        }
                    } else {
                        // Kiểm tra username đã tồn tại chưa
                        if (userDAO.isUsernameExists(username)) {
                            Toast.makeText(this, "Tên đăng nhập đã tồn tại",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        User newUser = new User(username, password,
                                fullName, email, phone, role);
                        long result = userDAO.insert(newUser);
                        if (result != -1) {
                            Toast.makeText(this, "Thêm user thành công",
                                    Toast.LENGTH_SHORT).show();
                            refreshList();
                        }
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDeleteDialog(User user) {
        // Không cho xóa chính mình
        if (user.getId() == session.getUserId()) {
            Toast.makeText(this, "Không thể xóa tài khoản đang đăng nhập",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa user \"" + user.getUsername() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    int result = userDAO.delete(user.getId());
                    if (result > 0) {
                        Toast.makeText(this, "Đã xóa user",
                                Toast.LENGTH_SHORT).show();
                        refreshList();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void refreshList() {
        userList = userDAO.getAllUsers();
        if (adapter != null) adapter.updateList(userList);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}