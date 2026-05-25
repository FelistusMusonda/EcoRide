package com.example.ecoride;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private static final int MAX_RECENT_TRIPS = 3;
    private static final String[] ECO_TIPS = {
            "Walking 1 km instead of driving saves about 0.19 kg of CO2.",
            "Cycling a short commute can save fuel, parking time, and daily emissions.",
            "Taking the bus can cut route emissions sharply compared with driving alone.",
            "Combining errands into one trip reduces avoidable distance.",
            "Carpooling shares the same journey impact across more people."
    };

    private TextView tvCarbonSaved, tvTripsCount, tvTrees, tvUserName, tvEcoTip, tvGreeting, tvRecentSummary;
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
        tvGreeting = view.findViewById(R.id.tv_greeting);
        tvRecentSummary = view.findViewById(R.id.tv_recent_summary);
        btnPlanRoute = view.findViewById(R.id.btn_plan_route);
        btnViewStats = view.findViewById(R.id.btn_view_stats);
        recentActivityContainer = view.findViewById(R.id.recent_activity_container);

        prefs = requireContext().getSharedPreferences("EcoRide", 0);

        loadHeader();
        loadStats();
        loadRecentTrips();
        loadDailyTip();

        btnPlanRoute.setOnClickListener(v -> selectBottomNavItem(R.id.navigation_route));
        btnViewStats.setOnClickListener(v -> selectBottomNavItem(R.id.navigation_carbon));

        return view;
    }

    private void loadHeader() {
        String userName = prefs.getString("userName", "Guest");
        if (TextUtils.isEmpty(userName)) {
            userName = "Guest";
        }

        tvUserName.setText(userName);
        tvGreeting.setText(getGreeting());
    }

    private String getGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) {
            return "Good morning,";
        } else if (hour < 17) {
            return "Good afternoon,";
        }
        return "Good evening,";
    }

    private void loadDailyTip() {
        int dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        tvEcoTip.setText(ECO_TIPS[dayOfYear % ECO_TIPS.length]);
    }

    private void selectBottomNavItem(int itemId) {
        if (getActivity() == null) {
            return;
        }

        BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(itemId);
        }
    }

    private void loadStats() {
        float carbon = prefs.getFloat("total_carbon", 0);
        int trips = prefs.getInt("total_trips", 0);
        double trees = carbon / 22;

        tvCarbonSaved.setText(String.format(Locale.US, "%.1f", carbon));
        tvTripsCount.setText(String.valueOf(trips));
        tvTrees.setText(String.format(Locale.US, "%.1f", trees));
    }

    private void loadRecentTrips() {
        recentActivityContainer.removeAllViews();

        List<RecentTrip> recentTrips = getRecentTrips();
        tvRecentSummary.setText(getRecentSummary(recentTrips.size()));

        for (int i = 0; i < recentTrips.size(); i++) {
            recentActivityContainer.addView(createTripItem(recentTrips.get(i)));

            if (i < recentTrips.size() - 1) {
                View divider = new View(requireContext());
                divider.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp(1)));
                divider.setBackgroundColor(0xFFEEEEEE);
                recentActivityContainer.addView(divider);
            }
        }

        if (recentActivityContainer.getChildCount() == 0) {
            TextView emptyText = new TextView(requireContext());
            emptyText.setText("No trips yet. Plan your first eco-friendly trip.");
            emptyText.setTextSize(14);
            emptyText.setTextColor(0xFF666666);
            emptyText.setPadding(dp(16), dp(18), dp(16), dp(18));
            emptyText.setGravity(android.view.Gravity.CENTER);
            emptyText.setBackgroundResource(R.drawable.recent_empty_bg);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(dp(8), dp(8), dp(8), dp(8));
            emptyText.setLayoutParams(params);
            recentActivityContainer.addView(emptyText);
        }
    }

    private String getRecentSummary(int count) {
        if (count == 0) {
            return "No trips";
        }
        if (count == 1) {
            return "Latest trip";
        }
        return count + " latest trips";
    }

    private List<RecentTrip> getRecentTrips() {
        List<RecentTrip> recentTrips = new ArrayList<>();
        String tripsData = prefs.getString("recent_trips", "");
        if (TextUtils.isEmpty(tripsData)) {
            return recentTrips;
        }

        String[] trips = tripsData.split("\\|\\|");
        for (int i = trips.length - 1; i >= 0 && recentTrips.size() < MAX_RECENT_TRIPS; i--) {
            RecentTrip trip = parseTrip(trips[i]);
            if (trip != null) {
                recentTrips.add(trip);
            }
        }
        return recentTrips;
    }

    @Nullable
    private RecentTrip parseTrip(String tripRecord) {
        if (TextUtils.isEmpty(tripRecord)) {
            return null;
        }

        String[] parts = tripRecord.split("\\|");
        if (parts.length < 5) {
            return null;
        }

        try {
            return new RecentTrip(
                    parts[0].trim(),
                    parts[1].trim(),
                    parts[2].trim(),
                    Double.parseDouble(parts[3]),
                    Double.parseDouble(parts[4])
            );
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LinearLayout createTripItem(RecentTrip trip) {
        LinearLayout itemLayout = new LinearLayout(requireContext());
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
        itemLayout.setPadding(dp(16), dp(14), dp(16), dp(14));
        itemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout iconCircle = new LinearLayout(requireContext());
        iconCircle.setLayoutParams(new LinearLayout.LayoutParams(dp(48), dp(48)));
        iconCircle.setGravity(android.view.Gravity.CENTER);

        TextView iconText = new TextView(requireContext());
        if (trip.mode.contains("Walking")) {
            iconCircle.setBackgroundResource(R.drawable.circle_bg_walking);
            iconText.setText("W");
        } else if (trip.mode.contains("Cycling")) {
            iconCircle.setBackgroundResource(R.drawable.circle_bg_cycling);
            iconText.setText("C");
        } else {
            iconCircle.setBackgroundResource(R.drawable.circle_bg_bus);
            iconText.setText("B");
        }
        iconText.setTextSize(20);
        iconText.setTextColor(0xFFFFFFFF);
        iconText.setTypeface(null, Typeface.BOLD);
        iconCircle.addView(iconText);

        LinearLayout detailsLayout = new LinearLayout(requireContext());
        detailsLayout.setOrientation(LinearLayout.VERTICAL);
        detailsLayout.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        detailsLayout.setPadding(dp(12), 0, dp(12), 0);

        TextView routeText = new TextView(requireContext());
        routeText.setText(trip.from + " -> " + trip.to);
        routeText.setTextSize(14);
        routeText.setTextColor(0xFF1A1A2E);
        routeText.setTypeface(null, Typeface.BOLD);
        routeText.setSingleLine(true);
        routeText.setEllipsize(TextUtils.TruncateAt.END);

        TextView infoText = new TextView(requireContext());
        infoText.setText(String.format(Locale.US, "%s - %.1f kg CO2 saved", trip.mode, trip.carbon));
        infoText.setTextSize(12);
        infoText.setTextColor(0xFF666666);
        infoText.setPadding(0, 4, 0, 0);
        infoText.setSingleLine(true);
        infoText.setEllipsize(TextUtils.TruncateAt.END);

        detailsLayout.addView(routeText);
        detailsLayout.addView(infoText);

        TextView distanceText = new TextView(requireContext());
        distanceText.setText(String.format(Locale.US, "%.1f km", trip.distance));
        distanceText.setTextSize(14);
        distanceText.setTextColor(0xFF0D47A1);
        distanceText.setTypeface(null, Typeface.BOLD);

        itemLayout.addView(iconCircle);
        itemLayout.addView(detailsLayout);
        itemLayout.addView(distanceText);

        return itemLayout;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (prefs != null && recentActivityContainer != null) {
            loadHeader();
            loadStats();
            loadRecentTrips();
        }
    }

    private static class RecentTrip {
        final String from;
        final String to;
        final String mode;
        final double distance;
        final double carbon;

        RecentTrip(String from, String to, String mode, double distance, double carbon) {
            this.from = from;
            this.to = to;
            this.mode = mode;
            this.distance = distance;
            this.carbon = carbon;
        }
    }
}
