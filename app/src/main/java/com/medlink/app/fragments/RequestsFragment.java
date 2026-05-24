package com.medlink.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.medlink.app.activities.MainActivity;
import com.medlink.app.models.MedicineRequest;
import com.medlink.app.R;
import com.medlink.app.adapters.RequestsAdapter;
import com.medlink.app.repository.AuthRepository;

public class RequestsFragment extends Fragment {

    public RequestsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_requests, container, false);

        // Setup "New Request" button
        TextView btnNewRequest = view.findViewById(R.id.btn_new_request);
        if (btnNewRequest != null) {
            btnNewRequest.setOnClickListener(v -> {
                // NEW: Check verification first
                new com.medlink.app.repository.AuthRepository().isUserVerified(isVerified -> {
                    if (!isAdded()) return;
                    
                    if (!isVerified) {
                        Toast.makeText(getContext(), "Profile Verification Required to request medicine.", Toast.LENGTH_LONG).show();
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).setSelectedTab(R.id.nav_profile);
                        }
                    } else {
                        // Navigate to the RequestFormFragment
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).loadFragment(new RequestFormFragment(), true);
                        }
                    }
                });
            });
        }

        // Setup RecyclerView
        androidx.recyclerview.widget.RecyclerView rvRequests = view.findViewById(R.id.rv_requests);
        TextView tvEmptyState = view.findViewById(R.id.tv_empty_state);
        TextView tvPendingCount = view.findViewById(R.id.tv_pending_count);
        TextView tvAcceptedCount = view.findViewById(R.id.tv_accepted_count);
        TextView tvCompletedCount = view.findViewById(R.id.tv_completed_count);

        rvRequests.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        RequestsAdapter adapter = new RequestsAdapter();
        rvRequests.setAdapter(adapter);

        // Fetch Data
        AuthRepository authRepo = new AuthRepository();
        com.google.firebase.auth.FirebaseUser currentUser = authRepo.getCurrentUser();
        
        if (currentUser != null) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("Requests")
                .whereEqualTo("requesterId", currentUser.getUid())
                .addSnapshotListener((value, error) -> {
                    if (error != null || !isAdded()) {
                        if (error != null && isAdded()) Toast.makeText(getContext(), "Failed to load requests.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (value != null && !value.isEmpty()) {
                        java.util.List<MedicineRequest> list = new java.util.ArrayList<>();
                        int pending = 0, accepted = 0, completed = 0;
                        
                        for (com.google.firebase.firestore.DocumentSnapshot doc : value.getDocuments()) {
                            MedicineRequest req = doc.toObject(MedicineRequest.class);
                            if (req != null) {
                                list.add(req);
                                String status = req.getStatus() != null ? req.getStatus().toUpperCase() : "PENDING";
                                if (status.equals("PENDING")) pending++;
                                else if (status.equals("ACCEPTED") || status.equals("PROCESSING")) accepted++;
                                else if (status.equals("COMPLETED")) completed++;
                            }
                        }
                        
                        adapter.setRequests(list);
                        tvPendingCount.setText(String.valueOf(pending));
                        tvAcceptedCount.setText(String.valueOf(accepted));
                        tvCompletedCount.setText(String.valueOf(completed));
                        
                        tvEmptyState.setVisibility(View.GONE);
                        rvRequests.setVisibility(View.VISIBLE);
                    } else {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        rvRequests.setVisibility(View.GONE);
                        tvPendingCount.setText("0");
                        tvAcceptedCount.setText("0");
                        tvCompletedCount.setText("0");
                    }
                });
        }

        return view;
    }
}
