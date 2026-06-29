package com.example.codeappdatvexemphim;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtFullname, edtUsername, edtEmail, edtPassword;
    private AppCompatButton btnRegister;
    private TextView txtLoginLink;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = DatabaseHelper.getInstance(this);

        // Find views
        edtFullname = findViewById(R.id.edt_fullname);
        edtUsername = findViewById(R.id.edt_username);
        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_password);
        btnRegister = findViewById(R.id.btn_register);
        txtLoginLink = findViewById(R.id.txt_login_link);

        // Click listeners
        txtLoginLink.setOnClickListener(v -> finish()); // Go back to login

        btnRegister.setOnClickListener(v -> handleRegistration());
    }

    private void handleRegistration() {
        String fullname = edtFullname.getText().toString().trim();
        String username = edtUsername.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        // Validate fields
        if (fullname.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (username.length() < 3) {
            Toast.makeText(this, "Tên đăng nhập phải chứa ít nhất 3 ký tự!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 4) {
            Toast.makeText(this, "Mật khẩu phải chứa ít nhất 4 ký tự!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if username exists
        if (dbHelper.isUsernameExists(username)) {
            Toast.makeText(this, "Tên đăng nhập đã tồn tại! Vui lòng chọn tên khác.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Register user
        boolean success = dbHelper.registerUser(username, email, password, fullname);
        if (success) {
            Toast.makeText(this, "Đăng ký tài khoản thành công!", Toast.LENGTH_SHORT).show();
            finish(); // return to Login screen
        } else {
            Toast.makeText(this, "Đăng ký thất bại! Lỗi cơ sở dữ liệu.", Toast.LENGTH_SHORT).show();
        }
    }
}
