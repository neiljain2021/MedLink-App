package com.medlink.app.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.medlink.app.R;
import com.medlink.app.adapters.AdminDonationAdapter;
import com.medlink.app.repository.AdminRepository;
import java.util.List;

public class AdminDonationsFragment extends Fragment {

    private AdminRepository repo;
    private AdminDonationAdapter adapter;
    private ProgressBar progress;
    private TextView tvEmpty, tvCount;
    private String currentFilter = "ALL";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_admin_donations, container, false);
        repo = new AdminRepository();
        RecyclerView rv = v.findViewById(R.id.rv_admin_donations);
        progress = v.findViewById(R.id.progress_donations);
        tvEmpty  = v.findViewById(R.id.tv_donations_empty);
        tvCount  = v.findViewById(R.id.tv_donations_count);

        adapter = new AdminDonationAdapter(new AdminDonationAdapter.DonationActionListener() {
            @Override public void onVerify(DocumentSnapshot doc) { updateStatus(doc, "VERIFIED"); }
            @Override public void onReject(DocumentSnapshot doc) { updateStatus(doc, "REJECTED"); }
            @Override public void onDelete(DocumentSnapshot doc) { confirmDelete(doc); }
        });
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        setupChip(v, R.id.chip_don_all,       "ALL");
        setupChip(v, R.id.chip_don_verifying,  "VERIFYING");
        setupChip(v, R.id.chip_don_verified,   "VERIFIED");
        setupChip(v, R.id.chip_don_rejected,   "REJECTED");

        loadDonations();
        return v;
    }

    private void setupChip(View parent, int id, String filter) {
        TextView chip = parent.findViewById(id);
        if (chip != null) chip.setOnClickListener(v -> { currentFilter = filter; loadDonations(); });
    }

    private void loadDonations() {
        progress.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        repo.getAllDonations(currentFilter, new AdminRepository.Callback<List<DocumentSnapshot>>() {
            @Override public void onSuccess(List<DocumentSnapshot> docs) {
                if (!isAdded()) return;
                progress.setVisibility(View.GONE);
                adapter.setData(docs);
                tvCount.setText(docs.size() + " donations (" + currentFilter + ")");
                tvEmpty.setVisibility(docs.isEmpty() ? View.VISIBLE : View.GONE);
            }
            @Override public void onFailure(Exception e) {
                if (!isAdded()) return;
                progress.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Failed to load", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStatus(DocumentSnapshot doc, String newStatus) {
        String medicine = doc.getString("medicineName");
        new AlertDialog.Builder(getContext())
            .setTitle("Update Donation")
            .setMessage("Set \"" + medicine + "\" to " + newStatus + "?")
            .setPositiveButton("Confirm", (d, w) ->
                repo.updateDonationStatus(doc.getId(), newStatus, new AdminRepository.Callback<Void>() {
                    @Override public void onSuccess(Void r) {
                        if (isAdded()) { Toast.makeText(getContext(), "Status → " + newStatus, Toast.LENGTH_SHORT).show(); loadDonations(); }
                    }
                    @Override public void onFailure(Exception e) {
                        if (isAdded()) Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
                    }
                }))
            .setNegativeButton("Cancel", null).show();
    }

    private void confirmDelete(DocumentSnapshot doc) {
        String medicine = doc.getString("medicineName");
        new AlertDialog.Builder(getContext())
            .setTitle("Delete Donation")
            .setMessage("Soft-delete \"" + medicine + "\"?")
            .setPositiveButton("Delete", (d, w) ->
                repo.softDeleteDonation(doc.getId(), new AdminRepository.Callback<Void>() {
                    @Override public void onSuccess(Void r) {
                        if (isAdded()) { Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show(); loadDonations(); }
                    }
                    @Override public void onFailure(Exception e) {
                        if (isAdded()) Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
                    }
                }))
            .setNegativeButton("Cancel", null).show();
    }
}
