package com.example.ecoride;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView rvUsers;
    private TextView tvTotalUsers, tvTotalTrips, tvWelcomeAdmin;
    private Button btnLogout;
    private DatabaseHelper databaseHelper;
    private UserAdapter userAdapter;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        rvUsers = findViewById(R.id.rv_users);
        tvTotalUsers = findViewById(R.id.tv_total_users);
        tvTotalTrips = findViewById(R.id.tv_total_trips);
        tvWelcomeAdmin = findViewById(R.id.tv_welcome_admin);
        btnLogout = findViewById(R.id.btn_logout);

        databaseHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("EcoRide", MODE_PRIVATE);

        // Get admin name
        String adminName = sharedPreferences.getString("userName", "Admin");
        tvWelcomeAdmin.setText("Welcome, " + adminName);

        loadAdminData();

        btnLogout.setOnClickListener(v -> logout());
    }

    private void loadAdminData() {
        List<DatabaseHelper.User> users = databaseHelper.getAllUsers();

        tvTotalUsers.setText("Total Users: " + users.size());

        int totalTrips = 0;
        for (DatabaseHelper.User user : users) {
            totalTrips += databaseHelper.getUserTrips(user.id).size();
        }
        tvTotalTrips.setText("Total Trips: " + totalTrips);

        userAdapter = new UserAdapter(users, databaseHelper);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(userAdapter);
    }

    private void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}