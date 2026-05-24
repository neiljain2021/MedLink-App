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
import com.medlink.app.adapters.NgoDonationAdapter;
import com.medlink.app.models.Donation;

import java.util.ArrayList;
import java.util.List;

public class NgoDonationsFragment extends Fragment implements NgoDonationAdapter.OnDonationActionListener {

    private RecyclerView recyclerView;
    private NgoDonationAdapter adapter;
    private List<Donation> donationList = new ArrayList<>();
    private TextView tvNoDonations;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ngo_requests, container, false); // Reusing requests layout as it's just a list

        recyclerView = view.findViewById(R.id.rv_ngo_requests);
        tvNoDonations = view.findViewById(R.id.tv_no_requests);
        if (tvNoDonations != null) tvNoDonations.setText("No donations available to claim yet.");

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NgoDonationAdapter(donationList, this);
        recyclerView.setAdapter(adapter);

        fetchDonations();

        return view;
    }

    private void fetchDonations() {
        FirebaseFirestore.getInstance().collection("Donations")
                .whereEqualTo("targetNgo", "Any Verified NGO")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || !isAdded()) return;
                    if (value != null) {
                        donationList.clear();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : value.getDocuments()) {
                            Donation don = doc.toObject(Donation.class);
                            if (don != null) {
                                don.setDonationId(doc.getId());
                                donationList.add(don);
                            }
                        }
                        adapter.updateData(donationList);
                        tvNoDonations.setVisibility(donationList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
    }

    @Override
    public void onClaim(Donation donation) {
        if (!(getActivity() instanceof NgoActivity)) return;
        String ngoName = ((NgoActivity) getActivity()).getCurrentNgoName();
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        db.runTransaction(transaction -> {
            // 1. Get NGO Document to access existing inventory
            com.google.firebase.firestore.DocumentReference ngoRef = db.collection("NGOs").document(uid);
            com.google.firebase.firestore.DocumentSnapshot ngoDoc = transaction.get(ngoRef);
            
            java.util.Map<String, Object> inventory = (java.util.Map<String, Object>) ngoDoc.get("inventory");
            if (inventory == null) inventory = new java.util.HashMap<>();
            
            // 2. Parse quantity (clean string like "10 Units" -> 10)
            int qtyToAdd = 0;
            try {
                String qtyStr = donation.getQuantity() != null ? donation.getQuantity() : "0";
                String cleanQty = qtyStr.replaceAll("[^0-9]", "");
                qtyToAdd = cleanQty.isEmpty() ? 0 : Integer.parseInt(cleanQty);
            } catch (Exception e) {
                qtyToAdd = 1;
            }
            
            // 3. Update inventory map
            String medName = donation.getMedicineName();
            long currentQty = 0;
            if (inventory.containsKey(medName)) {
                Object val = inventory.get(medName);
                if (val instanceof Long) currentQty = (Long) val;
                else if (val instanceof Integer) currentQty = (Integer) val;
            }
            inventory.put(medName, currentQty + qtyToAdd);
            
            // 4. Commit updates
            com.google.firebase.firestore.DocumentReference donRef = db.collection("Donations").document(donation.getDonationId());
            transaction.update(donRef, "status", "ACCEPTED", "targetNgo", ngoName);
            transaction.update(ngoRef, "inventory", inventory);
            
            return null;
        }).addOnSuccessListener(aVoid -> {
            if (isAdded()) {
                Toast.makeText(getContext(), "Donation claimed and added to inventory!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            if (isAdded()) {
                Toast.makeText(getContext(), "Failed to claim: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
