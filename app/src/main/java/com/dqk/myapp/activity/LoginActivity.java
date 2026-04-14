package com.dqk.myapp.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
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

import java.util.Locale;

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvMessage;

    private UserDAO userDAO;
    private SessionManager session;

    // Các hằng số và biến cho chức năng khóa
    private static final int MAX_ATTEMPTS = 3;
    private static final long LOCKOUT_DURATION = 30000; // 30 giây
    private SharedPreferences prefs;
    private int loginAttempts;
    private long lockoutTimestamp;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userDAO = new UserDAO(DatabaseHelper.getInstance(this));
        session = new SessionManager(this);
        prefs = getSharedPreferences("login_prefs", Context.MODE_PRIVATE);

        // Nếu login rồi thì vào thẳng app
        if (session.isLoggedIn()) {
            goToMain();
            return;
        }

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvMessage = findViewById(R.id.tvMessage);
        TextView tvRegister = findViewById(R.id.tvRegister);

        // Khôi phục trạng thái
        loginAttempts = prefs.getInt("login_attempts", 0);
        lockoutTimestamp = prefs.getLong("lockout_timestamp", 0);

        checkLockoutStatus();

        btnLogin.setOnClickListener(v -> handleLogin());
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void checkLockoutStatus() {
        long currentTime = System.currentTimeMillis();
        if (currentTime < lockoutTimestamp) {
            startLockoutTimer(lockoutTimestamp - currentTime);
        } else {
            resetLockout();
        }
    }

    private void startLockoutTimer(long millisInFuture) {
        btnLogin.setEnabled(false);
        etPassword.setEnabled(false);
        etUsername.setEnabled(false);

        if (countDownTimer != null) countDownTimer.cancel();

        countDownTimer = new CountDownTimer(millisInFuture, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                tvMessage.setText(String.format(Locale.getDefault(), 
                    "Bạn đã nhập sai quá nhiều lần. Thử lại sau %d giây", secondsRemaining));
                tvMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }

            @Override
            public void onFinish() {
                resetLockout();
            }
        }.start();
    }

    private void resetLockout() {
        loginAttempts = 0;
        lockoutTimestamp = 0;
        prefs.edit().putInt("login_attempts", 0).putLong("lockout_timestamp", 0).apply();

        btnLogin.setEnabled(true);
        etPassword.setEnabled(true);
        etUsername.setEnabled(true);
        tvMessage.setText("");
    }

    private void handleLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

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

        User user = userDAO.login(username, password);
        if (user != null) {
            // Đăng nhập thành công, reset số lần sai
            resetLockout();
            session.createSession(user.getId(), user.getUsername(), user.getFullname(), user.getRole());
            Toast.makeText(this, "Xin chào, " + user.getFullname(), Toast.LENGTH_SHORT).show();
            goToMain();
        } else {
            // Sai mật khẩu
            loginAttempts++;
            int remaining = MAX_ATTEMPTS - loginAttempts;

            if (loginAttempts >= MAX_ATTEMPTS) {
                lockoutTimestamp = System.currentTimeMillis() + LOCKOUT_DURATION;
                prefs.edit().putLong("lockout_timestamp", lockoutTimestamp).apply();
                startLockoutTimer(LOCKOUT_DURATION);
                Toast.makeText(this, "Bạn đã bị khóa tạm thời", Toast.LENGTH_LONG).show();
            } else {
                prefs.edit().putInt("login_attempts", loginAttempts).apply();
                tvMessage.setText(String.format(Locale.getDefault(), "Sai mật khẩu! Còn %d lần thử", remaining));
                tvMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                Toast.makeText(this, "Tên đăng nhập hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void goToMain() {
        Intent intent;
        if (session.isAdmin()) {
            intent = new Intent(this, AdminMainActivity.class);
        } else {
            intent = new Intent(this, UserMainActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
