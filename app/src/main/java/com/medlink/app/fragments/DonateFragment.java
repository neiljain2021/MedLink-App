package com.medlink.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.medlink.app.models.Donation;
import com.medlink.app.R;
import com.medlink.app.repository.DataRepository;

public class DonateFragment extends Fragment {

    public DonateFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_donate, container, false);

        EditText etDonorName = view.findViewById(R.id.et_donor_name);
        EditText etDonateMedName = view.findViewById(R.id.et_donate_med_name);
        EditText etDonateQuantity = view.findViewById(R.id.et_donate_quantity);
        LinearLayout btnScanExpiry = view.findViewById(R.id.btn_scan_expiry);
        TextView tvCameraLabel = view.findViewById(R.id.tv_camera_label);
        TextView btnSubmitDonation = view.findViewById(R.id.btn_submit_donation);

        // Camera Functionality
        androidx.activity.result.ActivityResultLauncher<Void> takePictureLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview(),
                bitmap -> {
                    if (bitmap != null) {
                        tvCameraLabel.setText("Photo Captured ✓");
                        tvCameraLabel.setTextColor(getResources().getColor(R.color.accent_green, null));
                        Toast.makeText(getContext(), "Scanning for expiry dates...", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        btnScanExpiry.setOnClickListener(v -> {
            try {
                takePictureLauncher.launch(null);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error opening camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Submit Logic
        btnSubmitDonation.setOnClickListener(v -> {
            new com.medlink.app.repository.AuthRepository().isUserVerified(isVerified -> {
                if (!isAdded()) return;
                
                if (!isVerified) {
                    Toast.makeText(getContext(), "Profile Verification Required to donate medicine.", Toast.LENGTH_LONG).show();
                    if (getActivity() instanceof com.medlink.app.activities.MainActivity) {
                        ((com.medlink.app.activities.MainActivity) getActivity()).setSelectedTab(R.id.nav_profile);
                    }
                    return;
                }

                String name = etDonorName.getText().toString().trim();
                String medName = etDonateMedName.getText().toString().trim();
                String quantity = etDonateQuantity.getText().toString().trim();
                
                if (name.isEmpty() || medName.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill all required details", Toast.LENGTH_SHORT).show();
                    return;
                }

                btnSubmitDonation.setEnabled(false);
                btnSubmitDonation.setText("Submitting...");

                Donation donation = new Donation(null, name, medName, quantity);
                new DataRepository().submitDonation(donation, new DataRepository.ResultCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Donation submitted! Thank you, " + name, Toast.LENGTH_LONG).show();
                        }
                        // Clear fields
                        etDonorName.setText("");
                        etDonateMedName.setText("");
                        etDonateQuantity.setText("");
                        tvCameraLabel.setText("Tap to open camera");
                        tvCameraLabel.setTextColor(getResources().getColor(R.color.text_primary, null));
                        
                        btnSubmitDonation.setEnabled(true);
                        btnSubmitDonation.setText("Submit Donation");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Failed to submit: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        btnSubmitDonation.setEnabled(true);
                        btnSubmitDonation.setText("Submit Donation");
                    }
                });
            });
        });

        return view;
    }
}
