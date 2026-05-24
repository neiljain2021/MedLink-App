package com.medlink.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.medlink.app.activities.AdminActivity;
import com.medlink.app.activities.LoginActivity;
import com.medlink.app.activities.MainActivity;
import com.medlink.app.R;
import com.medlink.app.models.User;
import com.medlink.app.repository.AuthRepository;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        TextView tvGreeting = view.findViewById(R.id.tv_greeting);
        TextView tvMedsCount = view.findViewById(R.id.tv_meds_count);
        TextView btnLogout = view.findViewById(R.id.btn_logout);
        View layoutAdminEntry = view.findViewById(R.id.layout_admin_entry);
        TextView btnAdminPanel  = view.findViewById(R.id.btn_admin_panel);

        // Wire Admin Panel button click — AdminActivity will re-verify role on its own
        btnAdminPanel.setOnClickListener(v -> startActivity(
                new Intent(getActivity(), AdminActivity.class)));

        // Fetch live user data from Firestore
        AuthRepository authRepo = new AuthRepository();
        com.google.firebase.auth.FirebaseUser currentUser = authRepo.getCurrentUser();
        
        if (currentUser != null) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("Users")
                .document(currentUser.getUid())
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (!isAdded() || getContext() == null) return;
                    if (error != null) return;
                    
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            if (tvGreeting != null) tvGreeting.setText("Hello, " + user.getName() + "!");
                            if (tvMedsCount != null) tvMedsCount.setText(String.valueOf(user.getMedicinesDonated()));
                            
                            ImageView ivProfilePic = view.findViewById(R.id.iv_profile_pic);
                            if (ivProfilePic != null && user.getProfileImageUrl() != null) {
                                try {
                                    android.net.Uri imageUri = android.net.Uri.parse(user.getProfileImageUrl());
                                    ivProfilePic.setImageURI(imageUri);
                                    ivProfilePic.setPadding(0,0,0,0);
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                        ivProfilePic.setImageTintList(null);
                                    }
                                } catch (Exception e) {
                                    // Fallback to default if URI is invalid
                                }
                            }

                            // Update verification UI with null checks
                            View btnVerify = view.findViewById(R.id.btn_verify_profile);
                            TextView tvVerifyTitle = view.findViewById(R.id.tv_verify_title);
                            TextView tvVerifySubtitle = view.findViewById(R.id.tv_verify_subtitle);
                            ImageView ivVerifyIcon = view.findViewById(R.id.iv_verify_icon);
                            View btnStart = view.findViewById(R.id.btn_start_verify);

                            if (user.isVerified() && btnVerify != null) {
                                btnVerify.setBackgroundResource(R.drawable.bg_status_ready);
                                if (tvVerifyTitle != null) tvVerifyTitle.setText("Verified Profile");
                                if (tvVerifySubtitle != null) {
                                    tvVerifySubtitle.setText("Your account is now fully verified");
                                    tvVerifySubtitle.setTextColor(androidx.core.content.ContextCompat.getColor(getContext(), R.color.accent_green));
                                }
                                if (ivVerifyIcon != null) ivVerifyIcon.setAlpha(1.0f);
                                if (btnStart != null) btnStart.setVisibility(View.GONE);
                                btnVerify.setOnClickListener(null);
                                btnVerify.setClickable(false);
                            } else if (btnVerify != null) {
                                btnVerify.setVisibility(View.VISIBLE);
                                if (btnStart != null) btnStart.setVisibility(View.VISIBLE);
                            }

                            // ── Role checks: show management buttons only for authorized roles ──
                            authRepo.fetchUserRole(role -> {
                                if (!isAdded() || getContext() == null) return;
                                if (role == null) return;
                                String trimmedRole = role.trim();

                                if ("admin".equals(trimmedRole) && layoutAdminEntry != null) {
                                    layoutAdminEntry.setVisibility(View.VISIBLE);
                                }
                                
                                View layoutNgoEntry = view.findViewById(R.id.layout_ngo_entry);
                                View btnNgoDashboard = view.findViewById(R.id.btn_ngo_dashboard);
                                if ("ngo".equals(trimmedRole) && layoutNgoEntry != null) {
                                    layoutNgoEntry.setVisibility(View.VISIBLE);
                                    if (btnNgoDashboard != null) {
                                        btnNgoDashboard.setOnClickListener(v -> startActivity(
                                                new Intent(getActivity(), com.medlink.app.activities.NgoActivity.class)));
                                    }
                                }
                            });
                        }
                    } else if (tvGreeting != null) {
                        tvGreeting.setText("Hello, Hero!");
                    }
                });
        } else {
            tvGreeting.setText("Hello, Guest!");
        }

        // Verification Button Logic
        view.findViewById(R.id.btn_verify_profile).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new VerifyProfileFragment(), true);
            }
        });

        // Logout Logic
        btnLogout.setOnClickListener(v -> {
            authRepo.logout();
            Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
            
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }
}
