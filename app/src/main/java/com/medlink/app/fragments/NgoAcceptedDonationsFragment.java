package com.medlink.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

public class NgoAcceptedDonationsFragment extends Fragment implements NgoDonationAdapter.OnDonationActionListener {

    private RecyclerView recyclerView;
    private NgoDonationAdapter adapter;
    private List<Donation> donationList = new ArrayList<>();
    private TextView tvNoDonations;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ngo_requests, container, false);

        recyclerView = view.findViewById(R.id.rv_ngo_requests);
        tvNoDonations = view.findViewById(R.id.tv_no_requests);
        if (tvNoDonations != null) tvNoDonations.setText("You haven't accepted any donations yet.");

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NgoDonationAdapter(donationList, this);
        recyclerView.setAdapter(adapter);

        fetchAcceptedDonations();

        return view;
    }

    private void fetchAcceptedDonations() {
        if (!(getActivity() instanceof NgoActivity)) return;
        String ngoName = ((NgoActivity) getActivity()).getCurrentNgoName();

        if (ngoName == null || ngoName.isEmpty()) return;

        FirebaseFirestore.getInstance().collection("Donations")
                .whereEqualTo("targetNgo", ngoName)
                .whereEqualTo("status", "ACCEPTED")
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
        // Already accepted, no action needed or maybe 'Mark as Received' in future
    }
}
