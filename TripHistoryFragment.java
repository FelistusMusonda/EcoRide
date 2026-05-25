package com.example.ecoride;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class TripHistoryFragment extends Fragment {

    private TextView tvTotalTrips, tvTotalDistance, tvTotalCarbon;
    private TextView tvWalkingCount, tvCyclingCount, tvPublicCount;
    private TextView tvWalkingDistance, tvCyclingDistance, tvPublicDistance;
    private TextView tvWalkingCarbon, tvCyclingCarbon, tvPublicCarbon;
    private RecyclerView rvRecentTrips;
    private SharedPreferences prefs;
    private DatabaseHelper databaseHelper;
    private int currentUserId;
    private TripAdapter tripAdapter;
    private List<Trip> allTrips = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trip_history, container, false);

        tvTotalTrips = view.findViewById(R.id.tv_total_trips);
        tvTotalDistance = view.findViewById(R.id.tv_total_distance);
        tvTotalCarbon = view.findViewById(R.id.tv_total_carbon);

        tvWalkingCount = view.findViewById(R.id.tv_walking_count);
        tvCyclingCount = view.findViewById(R.id.tv_cycling_count);
        tvPublicCount = view.findViewById(R.id.tv_public_count);

        tvWalkingDistance = view.findViewById(R.id.tv_walking_distance);
        tvCyclingDistance = view.findViewById(R.id.tv_cycling_distance);
        tvPublicDistance = view.findViewById(R.id.tv_public_distance);

        tvWalkingCarbon = view.findViewById(R.id.tv_walking_carbon);
        tvCyclingCarbon = view.findViewById(R.id.tv_cycling_carbon);
        tvPublicCarbon = view.findViewById(R.id.tv_public_carbon);

        rvRecentTrips = view.findViewById(R.id.rv_recent_trips);

        prefs = requireContext().getSharedPreferences("EcoRide", 0);
        databaseHelper = new DatabaseHelper(requireContext());
        currentUserId = prefs.getInt("userId", -1);

        loadTripHistory();
        setupRecyclerView();

        return view;
    }

    private void loadTripHistory() {
        if (currentUserId != -1) {
            loadTripHistoryFromDatabase();
            return;
        }

        int totalTrips = prefs.getInt("total_trips", 0);
        float totalDistance = prefs.getFloat("total_distance", 0);
        float totalCarbon = prefs.getFloat("total_carbon", 0);

        int walkingCount = prefs.getInt("walks_count", 0);
        int cyclingCount = prefs.getInt("cycles_count", 0);
        int publicCount = prefs.getInt("public_count", 0);

        float walkingDistance = prefs.getFloat("walking_distance", 0);
        float cyclingDistance = prefs.getFloat("cycling_distance", 0);
        float publicDistance = prefs.getFloat("public_distance", 0);

        float walkingCarbon = prefs.getFloat("walking_carbon", 0);
        float cyclingCarbon = prefs.getFloat("cycling_carbon", 0);
        float publicCarbon = prefs.getFloat("public_carbon", 0);

        tvTotalTrips.setText(String.valueOf(totalTrips));
        tvTotalDistance.setText(String.format("%.1f km", totalDistance));
        tvTotalCarbon.setText(String.format("%.1f kg", totalCarbon));

        tvWalkingCount.setText(String.valueOf(walkingCount));
        tvCyclingCount.setText(String.valueOf(cyclingCount));
        tvPublicCount.setText(String.valueOf(publicCount));

        tvWalkingDistance.setText(String.format("%.1f km", walkingDistance));
        tvCyclingDistance.setText(String.format("%.1f km", cyclingDistance));
        tvPublicDistance.setText(String.format("%.1f km", publicDistance));

        tvWalkingCarbon.setText(String.format("%.1f kg", walkingCarbon));
        tvCyclingCarbon.setText(String.format("%.1f kg", cyclingCarbon));
        tvPublicCarbon.setText(String.format("%.1f kg", publicCarbon));

        loadRecentTrips();
    }

    private void loadTripHistoryFromDatabase() {
        allTrips.clear();

        List<DatabaseHelper.Trip> dbTrips = databaseHelper.getUserTrips(currentUserId);

        int totalTrips = 0;
        double totalDistance = 0;
        double totalCarbon = 0;

        int walkingCount = 0;
        int cyclingCount = 0;
        int publicCount = 0;

        double walkingDistance = 0;
        double cyclingDistance = 0;
        double publicDistance = 0;

        double walkingCarbon = 0;
        double cyclingCarbon = 0;
        double publicCarbon = 0;

        for (DatabaseHelper.Trip dbTrip : dbTrips) {
            Trip trip = new Trip(dbTrip.from, dbTrip.to, dbTrip.mode, dbTrip.distance, dbTrip.carbonSaved);
            allTrips.add(trip);

            int tripCount = getTripCountFromMode(dbTrip.mode);
            String modeLower = dbTrip.mode == null ? "" : dbTrip.mode.toLowerCase();

            totalTrips += tripCount;
            totalDistance += dbTrip.distance;
            totalCarbon += dbTrip.carbonSaved;

            if (modeLower.contains("walking")) {
                walkingCount += tripCount;
                walkingDistance += dbTrip.distance;
                walkingCarbon += dbTrip.carbonSaved;
            } else if (modeLower.contains("cycling")) {
                cyclingCount += tripCount;
                cyclingDistance += dbTrip.distance;
                cyclingCarbon += dbTrip.carbonSaved;
            } else if (modeLower.contains("bus")) {
                publicCount += tripCount;
                publicDistance += dbTrip.distance;
                publicCarbon += dbTrip.carbonSaved;
            }
        }

        tvTotalTrips.setText(String.valueOf(totalTrips));
        tvTotalDistance.setText(String.format("%.1f km", totalDistance));
        tvTotalCarbon.setText(String.format("%.1f kg", totalCarbon));

        tvWalkingCount.setText(String.valueOf(walkingCount));
        tvCyclingCount.setText(String.valueOf(cyclingCount));
        tvPublicCount.setText(String.valueOf(publicCount));

        tvWalkingDistance.setText(String.format("%.1f km", walkingDistance));
        tvCyclingDistance.setText(String.format("%.1f km", cyclingDistance));
        tvPublicDistance.setText(String.format("%.1f km", publicDistance));

        tvWalkingCarbon.setText(String.format("%.1f kg", walkingCarbon));
        tvCyclingCarbon.setText(String.format("%.1f kg", cyclingCarbon));
        tvPublicCarbon.setText(String.format("%.1f kg", publicCarbon));

        if (tripAdapter != null) {
            tripAdapter.updateTrips(allTrips);
        }
    }

    private void loadRecentTrips() {
        allTrips.clear();

        String tripsData = prefs.getString("recent_trips", "");
        if (!tripsData.isEmpty()) {
            String[] trips = tripsData.split("\\|\\|");
            for (int i = trips.length - 1; i >= 0; i--) {
                String[] parts = trips[i].split("\\|");
                if (parts.length >= 5) {
                    try {
                        String from = parts[0];
                        String to = parts[1];
                        String mode = parts[2];
                        double distance = Double.parseDouble(parts[3]);
                        double carbon = Double.parseDouble(parts[4]);

                        Trip trip = new Trip(from, to, mode, distance, carbon);
                        allTrips.add(trip);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        if (tripAdapter != null) {
            tripAdapter.updateTrips(allTrips);
        }
    }

    private void setupRecyclerView() {
        rvRecentTrips.setLayoutManager(new LinearLayoutManager(getContext()));
        tripAdapter = new TripAdapter(allTrips);
        rvRecentTrips.setAdapter(tripAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        currentUserId = prefs.getInt("userId", -1);
        loadTripHistory();
    }

    private int getTripCountFromMode(String mode) {
        return mode != null && mode.toLowerCase().contains("round trip") ? 2 : 1;
    }

    static class Trip {
        String from, to, mode;
        double distance, carbon;
        Trip(String from, String to, String mode, double distance, double carbon) {
            this.from = from;
            this.to = to;
            this.mode = mode;
            this.distance = distance;
            this.carbon = carbon;
        }
    }

    class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder> {
        private List<Trip> trips;

        TripAdapter(List<Trip> trips) {
            this.trips = trips;
        }

        public void updateTrips(List<Trip> newTrips) {
            this.trips = newTrips;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Trip trip = trips.get(position);
            holder.tvFromTo.setText(trip.from + " → " + trip.to);
            holder.tvMode.setText(trip.mode);
            holder.tvDistance.setText(String.format("%.1f km", trip.distance));
            holder.tvCarbon.setText(String.format("%.1f kg CO2", trip.carbon));

            if (trip.mode.contains("Walking")) {
                holder.tvMode.setTextColor(0xFF4CAF50);
            } else if (trip.mode.contains("Cycling")) {
                holder.tvMode.setTextColor(0xFF2196F3);
            } else {
                holder.tvMode.setTextColor(0xFFFF9800);
            }
        }

        @Override
        public int getItemCount() {
            return trips.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvFromTo, tvMode, tvDistance, tvCarbon;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvFromTo = itemView.findViewById(R.id.tv_trip_from_to);
                tvMode = itemView.findViewById(R.id.tv_trip_mode);
                tvDistance = itemView.findViewById(R.id.tv_trip_distance);
                tvCarbon = itemView.findViewById(R.id.tv_trip_carbon);
            }
        }
    }
}
