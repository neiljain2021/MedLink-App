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

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.medlink.app.R;
import com.medlink.app.models.Ngo;
import com.medlink.app.repository.AdminRepository;

import java.util.ArrayList;
import java.util.List;

public class AdminVerificationFragment extends Fragment {

    private AdminRepository repo;
    private VerificationAdapter adapter;
    private TextView tvCount, tvEmpty;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_admin_verification, container, false);

        repo = new AdminRepository();
        db = FirebaseFirestore.getInstance();

        tvCount = v.findViewById(R.id.tv_pending_count);
        tvEmpty = v.findViewById(R.id.tv_verification_empty);
        RecyclerView rv = v.findViewById(R.id.rv_verification_list);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new VerificationAdapter();
        rv.setAdapter(adapter);

        loadPendingNgos();

        return v;
    }

    private void loadPendingNgos() {
        db.collection("NGOs")
                .whereEqualTo("verified", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) return;
                    List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                    adapter.setDocs(docs);
                    tvCount.setText(docs.size() + " NGOs awaiting approval");
                    tvEmpty.setVisibility(docs.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) Toast.makeText(getContext(), "Failed to load pending NGOs", Toast.LENGTH_SHORT).show();
                });
    }

    private class VerificationAdapter extends RecyclerView.Adapter<VerificationAdapter.VH> {
        private List<DocumentSnapshot> docs = new ArrayList<>();

        public void setDocs(List<DocumentSnapshot> docs) {
            this.docs = docs;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_verification, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            DocumentSnapshot doc = docs.get(position);
            holder.tvName.setText(doc.getString("name"));
            holder.tvLocation.setText(doc.getString("location"));

            holder.btnApprove.setOnClickListener(v -> {
                repo.toggleNgoVerified(doc.getId(), true, new AdminRepository.Callback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Toast.makeText(getContext(), "NGO Approved!", Toast.LENGTH_SHORT).show();
                        loadPendingNgos();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Action failed", Toast.LENGTH_SHORT).show();
                    }
                });
            });

            holder.btnReject.setOnClickListener(v -> {
                repo.softDeleteNgo(doc.getId(), new AdminRepository.Callback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Toast.makeText(getContext(), "NGO Registration Rejected", Toast.LENGTH_SHORT).show();
                        loadPendingNgos();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Action failed", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        @Override
        public int getItemCount() {
            return docs.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvLocation;
            View btnApprove, btnReject;

            public VH(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_ngo_name);
                tvLocation = itemView.findViewById(R.id.tv_ngo_location);
                btnApprove = itemView.findViewById(R.id.btn_approve);
                btnReject = itemView.findViewById(R.id.btn_reject);
            }
        }
    }
}
