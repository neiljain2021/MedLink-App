package com.medlink.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.medlink.app.R;
import com.medlink.app.models.Donation;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NgoDonationAdapter extends RecyclerView.Adapter<NgoDonationAdapter.ViewHolder> {

    public interface OnDonationActionListener {
        void onClaim(Donation donation);
    }

    private List<Donation> donations;
    private final OnDonationActionListener listener;

    public NgoDonationAdapter(List<Donation> donations, OnDonationActionListener listener) {
        this.donations = donations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ngo_donation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Donation donation = donations.get(position);
        holder.tvMedicine.setText(donation.getMedicineName());
        holder.tvDonor.setText("By: " + donation.getDonorName());
        holder.tvQuantity.setText("Qty: " + donation.getQuantity());

        if (donation.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
            holder.tvTime.setText(sdf.format(donation.getTimestamp()));
        } else {
            holder.tvTime.setText("Just now");
        }

        if ("ACCEPTED".equals(donation.getStatus())) {
            holder.btnClaim.setVisibility(View.GONE);
        } else {
            holder.btnClaim.setVisibility(View.VISIBLE);
            holder.btnClaim.setOnClickListener(v -> listener.onClaim(donation));
        }
    }

    @Override
    public int getItemCount() {
        return donations.size();
    }

    public void updateData(List<Donation> newDonations) {
        this.donations = newDonations;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedicine, tvDonor, tvQuantity, tvTime, btnClaim;

        ViewHolder(View itemView) {
            super(itemView);
            tvMedicine = itemView.findViewById(R.id.tv_donation_medicine);
            tvDonor = itemView.findViewById(R.id.tv_donor_name);
            tvQuantity = itemView.findViewById(R.id.tv_donation_quantity);
            tvTime = itemView.findViewById(R.id.tv_donation_time);
            btnClaim = itemView.findViewById(R.id.btn_claim_donation);
        }
    }
}
