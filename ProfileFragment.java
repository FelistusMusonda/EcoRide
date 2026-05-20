package com.example.ecoride;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    private TextView tvName, tvEmail, tvTotalTrips, tvTotalCarbon;
    private Button btnEditProfile, btnClearData, btnLogout;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvName = view.findViewById(R.id.tv_profile_name);
        tvEmail = view.findViewById(R.id.tv_profile_email);
        tvTotalTrips = view.findViewById(R.id.tv_total_trips);
        tvTotalCarbon = view.findViewById(R.id.tv_total_carbon);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnClearData = view.findViewById(R.id.btn_clear_data);
        btnLogout = view.findViewById(R.id.btn_logout);

        prefs = requireContext().getSharedPreferences("EcoRide", 0);
        loadUserProfile();

        btnEditProfile.setOnClickListener(v -> showEditDialog());
        btnClearData.setOnClickListener(v -> showClearDataDialog());
        btnLogout.setOnClickListener(v -> logout());

        return view;
    }

    private void loadUserProfile() {
        String name = prefs.getString("userName", "Guest User");
        String email = prefs.getString("userEmail", "Not signed in");
        boolean isGuest = prefs.getBoolean("isGuest", false);

        int totalTrips = prefs.getInt("total_trips", 0);
        float totalCarbon = prefs.getFloat("total_carbon", 0);

        tvName.setText(name);

        if (isGuest) {
            tvEmail.setText("Guest Mode");
        } else {
            tvEmail.setText(email);
        }

        tvTotalTrips.setText(String.valueOf(totalTrips));
        tvTotalCarbon.setText(String.format("%.1f kg", totalCarbon));
    }

    private void showEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Profile");

        final android.widget.EditText input = new android.widget.EditText(getContext());
        input.setHint("Enter new name");
        input.setText(prefs.getString("userName", ""));
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                prefs.edit().putString("userName", newName).apply();
                tvName.setText(newName);
                Toast.makeText(getContext(), "Name updated!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showClearDataDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Clear Trip Data");
        builder.setMessage("Delete all your trip history and carbon savings? Your account will remain.");
        builder.setPositiveButton("Clear", (dialog, which) -> {
            prefs.edit()
                    .putInt("total_trips", 0)
                    .putFloat("total_carbon", 0)
                    .putFloat("total_distance", 0)
                    .putInt("walks_count", 0)
                    .putInt("cycles_count", 0)
                    .putInt("public_count", 0)
                    .putFloat("walking_distance", 0)
                    .putFloat("cycling_distance", 0)
                    .putFloat("public_distance", 0)
                    .putFloat("walking_carbon", 0)
                    .putFloat("cycling_carbon", 0)
                    .putFloat("public_carbon", 0)
                    .putString("recent_trips", "")
                    .apply();

            tvTotalTrips.setText("0");
            tvTotalCarbon.setText("0.0 kg");
            Toast.makeText(getContext(), "Trip data cleared.", Toast.LENGTH_LONG).show();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Logout", (dialog, which) -> {
            prefs.edit()
                    .putBoolean("isLoggedIn", false)
                    .apply();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
    }
}