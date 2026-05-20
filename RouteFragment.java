package com.example.ecoride;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class RouteFragment extends Fragment {

    private EditText etStart, etEnd;
    private Button btnPlan;
    private TextView tvResult;
    private SharedPreferences prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_route, container, false);

        etStart = view.findViewById(R.id.et_start);
        etEnd = view.findViewById(R.id.et_end);
        btnPlan = view.findViewById(R.id.btn_plan);
        tvResult = view.findViewById(R.id.tv_result);
        prefs = requireContext().getSharedPreferences("EcoRide", 0);

        // Set toolbar title
        if (getActivity() != null) {
            getActivity().setTitle("Plan Route");
        }

        btnPlan.setOnClickListener(v -> {
            String start = etStart.getText().toString().trim();
            String end = etEnd.getText().toString().trim();

            if (start.isEmpty() || end.isEmpty()) {
                Toast.makeText(getContext(), "Please enter both locations", Toast.LENGTH_SHORT).show();
            } else {
                double distance = 3.5;
                double carbonSaved = distance * 0.192;

                // Save trip data
                float totalCarbon = prefs.getFloat("total_carbon", 0);
                float totalDistance = prefs.getFloat("total_distance", 0);
                int totalTrips = prefs.getInt("total_trips", 0);

                prefs.edit()
                        .putFloat("total_carbon", totalCarbon + (float)carbonSaved)
                        .putFloat("total_distance", totalDistance + (float)distance)
                        .putInt("total_trips", totalTrips + 1)
                        .apply();

                tvResult.setText("From: " + start + "\nTo: " + end +
                        "\n\nDistance: " + distance + " km" +
                        "\nCarbon Saved: " + String.format("%.2f", carbonSaved) + " kg CO₂" +
                        "\n\n🌿 Great choice for the environment!");
                tvResult.setVisibility(View.VISIBLE);

                Toast.makeText(getContext(), "Trip saved! +" + String.format("%.2f", carbonSaved) + " kg CO₂ saved", Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }
}