package com.medlink.app.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.medlink.app.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NgoInventoryFragment extends Fragment {

    private FirebaseFirestore db;
    private String ngoUid;
    private Map<String, Integer> inventoryMap = new HashMap<>();
    private InventoryAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_ngo_inventory, container, false);

        db = FirebaseFirestore.getInstance();
        ngoUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        RecyclerView rv = v.findViewById(R.id.rv_inventory);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new InventoryAdapter();
        rv.setAdapter(adapter);

        v.findViewById(R.id.fab_add_medicine).setOnClickListener(view -> showAddMedicineDialog());

        loadInventory();

        return v;
    }

    private void loadInventory() {
        db.collection("NGOs").document(ngoUid)
                .addSnapshotListener((doc, error) -> {
                    if (error != null || !isAdded()) return;
                    if (doc != null && doc.exists()) {
                        Map<String, Object> data = doc.getData();
                        if (data != null && data.get("inventory") != null) {
                            try {
                                Map<String, Object> rawMap = (Map<String, Object>) data.get("inventory");
                                Map<String, Integer> processedMap = new HashMap<>();
                                for (Map.Entry<String, Object> entry : rawMap.entrySet()) {
                                    if (entry.getValue() instanceof Long) {
                                        processedMap.put(entry.getKey(), ((Long) entry.getValue()).intValue());
                                    } else if (entry.getValue() instanceof Integer) {
                                        processedMap.put(entry.getKey(), (Integer) entry.getValue());
                                    }
                                }
                                inventoryMap = processedMap;
                            } catch (Exception e) {
                                inventoryMap = new HashMap<>();
                            }
                        } else {
                            inventoryMap = new HashMap<>();
                        }
                        adapter.setItems(inventoryMap);
                    }
                });
    }

    private void showAddMedicineDialog() {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etName = new EditText(getContext());
        etName.setHint("Medicine Name");
        layout.addView(etName);

        final EditText etQty = new EditText(getContext());
        etQty.setHint("Quantity");
        etQty.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etQty);

        new AlertDialog.Builder(getContext())
                .setTitle("Add to Inventory")
                .setView(layout)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String qtyStr = etQty.getText().toString().trim();
                    if (name.isEmpty() || qtyStr.isEmpty()) return;

                    int qty = Integer.parseInt(qtyStr);
                    inventoryMap.put(name, qty);
                    updateFirestore();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateFirestore() {
        db.collection("NGOs").document(ngoUid)
                .update("inventory", inventoryMap)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Inventory updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update", Toast.LENGTH_SHORT).show());
    }

    private class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.VH> {
        private List<String> keys = new ArrayList<>();
        private Map<String, Integer> items = new HashMap<>();

        public void setItems(Map<String, Integer> items) {
            this.items = items;
            this.keys = new ArrayList<>(items.keySet());
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ngo_inventory, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            String name = keys.get(position);
            int qty = items.get(name);
            holder.tvName.setText(name);
            holder.tvQty.setText("Quantity: " + qty + " units");

            holder.btnDelete.setOnClickListener(v -> {
                inventoryMap.remove(name);
                updateFirestore();
            });
        }

        @Override
        public int getItemCount() {
            return keys.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvQty;
            ImageView btnDelete;

            public VH(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_medicine_name);
                tvQty = itemView.findViewById(R.id.tv_medicine_quantity);
                btnDelete = itemView.findViewById(R.id.btn_delete_medicine);
            }
        }
    }
}
