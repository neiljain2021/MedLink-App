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
import com.medlink.app.R;
import com.medlink.app.models.AdminLog;
import com.medlink.app.repository.AdminRepository;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * AdminDashboardFragment — shows live stat counts, request status bar chart,
 * and recent admin activity feed.
 */
public class AdminDashboardFragment extends Fragment {

    private AdminRepository repo;

    // Stat card TextViews
    private TextView tvUsers, tvRequests, tvDonations, tvNgos;

    // Bar chart views
    private View barPending, barProcessing, barReady, barRejected;
    private TextView tvBarPending, tvBarProcessing, tvBarReady, tvBarRejected;

    // Activity feed
    private RecyclerView rvActivity;
    private TextView tvNoActivity;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);

        repo = new AdminRepository();

        // Bind stat cards
        tvUsers     = v.findViewById(R.id.tv_stat_users);
        tvRequests  = v.findViewById(R.id.tv_stat_requests);
        tvDonations = v.findViewById(R.id.tv_stat_donations);
        tvNgos      = v.findViewById(R.id.tv_stat_ngos);

        // Bind bar chart views
        barPending    = v.findViewById(R.id.bar_pending);
        barProcessing = v.findViewById(R.id.bar_processing);
        barReady      = v.findViewById(R.id.bar_ready);
        barRejected   = v.findViewById(R.id.bar_rejected);
        tvBarPending    = v.findViewById(R.id.tv_bar_pending);
        tvBarProcessing = v.findViewById(R.id.tv_bar_processing);
        tvBarReady      = v.findViewById(R.id.tv_bar_ready);
        tvBarRejected   = v.findViewById(R.id.tv_bar_rejected);

        // Bind activity feed
        rvActivity   = v.findViewById(R.id.rv_recent_activity);
        tvNoActivity = v.findViewById(R.id.tv_no_activity);
        rvActivity.setLayoutManager(new LinearLayoutManager(getContext()));

        loadStats();
        loadChart();
        loadActivity();

        return v;
    }

    /** Fetch aggregated counts and update stat cards. */
    private void loadStats() {
        repo.getDashboardStats(new AdminRepository.Callback<Map<String, Integer>>() {
            @Override public void onSuccess(Map<String, Integer> stats) {
                if (!isAdded()) return;
                tvUsers.setText(String.valueOf(stats.getOrDefault("users", 0)));
                tvRequests.setText(String.valueOf(stats.getOrDefault("requests", 0)));
                tvDonations.setText(String.valueOf(stats.getOrDefault("donations", 0)));
                tvNgos.setText(String.valueOf(stats.getOrDefault("ngos", 0)));
            }
            @Override public void onFailure(Exception e) {
                if (!isAdded()) return;
                tvUsers.setText("!"); tvRequests.setText("!"); tvDonations.setText("!"); tvNgos.setText("!");
            }
        });
    }

    /** Fetch request breakdown and animate bar widths. */
    private void loadChart() {
        repo.getRequestStatusBreakdown(new AdminRepository.Callback<Map<String, Integer>>() {
            @Override public void onSuccess(Map<String, Integer> counts) {
                if (!isAdded() || getView() == null) return;
                int pending    = counts.getOrDefault("PENDING", 0);
                int processing = counts.getOrDefault("PROCESSING", 0);
                int ready      = counts.getOrDefault("READY", 0);
                int rejected   = counts.getOrDefault("REJECTED", 0);
                int total      = pending + processing + ready + rejected;
                if (total == 0) return;

                getView().post(() -> {
                    // Use parent width as 100%
                    int parentW = ((View) barPending.getParent()).getWidth();
                    animateBar(barPending,    tvBarPending,    pending,    total, parentW);
                    animateBar(barProcessing, tvBarProcessing, processing, total, parentW);
                    animateBar(barReady,      tvBarReady,      ready,      total, parentW);
                    animateBar(barRejected,   tvBarRejected,   rejected,   total, parentW);
                });
            }
            @Override public void onFailure(Exception e) { /* silent */ }
        });
    }

    /** Animate a bar to its target width fraction. */
    private void animateBar(View bar, TextView label, int count, int total, int parentWidth) {
        int targetW = (int) ((count / (float) total) * parentWidth);
        label.setText(String.valueOf(count));
        android.animation.ValueAnimator anim = android.animation.ValueAnimator.ofInt(0, targetW);
        anim.setDuration(600);
        anim.setInterpolator(new android.view.animation.DecelerateInterpolator());
        anim.addUpdateListener(a -> {
            ViewGroup.LayoutParams lp = bar.getLayoutParams();
            lp.width = (int) a.getAnimatedValue();
            bar.setLayoutParams(lp);
        });
        anim.start();
    }

    /** Fetch recent admin log entries and display them. */
    private void loadActivity() {
        repo.getRecentActivity(new AdminRepository.Callback<List<AdminLog>>() {
            @Override public void onSuccess(List<AdminLog> logs) {
                if (!isAdded()) return;
                if (logs.isEmpty()) {
                    tvNoActivity.setVisibility(View.VISIBLE);
                    rvActivity.setVisibility(View.GONE);
                    return;
                }
                tvNoActivity.setVisibility(View.GONE);
                rvActivity.setVisibility(View.VISIBLE);
                rvActivity.setAdapter(new ActivityFeedAdapter(logs));
            }
            @Override public void onFailure(Exception e) {
                if (!isAdded()) return;
                tvNoActivity.setVisibility(View.VISIBLE);
            }
        });
    }

    // ── Inline lightweight adapter for activity feed ────────────────────────

    static class ActivityFeedAdapter extends RecyclerView.Adapter<ActivityFeedAdapter.VH> {
        private final List<AdminLog> logs;
        private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault());
        ActivityFeedAdapter(List<AdminLog> logs) { this.logs = logs; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Simple two-line text card built programmatically to avoid extra layout file
            android.widget.LinearLayout ll = new android.widget.LinearLayout(parent.getContext());
            ll.setOrientation(android.widget.LinearLayout.VERTICAL);
            ll.setPadding(40, 20, 40, 20);
            int bg = parent.getContext().getResources().getColor(R.color.surface, null);
            ll.setBackgroundColor(bg);

            android.widget.TextView tvAction = new android.widget.TextView(parent.getContext());
            tvAction.setId(R.id.tv_stat_users + 100); // reuse id slot safely
            tvAction.setTextSize(13);
            tvAction.setTextColor(0xFF0F172A);
            tvAction.setTypeface(null, android.graphics.Typeface.BOLD);

            android.widget.TextView tvTime = new android.widget.TextView(parent.getContext());
            tvTime.setTextSize(11);
            tvTime.setTextColor(0xFF94A3B8);

            ll.addView(tvAction);
            ll.addView(tvTime);

            android.view.ViewGroup.MarginLayoutParams mlp =
                    new android.view.ViewGroup.MarginLayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            mlp.bottomMargin = 2;
            ll.setLayoutParams(mlp);
            return new VH(ll, tvAction, tvTime);
        }

        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            AdminLog log = logs.get(pos);
            h.tvAction.setText(log.getAction() + " → " + log.getTargetType()
                    + " [" + shortenId(log.getTargetId()) + "]");
            h.tvTime.setText(log.getTimestamp() != null ? sdf.format(log.getTimestamp()) : "Just now");
        }

        private String shortenId(String id) {
            return (id != null && id.length() > 8) ? id.substring(0, 8) + "…" : (id != null ? id : "—");
        }

        @Override public int getItemCount() { return logs.size(); }

        static class VH extends RecyclerView.ViewHolder {
            android.widget.TextView tvAction, tvTime;
            VH(View root, android.widget.TextView a, android.widget.TextView t) {
                super(root); tvAction = a; tvTime = t;
            }
        }
    }
}
