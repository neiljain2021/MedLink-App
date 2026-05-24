package com.medlink.app.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.medlink.app.activities.MainActivity;
import com.medlink.app.R;
import com.medlink.app.repository.AuthRepository;

public class VerifyProfileFragment extends Fragment {

    private ImageView ivProfilePreview;
    private View layoutUploadPrompt;
    private EditText etMobile;
    private Spinner spinnerCountry, spinnerState;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    ivProfilePreview.setImageURI(selectedImageUri);
                    ivProfilePreview.setVisibility(View.VISIBLE);
                    layoutUploadPrompt.setVisibility(View.GONE);
                }
            }
    );

    public VerifyProfileFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_verify_profile, container, false);

        ivProfilePreview = view.findViewById(R.id.iv_profile_preview);
        layoutUploadPrompt = view.findViewById(R.id.layout_upload_prompt);
        etMobile = view.findViewById(R.id.et_mobile);
        spinnerCountry = view.findViewById(R.id.spinner_country);
        spinnerState = view.findViewById(R.id.spinner_state);

        setupSpinners();

        view.findViewById(R.id.btn_upload_image).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        view.findViewById(R.id.btn_submit_verification).setOnClickListener(v -> {
            String mobile = etMobile.getText().toString().trim();
            String country = spinnerCountry.getSelectedItem().toString();
            String state = spinnerState.getSelectedItem().toString();

            if (mobile.isEmpty()) {
                Toast.makeText(getContext(), "Please provide your mobile number", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save to Firestore
            AuthRepository authRepo = new AuthRepository();
            com.google.firebase.auth.FirebaseUser currentUser = authRepo.getCurrentUser();

            if (currentUser != null) {
                java.util.Map<String, Object> updates = new java.util.HashMap<>();
                updates.put("phone", mobile);
                updates.put("country", country);
                updates.put("state", state);
                updates.put("verified", true); // Updated field name
                if (selectedImageUri != null) {
                    updates.put("profileImageUrl", selectedImageUri.toString());
                }

                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(currentUser.getUid())
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Profile Updated & Verified!", Toast.LENGTH_LONG).show();
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).loadFragment(new ProfileFragment(), false);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error saving data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            }
        });

        return view;
    }

    private void setupSpinners() {
        String[] countries = {"India"};
        String[] states = {
            "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh", 
            "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand", 
            "Karnataka", "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur", 
            "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Punjab", 
            "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana", "Tripura", 
            "Uttar Pradesh", "Uttarakhand", "West Bengal", "Delhi"
        };

        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, countries);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountry.setAdapter(countryAdapter);

        ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, states);
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerState.setAdapter(stateAdapter);
    }
}
