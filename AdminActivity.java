package com.example.ecoride;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView rvUsers;
    private TextView tvTotalUsers, tvTotalTrips, tvTotalCarbon, tvWelcomeAdmin;
    private Button btnLogout, btnRefresh;
    private DatabaseHelper databaseHelper;
    private UserAdapter userAdapter;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_admin);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading layout: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        try {
            // Initialize views
            rvUsers = findViewById(R.id.rv_users);
            tvTotalUsers = findViewById(R.id.tv_total_users);
            tvTotalTrips = findViewById(R.id.tv_total_trips);
            tvTotalCarbon = findViewById(R.id.tv_total_carbon);
            tvWelcomeAdmin = findViewById(R.id.tv_welcome_admin);
            btnLogout = findViewById(R.id.btn_logout);
            btnRefresh = findViewById(R.id.btn_refresh);

            databaseHelper = new DatabaseHelper(this);
            sharedPreferences = getSharedPreferences("EcoRide", MODE_PRIVATE);

            // Check if user is actually admin
            boolean isAdmin = sharedPreferences.getBoolean("isAdmin", false);
            if (!isAdmin) {
                Toast.makeText(this, "Access denied. Admin only.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AdminActivity.this, LoginActivity.class));
                finish();
                return;
            }

            // Get admin name
            String adminName = sharedPreferences.getString("userName", "Admin");
            tvWelcomeAdmin.setText("Welcome, " + adminName);

            loadAdminData();

            btnLogout.setOnClickListener(v -> logout());
            btnRefresh.setOnClickListener(v -> loadAdminData());

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error initializing: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadAdminData() {
        try {
            List<DatabaseHelper.User> users = databaseHelper.getAllUsers();

            // Calculate totals
            int totalUsers = users.size();
            int totalTrips = 0;
            double totalCarbon = 0;

            for (DatabaseHelper.User user : users) {
                totalTrips += user.totalTrips;
                totalCarbon += user.totalCarbon;
            }

            tvTotalUsers.setText("Total Users: " + totalUsers);
            tvTotalTrips.setText("Total Trips: " + totalTrips);
            tvTotalCarbon.setText(String.format("Total CO2 Saved: %.1f kg", totalCarbon));

            // Setup adapter with delete functionality
            userAdapter = new UserAdapter(users, databaseHelper, this, new UserAdapter.OnUserActionListener() {
                @Override
                public void onDeleteUser(DatabaseHelper.User user, int position) {
                    showDeleteConfirmation(user, position);
                }

                @Override
                public void onViewUserDetails(DatabaseHelper.User user) {
                    showUserDetails(user);
                }
            });

            rvUsers.setLayoutManager(new LinearLayoutManager(this));
            rvUsers.setAdapter(userAdapter);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading data: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showDeleteConfirmation(DatabaseHelper.User user, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete user '" + user.name + "'?\nThis will also delete all their trips.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean deleted = databaseHelper.deleteUser(user.id);
                    if (deleted) {
                        Toast.makeText(this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                        loadAdminData(); // Refresh the list
                    } else {
                        Toast.makeText(this, "Failed to delete user", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showUserDetails(DatabaseHelper.User user) {
        // Get user's trips
        List<DatabaseHelper.Trip> userTrips = databaseHelper.getUserTrips(user.id);

        StringBuilder details = new StringBuilder();
        details.append("Name: ").append(user.name).append("\n");
        details.append("Email: ").append(user.email).append("\n");
        details.append("Total Trips: ").append(user.totalTrips).append("\n");
        details.append("Total CO2 Saved: ").append(String.format("%.1f kg", user.totalCarbon)).append("\n\n");

        if (userTrips != null && !userTrips.isEmpty()) {
            details.append("Recent Trips:\n");
            int count = 0;
            for (DatabaseHelper.Trip trip : userTrips) {
                if (count >= 5) break;
                details.append("• ").append(trip.from).append(" → ").append(trip.to).append("\n");
                details.append("  ").append(trip.mode).append(", ").append(String.format("%.1f", trip.distance)).append(" km\n");
                count++;
            }
        } else {
            details.append("No trips yet.");
        }

        new AlertDialog.Builder(this)
                .setTitle("User Details")
                .setMessage(details.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();

                    Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to admin activity
        loadAdminData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_refresh) {
            loadAdminData();
            return true;
        } else if (id == R.id.menu_all_trips) {
            viewAllTrips();
            return true;
        } else if (id == R.id.menu_statistics) {
            viewStatistics();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void viewAllTrips() {
        List<DatabaseHelper.Trip> allTrips = databaseHelper.getAllTrips();

        if (allTrips == null || allTrips.isEmpty()) {
            Toast.makeText(this, "No trips found", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder tripsList = new StringBuilder();
        tripsList.append("All Trips (").append(allTrips.size()).append("):\n\n");

        for (DatabaseHelper.Trip trip : allTrips) {
            tripsList.append(trip.userName).append("\n");
            tripsList.append("  ").append(trip.from).append(" → ").append(trip.to).append("\n");
            tripsList.append("  ").append(trip.mode).append(", ").append(String.format("%.1f", trip.distance)).append(" km\n");
            tripsList.append("  CO2 Saved: ").append(String.format("%.1f", trip.carbonSaved)).append(" kg\n\n");
        }

        new AlertDialog.Builder(this)
                .setTitle("All Trips")
                .setMessage(tripsList.toString())
                .setPositiveButton("OK", null)
                .setNegativeButton("Export", (dialog, which) -> {
                    // Optional: Add export functionality
                    Toast.makeText(this, "Export feature coming soon", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void viewStatistics() {
        int totalUsers = databaseHelper.getTotalUserCount();
        double totalCarbon = databaseHelper.getTotalCarbonAllUsers();

        String stats = "Admin Dashboard Statistics\n\n" +
                "Total Users: " + totalUsers + "\n" +
                "Total CO2 Saved: " + String.format("%.1f", totalCarbon) + " kg\n\n" +
                "Environmental Impact:\n" +
                "• Trees equivalent: " + String.format("%.1f", totalCarbon / 22) + " trees\n" +
                "• Cars off the road (1 year): " + String.format("%.1f", totalCarbon / 4600) + " cars\n" +
                "• kWh saved: " + String.format("%.1f", totalCarbon * 8.5) + " kWh";

        new AlertDialog.Builder(this)
                .setTitle("Statistics")
                .setMessage(stats)
                .setPositiveButton("OK", null)
                .show();
    }
}
