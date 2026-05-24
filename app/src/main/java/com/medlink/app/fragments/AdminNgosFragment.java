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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.medlink.app.R;
import com.medlink.app.adapters.AdminNgoAdapter;
import com.medlink.app.models.Ngo;
import com.medlink.app.repository.AdminRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AdminNgosFragment — manage all NGOs.
 * Features: search, edit, toggle verified, soft-delete, add new via FAB.
 */
public class AdminNgosFragment extends Fragment {

    private AdminRepository repo;
    private AdminNgoAdapter adapter;
    private ProgressBar progress;
    private TextView tvEmpty, tvCount;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_admin_ngos, container, false);

        repo = new AdminRepository();

        RecyclerView rv = v.findViewById(R.id.rv_admin_ngos);
        progress = v.findViewById(R.id.progress_ngos);
        tvEmpty  = v.findViewById(R.id.tv_ngos_empty);
        tvCount  = v.findViewById(R.id.tv_ngos_count);

        adapter = new AdminNgoAdapter(new AdminNgoAdapter.NgoActionListener() {
            @Override public void onEdit(DocumentSnapshot doc)                            { showEditDialog(doc); }
            @Override public void onToggleVerify(DocumentSnapshot doc, boolean verified)  { toggleVerify(doc, verified); }
            @Override public void onDelete(DocumentSnapshot doc)                          { confirmDelete(doc); }
        });

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        // Search
        EditText etSearch = v.findViewById(R.id.et_search_ngos);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int i, int b, int c) { adapter.filter(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        // FAB → Add NGO
        FloatingActionButton fab = v.findViewById(R.id.fab_add_ngo);
        fab.setOnClickListener(btn -> showAddNgoDialog());

        loadNgos();
        return v;
    }

    private void loadNgos() {
        progress.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        repo.getAllNgos(new AdminRepository.Callback<List<DocumentSnapshot>>() {
            @Override public void onSuccess(List<DocumentSnapshot> docs) {
                if (!isAdded()) return;
                progress.setVisibility(View.GONE);
                adapter.setData(docs);
                tvCount.setText(docs.size() + " NGOs registered");
                tvEmpty.setVisibility(docs.isEmpty() ? View.VISIBLE : View.GONE);
            }
            @Override public void onFailure(Exception e) {
                if (!isAdded()) return;
                progress.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Failed to load NGOs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Show form dialog to add a new NGO. */
    private void showAddNgoDialog() {
        android.widget.LinearLayout layout = buildNgoForm(null);
        EditText etName     = (EditText) layout.getChildAt(0);
        EditText etCategory = (EditText) layout.getChildAt(1);
        EditText etLocation = (EditText) layout.getChildAt(2);
        EditText etRating   = (EditText) layout.getChildAt(3);

        new AlertDialog.Builder(getContext())
                .setTitle("Add New NGO")
                .setView(layout)
                .setPositiveButton("Add", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    String cat  = etCategory.getText().toString().trim();
                    String loc  = etLocation.getText().toString().trim();
                    String rat  = etRating.getText().toString().trim();
                    if (name.isEmpty()) { Toast.makeText(getContext(), "Name required", Toast.LENGTH_SHORT).show(); return; }
                    double rating = 4.0;
                    try { rating = Double.parseDouble(rat); } catch (Exception ignored) {}
                    Ngo ngo = new Ngo(name, cat.isEmpty() ? "General" : cat, loc.isEmpty() ? "India" : loc, rating, 0);
                    repo.addNgo(ngo, new AdminRepository.Callback<Void>() {
                        @Override public void onSuccess(Void r) {
                            if (isAdded()) { Toast.makeText(getContext(), "NGO added!", Toast.LENGTH_SHORT).show(); loadNgos(); }
                        }
                        @Override public void onFailure(Exception e) {
                            if (isAdded()) Toast.makeText(getContext(), "Add failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /** Show form dialog to edit an existing NGO. */
    private void showEditDialog(DocumentSnapshot doc) {
        android.widget.LinearLayout layout = buildNgoForm(doc);
        EditText etName     = (EditText) layout.getChildAt(0);
        EditText etCategory = (EditText) layout.getChildAt(1);
        EditText etLocation = (EditText) layout.getChildAt(2);
        EditText etRating   = (EditText) layout.getChildAt(3);

        new AlertDialog.Builder(getContext())
                .setTitle("Edit NGO")
                .setView(layout)
                .setPositiveButton("Save", (d, w) -> {
                    Map<String, Object> fields = new HashMap<>();
                    fields.put("name",     etName.getText().toString().trim());
                    fields.put("category", etCategory.getText().toString().trim());
                    fields.put("location", etLocation.getText().toString().trim());
                    try { fields.put("rating", Double.parseDouble(etRating.getText().toString().trim())); }
                    catch (Exception ignored) {}
                    repo.updateNgo(doc.getId(), fields, new AdminRepository.Callback<Void>() {
                        @Override public void onSuccess(Void r) {
                            if (isAdded()) { Toast.makeText(getContext(), "NGO updated", Toast.LENGTH_SHORT).show(); loadNgos(); }
                        }
                        @Override public void onFailure(Exception e) {
                            if (isAdded()) Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /** Build a simple 4-field LinearLayout form for add/edit NGO dialogs. */
    private android.widget.LinearLayout buildNgoForm(@Nullable DocumentSnapshot doc) {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 0);

        EditText etName     = new EditText(getContext()); etName.setHint("NGO Name *");
        EditText etCategory = new EditText(getContext()); etCategory.setHint("Category (e.g. General)");
        EditText etLocation = new EditText(getContext()); etLocation.setHint("Location (e.g. Mumbai)");
        EditText etRating   = new EditText(getContext()); etRating.setHint("Rating (e.g. 4.5)");
        etRating.setInputType(android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL | android.text.InputType.TYPE_CLASS_NUMBER);

        if (doc != null) {
            String name = doc.getString("name"); if (name != null) etName.setText(name);
            String cat  = doc.getString("category"); if (cat != null) etCategory.setText(cat);
            String loc  = doc.getString("location"); if (loc != null) etLocation.setText(loc);
            Double rat  = doc.getDouble("rating"); if (rat != null) etRating.setText(String.valueOf(rat));
        }

        layout.addView(etName);
        layout.addView(etCategory);
        layout.addView(etLocation);
        layout.addView(etRating);
        return layout;
    }

    private void toggleVerify(DocumentSnapshot doc, boolean currentlyVerified) {
        boolean newState = !currentlyVerified;
        String ngoName = doc.getString("name");
        new AlertDialog.Builder(getContext())
                .setTitle(newState ? "Verify NGO" : "Remove Verification")
                .setMessage((newState ? "Mark " : "Unmark ") + "\"" + ngoName + "\" as verified?")
                .setPositiveButton("Confirm", (d, w) ->
                    repo.toggleNgoVerified(doc.getId(), newState, new AdminRepository.Callback<Void>() {
                        @Override public void onSuccess(Void r) {
                            if (isAdded()) { Toast.makeText(getContext(), newState ? "NGO verified" : "Verification removed", Toast.LENGTH_SHORT).show(); loadNgos(); }
                        }
                        @Override public void onFailure(Exception e) {
                            if (isAdded()) Toast.makeText(getContext(), "Action failed", Toast.LENGTH_SHORT).show();
                        }
                    }))
                .setNegativeButton("Cancel", null).show();
    }

    private void confirmDelete(DocumentSnapshot doc) {
        String name = doc.getString("name");
        new AlertDialog.Builder(getContext())
                .setTitle("Delete NGO")
                .setMessage("Soft-delete \"" + name + "\"?")
                .setPositiveButton("Delete", (d, w) ->
                    repo.softDeleteNgo(doc.getId(), new AdminRepository.Callback<Void>() {
                        @Override public void onSuccess(Void r) {
                            if (isAdded()) { Toast.makeText(getContext(), "NGO deleted", Toast.LENGTH_SHORT).show(); loadNgos(); }
                        }
                        @Override public void onFailure(Exception e) {
                            if (isAdded()) Toast.makeText(getContext(), "Delete failed", Toast.LENGTH_SHORT).show();
                        }
                    }))
                .setNegativeButton("Cancel", null).show();
    }
}
