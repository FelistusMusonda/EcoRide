package com.example.ecoride;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    private static final int PICK_PROFILE_IMAGE_REQUEST = 5101;

    private TextView tvName, tvEmail, tvTotalTrips, tvTotalCarbon;
    private ImageView imgProfilePicture;
    private ImageButton btnProfileMenu;
    private Button btnEditProfile, btnChangePassword, btnChangePicture, btnClearData, btnLogout;
    private SharedPreferences prefs;
    private DatabaseHelper databaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvName = view.findViewById(R.id.tv_profile_name);
        tvEmail = view.findViewById(R.id.tv_profile_email);
        tvTotalTrips = view.findViewById(R.id.tv_total_trips);
        tvTotalCarbon = view.findViewById(R.id.tv_total_carbon);
        imgProfilePicture = view.findViewById(R.id.img_profile_picture);
        btnProfileMenu = view.findViewById(R.id.btn_profile_menu);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnChangePassword = view.findViewById(R.id.btn_change_password);
        btnChangePicture = view.findViewById(R.id.btn_change_picture);
        btnClearData = view.findViewById(R.id.btn_clear_data);
        btnLogout = view.findViewById(R.id.btn_logout);

        prefs = requireContext().getSharedPreferences("EcoRide", 0);
        databaseHelper = new DatabaseHelper(requireContext());
        loadUserProfile();

        btnEditProfile.setOnClickListener(v -> showEditDialog());
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        btnChangePicture.setOnClickListener(v -> pickProfilePicture());
        imgProfilePicture.setOnClickListener(v -> pickProfilePicture());
        btnProfileMenu.setOnClickListener(v -> showProfileMenu());
        btnClearData.setOnClickListener(v -> showClearDataDialog());
        btnLogout.setOnClickListener(v -> logout());

        return view;
    }

    private void showProfileMenu() {
        PopupMenu popupMenu = new PopupMenu(requireContext(), btnProfileMenu);
        popupMenu.getMenu().add("Edit User");
        popupMenu.getMenu().add("Change Password");
        popupMenu.getMenu().add("Clear Data");
        popupMenu.getMenu().add("Update Profile Picture");
        popupMenu.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if (title.equals("Edit User")) {
                showEditDialog();
            } else if (title.equals("Change Password")) {
                showChangePasswordDialog();
            } else if (title.equals("Clear Data")) {
                showClearDataDialog();
            } else if (title.equals("Update Profile Picture")) {
                pickProfilePicture();
            }
            return true;
        });
        popupMenu.show();
    }

    private void loadUserProfile() {
        String name = prefs.getString("userName", "Guest User");
        String email = prefs.getString("userEmail", "Not signed in");
        boolean isGuest = prefs.getBoolean("isGuest", false);

        int totalTrips = prefs.getInt("total_trips", 0);
        float totalCarbon = prefs.getFloat("total_carbon", 0);

        tvName.setText(name);

        if (isGuest) {
            tvEmail.setText("Guest Mode");
        } else {
            tvEmail.setText(email);
        }

        tvTotalTrips.setText(String.valueOf(totalTrips));
        tvTotalCarbon.setText(String.format("%.1f kg", totalCarbon));
        loadProfilePicture();
    }

    private void loadProfilePicture() {
        String uriString = prefs.getString(getProfilePictureKey(), "");
        if (!uriString.isEmpty()) {
            imgProfilePicture.setPadding(0, 0, 0, 0);
            imgProfilePicture.setImageURI(Uri.parse(uriString));
        }
    }

    private void showEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Profile");

        final android.widget.EditText input = new android.widget.EditText(getContext());
        input.setHint("Enter new name");
        input.setText(prefs.getString("userName", ""));
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                int userId = prefs.getInt("userId", -1);
                if (userId != -1) {
                    databaseHelper.updateUserName(userId, newName);
                }
                prefs.edit().putString("userName", newName).apply();
                tvName.setText(newName);
                Toast.makeText(getContext(), "Name updated!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showChangePasswordDialog() {
        if (prefs.getBoolean("isGuest", false)) {
            Toast.makeText(getContext(), "Guest accounts do not have passwords.", Toast.LENGTH_SHORT).show();
            return;
        }

        View layout = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null);
        android.widget.EditText oldPassword = layout.findViewById(R.id.et_current_password);
        android.widget.EditText newPassword = layout.findViewById(R.id.et_new_password);
        android.widget.EditText confirmPassword = layout.findViewById(R.id.et_confirm_password);

        new AlertDialog.Builder(requireContext())
                .setTitle("Change Password")
                .setView(layout)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {
                    String oldValue = oldPassword.getText().toString();
                    String newValue = newPassword.getText().toString();
                    String confirmValue = confirmPassword.getText().toString();

                    if (newValue.length() < 4) {
                        Toast.makeText(getContext(), "Password must be at least 4 characters.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!newValue.equals(confirmValue)) {
                        Toast.makeText(getContext(), "Passwords do not match.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean updated = databaseHelper.updateUserPassword(prefs.getInt("userId", -1), oldValue, newValue);
                    Toast.makeText(getContext(),
                            updated ? "Password updated." : "Current password is incorrect.",
                            Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void pickProfilePicture() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, PICK_PROFILE_IMAGE_REQUEST);
    }

    private String getProfilePictureKey() {
        return "profile_picture_" + prefs.getInt("userId", -1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PROFILE_IMAGE_REQUEST && resultCode == android.app.Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                requireContext().getContentResolver().takePersistableUriPermission(
                        imageUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                );
                prefs.edit().putString(getProfilePictureKey(), imageUri.toString()).apply();
                imgProfilePicture.setPadding(0, 0, 0, 0);
                imgProfilePicture.setImageURI(imageUri);
            }
        }
    }

    private void showClearDataDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Clear Trip Data");
        builder.setMessage("Delete all your trip history and carbon savings? Your account will remain.");
        builder.setPositiveButton("Clear", (dialog, which) -> {
            prefs.edit()
                    .putInt("total_trips", 0)
                    .putFloat("total_carbon", 0)
                    .putFloat("total_distance", 0)
                    .putInt("walks_count", 0)
                    .putInt("cycles_count", 0)
                    .putInt("public_count", 0)
                    .putFloat("walking_distance", 0)
                    .putFloat("cycling_distance", 0)
                    .putFloat("public_distance", 0)
                    .putFloat("walking_carbon", 0)
                    .putFloat("cycling_carbon", 0)
                    .putFloat("public_carbon", 0)
                    .putString("recent_trips", "")
                    .apply();

            tvTotalTrips.setText("0");
            tvTotalCarbon.setText("0.0 kg");
            Toast.makeText(getContext(), "Trip data cleared.", Toast.LENGTH_LONG).show();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Logout", (dialog, which) -> {
            prefs.edit()
                    .putBoolean("isLoggedIn", false)
                    .apply();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
    }
}
