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
import com.google.firebase.firestore.QuerySnapshot;
import com.medlink.app.R;
import com.medlink.app.activities.NgoActivity;
import com.medlink.app.models.ActivityItem;
import com.medlink.app.models.MedicineRequest;
import com.medlink.app.models.Donation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NgoActivitiesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ActivityAdapter adapter;
    private TextView tvEmpty;
    private List<ActivityItem> activityList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ngo_requests, container, false); // Reusing list layout

        recyclerView = view.findViewById(R.id.rv_ngo_requests);
        tvEmpty = view.findViewById(R.id.tv_no_requests);
        if (tvEmpty != null) tvEmpty.setText("No activities recorded yet.");

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ActivityAdapter();
        recyclerView.setAdapter(adapter);

        fetchActivities();

        return view;
    }

    private void fetchActivities() {
        if (!(getActivity() instanceof NgoActivity)) return;
        String ngoName = ((NgoActivity) getActivity()).getCurrentNgoName();
        if (ngoName == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Fetch Requests
        db.collection("Requests")
                .whereEqualTo("targetNgo", ngoName)
                .addSnapshotListener((value, error) -> {
                    if (error != null || !isAdded()) return;
                    if (value != null) {
                        processRequests(value);
                    }
                });

        // Fetch Donations (Claims)
        db.collection("Donations")
                .whereEqualTo("targetNgo", ngoName)
                .addSnapshotListener((value, error) -> {
                    if (error != null || !isAdded()) return;
                    if (value != null) {
                        processDonations(value);
                    }
                });
    }

    private synchronized void processRequests(QuerySnapshot snapshot) {
        activityList.removeIf(item -> "REQUEST".equals(item.type));
        for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
            MedicineRequest req = doc.toObject(MedicineRequest.class);
            if (req != null) {
                activityList.add(new ActivityItem(
                        "Request: " + req.getMedicineName(),
                        "Status: " + req.getStatus(),
                        req.getTimestamp() != null ? req.getTimestamp().getTime() : 0,
                        "REQUEST"
                ));
            }
        }
        updateUI();
    }

    private synchronized void processDonations(QuerySnapshot snapshot) {
        activityList.removeIf(item -> "CLAIM".equals(item.type));
        for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
            Donation don = doc.toObject(Donation.class);
            if (don != null) {
                activityList.add(new ActivityItem(
                        "Claimed " + don.getMedicineName(),
                        "From: " + don.getDonorName(),
                        don.getTimestamp() != null ? don.getTimestamp().getTime() : 0,
                        "CLAIM"
                ));
            }
        }
        updateUI();
    }

    private void updateUI() {
        Collections.sort(activityList, (a, b) -> Long.compare(b.timestamp, a.timestamp));
        adapter.setItems(activityList);
        tvEmpty.setVisibility(activityList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.VH> {
        private List<ActivityItem> items = new ArrayList<>();
        private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault());

        public void setItems(List<ActivityItem> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ngo_activity, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            ActivityItem item = items.get(position);
            holder.tvTitle.setText(item.title);
            holder.tvDesc.setText(item.description);
            holder.tvTime.setText(item.timestamp > 0 ? sdf.format(new Date(item.timestamp)) : "");
            
            if ("CLAIM".equals(item.type)) {
                holder.ivIcon.setImageResource(R.drawable.ic_donate);
            } else {
                holder.ivIcon.setImageResource(R.drawable.ic_notification);
            }
        }

        @Override public int getItemCount() { return items.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDesc, tvTime;
            android.widget.ImageView ivIcon;
            VH(View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tv_activity_title);
                tvDesc = v.findViewById(R.id.tv_activity_desc);
                tvTime = v.findViewById(R.id.tv_activity_time);
                ivIcon = v.findViewById(R.id.iv_activity_icon);
            }
        }
    }
}
