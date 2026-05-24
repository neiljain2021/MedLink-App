package com.medlink.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.medlink.app.activities.MainActivity;
import com.medlink.app.models.MedicineRequest;
import com.medlink.app.R;
import com.medlink.app.repository.DataRepository;

public class RequestFormFragment extends Fragment {

    private int currentStep = 1;
    private int selectedNgo = 1;

    public RequestFormFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request_form, container, false);

        // UI Elements Step 1
        LinearLayout step1Container = view.findViewById(R.id.step1_container);
        EditText etMedicineName = view.findViewById(R.id.et_medicine_name);
        EditText etDosage = view.findViewById(R.id.et_dosage);
        

        // Upload Button
        LinearLayout btnUpload = view.findViewById(R.id.btn_upload_prescription);
        TextView tvUploadLabel = view.findViewById(R.id.tv_upload_label);

        // UI Elements Step 2
        LinearLayout step2Container = view.findViewById(R.id.step2_container);
        TextView tvStepLabel = view.findViewById(R.id.tv_step_label);
        TextView tvStepDesc = view.findViewById(R.id.tv_step_desc);
        View stepProgressFill = view.findViewById(R.id.step_progress_fill);
        
        // Review Fields
        TextView reviewMedicine = view.findViewById(R.id.review_medicine);
        TextView reviewDosage = view.findViewById(R.id.review_dosage);
        TextView reviewNgo = view.findViewById(R.id.review_ngo);

        // Global buttons
        ImageView btnBack = view.findViewById(R.id.btn_back);
        TextView btnContinue = view.findViewById(R.id.btn_continue);

        // Back button logic
        btnBack.setOnClickListener(v -> {
            if (currentStep == 2) {
                // Go back to step 1
                currentStep = 1;
                step1Container.setVisibility(View.VISIBLE);
                step2Container.setVisibility(View.GONE);
                tvStepLabel.setText("Step 1 of 2");
                tvStepDesc.setText("Medicine & Verification");
                btnContinue.setText("Continue →");
                
                // Adjust progress bar
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) stepProgressFill.getLayoutParams();
                params.weight = 0.5f;
                stepProgressFill.setLayoutParams(params);
            } else {
                // Return to requests list
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).loadFragment(new RequestsFragment(), false);
                }
            }
        });

        // Camera Functionality
        androidx.activity.result.ActivityResultLauncher<Void> takePictureLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview(),
                bitmap -> {
                    if (bitmap != null) {
                        tvUploadLabel.setText("Prescription Scanned ✓");
                        tvUploadLabel.setTextColor(getResources().getColor(R.color.accent_green, null));
                        Toast.makeText(getContext(), "Prescription verified successfully", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Upload button logic (Now Camera)
        btnUpload.setOnClickListener(v -> {
            try {
                takePictureLauncher.launch(null);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error opening camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        // Continue / Submit logic
        btnContinue.setOnClickListener(v -> {
            if (currentStep == 1) {
                // Validate
                String medName = etMedicineName.getText().toString().trim();
                String dosage = etDosage.getText().toString().trim();

                if (medName.isEmpty() || dosage.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill in medicine details", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Proceed to step 2
                currentStep = 2;
                step1Container.setVisibility(View.GONE);
                step2Container.setVisibility(View.VISIBLE);
                
                // Update header
                tvStepLabel.setText("Step 2 of 2");
                tvStepDesc.setText("Review & Confirm");
                btnContinue.setText("Submit Request ✓");
                
                // Update progress bar
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
                stepProgressFill.setLayoutParams(params);

                // Populate Review data
                reviewMedicine.setText(medName);
                reviewDosage.setText(dosage);
                
                reviewNgo.setText("Any Verified NGO");

            } else {
                // Submit Form
                btnContinue.setEnabled(false);
                btnContinue.setText("Submitting...");

                String ngoStr = "Any Verified NGO";

                MedicineRequest request = new MedicineRequest(null, reviewMedicine.getText().toString(), ngoStr, reviewDosage.getText().toString());
                
                new DataRepository().submitRequest(request, new DataRepository.ResultCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Request Submitted Successfully!", Toast.LENGTH_LONG).show();
                        }
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).loadFragment(new RequestsFragment(), false);
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        btnContinue.setEnabled(true);
                        btnContinue.setText("Submit Request ✓");
                    }
                });
            }
        });

        // Initialize progress bar weight
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0.5f);
        stepProgressFill.setLayoutParams(params);

        return view;
    }
}
