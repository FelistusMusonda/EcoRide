package com.example.ecoride;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<DatabaseHelper.User> userList;
    private DatabaseHelper databaseHelper;

    public UserAdapter(List<DatabaseHelper.User> userList, DatabaseHelper databaseHelper) {
        this.userList = userList;
        this.databaseHelper = databaseHelper;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        DatabaseHelper.User user = userList.get(position);
        holder.tvUserName.setText(user.name);
        holder.tvUserEmail.setText(user.email);
        holder.tvUserType.setText(user.isAdmin ? "Admin" : "User");

        int tripCount = databaseHelper.getUserTrips(user.id).size();
        holder.tvTripCount.setText("Trips: " + tripCount);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvUserEmail, tvUserType, tvTripCount;

        UserViewHolder(View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvUserEmail = itemView.findViewById(R.id.tv_user_email);
            tvUserType = itemView.findViewById(R.id.tv_user_type);
            tvTripCount = itemView.findViewById(R.id.tv_trip_count);
        }
    }
}