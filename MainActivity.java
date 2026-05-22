package com.example.ecoride;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    private SharedPreferences sharedPreferences;
    private String userName;
    private String userEmail;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("EcoRide", MODE_PRIVATE);
        databaseHelper = new DatabaseHelper(this);

        // Check if logged in
        if (!sharedPreferences.getBoolean("isLoggedIn", false)) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // Refresh user data from database
        refreshUserData();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("EcoRide");
        }

        // Setup drawer layout
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Setup bottom navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            int id = item.getItemId();

            try {
                if (id == R.id.navigation_home) {
                    selected = new HomeFragment();
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle("EcoRide");
                    }
                } else if (id == R.id.navigation_route) {
                    selected = new RoutePlannerFragment();
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle("Plan Route");
                    }
                } else if (id == R.id.navigation_carbon) {
                    selected = new CarbonTrackerFragment();
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle("Carbon Impact");
                    }
                } else if (id == R.id.navigation_history) {
                    selected = new TripHistoryFragment();
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle("Trip History");
                    }
                } else if (id == R.id.navigation_profile) {
                    selected = new ProfileFragment();
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle("Profile");
                    }
                }

                if (selected != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selected)
                            .commit();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading fragment", Toast.LENGTH_SHORT).show();
            }
            return true;
        });

        // Setup navigation drawer
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView tvUserName = headerView.findViewById(R.id.tv_user_name);
        TextView tvUserEmail = headerView.findViewById(R.id.tv_user_email);

        if (tvUserName != null) {
            tvUserName.setText(userName);
        }
        if (tvUserEmail != null) {
            tvUserEmail.setText(userEmail);
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                bottomNavigationView.setSelectedItemId(R.id.navigation_home);
                drawerLayout.closeDrawers();
            } else if (id == R.id.nav_route) {
                bottomNavigationView.setSelectedItemId(R.id.navigation_route);
                drawerLayout.closeDrawers();
            } else if (id == R.id.nav_carbon) {
                bottomNavigationView.setSelectedItemId(R.id.navigation_carbon);
                drawerLayout.closeDrawers();
            } else if (id == R.id.nav_history) {
                bottomNavigationView.setSelectedItemId(R.id.navigation_history);
                drawerLayout.closeDrawers();
            } else if (id == R.id.nav_profile) {
                bottomNavigationView.setSelectedItemId(R.id.navigation_profile);
                drawerLayout.closeDrawers();
            } else if (id == R.id.nav_logout) {
                logout();
            }
            return true;
        });

        // Load default fragment
        if (savedInstanceState == null) {
            try {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();
                bottomNavigationView.setSelectedItemId(R.id.navigation_home);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("EcoRide");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void refreshUserData() {
        int userId = sharedPreferences.getInt("userId", -1);
        if (userId != -1) {
            DatabaseHelper.User user = databaseHelper.getUserById(userId);
            if (user != null) {
                userName = user.name;
                userEmail = user.email;

                // Update SharedPreferences with latest data from database
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("userName", user.name);
                editor.putString("userEmail", user.email);
                editor.putFloat("total_carbon", (float) user.totalCarbon);
                editor.putInt("total_trips", user.totalTrips);
                editor.apply();
            } else {
                userName = sharedPreferences.getString("userName", "Guest");
                userEmail = sharedPreferences.getString("userEmail", "guest@ecoride.com");
            }
        } else {
            userName = sharedPreferences.getString("userName", "Guest");
            userEmail = sharedPreferences.getString("userEmail", "guest@ecoride.com");
        }
    }

    private void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUserData();
    }

    @Override
    public void onBackPressed() {
        int currentItem = bottomNavigationView.getSelectedItemId();

        if (currentItem != R.id.navigation_home) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_home);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("EcoRide");
            }
        } else {
            super.onBackPressed();
        }
    }
}
