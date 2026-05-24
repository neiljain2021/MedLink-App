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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.medlink.app.R;
import com.medlink.app.activities.NgoActivity;
import com.medlink.app.adapters.NgoRequestAdapter;
import com.medlink.app.models.MedicineRequest;

import java.util.ArrayList;
import java.util.List;

public class NgoRequestsFragment extends Fragment implements NgoRequestAdapter.OnRequestActionListener {

    private String targetStatus;
    private RecyclerView recyclerView;
    private NgoRequestAdapter adapter;
    private List<MedicineRequest> requestList = new ArrayList<>();
    private TextView tvNoRequests;

    public NgoRequestsFragment() {
        this.targetStatus = "PENDING"; // Default
    }

    public NgoRequestsFragment(String targetStatus) {
        this.targetStatus = targetStatus;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ngo_requests, container, false);

        recyclerView = view.findViewById(R.id.rv_ngo_requests);
        tvNoRequests = view.findViewById(R.id.tv_no_requests);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NgoRequestAdapter(requestList, this);
        recyclerView.setAdapter(adapter);

        fetchRequests();

        return view;
    }

    private void fetchRequests() {
        if (!(getActivity() instanceof NgoActivity)) return;
        String currentNgoName = ((NgoActivity) getActivity()).getCurrentNgoName();
        if (currentNgoName == null) return;

        Query query = FirebaseFirestore.getInstance().collection("Requests");
        if (targetStatus.equals("PENDING")) {
            query = query.whereEqualTo("targetNgo", "Any Verified NGO");
        } else {
            query = query.whereEqualTo("targetNgo", currentNgoName);
        }
        
        query.whereEqualTo("status", targetStatus)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || !isAdded()) return;
                    if (value != null) {
                        requestList.clear();
                        List<MedicineRequest> all = value.toObjects(MedicineRequest.class);
                        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
                        
                        for (int i = 0; i < all.size(); i++) {
                            MedicineRequest req = all.get(i);
                            req.setRequestId(value.getDocuments().get(i).getId());
                            
                            // Only add if this NGO hasn't rejected it
                            if (req.getRejectedBy() == null || !req.getRejectedBy().contains(uid)) {
                                requestList.add(req);
                            }
                        }
                        
                        adapter.notifyDataSetChanged();
                        tvNoRequests.setVisibility(requestList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
    }

    @Override
    public void onAccept(MedicineRequest request) {
        updateRequestStatus(request, "ACCEPTED");
    }

    @Override
    public void onReject(MedicineRequest request) {
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        com.google.firebase.firestore.DocumentReference docRef = db.collection("Requests")
                .document(request.getRequestId());
        
        // 1. Add current NGO to rejectedBy list
        docRef.update("rejectedBy", com.google.firebase.firestore.FieldValue.arrayUnion(uid))
            .addOnSuccessListener(aVoid -> {
                if (isAdded()) Toast.makeText(getContext(), "Request Hidden", Toast.LENGTH_SHORT).show();
                
                // 2. Check if ALL verified NGOs have now rejected it
                db.collection("NGOs").whereEqualTo("verified", true).get()
                    .addOnSuccessListener(querySnapshot -> {
                        int totalVerifiedNgos = querySnapshot.size();
                        
                        // Refetch the request to get updated rejectedBy list
                        docRef.get().addOnSuccessListener(requestDoc -> {
                            java.util.List<String> rejectedBy = (java.util.List<String>) requestDoc.get("rejectedBy");
                            int rejectionCount = (rejectedBy != null) ? rejectedBy.size() : 0;
                            
                            if (rejectionCount >= totalVerifiedNgos && totalVerifiedNgos > 0) {
                                // Everyone rejected it!
                                docRef.update("status", "REJECTED");
                            }
                        });
                    });
            })
            .addOnFailureListener(e -> {
                if (isAdded()) Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    public void onComplete(MedicineRequest request) {
        updateRequestStatus(request, "COMPLETED");
    }

    private void updateRequestStatus(MedicineRequest request, String newStatus) {
        com.google.firebase.firestore.DocumentReference docRef = FirebaseFirestore.getInstance().collection("Requests")
                .document(request.getRequestId());
        
        if (newStatus.equals("ACCEPTED")) {
            if (getActivity() instanceof NgoActivity) {
                String ngoName = ((NgoActivity) getActivity()).getCurrentNgoName();
                docRef.update("status", newStatus, "targetNgo", ngoName)
                    .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Request Accepted", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        } else {
            docRef.update("status", newStatus)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Request " + newStatus, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}
