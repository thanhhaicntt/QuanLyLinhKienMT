package com.dqk.myapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dqk.myapp.R;
import com.dqk.myapp.activity.admin.AdminMainActivity;
import com.dqk.myapp.activity.user.UserMainActivity;
import com.dqk.myapp.database.DatabaseHelper;
import com.dqk.myapp.database.UserDAO;
import com.dqk.myapp.model.User;
import com.dqk.myapp.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;

    private UserDAO userDAO;
    //private Button btnLogin;
    //private TextView tvRegister;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userDAO = new UserDAO(DatabaseHelper.getInstance(this));
        session = new SessionManager(this);

        //Nếu login rồi thì không cần đăng nhập lại vào thẳng app
        if (session.isLoggedIn()) {
            goToMain();
            finish();
            return;
        }

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);

        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegister = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> handleLogin());

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

    }

    // Thêm vào LoginActivity
    @Override
    public void onBackPressed() {
        finishAffinity(); // Thoát hoàn toàn app
    }

    private void handleLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        //Validate - kiểm tra thông tin đầu vào
        if (username.isEmpty()) {
            etUsername.setError("Vui lòng nhập tên đăng nhập");
            etUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return;
        }

        //Kiểm tra đăng nhập
        User user = userDAO.login(username, password);
        if (user != null) {
            //Lưu session
            session.createSession(user.getId(), user.getUsername(), user.getFullname(), user.getRole());

            Toast.makeText(this, "Xin chào," + user.getFullname(), Toast.LENGTH_SHORT).show();
            goToMain();
            finish();
        } else {
            Toast.makeText(this, "Tên đăng nhập hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
        }

    }

    //Role :Admin ->AdminMainActivity, User ->UserMainActivity
    private void goToMain() {
        Intent intent;
        if (session.isAdmin()) {
            intent = new Intent(this, AdminMainActivity.class);
        } else {
            intent = new Intent(this, UserMainActivity.class);
        }
        // Thêm 2 dòng flag này — xóa toàn bộ back stack, không back về Login được
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

