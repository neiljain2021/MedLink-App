package com.medlink.app.adapters;

import android.text.Editable;
import android.text.TextWatcher;
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
 * AdminUserAdapter — displays user documents in the admin User Management list.
 * Supports live search filtering against name + email fields.
 */
public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {

    public interface UserActionListener {
        void onItemClick(DocumentSnapshot doc);
        void onEdit(DocumentSnapshot doc);
        void onBlock(DocumentSnapshot doc, boolean currentlyBlocked);
        void onDelete(DocumentSnapshot doc);
    }

    private List<DocumentSnapshot> allItems = new ArrayList<>();
    private List<DocumentSnapshot> filtered = new ArrayList<>();
    private final UserActionListener listener;

    public AdminUserAdapter(UserActionListener listener) {
        this.listener = listener;
    }

    public void setData(List<DocumentSnapshot> docs) {
        allItems = new ArrayList<>(docs);
        filtered = new ArrayList<>(docs);
        notifyDataSetChanged();
    }

    public void addData(List<DocumentSnapshot> docs) {
        int start = filtered.size();
        allItems.addAll(docs);
        filtered.addAll(docs);
        notifyItemRangeInserted(start, docs.size());
    }

    /** Filter by name or email (case-insensitive). */
    public void filter(String query) {
        filtered.clear();
        if (query == null || query.trim().isEmpty()) {
            filtered.addAll(allItems);
        } else {
            String q = query.toLowerCase().trim();
            for (DocumentSnapshot doc : allItems) {
                String name  = doc.getString("name");
                String email = doc.getString("email");
                if ((name  != null && name.toLowerCase().contains(q)) ||
                    (email != null && email.toLowerCase().contains(q))) {
                    filtered.add(doc);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_user, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        DocumentSnapshot doc = filtered.get(position);

        String name  = doc.getString("name");
        String email = doc.getString("email");
        Boolean verified = doc.getBoolean("verified");
        Boolean blocked  = doc.getBoolean("blocked");
        Boolean deleted  = doc.getBoolean("isDeleted");

        // Avatar initial
        h.tvInitial.setText(name != null && !name.isEmpty()
                ? String.valueOf(name.charAt(0)).toUpperCase() : "?");

        h.tvName.setText(name != null ? name : "Unknown");
        h.tvEmail.setText(email != null ? email : "—");

        // Verified badge
        h.tvVerified.setVisibility(Boolean.TRUE.equals(verified) ? View.VISIBLE : View.GONE);

        // Blocked badge
        h.tvBlocked.setVisibility(Boolean.TRUE.equals(blocked) ? View.VISIBLE : View.GONE);

        // Block / Unblock toggle text
        boolean isBlocked = Boolean.TRUE.equals(blocked);
        h.btnBlock.setText(isBlocked ? "Unblock" : "Block");

        // Soft-deleted items shown with strikethrough name
        if (Boolean.TRUE.equals(deleted)) {
            h.tvName.setPaintFlags(h.tvName.getPaintFlags()
                    | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            h.tvName.setPaintFlags(h.tvName.getPaintFlags()
                    & ~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        }

        h.itemView.setOnClickListener(v -> listener.onItemClick(doc));
        h.btnEdit.setOnClickListener(v -> listener.onEdit(doc));
        h.btnBlock.setOnClickListener(v -> listener.onBlock(doc, isBlocked));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(doc));
    }

    @Override public int getItemCount() { return filtered.size(); }

    /** Returns the last document snapshot for pagination cursor. */
    public DocumentSnapshot getLastDocument() {
        return filtered.isEmpty() ? null : filtered.get(filtered.size() - 1);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInitial, tvName, tvEmail, tvVerified, tvBlocked, btnEdit, btnBlock, btnDelete;
        ViewHolder(View v) {
            super(v);
            tvInitial = v.findViewById(R.id.tv_avatar_initial);
            tvName    = v.findViewById(R.id.tv_user_name);
            tvEmail   = v.findViewById(R.id.tv_user_email);
            tvVerified= v.findViewById(R.id.tv_verified_badge);
            tvBlocked = v.findViewById(R.id.tv_blocked_badge);
            btnEdit   = v.findViewById(R.id.btn_edit_user);
            btnBlock  = v.findViewById(R.id.btn_block_user);
            btnDelete = v.findViewById(R.id.btn_delete_user);
        }
    }
}
