package com.medlink.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.medlink.app.R;
import com.medlink.app.models.MedicineRequest;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NgoRequestAdapter extends RecyclerView.Adapter<NgoRequestAdapter.ViewHolder> {

    private List<MedicineRequest> requestList;
    private OnRequestActionListener actionListener;

    public interface OnRequestActionListener {
        void onAccept(MedicineRequest request);
        void onReject(MedicineRequest request);
        void onComplete(MedicineRequest request);
    }

    public NgoRequestAdapter(List<MedicineRequest> requestList, OnRequestActionListener actionListener) {
        this.requestList = requestList;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ngo_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MedicineRequest request = requestList.get(position);
        holder.tvMedicineName.setText(request.getMedicineName());
        holder.tvQuantity.setText("Quantity: " + request.getQuantity());
        holder.tvStatus.setText(request.getStatus().toUpperCase());
        
        // Format timestamp
        if (request.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
            holder.tvTime.setText(sdf.format(request.getTimestamp()));
        } else {
            holder.tvTime.setText("Recently");
        }

        // Configure UI based on status
        String status = request.getStatus().toUpperCase();
        switch (status) {
            case "PENDING":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_ready); // Using existing status drawable (yellow-ish)
                holder.btnAccept.setVisibility(View.VISIBLE);
                holder.btnReject.setVisibility(View.VISIBLE);
                holder.btnComplete.setVisibility(View.GONE);
                break;
            case "ACCEPTED":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_primary_button); // Blue
                holder.btnAccept.setVisibility(View.GONE);
                holder.btnReject.setVisibility(View.GONE);
                holder.btnComplete.setVisibility(View.VISIBLE);
                break;
            case "COMPLETED":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_ready); // Green
                holder.btnAccept.setVisibility(View.GONE);
                holder.btnReject.setVisibility(View.GONE);
                holder.btnComplete.setVisibility(View.GONE);
                holder.layoutActions.setVisibility(View.GONE);
                break;
            default:
                holder.layoutActions.setVisibility(View.GONE);
                break;
        }

        holder.btnAccept.setOnClickListener(v -> actionListener.onAccept(request));
        holder.btnReject.setOnClickListener(v -> actionListener.onReject(request));
        holder.btnComplete.setOnClickListener(v -> actionListener.onComplete(request));
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedicineName, tvQuantity, tvStatus, tvLocation, tvTime;
        View btnAccept, btnReject, btnComplete, layoutActions;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMedicineName = itemView.findViewById(R.id.tv_request_medicine_name);
            tvQuantity = itemView.findViewById(R.id.tv_request_quantity);
            tvStatus = itemView.findViewById(R.id.tv_request_status_pill);
            tvLocation = itemView.findViewById(R.id.tv_request_location);
            tvTime = itemView.findViewById(R.id.tv_request_time);
            btnAccept = itemView.findViewById(R.id.btn_accept_request);
            btnReject = itemView.findViewById(R.id.btn_reject_request);
            btnComplete = itemView.findViewById(R.id.btn_complete_request);
            layoutActions = itemView.findViewById(R.id.layout_request_actions);
        }
    }
}
