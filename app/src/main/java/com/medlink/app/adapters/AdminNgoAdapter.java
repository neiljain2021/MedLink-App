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
 * AdminNgoAdapter — displays NGO documents in the admin NGO Management list.
 * Supports client-side search filtering.
 */
public class AdminNgoAdapter extends RecyclerView.Adapter<AdminNgoAdapter.ViewHolder> {

    public interface NgoActionListener {
        void onEdit(DocumentSnapshot doc);
        void onToggleVerify(DocumentSnapshot doc, boolean currentlyVerified);
        void onDelete(DocumentSnapshot doc);
    }

    private List<DocumentSnapshot> allItems = new ArrayList<>();
    private List<DocumentSnapshot> filtered = new ArrayList<>();
    private final NgoActionListener listener;

    public AdminNgoAdapter(NgoActionListener listener) {
        this.listener = listener;
    }

    public void setData(List<DocumentSnapshot> docs) {
        allItems = new ArrayList<>(docs);
        filtered = new ArrayList<>(docs);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        filtered.clear();
        if (query == null || query.trim().isEmpty()) {
            filtered.addAll(allItems);
        } else {
            String q = query.toLowerCase().trim();
            for (DocumentSnapshot doc : allItems) {
                String name = doc.getString("name");
                String cat  = doc.getString("category");
                String loc  = doc.getString("location");
                if ((name != null && name.toLowerCase().contains(q)) ||
                    (cat  != null && cat.toLowerCase().contains(q)) ||
                    (loc  != null && loc.toLowerCase().contains(q))) {
                    filtered.add(doc);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_ngo, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        DocumentSnapshot doc = filtered.get(position);

        String name     = doc.getString("name");
        String category = doc.getString("category");
        String location = doc.getString("location");
        Boolean verified = doc.getBoolean("verified");
        Double rating   = doc.getDouble("rating");

        h.tvName.setText(name != null ? name : "—");
        h.tvCategory.setText(category != null ? category : "—");
        h.tvLocation.setText("📍 " + (location != null ? location : "—"));
        h.tvRating.setText(rating != null ? String.format("⭐ %.1f", rating) : "⭐ —");

        boolean isVerified = Boolean.TRUE.equals(verified);
        h.tvVerified.setVisibility(isVerified ? View.VISIBLE : View.GONE);

        // Toggle verify button label
        h.btnVerify.setText(isVerified ? "Unverify" : "Verify");

        h.btnEdit.setOnClickListener(v -> listener.onEdit(doc));
        h.btnVerify.setOnClickListener(v -> listener.onToggleVerify(doc, isVerified));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(doc));
    }

    @Override public int getItemCount() { return filtered.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvLocation, tvRating, tvVerified;
        TextView btnEdit, btnVerify, btnDelete;
        ViewHolder(View v) {
            super(v);
            tvName     = v.findViewById(R.id.tv_ngo_name);
            tvCategory = v.findViewById(R.id.tv_ngo_category);
            tvLocation = v.findViewById(R.id.tv_ngo_location);
            tvRating   = v.findViewById(R.id.tv_ngo_rating);
            tvVerified = v.findViewById(R.id.tv_ngo_verified);
            btnEdit    = v.findViewById(R.id.btn_ngo_edit);
            btnVerify  = v.findViewById(R.id.btn_ngo_verify);
            btnDelete  = v.findViewById(R.id.btn_ngo_delete);
        }
    }
}
