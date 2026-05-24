package com.medlink.app.fragments;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.medlink.app.activities.MainActivity;
import com.medlink.app.models.MedicineRequest;
import com.medlink.app.models.Ngo;
import com.medlink.app.adapters.NgoAdapter;
import com.medlink.app.R;
import com.medlink.app.adapters.RequestsAdapter;
import com.medlink.app.repository.AuthRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private List<Ngo> allNgos = new ArrayList<>();
    private NgoAdapter searchAdapter;
    private LinearLayout layoutSearchResults, layoutNoMedicineFound;
    private RecyclerView rvSearchResults;
    private EditText etSearch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        setupViews(view);
        return view;
    }

    private void setupViews(View view) {
        // ── Search Logic ──────────────────────────────────────────
        etSearch = view.findViewById(R.id.et_search);
        TextView btnSearch = view.findViewById(R.id.btn_search);
        layoutSearchResults = view.findViewById(R.id.layout_search_results);
        layoutNoMedicineFound = view.findViewById(R.id.layout_no_medicine_found);
        rvSearchResults = view.findViewById(R.id.rv_search_results);
        
        rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        searchAdapter = new NgoAdapter();
        rvSearchResults.setAdapter(searchAdapter);
        
        fetchNgos();
        
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchMedicines(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnSearch.setOnClickListener(v -> {
            animateClick(v);
            searchMedicines(etSearch.getText().toString());
        });

        view.findViewById(R.id.btn_request_from_search).setOnClickListener(v -> {
            new com.medlink.app.repository.AuthRepository().isUserVerified(isVerified -> {
                if (!isAdded()) return;
                
                if (!isVerified) {
                    Toast.makeText(getContext(), "Profile Verification Required to request medicine.", Toast.LENGTH_LONG).show();
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).setSelectedTab(R.id.nav_profile);
                    }
                } else {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).setSelectedTab(R.id.nav_requests);
                    }
                }
            });
        });


        // ── NGO banner ─────────────────────────────────────────────
        view.findViewById(R.id.banner_ngo).setOnClickListener(v ->
                Toast.makeText(getContext(), "Finding NGO partners near you…", Toast.LENGTH_SHORT).show());

        TextView btnFindNgo = view.findViewById(R.id.btn_find_ngo);
        btnFindNgo.setOnClickListener(v -> {
            animateClick(v);
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setSelectedTab(R.id.nav_ngos);
            }
        });

        // ── Track History ──────────────────────────────────────────
        view.findViewById(R.id.tv_track_history).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setSelectedTab(R.id.nav_requests);
            }
        });

        // ── Recent Requests RecyclerView ───────────────────────────
        androidx.recyclerview.widget.RecyclerView rvRecent = view.findViewById(R.id.rv_recent_requests);
        TextView tvEmpty = view.findViewById(R.id.tv_home_empty_state);
        rvRecent.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        RequestsAdapter adapter = new RequestsAdapter();
        rvRecent.setAdapter(adapter);

        // Fetch Recent Data
        AuthRepository authRepo = new AuthRepository();
        com.google.firebase.auth.FirebaseUser currentUser = authRepo.getCurrentUser();
        
        if (currentUser != null) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("Requests")
                .whereEqualTo("requesterId", currentUser.getUid())
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(2)
                .addSnapshotListener((value, error) -> {
                    if (error != null || !isAdded()) return;
                    if (value != null && !value.isEmpty()) {
                        java.util.List<MedicineRequest> list = new java.util.ArrayList<>();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : value.getDocuments()) {
                            MedicineRequest req = doc.toObject(MedicineRequest.class);
                            if (req != null) list.add(req);
                        }
                        adapter.setRequests(list);
                        tvEmpty.setVisibility(View.GONE);
                        rvRecent.setVisibility(View.VISIBLE);
                    } else {
                        tvEmpty.setVisibility(View.VISIBLE);
                        rvRecent.setVisibility(View.GONE);
                    }
                });
        }

        // ── Notification & profile ─────────────────────────────────
        view.findViewById(R.id.btn_profile).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setSelectedTab(R.id.nav_profile);
            }
        });

        // Load Home Avatar
        if (currentUser != null) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("Users")
                .document(currentUser.getUid())
                .addSnapshotListener((doc, err) -> {
                    if (err != null || !isAdded() || getContext() == null) return;
                    if (doc != null && doc.exists()) {
                        String imageUrl = doc.getString("profileImageUrl");
                        ImageView ivHomePic = view.findViewById(R.id.iv_home_profile_pic);
                        if (ivHomePic != null && imageUrl != null) {
                            try {
                                ivHomePic.setImageURI(android.net.Uri.parse(imageUrl));
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                    ivHomePic.setImageTintList(null);
                                }
                            } catch (Exception e) {}
                        }
                    }
                });
        }
    }


    /**
     * Animates a progress fill View from 0% to targetFraction of its parent width.
     */
    private void animateProgressBar(View fillView, float targetFraction) {
        if (fillView == null) return;
        fillView.post(() -> {
            View parent = (View) fillView.getParent();
            int parentWidth = parent.getWidth();
            int targetWidth = (int) (parentWidth * targetFraction);

            ValueAnimator animator = ValueAnimator.ofInt(0, targetWidth);
            animator.setDuration(900);
            animator.setStartDelay(300);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addUpdateListener(animation -> {
                int val = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams params = fillView.getLayoutParams();
                params.width = val;
                fillView.setLayoutParams(params);
            });
            animator.start();
        });
    }

    private void fetchNgos() {
        FirebaseFirestore.getInstance().collection("NGOs")
            .whereEqualTo("verified", true)
            .get()
            .addOnSuccessListener(value -> {
                if (value != null) {
                    allNgos.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        Ngo ngo = doc.toObject(Ngo.class);
                        allNgos.add(ngo);
                    }
                }
            });
    }

    private void searchMedicines(String query) {
        if (query.trim().isEmpty()) {
            layoutSearchResults.setVisibility(View.GONE);
            return;
        }

        List<Ngo> filteredNgos = new ArrayList<>();
        String lowerQuery = query.toLowerCase().trim();

        for (Ngo ngo : allNgos) {
            Map<String, Integer> inventory = ngo.getInventory();
            if (inventory != null) {
                for (String medicine : inventory.keySet()) {
                    if (medicine.toLowerCase().contains(lowerQuery)) {
                        filteredNgos.add(ngo);
                        break;
                    }
                }
            }
        }

        layoutSearchResults.setVisibility(View.VISIBLE);
        if (filteredNgos.isEmpty()) {
            rvSearchResults.setVisibility(View.GONE);
            layoutNoMedicineFound.setVisibility(View.VISIBLE);
        } else {
            rvSearchResults.setVisibility(View.VISIBLE);
            layoutNoMedicineFound.setVisibility(View.GONE);
            searchAdapter.setNgos(filteredNgos);
        }
    }

    /** subtle scale-down click animation */
    private void animateClick(View v) {
        v.animate()
                .scaleX(0.93f)
                .scaleY(0.93f)
                .setDuration(100)
                .withEndAction(() ->
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100).start())
                .start();
    }
}
