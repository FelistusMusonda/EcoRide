package com.example.ecoride;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CarbonTrackerFragment extends Fragment {

    private TextView tvTotalCarbon, tvTrees, tvTotalDistance, tvTotalTrips;
    private TextView tvWalkingCount, tvWalkingDistance, tvWalkingCarbon;
    private TextView tvCyclingCount, tvCyclingDistance, tvCyclingCarbon;
    private TextView tvBusCount, tvBusDistance, tvBusCarbon;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_carbon_tracker, container, false);

        // Back button
        ImageView btnBackHome = view.findViewById(R.id.btn_back_home);
        btnBackHome.setOnClickListener(v -> {
            if (getActivity() != null) {
                BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
                bottomNav.setSelectedItemId(R.id.navigation_home);
            }
        });

        // Summary stats
        tvTotalCarbon = view.findViewById(R.id.tv_total_carbon);
        tvTrees = view.findViewById(R.id.tv_trees);
        tvTotalDistance = view.findViewById(R.id.tv_total_distance);
        tvTotalTrips = view.findViewById(R.id.tv_total_trips);

        // Walking stats
        tvWalkingCount = view.findViewById(R.id.tv_walking_count);
        tvWalkingDistance = view.findViewById(R.id.tv_walking_distance);
        tvWalkingCarbon = view.findViewById(R.id.tv_walking_carbon);

        // Cycling stats
        tvCyclingCount = view.findViewById(R.id.tv_cycling_count);
        tvCyclingDistance = view.findViewById(R.id.tv_cycling_distance);
        tvCyclingCarbon = view.findViewById(R.id.tv_cycling_carbon);

        // Bus stats
        tvBusCount = view.findViewById(R.id.tv_bus_count);
        tvBusDistance = view.findViewById(R.id.tv_bus_distance);
        tvBusCarbon = view.findViewById(R.id.tv_bus_carbon);

        prefs = requireContext().getSharedPreferences("EcoRide", 0);
        loadStats();

        return view;
    }

    private void loadStats() {
        // Summary stats
        float totalCarbon = prefs.getFloat("total_carbon", 0);
        float totalDistance = prefs.getFloat("total_distance", 0);
        int totalTrips = prefs.getInt("total_trips", 0);
        double trees = totalCarbon / 22;

        tvTotalCarbon.setText(String.format("%.1f", totalCarbon));
        tvTrees.setText(String.format("%.1f", trees));
        tvTotalDistance.setText(String.format("%.1f", totalDistance));
        tvTotalTrips.setText(String.valueOf(totalTrips));

        // Walking stats
        int walkingCount = prefs.getInt("walks_count", 0);
        float walkingDistance = prefs.getFloat("walking_distance", 0);
        float walkingCarbon = prefs.getFloat("walking_carbon", 0);

        tvWalkingCount.setText(String.valueOf(walkingCount));
        tvWalkingDistance.setText(String.format("%.1f km", walkingDistance));
        tvWalkingCarbon.setText(String.format("%.1f kg", walkingCarbon));

        // Cycling stats
        int cyclingCount = prefs.getInt("cycles_count", 0);
        float cyclingDistance = prefs.getFloat("cycling_distance", 0);
        float cyclingCarbon = prefs.getFloat("cycling_carbon", 0);

        tvCyclingCount.setText(String.valueOf(cyclingCount));
        tvCyclingDistance.setText(String.format("%.1f km", cyclingDistance));
        tvCyclingCarbon.setText(String.format("%.1f kg", cyclingCarbon));

        // Bus stats
        int busCount = prefs.getInt("public_count", 0);
        float busDistance = prefs.getFloat("public_distance", 0);
        float busCarbon = prefs.getFloat("public_carbon", 0);

        tvBusCount.setText(String.valueOf(busCount));
        tvBusDistance.setText(String.format("%.1f km", busDistance));
        tvBusCarbon.setText(String.format("%.1f kg", busCarbon));
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStats();
    }
}