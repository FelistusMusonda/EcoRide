package com.example.ecoride;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<DatabaseHelper.User> users;
    private DatabaseHelper databaseHelper;
    private Context context;
    private OnUserActionListener listener;

    public interface OnUserActionListener {
        void onDeleteUser(DatabaseHelper.User user, int position);
        void onViewUserDetails(DatabaseHelper.User user);
    }

    public UserAdapter(List<DatabaseHelper.User> users, DatabaseHelper databaseHelper,
                       Context context, OnUserActionListener listener) {
        this.users = users;
        this.databaseHelper = databaseHelper;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        try {
            DatabaseHelper.User user = users.get(position);
            holder.tvUserName.setText(user.name);
            holder.tvUserEmail.setText(user.email);
            holder.tvUserTrips.setText("Trips: " + user.totalTrips);
            holder.tvUserCarbon.setText(String.format("CO2: %.1f kg", user.totalCarbon));

            // Load user's recent trips
            List<DatabaseHelper.Trip> userTrips = databaseHelper.getUserTrips(user.id);
            if (userTrips != null && !userTrips.isEmpty()) {
                DatabaseHelper.Trip latestTrip = userTrips.get(0);
                holder.tvLastTrip.setText("Last: " + latestTrip.from + " → " + latestTrip.to);
            } else {
                holder.tvLastTrip.setText("No trips yet");
            }

            // View details button
            holder.btnView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewUserDetails(user);
                }
            });

            // Delete button
            holder.btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteUser(user, position);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvUserEmail, tvUserTrips, tvUserCarbon, tvLastTrip;
        ImageButton btnView, btnDelete;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvUserEmail = itemView.findViewById(R.id.tv_user_email);
            tvUserTrips = itemView.findViewById(R.id.tv_user_trips);
            tvUserCarbon = itemView.findViewById(R.id.tv_user_carbon);
            tvLastTrip = itemView.findViewById(R.id.tv_last_trip);
            btnView = itemView.findViewById(R.id.btn_view);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}