package com.example.ecoride;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    private final List<DatabaseHelper.LocationPlace> locations;
    private final OnLocationActionListener listener;

    public interface OnLocationActionListener {
        void onEditLocation(DatabaseHelper.LocationPlace location);
        void onDeleteLocation(DatabaseHelper.LocationPlace location);
    }

    public LocationAdapter(List<DatabaseHelper.LocationPlace> locations, OnLocationActionListener listener) {
        this.locations = locations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_location, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        DatabaseHelper.LocationPlace location = locations.get(position);
        holder.tvLocationName.setText(location.name);
        holder.tvLocationCoordinates.setText(String.format(java.util.Locale.US,
                "%.5f, %.5f", location.latitude, location.longitude));

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditLocation(location);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteLocation(location);
            }
        });
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView tvLocationName, tvLocationCoordinates;
        ImageButton btnEdit, btnDelete;

        LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLocationName = itemView.findViewById(R.id.tv_location_name);
            tvLocationCoordinates = itemView.findViewById(R.id.tv_location_coordinates);
            btnEdit = itemView.findViewById(R.id.btn_edit_location);
            btnDelete = itemView.findViewById(R.id.btn_delete_location);
        }
    }
}
