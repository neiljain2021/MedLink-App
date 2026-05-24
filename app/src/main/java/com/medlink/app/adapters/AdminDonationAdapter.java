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
 * AdminDonationAdapter — displays donation documents in the admin Donations list.
 */
public class AdminDonationAdapter extends RecyclerView.Adapter<AdminDonationAdapter.ViewHolder> {

    public interface DonationActionListener {
        void onVerify(DocumentSnapshot doc);
        void onReject(DocumentSnapshot doc);
        void onDelete(DocumentSnapshot doc);
    }

    private List<DocumentSnapshot> items = new ArrayList<>();
    private final DonationActionListener listener;

    public AdminDonationAdapter(DonationActionListener listener) {
        this.listener = listener;
    }

    public void setData(List<DocumentSnapshot> docs) {
        items = new ArrayList<>(docs);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_donation, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        DocumentSnapshot doc = items.get(position);

        String medicine = doc.getString("medicineName");
        String donor    = doc.getString("donorName");
        String qty      = doc.getString("quantity");
        String status   = doc.getString("status");

        h.tvMedicine.setText(medicine != null ? medicine : "—");
        h.tvDonor.setText(donor != null ? donor : "—");
        h.tvQty.setText(qty != null ? qty : "—");
        h.tvStatus.setText(status != null ? status : "UNKNOWN");

        // Status chip color
        if ("VERIFIED".equals(status)) {
            h.tvStatus.setTextColor(0xFF10B981);
            h.tvStatus.setBackgroundResource(R.drawable.bg_status_ready);
        } else if ("REJECTED".equals(status)) {
            h.tvStatus.setTextColor(0xFFEF4444);
            h.tvStatus.setBackgroundResource(R.drawable.bg_status_rejected);
        } else {
            // VERIFYING (default)
            h.tvStatus.setTextColor(0xFF3B82F6);
            h.tvStatus.setBackgroundResource(R.drawable.bg_status_processing);
        }

        h.btnVerify.setOnClickListener(v -> listener.onVerify(doc));
        h.btnReject.setOnClickListener(v -> listener.onReject(doc));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(doc));
    }

    @Override public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedicine, tvDonor, tvQty, tvStatus;
        TextView btnVerify, btnReject, btnDelete;
        ViewHolder(View v) {
            super(v);
            tvMedicine = v.findViewById(R.id.tv_donation_medicine);
            tvDonor    = v.findViewById(R.id.tv_donor_name);
            tvQty      = v.findViewById(R.id.tv_donation_quantity);
            tvStatus   = v.findViewById(R.id.tv_donation_status);
            btnVerify  = v.findViewById(R.id.btn_don_verify);
            btnReject  = v.findViewById(R.id.btn_don_reject);
            btnDelete  = v.findViewById(R.id.btn_don_delete);
        }
    }
}
