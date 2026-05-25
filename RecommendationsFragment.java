package com.example.ecoride;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RecommendationsFragment extends Fragment {

    private static final double CAR_EMISSION_KG_PER_KM = 0.192;
    private static final double BUS_EMISSION_KG_PER_KM = 0.045;

    private Spinner spinnerFrom, spinnerTo;
    private Button btnGetRecommendation, btnPlanRecommendedRoute;
    private LinearLayout resultLayout;
    private TextView tvRecommendation, tvWalkingTime, tvCyclingTime, tvBusTime;
    private TextView tvWalkingCarbon, tvCyclingCarbon, tvBusCarbon;
    private TextView tvBestMode;

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
        private final String hint;

        HintSpinnerAdapter(Context context, List<String> items, String hint) {
            super(context, android.R.layout.simple_spinner_item, items);
            this.hint = hint;
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView) super.getView(position, convertView, parent);
            view.setText(getItem(position));
            view.setTextColor(position == 0 ? 0xFF999999 : 0xFF1A1A2E);
            return view;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recommendations, container, false);

        spinnerFrom = view.findViewById(R.id.spinner_from);
        spinnerTo = view.findViewById(R.id.spinner_to);
        btnGetRecommendation = view.findViewById(R.id.btn_get_recommendation);
        btnPlanRecommendedRoute = view.findViewById(R.id.btn_plan_recommended_route);
        resultLayout = view.findViewById(R.id.result_layout);
        tvRecommendation = view.findViewById(R.id.tv_recommendation);
        tvWalkingTime = view.findViewById(R.id.tv_walking_time);
        tvCyclingTime = view.findViewById(R.id.tv_cycling_time);
        tvBusTime = view.findViewById(R.id.tv_bus_time);
        tvWalkingCarbon = view.findViewById(R.id.tv_walking_carbon);
        tvCyclingCarbon = view.findViewById(R.id.tv_cycling_carbon);
        tvBusCarbon = view.findViewById(R.id.tv_bus_carbon);
        tvBestMode = view.findViewById(R.id.tv_best_mode);

        List<String> locations = new ArrayList<>(ALL_LOCATIONS);
        spinnerFrom.setAdapter(new HintSpinnerAdapter(requireContext(), locations, "Select starting point"));
        spinnerTo.setAdapter(new HintSpinnerAdapter(requireContext(), locations, "Select destination"));

        btnGetRecommendation.setOnClickListener(v -> getRecommendation());
        btnPlanRecommendedRoute.setOnClickListener(v -> openRoutePlanner());

        return view;
    }

    private void getRecommendation() {
        int fromPosition = spinnerFrom.getSelectedItemPosition();
        int toPosition = spinnerTo.getSelectedItemPosition();

        if (fromPosition == 0 || toPosition == 0) {
            Toast.makeText(getContext(), "Please select both locations", Toast.LENGTH_SHORT).show();
            return;
        }

        String from = ALL_LOCATIONS.get(fromPosition - 1);
        String to = ALL_LOCATIONS.get(toPosition - 1);

        if (from.equals(to)) {
            Toast.makeText(getContext(), "Please select different locations", Toast.LENGTH_SHORT).show();
            return;
        }

        double[] fromCoords = KITWE_LOCATIONS.get(from);
        double[] toCoords = KITWE_LOCATIONS.get(to);
        if (fromCoords == null || toCoords == null) {
            Toast.makeText(getContext(), "Location not found", Toast.LENGTH_SHORT).show();
            return;
        }

        double distance = calculateDistance(fromCoords[0], fromCoords[1], toCoords[0], toCoords[1]);
        Recommendation walking = new Recommendation("Walking", distance, (int) Math.ceil(distance * 12), CAR_EMISSION_KG_PER_KM * distance);
        Recommendation cycling = new Recommendation("Cycling", distance, (int) Math.ceil(distance * 4), CAR_EMISSION_KG_PER_KM * distance);
        Recommendation bus = new Recommendation("Bus", distance, (int) Math.ceil(distance * 3 + 5), (CAR_EMISSION_KG_PER_KM - BUS_EMISSION_KG_PER_KM) * distance);
        Recommendation best = chooseBestMode(distance, walking, cycling, bus);

        tvWalkingTime.setText(formatMinutes(walking.minutes));
        tvCyclingTime.setText(formatMinutes(cycling.minutes));
        tvBusTime.setText(formatMinutes(bus.minutes));

        tvWalkingCarbon.setText(formatCarbon(walking.carbonSaved));
        tvCyclingCarbon.setText(formatCarbon(cycling.carbonSaved));
        tvBusCarbon.setText(formatCarbon(bus.carbonSaved));

        tvBestMode.setText(best.mode);
        tvRecommendation.setText(String.format(Locale.US,
                "%.1f km from %s to %s. %s is the best fit because it balances time and CO2 saved for this distance.",
                distance, from, to, best.mode));

        resultLayout.setVisibility(View.VISIBLE);
    }

    private Recommendation chooseBestMode(double distance, Recommendation walking, Recommendation cycling, Recommendation bus) {
        if (distance <= 1.5) {
            return walking;
        }
        if (distance <= 6.0) {
            return cycling;
        }
        return bus;
    }

    private void openRoutePlanner() {
        if (getActivity() == null) {
            return;
        }

        BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.navigation_route);
        }
    }

    private String formatMinutes(int minutes) {
        if (minutes >= 60) {
            return String.format(Locale.US, "%dh %dm", minutes / 60, minutes % 60);
        }
        return String.format(Locale.US, "%d min", minutes);
    }

    private String formatCarbon(double carbon) {
        return String.format(Locale.US, "%.2f kg", carbon);
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double radiusKm = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return radiusKm * c;
    }

    private static class Recommendation {
        final String mode;
        final double distance;
        final int minutes;
        final double carbonSaved;

        Recommendation(String mode, double distance, int minutes, double carbonSaved) {
            this.mode = mode;
            this.distance = distance;
            this.minutes = minutes;
            this.carbonSaved = carbonSaved;
        }
    }
}
