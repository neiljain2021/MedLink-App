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
import com.medlink.app.adapters.AdminRequestAdapter;
import com.medlink.app.repository.AdminRepository;
import java.util.List;

/**
 * AdminRequestsFragment — manage all medicine requests.
 * Filter by status: ALL / PENDING / PROCESSING / READY / REJECTED
 * Actions: Approve (→PROCESSING), Ready (→READY), Reject (→REJECTED), Soft-Delete.
 */
public class AdminRequestsFragment extends Fragment {

    private AdminRepository repo;
    private AdminRequestAdapter adapter;
    private ProgressBar progress;
    private TextView tvEmpty, tvCount;

    private String currentFilter = "ALL";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_admin_requests, container, false);

        repo = new AdminRepository();

        RecyclerView rv = v.findViewById(R.id.rv_admin_requests);
        progress = v.findViewById(R.id.progress_requests);
        tvEmpty  = v.findViewById(R.id.tv_requests_empty);
        tvCount  = v.findViewById(R.id.tv_requests_count);

        adapter = new AdminRequestAdapter(new AdminRequestAdapter.RequestActionListener() {
            @Override public void onApprove(DocumentSnapshot doc) { updateStatus(doc, "PROCESSING"); }
            @Override public void onReady(DocumentSnapshot doc)   { updateStatus(doc, "READY"); }
            @Override public void onReject(DocumentSnapshot doc)  { confirmReject(doc); }
            @Override public void onDelete(DocumentSnapshot doc)  { confirmDelete(doc); }
        });

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        // Chip click listeners
        setupChip(v, R.id.chip_all,        "ALL");
        setupChip(v, R.id.chip_pending,    "PENDING");
        setupChip(v, R.id.chip_processing, "PROCESSING");
        setupChip(v, R.id.chip_ready,      "READY");
        setupChip(v, R.id.chip_rejected,   "REJECTED");

        loadRequests();
        return v;
    }

    private void setupChip(View parent, int chipId, String filter) {
        TextView chip = parent.findViewById(chipId);
        if (chip == null) return;
        chip.setOnClickListener(v -> {
            currentFilter = filter;
            loadRequests();
        });
    }

    private void loadRequests() {
        progress.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        repo.getAllRequests(currentFilter, new AdminRepository.Callback<List<DocumentSnapshot>>() {
            @Override public void onSuccess(List<DocumentSnapshot> docs) {
                if (!isAdded()) return;
                progress.setVisibility(View.GONE);
                adapter.setData(docs);
                tvCount.setText(docs.size() + " requests (" + currentFilter + ")");
                tvEmpty.setVisibility(docs.isEmpty() ? View.VISIBLE : View.GONE);
            }
            @Override public void onFailure(Exception e) {
                if (!isAdded()) return;
                progress.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Failed to load requests", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStatus(DocumentSnapshot doc, String newStatus) {
        String medicine = doc.getString("medicineName");
        new AlertDialog.Builder(getContext())
                .setTitle("Update Status")
                .setMessage("Set \"" + medicine + "\" to " + newStatus + "?")
                .setPositiveButton("Confirm", (d, w) -> {
                    repo.updateRequestStatus(doc.getId(), newStatus, new AdminRepository.Callback<Void>() {
                        @Override public void onSuccess(Void r) {
                            if (!isAdded()) return;
                            Toast.makeText(getContext(), "Status → " + newStatus, Toast.LENGTH_SHORT).show();
                            loadRequests();
                        }
                        @Override public void onFailure(Exception e) {
                            if (!isAdded()) return;
                            Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmReject(DocumentSnapshot doc) {
        updateStatus(doc, "REJECTED");
    }

    private void confirmDelete(DocumentSnapshot doc) {
        String medicine = doc.getString("medicineName");
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Request")
                .setMessage("Soft-delete request for \"" + medicine + "\"?")
                .setPositiveButton("Delete", (d, w) -> {
                    repo.softDeleteRequest(doc.getId(), new AdminRepository.Callback<Void>() {
                        @Override public void onSuccess(Void r) {
                            if (!isAdded()) return;
                            Toast.makeText(getContext(), "Request deleted", Toast.LENGTH_SHORT).show();
                            loadRequests();
                        }
                        @Override public void onFailure(Exception e) {
                            if (!isAdded()) return;
                            Toast.makeText(getContext(), "Delete failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
