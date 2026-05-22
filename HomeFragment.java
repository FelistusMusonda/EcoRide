package com.example.ecoride;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeFragment extends Fragment {

    private TextView tvCarbonSaved, tvTripsCount, tvTrees, tvUserName, tvEcoTip;
    private LinearLayout btnPlanRoute, btnViewStats, recentActivityContainer;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvCarbonSaved = view.findViewById(R.id.tv_carbon_saved);
        tvTripsCount = view.findViewById(R.id.tv_trips_count);
        tvTrees = view.findViewById(R.id.tv_trees);
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvEcoTip = view.findViewById(R.id.tv_eco_tip);
        btnPlanRoute = view.findViewById(R.id.btn_plan_route);
        btnViewStats = view.findViewById(R.id.btn_view_stats);
        recentActivityContainer = view.findViewById(R.id.recent_activity_container);

        prefs = requireContext().getSharedPreferences("EcoRide", 0);

        String userName = prefs.getString("userName", "Guest");
        tvUserName.setText(userName);

        loadStats();
        loadRecentTrips();

        String[] tips = {
                "Walking 1 km instead of driving saves 0.19 kg of CO2",
                "Cycling 5 km to work saves 1 kg of CO2 per day",
                "Taking the bus reduces emissions by 75% compared to driving",
                "One bus can take 40 cars off the road",
                "Carpooling cuts your carbon footprint in half"
        };
        tvEcoTip.setText(tips[(int)(Math.random() * tips.length)]);

        btnPlanRoute.setOnClickListener(v -> {
            if (getActivity() != null) {
                BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
                bottomNav.setSelectedItemId(R.id.navigation_route);
            }
        });

        btnViewStats.setOnClickListener(v -> {
            if (getActivity() != null) {
                BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
                bottomNav.setSelectedItemId(R.id.navigation_carbon);
            }
        });

        return view;
    }

    private void loadStats() {
        float carbon = prefs.getFloat("total_carbon", 0);
        int trips = prefs.getInt("total_trips", 0);
        double trees = carbon / 22;

        tvCarbonSaved.setText(String.format("%.1f", carbon));
        tvTripsCount.setText(String.valueOf(trips));
        tvTrees.setText(String.format("%.1f", trees));
    }

    private void loadRecentTrips() {
        // Clear existing views
        recentActivityContainer.removeAllViews();

        String tripsData = prefs.getString("recent_trips", "");
        if (!tripsData.isEmpty()) {
            String[] trips = tripsData.split("\\|\\|");
            int count = 0;

            // Show last 3 trips
            for (int i = trips.length - 1; i >= 0 && count < 3; i--) {
                String[] parts = trips[i].split("\\|");
                if (parts.length >= 5) {
                    String from = parts[0];
                    String to = parts[1];
                    String mode = parts[2];
                    double distance = Double.parseDouble(parts[3]);

                    // Create trip item view
                    LinearLayout tripItem = createTripItem(from, to, mode, distance);
                    recentActivityContainer.addView(tripItem);

                    // Add divider if not last
                    if (count < 2 && i > 0) {
                        View divider = new View(getContext());
                        divider.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, 1));
                        divider.setBackgroundColor(0xFFEEEEEE);
                        recentActivityContainer.addView(divider);
                    }
                    count++;
                }
            }
        }

        // If no trips, show empty message
        if (recentActivityContainer.getChildCount() == 0) {
            TextView emptyText = new TextView(getContext());
            emptyText.setText("No trips yet.\nPlan your first eco-friendly trip!");
            emptyText.setTextSize(14);
            emptyText.setTextColor(0xFF666666);
            emptyText.setPadding(16, 16, 16, 16);
            emptyText.setGravity(android.view.Gravity.CENTER);
            recentActivityContainer.addView(emptyText);
        }
    }

    private LinearLayout createTripItem(String from, String to, String mode, double distance) {
        LinearLayout itemLayout = new LinearLayout(getContext());
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setPadding(16, 16, 16, 16);
        itemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        // Mode icon circle
        LinearLayout iconCircle = new LinearLayout(getContext());
        iconCircle.setLayoutParams(new LinearLayout.LayoutParams(48, 48));
        iconCircle.setGravity(android.view.Gravity.CENTER);

        // Set icon based on mode
        TextView iconText = new TextView(getContext());
        if (mode.contains("Walking")) {
            iconCircle.setBackgroundResource(R.drawable.circle_bg_walking);
            iconText.setText("🚶");
        } else if (mode.contains("Cycling")) {
            iconCircle.setBackgroundResource(R.drawable.circle_bg_cycling);
            iconText.setText("🚲");
        } else {
            iconCircle.setBackgroundResource(R.drawable.circle_bg_bus);
            iconText.setText("🚌");
        }
        iconText.setTextSize(20);
        iconCircle.addView(iconText);

        // Trip details
        LinearLayout detailsLayout = new LinearLayout(getContext());
        detailsLayout.setOrientation(LinearLayout.VERTICAL);
        detailsLayout.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        detailsLayout.setPadding(12, 0, 0, 0);

        TextView routeText = new TextView(getContext());
        routeText.setText(from + " → " + to);
        routeText.setTextSize(14);
        routeText.setTextColor(0xFF1A1A2E);
        routeText.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView infoText = new TextView(getContext());
        infoText.setText(mode + " • " + String.format("%.1f", distance) + " km");
        infoText.setTextSize(12);
        infoText.setTextColor(0xFF666666);
        infoText.setPadding(0, 4, 0, 0);

        detailsLayout.addView(routeText);
        detailsLayout.addView(infoText);

        // Distance
        TextView distanceText = new TextView(getContext());
        distanceText.setText(String.format("%.1f", distance) + " km");
        distanceText.setTextSize(14);
        distanceText.setTextColor(0xFF0D47A1);
        distanceText.setTypeface(null, android.graphics.Typeface.BOLD);

        itemLayout.addView(iconCircle);
        itemLayout.addView(detailsLayout);
        itemLayout.addView(distanceText);

        return itemLayout;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStats();
        loadRecentTrips();
    }
}
