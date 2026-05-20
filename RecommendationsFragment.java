package com.example.ecoride;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommendationsFragment extends Fragment {

    private Spinner spinnerFrom, spinnerTo;
    private Button btnGetRecommendation;
    private TextView tvRecommendation, tvWalkingTime, tvCyclingTime, tvBusTime;
    private TextView tvWalkingCarbon, tvCyclingCarbon, tvBusCarbon;
    private TextView tvBestMode;

    private static final Map<String, double[]> KITWE_LOCATIONS = new HashMap<>();
    private static final List<String> ALL_LOCATIONS = new ArrayList<>();

    static {
        KITWE_LOCATIONS.put("Town Centre", new double[]{-12.8231, 28.2136});
        KITWE_LOCATIONS.put("Mindolo Dam", new double[]{-12.8000, 28.2500});
        KITWE_LOCATIONS.put("Mukuba Mall", new double[]{-12.8200, 28.2100});
        KITWE_LOCATIONS.put("Parklands", new double[]{-12.8100, 28.2300});
        KITWE_LOCATIONS.put("Riverside", new double[]{-12.8250, 28.2050});
        KITWE_LOCATIONS.put("Buchi", new double[]{-12.8350, 28.2000});
        KITWE_LOCATIONS.put("Chimwemwe", new double[]{-12.8400, 28.1950});
        KITWE_LOCATIONS.put("Copperbelt University", new double[]{-12.8300, 28.2200});
        KITWE_LOCATIONS.put("Kitwe Central Hospital", new double[]{-12.8150, 28.2150});

        ALL_LOCATIONS.addAll(KITWE_LOCATIONS.keySet());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recommendations, container, false);

        spinnerFrom = view.findViewById(R.id.spinner_from);
        spinnerTo = view.findViewById(R.id.spinner_to);
        btnGetRecommendation = view.findViewById(R.id.btn_get_recommendation);
        tvRecommendation = view.findViewById(R.id.tv_recommendation);
        tvWalkingTime = view.findViewById(R.id.tv_walking_time);
        tvCyclingTime = view.findViewById(R.id.tv_cycling_time);
        tvBusTime = view.findViewById(R.id.tv_bus_time);
        tvWalkingCarbon = view.findViewById(R.id.tv_walking_carbon);
        tvCyclingCarbon = view.findViewById(R.id.tv_cycling_carbon);
        tvBusCarbon = view.findViewById(R.id.tv_bus_carbon);
        tvBestMode = view.findViewById(R.id.tv_best_mode);

        // Setup spinners
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, ALL_LOCATIONS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrom.setAdapter(adapter);
        spinnerTo.setAdapter(adapter);

        btnGetRecommendation.setOnClickListener(v -> getRecommendation());

        return view;
    }

    private void getRecommendation() {
        if (spinnerFrom.getSelectedItem() == null || spinnerTo.getSelectedItem() == null) {
            Toast.makeText(getContext(), "Please select locations", Toast.LENGTH_SHORT).show();
            return;
        }

        String from = spinnerFrom.getSelectedItem().toString();
        String to = spinnerTo.getSelectedItem().toString();

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

        int walkingTime = (int)(distance * 12);
        int cyclingTime = (int)(distance * 4);
        int busTime = (int)(distance * 3 + 5);

        double walkingCarbon = 0.192 * distance;
        double cyclingCarbon = 0.192 * distance;
        double busCarbon = 0.147 * distance;

        tvWalkingTime.setText(walkingTime + " min");
        tvCyclingTime.setText(cyclingTime + " min");
        tvBusTime.setText(busTime + " min");

        tvWalkingCarbon.setText(String.format("%.2f kg", walkingCarbon));
        tvCyclingCarbon.setText(String.format("%.2f kg", cyclingCarbon));
        tvBusCarbon.setText(String.format("%.2f kg", busCarbon));

        String bestMode;
        if (distance < 2) {
            bestMode = "Walking";
        } else if (distance < 5) {
            bestMode = "Cycling";
        } else {
            bestMode = "Bus";
        }

        tvBestMode.setText(bestMode);
        tvRecommendation.setText("For " + String.format("%.1f", distance) + " km, " + bestMode + " is recommended.");

        // Make result visible
        View resultLayout = getView().findViewById(R.id.result_layout);
        if (resultLayout != null) {
            resultLayout.setVisibility(View.VISIBLE);
        }
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
}