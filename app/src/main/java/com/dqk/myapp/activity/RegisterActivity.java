package com.dqk.myapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dqk.myapp.R;
import com.dqk.myapp.database.DatabaseHelper;
import com.dqk.myapp.database.UserDAO;
import com.dqk.myapp.model.User;

public class RegisterActivity extends AppCompatActivity {
    private EditText etFullName, etUsername, etEmail, etPhone, etPassword;
    private UserDAO userDAO;

    @Override // Khai báo layout cho activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userDAO = new UserDAO(DatabaseHelper.getInstance(this));

        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        Button bthRegister = findViewById(R.id.btnRegister);
        TextView tvLogin = findViewById(R.id.tvLogin);

        bthRegister.setOnClickListener(v -> handleRegister());

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void handleRegister() {
        String f_name = etFullName.getText().toString().trim();
        String u_name = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        //Kiểm tra đầu vào từng trường
        if (f_name.isEmpty()) {
            etFullName.setError("Vui lòng nhập họ tên");
            etFullName.requestFocus();
            return;
        }
        if (u_name.isEmpty() || u_name.length() < 4) {
            etUsername.setError("Tên đăng nhập tối thiểu 4 ký tự");
            etUsername.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            etEmail.requestFocus();
            return;
        }
        if (phone.isEmpty() || !phone.matches("^[0-9]{10,11}$")) {
            etPhone.setError("Số điện thoại không hợp lệ (10-11 số)");
            etPhone.requestFocus();
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Mật khẩu tối thiểu 6 ký tự");
            etPassword.requestFocus();
            return;
        }

        // Kiểm tra username và email đã tồn tại chưa
        if (userDAO.isUsernameExists(u_name)) {
            etUsername.setError("Tên đăng nhập đã được sử dụng");
            etUsername.requestFocus();
            return;
        }
        if (userDAO.isEmailExists(email)) {
            etEmail.setError("Email đã được sử dụng");
            etEmail.requestFocus();
            return;
        }

        // Tạo user mới với role mặc định là "user"
//        User newUser = new User(u_name, password, f_name, email, phone, "user");
        User newUser = new User(u_name, password, f_name, email, phone, "user");
        long result = userDAO.insert(newUser);
        //Log.d("REGISTER", "Insert result: " + result); // -1 = thất bại, >0 = thành công

        if (result != -1) {
            Toast.makeText(this, "Đăng ký thành công! Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();

        } else {
            Toast.makeText(this, "Đăng ký thất bại, thử lại", Toast.LENGTH_SHORT).show();
        }
    }

}
