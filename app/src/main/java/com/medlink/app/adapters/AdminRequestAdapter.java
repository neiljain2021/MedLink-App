package com.medlink.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.medlink.app.R;
import java.util.ArrayList;
import java.util.List;

/**
 * AdminRequestAdapter — displays medicine request documents in the admin Requests list.
 */
public class AdminRequestAdapter extends RecyclerView.Adapter<AdminRequestAdapter.ViewHolder> {

    public interface RequestActionListener {
        void onApprove(DocumentSnapshot doc);
        void onReady(DocumentSnapshot doc);
        void onReject(DocumentSnapshot doc);
        void onDelete(DocumentSnapshot doc);
    }

    private List<DocumentSnapshot> items = new ArrayList<>();
    private final RequestActionListener listener;

    public AdminRequestAdapter(RequestActionListener listener) {
        this.listener = listener;
    }

    public void setData(List<DocumentSnapshot> docs) {
        items = new ArrayList<>(docs);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_request, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        DocumentSnapshot doc = items.get(position);

        String medicine  = doc.getString("medicineName");
        String ngo       = doc.getString("targetNgo");
        String qty       = doc.getString("quantity");
        String status    = doc.getString("status");
        String requester = doc.getString("requesterId");

        h.tvMedicine.setText(medicine != null ? medicine : "—");
        h.tvNgo.setText(ngo != null ? ngo : "—");
        h.tvQty.setText(qty != null ? qty : "—");
        h.tvRequester.setText(requester != null ? requester : "—");
        h.tvStatus.setText(status != null ? status : "UNKNOWN");

        // Status chip color
        applyStatusColor(h.tvStatus, status);

        h.btnApprove.setOnClickListener(v -> listener.onApprove(doc));
        h.btnReady.setOnClickListener(v   -> listener.onReady(doc));
        h.btnReject.setOnClickListener(v  -> listener.onReject(doc));
        h.btnDelete.setOnClickListener(v  -> listener.onDelete(doc));
    }

    private void applyStatusColor(TextView tv, String status) {
        if (status == null) return;
        switch (status) {
            case "PENDING":
                tv.setTextColor(0xFFF59E0B);
                tv.setBackgroundResource(R.drawable.bg_status_pending);
                break;
            case "PROCESSING":
                tv.setTextColor(0xFF3B82F6);
                tv.setBackgroundResource(R.drawable.bg_status_processing);
                break;
            case "READY":
                tv.setTextColor(0xFF10B981);
                tv.setBackgroundResource(R.drawable.bg_status_ready);
                break;
            case "REJECTED":
                tv.setTextColor(0xFFEF4444);
                tv.setBackgroundResource(R.drawable.bg_status_rejected);
                break;
        }
    }

    @Override public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedicine, tvNgo, tvQty, tvStatus, tvRequester;
        TextView btnApprove, btnReady, btnReject, btnDelete;
        ViewHolder(View v) {
            super(v);
            tvMedicine  = v.findViewById(R.id.tv_medicine_name);
            tvNgo       = v.findViewById(R.id.tv_target_ngo);
            tvQty       = v.findViewById(R.id.tv_quantity);
            tvStatus    = v.findViewById(R.id.tv_request_status);
            tvRequester = v.findViewById(R.id.tv_requester_id);
            btnApprove  = v.findViewById(R.id.btn_req_approve);
            btnReady    = v.findViewById(R.id.btn_req_ready);
            btnReject   = v.findViewById(R.id.btn_req_reject);
            btnDelete   = v.findViewById(R.id.btn_req_delete);
        }
    }
}
