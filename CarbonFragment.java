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

public class CarbonFragment extends Fragment {

    private TextView tvTotalCarbon, tvTrees;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_carbon_tracker, container, false);

        tvTotalCarbon = view.findViewById(R.id.tv_total_carbon);
        tvTrees = view.findViewById(R.id.tv_trees);

        prefs = requireContext().getSharedPreferences("EcoRide", 0);
        loadStats();

        return view;
    }

    private void loadStats() {
        float totalCarbon = prefs.getFloat("total_carbon", 0);
        double trees = totalCarbon / 22;

        tvTotalCarbon.setText(String.format("%.1f", totalCarbon));
        tvTrees.setText(String.format("%.1f", trees));
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStats();
    }
}