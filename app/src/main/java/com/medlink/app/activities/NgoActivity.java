package com.medlink.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.medlink.app.R;
import com.medlink.app.fragments.NgoAcceptedDonationsFragment;
import com.medlink.app.fragments.NgoDashboardFragment;
import com.medlink.app.fragments.NgoDonationsFragment;
import com.medlink.app.fragments.NgoInventoryFragment;
import com.medlink.app.fragments.NgoProfileFragment;
import com.medlink.app.fragments.NgoRequestsFragment;

public class NgoActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private TextView tvNgoTitle;
    private String currentNgoName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ngo);

        View drawer = findViewById(R.id.ngo_drawer_layout);
        if (drawer != null) {
            drawer.setVisibility(View.INVISIBLE);
        }

        verifyNgoRole();
    }

    private void verifyNgoRole() {
        com.google.firebase.auth.FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            redirectToLogin("Not logged in.");
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) { redirectToLogin("User not found."); return; }
                    String role = doc.getString("role");
                    currentNgoName = doc.getString("ngoName");

                    if (role != null && "ngo".equals(role.trim())) {
                        // Check if the NGO is verified in the "NGOs" collection
                        checkNgoVerification(user.getUid());
                    } else {
                        redirectToLogin("Access denied. NGO role required.");
                    }
                })
                .addOnFailureListener(e -> redirectToLogin("Auth check failed: " + e.getMessage()));
    }

    private void checkNgoVerification(String uid) {
        FirebaseFirestore.getInstance()
                .collection("NGOs")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    boolean isVerified = doc.exists() && Boolean.TRUE.equals(doc.getBoolean("verified"));
                    
                    if (isVerified) {
                        buildNgoUI();
                    } else {
                        showPendingApprovalUI();
                    }
                })
                .addOnFailureListener(e -> redirectToLogin("Verification check failed: " + e.getMessage()));
    }

    private void showPendingApprovalUI() {
        setContentView(R.layout.activity_ngo); // Re-inflate to reset if needed
        View pendingLayout = findViewById(R.id.layout_ngo_pending);
        TextView tvNgoName = findViewById(R.id.tv_pending_ngo_name);
        View btnLogout = findViewById(R.id.btn_pending_logout);

        if (pendingLayout != null) pendingLayout.setVisibility(View.VISIBLE);
        if (tvNgoName != null) tvNgoName.setText(currentNgoName);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                redirectToLogin("Signed out");
            });
        }
        
        // Hide sidebar and content
        View sidebar = findViewById(R.id.ngo_nav_view);
        View content = findViewById(R.id.ngo_fragment_container);
        View toolbar = findViewById(R.id.ngo_toolbar);
        if (sidebar != null) sidebar.setVisibility(View.GONE);
        if (content != null) content.setVisibility(View.GONE);
        if (toolbar != null) toolbar.setVisibility(View.GONE);
        
        if (drawerLayout != null) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }

    private void redirectToLogin(String message) {
        if (message != null && !message.isEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void buildNgoUI() {
        drawerLayout = findViewById(R.id.ngo_drawer_layout);
        if (drawerLayout != null) {
            drawerLayout.setVisibility(View.VISIBLE);
        }
        tvNgoTitle = findViewById(R.id.tv_ngo_title);

        ImageView btnMenu = findViewById(R.id.btn_menu_ngo);
        NavigationView navView = findViewById(R.id.ngo_nav_view);

        if (drawerLayout != null && btnMenu != null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();

            btnMenu.setOnClickListener(v -> {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }

        if (navView != null) {
            View headerView = navView.getHeaderView(0);
            if (headerView != null) {
                TextView tvHeaderName = headerView.findViewById(R.id.nav_header_ngo_name);
                TextView tvHeaderEmail = headerView.findViewById(R.id.nav_header_ngo_email);
                if (tvHeaderName != null) tvHeaderName.setText(currentNgoName != null ? currentNgoName : "NGO Portal");
                if (tvHeaderEmail != null && FirebaseAuth.getInstance().getCurrentUser() != null) {
                    tvHeaderEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                }
            }

            navView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                Fragment selectedFragment = null;
                String title = "NGO Dashboard";

                if (id == R.id.ngo_nav_dashboard) {
                    selectedFragment = new NgoDashboardFragment();
                    title = "Dashboard";
                } else if (id == R.id.ngo_nav_incoming) {
                    selectedFragment = new NgoRequestsFragment("PENDING");
                    title = "Incoming Requests";
                } else if (id == R.id.ngo_nav_accepted) {
                    selectedFragment = new NgoRequestsFragment("ACCEPTED");
                    title = "Accepted Requests";
                } else if (id == R.id.ngo_nav_completed) {
                    selectedFragment = new NgoRequestsFragment("COMPLETED");
                    title = "Completed Requests";
                } else if (id == R.id.ngo_nav_available_donations) {
                    selectedFragment = new NgoDonationsFragment();
                    title = "Available Donations";
                } else if (id == R.id.ngo_nav_accepted_donations) {
                    selectedFragment = new NgoAcceptedDonationsFragment();
                    title = "Accepted Donations";
                } else if (id == R.id.ngo_nav_inventory) {
                    selectedFragment = new NgoInventoryFragment();
                    title = "Manage Inventory";
                } else if (id == R.id.ngo_nav_profile) {
                    selectedFragment = new NgoProfileFragment();
                    title = "NGO Profile";
                } else if (id == R.id.ngo_nav_logout) {
                    FirebaseAuth.getInstance().signOut();
                    redirectToLogin("Logged out successfully");
                    return true;
                }

                if (selectedFragment != null) {
                    loadFragment(selectedFragment, title);
                }
                
                if (drawerLayout != null) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                return true;
            });
        }

        // Load Default Fragment
        if (getSupportFragmentManager().findFragmentById(R.id.ngo_fragment_container) == null) {
            loadFragment(new NgoDashboardFragment(), "Dashboard");
            navView.setCheckedItem(R.id.ngo_nav_dashboard);
        }
    }

    public void setSelectedTab(int id) {
        NavigationView navView = findViewById(R.id.ngo_nav_view);
        if (navView != null) {
            navView.setCheckedItem(id);
            // Simulate item selection
            android.view.MenuItem item = navView.getMenu().findItem(id);
            if (item != null) {
                navView.getMenu().performIdentifierAction(id, 0);
            }
        }
    }

    public void loadFragment(Fragment fragment, String title) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.ngo_fragment_container, fragment)
                .commit();
        if (tvNgoTitle != null) {
            tvNgoTitle.setText(title);
        }
    }
    
    public String getCurrentNgoName() {
        return currentNgoName;
    }
}
