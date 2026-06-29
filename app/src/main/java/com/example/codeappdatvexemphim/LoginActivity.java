package com.example.codeappdatvexemphim;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class LoginActivity extends AppCompatActivity {

    private EditText edtUsername, edtPassword;
    private AppCompatButton btnLogin;
    private TextView txtRegisterLink;
    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = DatabaseHelper.getInstance(this);
        sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE);

        // Check if user is already logged in
        if (sharedPreferences.contains("user_id")) {
            String role = sharedPreferences.getString("role", "user");
            if (role.equals("admin")) {
                startActivity(new Intent(LoginActivity.this, AdminActivity.class));
            } else {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            }
            finish();
            return;
        }

        // Find views
        edtUsername = findViewById(R.id.edt_username);
        edtPassword = findViewById(R.id.edt_password);
        btnLogin = findViewById(R.id.btn_login);
        txtRegisterLink = findViewById(R.id.txt_register_link);

        // Click listeners
        txtRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        btnLogin.setOnClickListener(v -> handleLogin());
    }

    private void handleLogin() {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tài khoản và mật khẩu!", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = dbHelper.checkLogin(username, password);
        if (user != null) {
            // Save user session in SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("user_id", user.getId());
            editor.putString("username", user.getUsername());
            editor.putString("fullname", user.getFullname());
            editor.putString("email", user.getEmail());
            editor.putString("role", user.getRole());
            editor.apply();

            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

            // Redirect based on role
            if (user.getRole().equals("admin")) {
                startActivity(new Intent(LoginActivity.this, AdminActivity.class));
            } else {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            }
            finish();
        } else {
            Toast.makeText(this, "Tài khoản hoặc mật khẩu không đúng!", Toast.LENGTH_SHORT).show();
        }
    }
}
