package com.example.ecoride;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoutePlannerFragment extends Fragment {

    // COMPLETE KITWE LOCATIONS DATABASE
    private static final Map<String, double[]> KITWE_LOCATIONS = new HashMap<>();
    private static final List<String> ALL_LOCATIONS = new ArrayList<>();

    static {
        // Residential Areas
        KITWE_LOCATIONS.put("Nkana East", new double[]{-12.8180, 28.2250});
        KITWE_LOCATIONS.put("Nkana West", new double[]{-12.8200, 28.1900});
        KITWE_LOCATIONS.put("Parklands", new double[]{-12.8100, 28.2300});
        KITWE_LOCATIONS.put("Riverside", new double[]{-12.8250, 28.2050});
        KITWE_LOCATIONS.put("Buchi", new double[]{-12.8350, 28.2000});
        KITWE_LOCATIONS.put("Chimwemwe", new double[]{-12.8400, 28.1950});
        KITWE_LOCATIONS.put("Ndeke", new double[]{-12.8250, 28.2250});
        KITWE_LOCATIONS.put("Kwacha", new double[]{-12.8300, 28.2150});
        KITWE_LOCATIONS.put("Wusakile", new double[]{-12.8450, 28.2050});
        KITWE_LOCATIONS.put("Bulangililo", new double[]{-12.8280, 28.2080});
        KITWE_LOCATIONS.put("Garneton", new double[]{-12.8150, 28.2100});
        KITWE_LOCATIONS.put("Mindolo", new double[]{-12.8000, 28.2500});
        KITWE_LOCATIONS.put("Itimpi", new double[]{-12.7900, 28.2400});
        KITWE_LOCATIONS.put("Ipusukilo", new double[]{-12.8000, 28.2400});
        KITWE_LOCATIONS.put("Kawama", new double[]{-12.8450, 28.1850});
        KITWE_LOCATIONS.put("Racecourse", new double[]{-12.8200, 28.2150});
        KITWE_LOCATIONS.put("Chamboli", new double[]{-12.8280, 28.2250});
        KITWE_LOCATIONS.put("Kamitondo", new double[]{-12.8380, 28.1980});
        KITWE_LOCATIONS.put("Kamatipa", new double[]{-12.8320, 28.2100});
        KITWE_LOCATIONS.put("Mulenga", new double[]{-12.8220, 28.2300});
        KITWE_LOCATIONS.put("Luangwa", new double[]{-12.8280, 28.2250});
        KITWE_LOCATIONS.put("Buyantanshi", new double[]{-12.8150, 28.2350});

        // Bus Stations
        KITWE_LOCATIONS.put("Kitwe Main Bus Station", new double[]{-12.8230, 28.2100});
        KITWE_LOCATIONS.put("Chisokone Bus Station", new double[]{-12.8230, 28.2150});
        KITWE_LOCATIONS.put("Mukuba Mall Taxi Rank", new double[]{-12.8200, 28.2100});
        KITWE_LOCATIONS.put("Power Tools Bus Station", new double[]{-12.82199, 28.20912});
        KITWE_LOCATIONS.put("KMB Bus Station", new double[]{-12.8162, 28.20253});

        // Shopping Malls
        KITWE_LOCATIONS.put("Mukuba Mall", new double[]{-12.8200, 28.2100});
        KITWE_LOCATIONS.put("ECL Mall", new double[]{-12.8230, 28.2130});
        KITWE_LOCATIONS.put("Nkana Mall", new double[]{-12.8180, 28.2250});
        KITWE_LOCATIONS.put("Copperhill Mall", new double[]{-12.8150, 28.2250});
        KITWE_LOCATIONS.put("Freedom Park Mall", new double[]{-12.8069, 28.2155});

        // Supermarkets
        KITWE_LOCATIONS.put("Shoprite", new double[]{-12.8231, 28.2136});
        KITWE_LOCATIONS.put("Pick n Pay", new double[]{-12.8200, 28.2100});
        KITWE_LOCATIONS.put("Game", new double[]{-12.8200, 28.2100});
        KITWE_LOCATIONS.put("Choppies", new double[]{-12.8080, 28.2280});

        // Markets
        KITWE_LOCATIONS.put("Chisokone Market", new double[]{-12.8230, 28.2150});
        KITWE_LOCATIONS.put("Nakadoli Market", new double[]{-12.8280, 28.2080});
        KITWE_LOCATIONS.put("Buchi Market", new double[]{-12.8350, 28.2000});

        // Schools & Universities
        KITWE_LOCATIONS.put("Copperbelt University (CBU)", new double[]{-12.8300, 28.2200});
        KITWE_LOCATIONS.put("Kitwe Boys Secondary", new double[]{-12.8180, 28.2150});
        KITWE_LOCATIONS.put("Nkana Trust School", new double[]{-12.8200, 28.2000});
        KITWE_LOCATIONS.put("Parklands Secondary", new double[]{-12.8080, 28.2280});
        KITWE_LOCATIONS.put("Lechwe School", new double[]{-12.8100, 28.2250});
        KITWE_LOCATIONS.put("Zambia Catholic University", new double[]{-12.8280, 28.2180});
        KITWE_LOCATIONS.put("Copperstone University", new double[]{-12.8250, 28.2250});

        // Hospitals
        KITWE_LOCATIONS.put("Kitwe Central Hospital", new double[]{-12.8150, 28.2150});
        KITWE_LOCATIONS.put("Wusakile Mine Hospital", new double[]{-12.8450, 28.2050});
        KITWE_LOCATIONS.put("Nkana Mine Hospital", new double[]{-12.8200, 28.2000});

        // Hotels
        KITWE_LOCATIONS.put("Garden Court Hotel", new double[]{-12.8086, 28.2149});
        KITWE_LOCATIONS.put("Sherbourne Hotel", new double[]{-12.8200, 28.2150});
        KITWE_LOCATIONS.put("Pamo Hotel", new double[]{-12.8230, 28.2130});
        KITWE_LOCATIONS.put("Oriental Swan Hotel", new double[]{-12.8220, 28.2130});

        // Recreation
        KITWE_LOCATIONS.put("Nkana Stadium", new double[]{-12.8180, 28.2150});
        KITWE_LOCATIONS.put("Arthur Davies Stadium", new double[]{-12.8230, 28.2136});
        KITWE_LOCATIONS.put("Mindolo Dam", new double[]{-12.8000, 28.2500});
        KITWE_LOCATIONS.put("Freedom Park", new double[]{-12.8069, 28.2155});
        KITWE_LOCATIONS.put("Kitwe Golf Club", new double[]{-12.8100, 28.2220});

        // Restaurants
        KITWE_LOCATIONS.put("Nando's", new double[]{-12.8200, 28.2100});
        KITWE_LOCATIONS.put("KFC", new double[]{-12.8200, 28.2100});
        KITWE_LOCATIONS.put("Hungry Lion", new double[]{-12.8200, 28.2100});
        KITWE_LOCATIONS.put("Steers", new double[]{-12.8200, 28.2100});
        KITWE_LOCATIONS.put("Debonairs Pizza", new double[]{-12.8200, 28.2100});

        // Banks
        KITWE_LOCATIONS.put("Zanaco", new double[]{-12.8230, 28.2130});
        KITWE_LOCATIONS.put("FNB", new double[]{-12.7970, 28.2060});
        KITWE_LOCATIONS.put("Eco Bank", new double[]{-12.8230, 28.2130});
        KITWE_LOCATIONS.put("Standard Chartered", new double[]{-12.8230, 28.2130});

        // Town Centre
        KITWE_LOCATIONS.put("Town Centre", new double[]{-12.8231, 28.2136});

        ALL_LOCATIONS.addAll(KITWE_LOCATIONS.keySet());
    }

    private class HintSpinnerAdapter extends ArrayAdapter<String> {
        private String hint;

        public HintSpinnerAdapter(Context context, int resource, List<String> items, String hint) {
            super(context, resource, items);
            this.hint = hint;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView) super.getView(position, convertView, parent);
            if (position == 0) {
                view.setText(hint);
                view.setTextColor(getResources().getColor(android.R.color.darker_gray));
            } else {
                view.setTextColor(getResources().getColor(android.R.color.black));
            }
            return view;
        }

        @Override
        public int getCount() {
            return super.getCount() + 1;
        }

        @Override
        public String getItem(int position) {
            if (position == 0) {
                return hint;
            }
            return super.getItem(position - 1);
        }
    }

    private Spinner spinnerFrom, spinnerTo;
    private Button btnPlan;
    private LinearLayout resultLayout;
    private TextView tvResult;
    private TextView modeWalking, modeCycling, modeBus;
    private SwitchCompat switchRoundTrip;
    private String selectedMode = "walking";
    private SharedPreferences prefs;
    private DatabaseHelper databaseHelper;

    private double fromLat = 0, fromLon = 0;
    private double toLat = 0, toLon = 0;
    private String fromAddress = "", toAddress = "";
    private boolean hasCalculated = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_route_planner, container, false);

        spinnerFrom = view.findViewById(R.id.spinner_from);
        spinnerTo = view.findViewById(R.id.spinner_to);
        btnPlan = view.findViewById(R.id.btn_plan);
        resultLayout = view.findViewById(R.id.result_layout);
        tvResult = view.findViewById(R.id.tv_result);
        modeWalking = view.findViewById(R.id.mode_walking);
        modeCycling = view.findViewById(R.id.mode_cycling);
        modeBus = view.findViewById(R.id.mode_bus);
        switchRoundTrip = view.findViewById(R.id.switch_round_trip);

        prefs = requireContext().getSharedPreferences("EcoRide", 0);
        databaseHelper = new DatabaseHelper(requireContext());

        List<String> locationList = new ArrayList<>(ALL_LOCATIONS);

        HintSpinnerAdapter fromAdapter = new HintSpinnerAdapter(requireContext(),
                android.R.layout.simple_spinner_item, locationList, "▼ Select starting point");
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrom.setAdapter(fromAdapter);

        HintSpinnerAdapter toAdapter = new HintSpinnerAdapter(requireContext(),
                android.R.layout.simple_spinner_item, locationList, "▼ Select destination");
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTo.setAdapter(toAdapter);

        modeWalking.setOnClickListener(v -> {
            selectedMode = "walking";
            selectMode(modeWalking);
            if (hasCalculated) {
                calculateAndShowPopup();
            }
        });

        modeCycling.setOnClickListener(v -> {
            selectedMode = "cycling";
            selectMode(modeCycling);
            if (hasCalculated) {
                calculateAndShowPopup();
            }
        });

        modeBus.setOnClickListener(v -> {
            selectedMode = "bus";
            selectMode(modeBus);
            if (hasCalculated) {
                calculateAndShowPopup();
            }
        });

        selectMode(modeWalking);

        btnPlan.setOnClickListener(v -> calculateAndShowPopup());

        return view;
    }

    private void selectMode(TextView selectedView) {
        modeWalking.setSelected(false);
        modeCycling.setSelected(false);
        modeBus.setSelected(false);
        selectedView.setSelected(true);
    }

    private void calculateAndShowPopup() {
        int fromPosition = spinnerFrom.getSelectedItemPosition();
        int toPosition = spinnerTo.getSelectedItemPosition();

        if (fromPosition == 0 || toPosition == 0) {
            Toast.makeText(getContext(), "Please select both starting point and destination", Toast.LENGTH_SHORT).show();
            return;
        }

        fromAddress = ALL_LOCATIONS.get(fromPosition - 1);
        toAddress = ALL_LOCATIONS.get(toPosition - 1);

        double[] fromCoords = KITWE_LOCATIONS.get(fromAddress);
        double[] toCoords = KITWE_LOCATIONS.get(toAddress);

        if (fromCoords == null || toCoords == null) {
            Toast.makeText(getContext(), "Location coordinates not found", Toast.LENGTH_LONG).show();
            return;
        }

        fromLat = fromCoords[0];
        fromLon = fromCoords[1];
        toLat = toCoords[0];
        toLon = toCoords[1];

        double oneWayDistance = calculateDistance(fromLat, fromLon, toLat, toLon);
        boolean isRoundTrip = switchRoundTrip.isChecked();
        double totalDistance = isRoundTrip ? oneWayDistance * 2 : oneWayDistance;
        int durationMinutes = calculateTime(totalDistance, selectedMode);
        double carbonSaved = calculateCarbonSaved(totalDistance, selectedMode);

        // Save to BOTH database and SharedPreferences
        saveTripToDatabase(totalDistance, carbonSaved, isRoundTrip);
        saveTripData(totalDistance, carbonSaved, isRoundTrip);

        showPopup(fromAddress, toAddress, totalDistance, durationMinutes, carbonSaved, isRoundTrip, oneWayDistance);
    }

    // NEW METHOD: Save trip to database
    private void saveTripToDatabase(double distance, double carbon, boolean isRoundTrip) {
        int userId = prefs.getInt("userId", -1);
        String userName = prefs.getString("userName", "User");

        if (userId != -1) {
            String modeName = getModeName();
            String tripType = isRoundTrip ? "Round Trip" : "One Way";
            String modeWithType = modeName + " (" + tripType + ")";

            boolean success = databaseHelper.saveTrip(
                    userId,
                    userName,
                    fromAddress,
                    toAddress,
                    modeWithType,
                    distance,
                    carbon
            );

            if (success) {
                // Update user stats in database
                databaseHelper.updateUserStats(userId, carbon, isRoundTrip ? 2 : 1);
                Toast.makeText(getContext(), "Trip saved to database!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to save trip to database", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Login to save trips permanently", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPopup(String from, String to, double totalDistance, int durationMinutes, double carbonSaved, boolean isRoundTrip, double oneWayDistance) {
        String timeString;
        if (durationMinutes >= 60) {
            int hours = durationMinutes / 60;
            int mins = durationMinutes % 60;
            timeString = hours + "h " + mins + "m";
        } else {
            timeString = durationMinutes + " min";
        }

        String modeName = getModeName();
        String tripType = isRoundTrip ? "Round Trip" : "One Way";

        String displayDistance = String.format("%.1f km", totalDistance);

        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_route_info);
        dialog.setCancelable(true);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            dialog.getWindow().setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView tvFrom = dialog.findViewById(R.id.tv_popup_from);
        TextView tvTo = dialog.findViewById(R.id.tv_popup_to);
        TextView tvMode = dialog.findViewById(R.id.tv_popup_mode);
        TextView tvDistance = dialog.findViewById(R.id.tv_popup_distance);
        TextView tvTime = dialog.findViewById(R.id.tv_popup_time);
        TextView tvCarbon = dialog.findViewById(R.id.tv_popup_carbon);
        Button btnMinimize = dialog.findViewById(R.id.btn_minimize);
        Button btnNavigate = dialog.findViewById(R.id.btn_navigate);

        tvFrom.setText(from);
        tvTo.setText(to);
        tvMode.setText(modeName + " - " + tripType);
        tvDistance.setText(displayDistance);
        tvTime.setText(timeString);
        tvCarbon.setText(String.format("%.2f kg", carbonSaved));

        btnMinimize.setOnClickListener(v -> dialog.dismiss());

        btnNavigate.setOnClickListener(v -> {
            dialog.dismiss();
            openGoogleMaps(to);
        });

        dialog.show();
    }

    private void openGoogleMaps(String destination) {
        String fullDestination = destination + ", Kitwe, Zambia";
        Uri uri = Uri.parse("google.navigation:q=" + fullDestination.replace(" ", "+"));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");

        if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Uri webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + fullDestination.replace(" ", "+"));
            startActivity(new Intent(Intent.ACTION_VIEW, webUri));
        }
    }

    private String getModeName() {
        switch (selectedMode) {
            case "walking": return "Walking";
            case "cycling": return "Cycling";
            case "bus": return "Bus";
            default: return "";
        }
    }

    private void saveTripData(double distance, double carbon, boolean isRoundTrip) {
        float totalDistance = prefs.getFloat("total_distance", 0);
        float totalCarbon = prefs.getFloat("total_carbon", 0);
        int totalTrips = prefs.getInt("total_trips", 0);

        if (selectedMode.equals("walking")) {
            int walks = prefs.getInt("walks_count", 0);
            float walkingDistance = prefs.getFloat("walking_distance", 0);
            float walkingCarbon = prefs.getFloat("walking_carbon", 0);
            prefs.edit().putInt("walks_count", walks + (isRoundTrip ? 2 : 1)).apply();
            prefs.edit().putFloat("walking_distance", walkingDistance + (float) distance).apply();
            prefs.edit().putFloat("walking_carbon", walkingCarbon + (float) carbon).apply();
        } else if (selectedMode.equals("cycling")) {
            int cycles = prefs.getInt("cycles_count", 0);
            float cyclingDistance = prefs.getFloat("cycling_distance", 0);
            float cyclingCarbon = prefs.getFloat("cycling_carbon", 0);
            prefs.edit().putInt("cycles_count", cycles + (isRoundTrip ? 2 : 1)).apply();
            prefs.edit().putFloat("cycling_distance", cyclingDistance + (float) distance).apply();
            prefs.edit().putFloat("cycling_carbon", cyclingCarbon + (float) carbon).apply();
        } else if (selectedMode.equals("bus")) {
            int busTrips = prefs.getInt("public_count", 0);
            float busDistance = prefs.getFloat("public_distance", 0);
            float busCarbon = prefs.getFloat("public_carbon", 0);
            prefs.edit().putInt("public_count", busTrips + (isRoundTrip ? 2 : 1)).apply();
            prefs.edit().putFloat("public_distance", busDistance + (float) distance).apply();
            prefs.edit().putFloat("public_carbon", busCarbon + (float) carbon).apply();
        }

        String tripType = isRoundTrip ? "Round Trip" : "One Way";
        String modeName = selectedMode.equals("walking") ? "Walking" : (selectedMode.equals("cycling") ? "Cycling" : "Bus");
        String tripRecord = fromAddress + "|" + toAddress + "|" + modeName + " (" + tripType + ")|" + distance + "|" + carbon;
        String existingTrips = prefs.getString("recent_trips", "");
        if (existingTrips.isEmpty()) {
            prefs.edit().putString("recent_trips", tripRecord).apply();
        } else {
            prefs.edit().putString("recent_trips", existingTrips + "||" + tripRecord).apply();
        }

        prefs.edit()
                .putFloat("total_distance", totalDistance + (float) distance)
                .putFloat("total_carbon", totalCarbon + (float) carbon)
                .putInt("total_trips", totalTrips + (isRoundTrip ? 2 : 1))
                .apply();
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    private int calculateTime(double distanceKm, String mode) {
        switch (mode) {
            case "walking": return (int)(distanceKm * 12);
            case "cycling": return (int)(distanceKm * 4);
            case "bus": return (int)(distanceKm * 3 + 5);
            default: return (int)(distanceKm * 12);
        }
    }

    private double calculateCarbonSaved(double distanceKm, String mode) {
        double carEmission = 0.192;
        double alternativeEmission = mode.equals("bus") ? 0.045 : 0;
        return (carEmission - alternativeEmission) * distanceKm;
    }
}
