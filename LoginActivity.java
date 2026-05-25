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

        btnLogin.setOnClickListener(v -> loginUser());
        btnSignup.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        tvGuest.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isLoggedIn", true);
            editor.putString("userName", "Guest");
            editor.putString("userEmail", "guest@ecoride.com");
            editor.putBoolean("isGuest", true);
            editor.putBoolean("isAdmin", false);
            editor.putInt("userId", -1);
            editor.putFloat("total_carbon", 0);
            editor.putInt("total_trips", 0);
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

        DatabaseHelper.User user = databaseHelper.loginUser(email, password);

        if (user != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isLoggedIn", true);
            editor.putString("currentUserEmail", email);
            editor.putString("userName", user.name);
            editor.putString("userEmail", email);
            editor.putInt("userId", user.id);
            editor.putBoolean("isGuest", false);

            // IMPORTANT: Load user's stats from database
            editor.putFloat("total_carbon", (float) user.totalCarbon);
            editor.putInt("total_trips", user.totalTrips);

            if (user.role != null && user.role.equals("admin")) {
                editor.putBoolean("isAdmin", true);
                editor.apply();
                Toast.makeText(this, "Admin login successful! Welcome " + user.name + "!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, AdminActivity.class));
            } else {
                editor.putBoolean("isAdmin", false);
                editor.apply();
                Toast.makeText(this, "Welcome back " + user.name + "!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            }
            finish();
        } else {
            Toast.makeText(this, "Invalid email or password.", Toast.LENGTH_LONG).show();
        }
    }
}