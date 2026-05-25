package com.example.ecoride;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ManageLocationsActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private RecyclerView rvLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_locations);

        SharedPreferences prefs = getSharedPreferences("EcoRide", MODE_PRIVATE);
        if (!prefs.getBoolean("isAdmin", false)) {
            Toast.makeText(this, "Access denied. Admin only.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        databaseHelper = new DatabaseHelper(this);
        RoutePlannerFragment.seedDefaultLocations(databaseHelper);

        rvLocations = findViewById(R.id.rv_locations);
        Button btnAddLocation = findViewById(R.id.btn_add_location);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Locations");
        }

        rvLocations.setLayoutManager(new LinearLayoutManager(this));
        btnAddLocation.setOnClickListener(v -> showLocationDialog(null));
        loadLocations();
    }

    private void loadLocations() {
        List<DatabaseHelper.LocationPlace> locations = databaseHelper.getAllLocations();
        LocationAdapter adapter = new LocationAdapter(locations, new LocationAdapter.OnLocationActionListener() {
            @Override
            public void onEditLocation(DatabaseHelper.LocationPlace location) {
                showLocationDialog(location);
            }

            @Override
            public void onDeleteLocation(DatabaseHelper.LocationPlace location) {
                confirmDelete(location);
            }
        });
        rvLocations.setAdapter(adapter);
    }

    private void showLocationDialog(DatabaseHelper.LocationPlace location) {
        boolean isEdit = location != null;

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, 8, padding, 0);

        EditText etName = new EditText(this);
        etName.setHint("Location name");
        etName.setSingleLine(true);
        layout.addView(etName);

        EditText etLatitude = new EditText(this);
        etLatitude.setHint("Latitude, e.g. -12.82310");
        etLatitude.setSingleLine(true);
        etLatitude.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        layout.addView(etLatitude);

        EditText etLongitude = new EditText(this);
        etLongitude.setHint("Longitude, e.g. 28.21360");
        etLongitude.setSingleLine(true);
        etLongitude.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        layout.addView(etLongitude);

        if (isEdit) {
            etName.setText(location.name);
            etLatitude.setText(String.valueOf(location.latitude));
            etLongitude.setText(String.valueOf(location.longitude));
        }

        new AlertDialog.Builder(this)
                .setTitle(isEdit ? "Edit Location" : "Add Location")
                .setView(layout)
                .setNegativeButton("Cancel", null)
                .setPositiveButton(isEdit ? "Save" : "Add", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String latitudeText = etLatitude.getText().toString().trim();
                    String longitudeText = etLongitude.getText().toString().trim();

                    if (name.isEmpty() || latitudeText.isEmpty() || longitudeText.isEmpty()) {
                        Toast.makeText(this, "Fill in name, latitude, and longitude.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double latitude = Double.parseDouble(latitudeText);
                        double longitude = Double.parseDouble(longitudeText);
                        if (!isValidCoordinates(latitude, longitude)) {
                            Toast.makeText(this, "Enter valid latitude and longitude.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        boolean success = isEdit
                                ? databaseHelper.updateLocation(location.id, name, latitude, longitude)
                                : databaseHelper.addLocation(name, latitude, longitude);

                        Toast.makeText(this,
                                success ? "Location saved." : "Location could not be saved.",
                                Toast.LENGTH_SHORT).show();
                        loadLocations();
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Latitude and longitude must be numbers.", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private boolean isValidCoordinates(double latitude, double longitude) {
        return latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
    }

    private void confirmDelete(DatabaseHelper.LocationPlace location) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Location")
                .setMessage("Delete '" + location.name + "' from route planning?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean success = databaseHelper.deleteLocation(location.id);
                    Toast.makeText(this,
                            success ? "Location deleted." : "Location could not be deleted.",
                            Toast.LENGTH_SHORT).show();
                    loadLocations();
                })
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
