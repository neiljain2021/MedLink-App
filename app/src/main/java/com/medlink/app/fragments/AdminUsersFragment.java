package com.medlink.app.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.medlink.app.R;
import com.medlink.app.adapters.AdminUserAdapter;
import com.medlink.app.repository.AdminRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AdminUsersFragment — Full user management: view, search, edit, block/unblock, soft-delete.
 * Includes pagination (load 20 at a time).
 */
public class AdminUsersFragment extends Fragment {

    private AdminRepository repo;
    private AdminUserAdapter adapter;
    private ProgressBar progress;
    private TextView tvEmpty, tvCount, btnLoadMore;
    private RecyclerView rv;

    private DocumentSnapshot lastDoc = null;
    private boolean moreAvailable = true;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_admin_users, container, false);

        repo = new AdminRepository();

        rv         = v.findViewById(R.id.rv_admin_users);
        progress   = v.findViewById(R.id.progress_users);
        tvEmpty    = v.findViewById(R.id.tv_users_empty);
        tvCount    = v.findViewById(R.id.tv_user_count);
        btnLoadMore= v.findViewById(R.id.btn_load_more_users);

        adapter = new AdminUserAdapter(new AdminUserAdapter.UserActionListener() {
            @Override public void onItemClick(DocumentSnapshot doc)    { showDetailsDialog(doc); }
            @Override public void onEdit(DocumentSnapshot doc)          { showEditDialog(doc); }
            @Override public void onBlock(DocumentSnapshot doc, boolean blocked) { toggleBlock(doc, blocked); }
            @Override public void onDelete(DocumentSnapshot doc)        { confirmDelete(doc); }
        });

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        // Live search
        EditText etSearch = v.findViewById(R.id.et_search_users);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int i, int b, int c) { adapter.filter(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnLoadMore.setOnClickListener(btn -> loadNextPage());

        loadFirstPage();
        return v;
    }

    private void loadFirstPage() {
        progress.setVisibility(View.VISIBLE);
        repo.getAllUsers("user", new AdminRepository.Callback<List<DocumentSnapshot>>() {
            @Override public void onSuccess(List<DocumentSnapshot> docs) {
                if (!isAdded()) return;
                progress.setVisibility(View.GONE);
                adapter.setData(docs);
                updateUI(docs.size());
                moreAvailable = docs.size() == AdminRepository.PAGE_SIZE;
                btnLoadMore.setVisibility(moreAvailable ? View.VISIBLE : View.GONE);
                if (!docs.isEmpty()) lastDoc = docs.get(docs.size() - 1);
            }
            @Override public void onFailure(Exception e) {
                if (!isAdded()) return;
                progress.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadNextPage() {
        if (lastDoc == null || !moreAvailable) return;
        btnLoadMore.setText("Loading...");
        repo.getUsersAfter("user", lastDoc, new AdminRepository.Callback<List<DocumentSnapshot>>() {
            @Override public void onSuccess(List<DocumentSnapshot> docs) {
                if (!isAdded()) return;
                adapter.addData(docs);
                moreAvailable = docs.size() == AdminRepository.PAGE_SIZE;
                btnLoadMore.setText("Load More");
                btnLoadMore.setVisibility(moreAvailable ? View.VISIBLE : View.GONE);
                if (!docs.isEmpty()) lastDoc = docs.get(docs.size() - 1);
            }
            @Override public void onFailure(Exception e) {
                if (!isAdded()) return;
                btnLoadMore.setText("Load More");
                Toast.makeText(getContext(), "Failed to load more", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(int count) {
        tvCount.setText(count + " users loaded");
        tvEmpty.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
        rv.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
    }

    private void showDetailsDialog(DocumentSnapshot doc) {
        StringBuilder details = new StringBuilder();
        details.append("Name: ").append(doc.getString("name")).append("\n");
        details.append("Email: ").append(doc.getString("email")).append("\n");
        details.append("Phone: ").append(doc.getString("phone") != null ? doc.getString("phone") : "Not provided").append("\n\n");
        
        details.append("--- Location ---\n");
        details.append("Country: ").append(doc.getString("country") != null ? doc.getString("country") : "—").append("\n");
        details.append("State: ").append(doc.getString("state") != null ? doc.getString("state") : "—").append("\n\n");
        
        details.append("--- Impact Stats ---\n");
        Long donated = doc.getLong("medicinesDonated");
        Long helped = doc.getLong("livesHelped");
        details.append("Medicines Donated: ").append(donated != null ? donated : 0).append("\n");
        details.append("Lives Helped: ").append(helped != null ? helped : 0).append("\n\n");
        
        details.append("--- Status ---\n");
        details.append("Verified: ").append(Boolean.TRUE.equals(doc.getBoolean("verified")) ? "YES" : "NO").append("\n");
        details.append("Blocked: ").append(Boolean.TRUE.equals(doc.getBoolean("blocked")) ? "YES" : "NO");

        new AlertDialog.Builder(getContext())
                .setTitle("Account Details")
                .setMessage(details.toString())
                .setPositiveButton("Close", null)
                .setNeutralButton("Edit Profile", (d, w) -> showEditDialog(doc))
                .show();
    }

    /** Show edit dialog for name, phone, state. */
    private void showEditDialog(DocumentSnapshot doc) {
        View form = LayoutInflater.from(getContext()).inflate(R.layout.activity_register, null);
        // Re-use a simple 3-field dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit User");

        // Custom inline dialog layout
        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 0);

        EditText etName  = new EditText(getContext()); etName.setHint("Name");
        EditText etPhone = new EditText(getContext()); etPhone.setHint("Phone");
        EditText etState = new EditText(getContext()); etState.setHint("State");

        // Pre-fill current values
        String name  = doc.getString("name");
        String phone = doc.getString("phone");
        String state = doc.getString("state");
        if (name  != null) etName.setText(name);
        if (phone != null) etPhone.setText(phone);
        if (state != null) etState.setText(state);

        layout.addView(etName);
        layout.addView(etPhone);
        layout.addView(etState);
        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            Map<String, Object> fields = new HashMap<>();
            fields.put("name",  etName.getText().toString().trim());
            fields.put("phone", etPhone.getText().toString().trim());
            fields.put("state", etState.getText().toString().trim());
            repo.updateUser(doc.getId(), fields, new AdminRepository.Callback<Void>() {
                @Override public void onSuccess(Void r) {
                    if (!isAdded()) return;
                    Toast.makeText(getContext(), "User updated", Toast.LENGTH_SHORT).show();
                    loadFirstPage();
                }
                @Override public void onFailure(Exception e) {
                    if (!isAdded()) return;
                    Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
                }
            });
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void toggleBlock(DocumentSnapshot doc, boolean currentlyBlocked) {
        boolean newState = !currentlyBlocked;
        String msg = newState ? "Block this user?" : "Unblock this user?";
        new AlertDialog.Builder(getContext())
                .setTitle(newState ? "Block User" : "Unblock User")
                .setMessage(msg)
                .setPositiveButton("Yes", (d, w) -> {
                    repo.setUserBlocked(doc.getId(), newState, new AdminRepository.Callback<Void>() {
                        @Override public void onSuccess(Void r) {
                            if (!isAdded()) return;
                            Toast.makeText(getContext(),
                                    newState ? "User blocked" : "User unblocked",
                                    Toast.LENGTH_SHORT).show();
                            loadFirstPage();
                        }
                        @Override public void onFailure(Exception e) {
                            if (!isAdded()) return;
                            Toast.makeText(getContext(), "Action failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDelete(DocumentSnapshot doc) {
        String name = doc.getString("name");
        new AlertDialog.Builder(getContext())
                .setTitle("Delete User")
                .setMessage("Soft-delete \"" + name + "\"? This can be undone from Firestore.")
                .setPositiveButton("Delete", (d, w) -> {
                    repo.softDeleteUser(doc.getId(), new AdminRepository.Callback<Void>() {
                        @Override public void onSuccess(Void r) {
                            if (!isAdded()) return;
                            Toast.makeText(getContext(), "User deleted (soft)", Toast.LENGTH_SHORT).show();
                            loadFirstPage();
                        }
                        @Override public void onFailure(Exception e) {
                            if (!isAdded()) return;
                            Toast.makeText(getContext(), "Delete failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
