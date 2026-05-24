package com.medlink.app.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.medlink.app.models.Ngo;
import com.medlink.app.adapters.NgoAdapter;
import com.medlink.app.R;

import java.util.ArrayList;
import java.util.List;

public class NgoListFragment extends Fragment {

    private RecyclerView rvNgoList;
    private NgoAdapter adapter;
    private List<Ngo> allNgos = new ArrayList<>();
    private TextView tvEmptyState;

    public NgoListFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ngo_list, container, false);
        
        rvNgoList = view.findViewById(R.id.rv_ngo_list);
        tvEmptyState = view.findViewById(R.id.tv_ngo_empty_state);
        EditText etSearch = view.findViewById(R.id.et_search_ngo);
        
        rvNgoList.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NgoAdapter();
        rvNgoList.setAdapter(adapter);
        
        fetchNgos();
        
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterNgos(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        return view;
    }

    private void fetchNgos() {
        FirebaseFirestore.getInstance().collection("NGOs")
            .whereEqualTo("verified", true) // Only show verified NGOs to regular users
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    if (isAdded()) Toast.makeText(getContext(), "Error loading NGOs", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (value != null) {
                    allNgos.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        Ngo ngo = doc.toObject(Ngo.class);
                        allNgos.add(ngo);
                    }
                    adapter.setNgos(allNgos);
                }
            });
    }

    private void filterNgos(String query) {
        List<Ngo> filtered = new ArrayList<>();
        for (Ngo ngo : allNgos) {
            if (ngo.getName().toLowerCase().contains(query.toLowerCase()) ||
                ngo.getLocation().toLowerCase().contains(query.toLowerCase()) ||
                ngo.getCategory().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(ngo);
            }
        }
        adapter.setNgos(filtered);
        tvEmptyState.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
