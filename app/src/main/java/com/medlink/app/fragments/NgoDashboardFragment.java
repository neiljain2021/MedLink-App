package com.medlink.app.fragments;

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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.medlink.app.R;
import com.medlink.app.activities.NgoActivity;
import com.medlink.app.models.MedicineRequest;
import com.medlink.app.models.ActivityItem;
import com.medlink.app.models.Donation;

public class NgoDashboardFragment extends Fragment {

    private TextView tvTotal, tvPending, tvAccepted, tvCompleted, tvNoActivities;
    private ProgressBar progressBar;
    private androidx.recyclerview.widget.RecyclerView rvActivities;
    private ActivityAdapter activityAdapter;
    private java.util.List<ActivityItem> activityList = new java.util.ArrayList<>();

    public NgoDashboardFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ngo_dashboard, container, false);

        tvTotal = view.findViewById(R.id.tv_total_requests);
        tvPending = view.findViewById(R.id.tv_pending_requests);
        tvAccepted = view.findViewById(R.id.tv_accepted_requests);
        tvCompleted = view.findViewById(R.id.tv_completed_requests);
        tvNoActivities = view.findViewById(R.id.tv_no_activities);
        progressBar = view.findViewById(R.id.progress_completion);
        rvActivities = view.findViewById(R.id.rv_recent_activities);

        rvActivities.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        activityAdapter = new ActivityAdapter();
        rvActivities.setAdapter(activityAdapter);

        view.findViewById(R.id.btn_view_all_activities).setOnClickListener(v -> {
            if (getActivity() instanceof NgoActivity) {
                ((NgoActivity) getActivity()).loadFragment(new NgoActivitiesFragment(), "Recent Activities");
            }
        });

        fetchStatsAndActivities();

        return view;
    }

    private void fetchStatsAndActivities() {
        if (!(getActivity() instanceof NgoActivity)) return;
        NgoActivity activity = (NgoActivity) getActivity();
        String currentNgoName = activity.getCurrentNgoName();

        if (currentNgoName == null || currentNgoName.isEmpty()) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 1. Fetch Requests Stats & Recent Requests
        db.collection("Requests")
                .whereIn("targetNgo", java.util.Arrays.asList(currentNgoName, "Any Verified NGO"))
                .addSnapshotListener((value, error) -> {
                    if (error != null || !isAdded()) return;
                    if (value != null) {
                        calculateStats(value);
                        updateActivityList(value, currentNgoName);
                    }
                });

        // 2. Fetch Recent Donations (Claims)
        db.collection("Donations")
                .whereEqualTo("targetNgo", currentNgoName)
                .limit(10)
                .addSnapshotListener((value, error) -> {
                    if (error != null || !isAdded()) return;
                    if (value != null) {
                        updateActivityListDonations(value);
                    }
                });
    }

    private synchronized void updateActivityList(QuerySnapshot snapshot, String ngoName) {
        activityList.removeIf(item -> "REQUEST".equals(item.type));
        for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
            MedicineRequest req = doc.toObject(MedicineRequest.class);
            if (req != null && ngoName.equals(req.getTargetNgo())) {
                ActivityItem item = new ActivityItem();
                item.title = "Request for " + req.getMedicineName();
                item.description = "Status: " + req.getStatus();
                item.timestamp = req.getTimestamp() != null ? req.getTimestamp().getTime() : 0;
                item.type = "REQUEST";
                activityList.add(item);
            }
        }
        sortAndDisplayActivities();
    }

    private synchronized void updateActivityListDonations(QuerySnapshot snapshot) {
        activityList.removeIf(item -> "CLAIM".equals(item.type));
        for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
            com.medlink.app.models.Donation don = doc.toObject(com.medlink.app.models.Donation.class);
            if (don != null) {
                ActivityItem item = new ActivityItem();
                item.title = "Claimed " + don.getMedicineName();
                item.description = "From: " + don.getDonorName();
                item.timestamp = don.getTimestamp() != null ? don.getTimestamp().getTime() : 0;
                item.type = "CLAIM";
                activityList.add(item);
            }
        }
        sortAndDisplayActivities();
    }

    private void sortAndDisplayActivities() {
        java.util.Collections.sort(activityList, (a, b) -> Long.compare(b.timestamp, a.timestamp));
        activityAdapter.setItems(activityList.size() > 5 ? activityList.subList(0, 5) : activityList);
        tvNoActivities.setVisibility(activityList.isEmpty() ? View.VISIBLE : View.GONE);
        rvActivities.setVisibility(activityList.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void calculateStats(QuerySnapshot snapshot) {
        int total = snapshot.size();
        int pending = 0, accepted = 0, completed = 0;

        for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
            MedicineRequest req = doc.toObject(MedicineRequest.class);
            if (req != null && req.getStatus() != null) {
                switch (req.getStatus().toUpperCase()) {
                    case "PENDING": pending++; break;
                    case "ACCEPTED": accepted++; break;
                    case "COMPLETED": completed++; break;
                }
            }
        }

        tvTotal.setText(String.valueOf(total));
        tvPending.setText(String.valueOf(pending));
        tvAccepted.setText(String.valueOf(accepted));
        tvCompleted.setText(String.valueOf(completed));

        if (total > 0) {
            int progress = (int) (((float) completed / total) * 100);
            progressBar.setProgress(progress);
        } else {
            progressBar.setProgress(0);
        }
    }

    // ── Internal Helper Classes ──────────────────────────────────────────

    private class ActivityAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<ActivityAdapter.VH> {
        private java.util.List<ActivityItem> items = new java.util.ArrayList<>();
        private final java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault());

        public void setItems(java.util.List<ActivityItem> items) {
            this.items = new java.util.ArrayList<>(items);
            notifyDataSetChanged();
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ngo_activity, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            ActivityItem item = items.get(position);
            holder.tvTitle.setText(item.title);
            holder.tvDesc.setText(item.description);
            holder.tvTime.setText(item.timestamp > 0 ? sdf.format(new java.util.Date(item.timestamp)) : "");
            
            if ("CLAIM".equals(item.type)) {
                holder.ivIcon.setImageResource(R.drawable.ic_donate);
            } else {
                holder.ivIcon.setImageResource(R.drawable.ic_notification);
            }
        }

        @Override public int getItemCount() { return items.size(); }

        class VH extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
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
