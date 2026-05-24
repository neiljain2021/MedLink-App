package com.medlink.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.medlink.app.R;
import com.medlink.app.models.MedicineRequest;

import java.util.ArrayList;
import java.util.List;

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.RequestViewHolder> {

    private List<MedicineRequest> requestList = new ArrayList<>();

    public void setRequests(List<MedicineRequest> requests) {
        this.requestList = requests;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request_card, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        MedicineRequest request = requestList.get(position);
        holder.tvMedicineName.setText(request.getMedicineName());
        holder.tvNgoName.setText(request.getTargetNgo());
        
        String qty = request.getQuantity();
        if (qty != null && !qty.isEmpty()) {
            holder.tvQuantity.setText(qty + " Units");
            holder.tvQuantity.setVisibility(View.VISIBLE);
        } else {
            holder.tvQuantity.setVisibility(View.GONE);
        }
        
        String status = request.getStatus().toUpperCase();
        holder.tvStatus.setText(status);

        // Status styling
        if (status.equals("PENDING")) {
            holder.tvStatus.setTextColor(0xFFFF9800); // Orange
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
        } else if (status.equals("ACCEPTED") || status.equals("PROCESSING")) {
            holder.tvStatus.setTextColor(0xFF3B82F6); // Blue
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_processing);
        } else if (status.equals("COMPLETED")) {
            holder.tvStatus.setTextColor(0xFF10B981); // Green
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_ready);
        } else {
            holder.tvStatus.setTextColor(0xFF6B7280); // Gray
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_processing);
        }
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedicineName, tvNgoName, tvQuantity, tvStatus;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMedicineName = itemView.findViewById(R.id.tv_medicine_name);
            tvNgoName = itemView.findViewById(R.id.tv_ngo_name);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvStatus = itemView.findViewById(R.id.tv_status);
        }
    }
}
