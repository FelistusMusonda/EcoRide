package com.example.ecoride;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.navigation.NavigationView;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    private static final int PICK_ADMIN_IMAGE_REQUEST = 5201;

    private RecyclerView rvUsers;
    private TextView tvTotalUsers, tvTotalTrips, tvTotalCarbon, tvWelcomeAdmin;
    private ImageView imgAdminProfilePicture, imgAdminNavProfilePicture;
    private DrawerLayout adminDrawerLayout;
    private NavigationView adminNavView;
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
            imgAdminProfilePicture = findViewById(R.id.img_admin_profile_picture);
            adminDrawerLayout = findViewById(R.id.admin_drawer_layout);
            adminNavView = findViewById(R.id.admin_nav_view);
            Toolbar toolbar = findViewById(R.id.admin_toolbar);
            setSupportActionBar(toolbar);

            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this,
                    adminDrawerLayout,
                    toolbar,
                    R.string.open_drawer,
                    R.string.close_drawer
            );
            adminDrawerLayout.addDrawerListener(toggle);
            toggle.syncState();

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
            tvWelcomeAdmin.setText("Registered Users");
            TextView navName = adminNavView.getHeaderView(0).findViewById(R.id.tv_admin_nav_name);
            imgAdminNavProfilePicture = adminNavView.getHeaderView(0).findViewById(R.id.img_admin_nav_profile);
            navName.setText(adminName);
            loadAdminProfilePicture();

            loadAdminData();
            setupAdminDrawer();

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

    private void setupAdminDrawer() {
        adminNavView.setCheckedItem(R.id.admin_nav_users);
        adminNavView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.admin_nav_users) {
                loadAdminData();
            } else if (id == R.id.admin_nav_locations) {
                startActivity(new Intent(AdminActivity.this, ManageLocationsActivity.class));
            } else if (id == R.id.admin_nav_password) {
                showChangePasswordDialog();
            } else if (id == R.id.admin_nav_picture) {
                pickAdminProfilePicture();
            } else if (id == R.id.admin_nav_logout) {
                logout();
            }

            adminDrawerLayout.closeDrawers();
            return true;
        });
    }

    private void loadAdminProfilePicture() {
        String uriString = sharedPreferences.getString(getAdminProfilePictureKey(), "");
        if (!uriString.isEmpty()) {
            imgAdminProfilePicture.setPadding(0, 0, 0, 0);
            imgAdminProfilePicture.setImageURI(Uri.parse(uriString));
            if (imgAdminNavProfilePicture != null) {
                imgAdminNavProfilePicture.setPadding(0, 0, 0, 0);
                imgAdminNavProfilePicture.setImageURI(Uri.parse(uriString));
            }
        }
    }

    private String getAdminProfilePictureKey() {
        return "profile_picture_" + sharedPreferences.getInt("userId", -1);
    }

    private void showChangePasswordDialog() {
        android.view.View layout = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        android.widget.EditText oldPassword = layout.findViewById(R.id.et_current_password);
        android.widget.EditText newPassword = layout.findViewById(R.id.et_new_password);
        android.widget.EditText confirmPassword = layout.findViewById(R.id.et_confirm_password);

        new AlertDialog.Builder(this)
                .setTitle("Change Password")
                .setView(layout)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {
                    String oldValue = oldPassword.getText().toString();
                    String newValue = newPassword.getText().toString();
                    String confirmValue = confirmPassword.getText().toString();

                    if (newValue.length() < 4) {
                        Toast.makeText(this, "Password must be at least 4 characters.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!newValue.equals(confirmValue)) {
                        Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean updated = databaseHelper.updateUserPassword(
                            sharedPreferences.getInt("userId", -1),
                            oldValue,
                            newValue
                    );
                    Toast.makeText(this,
                            updated ? "Password updated." : "Current password is incorrect.",
                            Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void pickAdminProfilePicture() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, PICK_ADMIN_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_ADMIN_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                getContentResolver().takePersistableUriPermission(
                        imageUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                );
                sharedPreferences.edit().putString(getAdminProfilePictureKey(), imageUri.toString()).apply();
                imgAdminProfilePicture.setPadding(0, 0, 0, 0);
                imgAdminProfilePicture.setImageURI(imageUri);
                if (imgAdminNavProfilePicture != null) {
                    imgAdminNavProfilePicture.setPadding(0, 0, 0, 0);
                    imgAdminNavProfilePicture.setImageURI(imageUri);
                }
            }
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
        return false;
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
        } else if (id == R.id.menu_locations) {
            startActivity(new Intent(AdminActivity.this, ManageLocationsActivity.class));
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
