package com.example.ecoride;

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
import java.util.List;

public class CarbonTrackerFragment extends Fragment {

    private TextView tvTotalCarbon, tvTrees, tvTotalDistance, tvTotalTrips;
    private TextView tvWalkingCount, tvWalkingDistance, tvWalkingCarbon;
    private TextView tvCyclingCount, tvCyclingDistance, tvCyclingCarbon;
    private TextView tvBusCount, tvBusDistance, tvBusCarbon;
    private DatabaseHelper databaseHelper;
    private int currentUserId;
    private android.content.SharedPreferences prefs;

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

        databaseHelper = new DatabaseHelper(requireContext());
        prefs = requireContext().getSharedPreferences("EcoRide", 0);

        // Get current user ID - using correct key "userId"
        currentUserId = prefs.getInt("userId", -1);

        loadStats();

        return view;
    }

    private void loadStats() {
        if (currentUserId != -1) {
            loadStatsFromDatabase();
        } else {
            loadStatsFromSharedPreferences();
        }
    }

    private void loadStatsFromDatabase() {
        // Get user from database
        DatabaseHelper.User user = databaseHelper.getUserById(currentUserId);

        if (user != null) {
            // Get trips from database to calculate mode-specific stats
            List<DatabaseHelper.Trip> trips = databaseHelper.getUserTrips(currentUserId);

            int totalTrips = 0;
            double totalDistance = 0;
            double totalCarbon = 0;
            int walkingCount = 0;
            int cyclingCount = 0;
            int busCount = 0;
            double walkingDistance = 0;
            double cyclingDistance = 0;
            double busDistance = 0;
            double walkingCarbon = 0;
            double cyclingCarbon = 0;
            double busCarbon = 0;

            for (DatabaseHelper.Trip trip : trips) {
                String modeLower = trip.mode == null ? "" : trip.mode.toLowerCase();
                int tripCount = getTripCountFromMode(trip.mode);
                totalTrips += tripCount;
                totalDistance += trip.distance;
                totalCarbon += trip.carbonSaved;

                if (modeLower.contains("walking")) {
                    walkingCount += tripCount;
                    walkingDistance += trip.distance;
                    walkingCarbon += trip.carbonSaved;
                } else if (modeLower.contains("cycling")) {
                    cyclingCount += tripCount;
                    cyclingDistance += trip.distance;
                    cyclingCarbon += trip.carbonSaved;
                } else if (modeLower.contains("bus")) {
                    busCount += tripCount;
                    busDistance += trip.distance;
                    busCarbon += trip.carbonSaved;
                }
            }

            double trees = totalCarbon / 22;
            tvTotalCarbon.setText(String.format("%.1f", totalCarbon));
            tvTrees.setText(String.format("%.1f", trees));
            tvTotalTrips.setText(String.valueOf(totalTrips));
            tvTotalDistance.setText(String.format("%.1f", totalDistance));

            tvWalkingCount.setText(String.valueOf(walkingCount));
            tvWalkingDistance.setText(String.format("%.1f km", walkingDistance));
            tvWalkingCarbon.setText(String.format("%.1f kg", walkingCarbon));

            tvCyclingCount.setText(String.valueOf(cyclingCount));
            tvCyclingDistance.setText(String.format("%.1f km", cyclingDistance));
            tvCyclingCarbon.setText(String.format("%.1f kg", cyclingCarbon));

            tvBusCount.setText(String.valueOf(busCount));
            tvBusDistance.setText(String.format("%.1f km", busDistance));
            tvBusCarbon.setText(String.format("%.1f kg", busCarbon));
        } else {
            loadStatsFromSharedPreferences();
        }
    }

    private int getTripCountFromMode(String mode) {
        return mode != null && mode.toLowerCase().contains("round trip") ? 2 : 1;
    }

    private void loadStatsFromSharedPreferences() {
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
