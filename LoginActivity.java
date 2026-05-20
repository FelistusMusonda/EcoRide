package com.example.ecoride;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnSignup;
    private TextView tvGuest;
    private SharedPreferences sharedPreferences;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnSignup = findViewById(R.id.btn_signup);
        tvGuest = findViewById(R.id.tv_guest);

        databaseHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("EcoRide", MODE_PRIVATE);

        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            // Check if logged in user is admin
            if (sharedPreferences.getBoolean("isAdmin", false)) {
                startActivity(new Intent(LoginActivity.this, AdminActivity.class));
            } else {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            }
            finish();
            return;
        }

        btnLogin.setOnClickListener(v -> loginUser());
        btnSignup.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        tvGuest.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isLoggedIn", true);
            editor.putString("userName", "Guest");
            editor.putString("userEmail", "guest@ecoride.com");
            editor.putString("currentUserEmail", "guest@ecoride.com");
            editor.putBoolean("isGuest", true);
            editor.putBoolean("isAdmin", false);
            editor.apply();

            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }

        // Check login result: 1=admin, 2=regular user, 0=failed
        int loginResult = databaseHelper.loginUser(email, password);

        if (loginResult == 1) { // Admin login
            String userName = databaseHelper.getUserName(email);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isLoggedIn", true);
            editor.putString("currentUserEmail", email);
            editor.putString("userName", userName);
            editor.putString("userEmail", email);
            editor.putBoolean("isGuest", false);
            editor.putBoolean("isAdmin", true);
            editor.apply();

            Toast.makeText(this, "Admin login successful! Welcome " + userName + "!", Toast.LENGTH_SHORT).show();

            startActivity(new Intent(LoginActivity.this, AdminActivity.class));
            finish();
        } else if (loginResult == 2) { // Regular user login
            String userName = databaseHelper.getUserName(email);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isLoggedIn", true);
            editor.putString("currentUserEmail", email);
            editor.putString("userName", userName);
            editor.putString("userEmail", email);
            editor.putBoolean("isGuest", false);
            editor.putBoolean("isAdmin", false);
            editor.apply();

            Toast.makeText(this, "Welcome back " + userName + "!", Toast.LENGTH_SHORT).show();

            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Invalid email or password.", Toast.LENGTH_LONG).show();
        }
    }
}