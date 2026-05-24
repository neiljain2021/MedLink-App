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
import com.medlink.app.fragments.AdminDashboardFragment;
import com.medlink.app.fragments.AdminDonationsFragment;
import com.medlink.app.fragments.AdminNgosFragment;
import com.medlink.app.fragments.AdminRequestsFragment;
import com.medlink.app.fragments.AdminUsersFragment;
import com.medlink.app.fragments.AdminVerificationFragment;

/**
 * AdminActivity — the host activity for the entire admin panel.
 *
 * SECURITY: On launch, this activity verifies that the current Firebase user
 * has role == "admin" stored in their Firestore document.  If not, it
 * immediately redirects to MainActivity so no admin UI is ever shown to
 * regular users — even if they deep-link into this activity.
 *
 * Layout: DrawerLayout with a sidebar NavigationView on the left and a
 * FrameLayout fragment container on the right.
 */
public class AdminActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private TextView tvAdminTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        
        // Hide UI until role is verified
        View drawer = findViewById(R.id.admin_drawer_layout);
        if (drawer != null) {
            drawer.setVisibility(View.INVISIBLE);
        }

        // ── SECURITY CHECK ────────────────────────────────────────────────
        // Do not show the admin UI until we confirm the role server-side.
        verifyAdminRole();
    }

    /**
     * Reads the current user's "role" field from Firestore.
     * Only proceeds to build the UI if role == "admin".
     */
    private void verifyAdminRole() {
        com.google.firebase.auth.FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            redirectToMain("Not logged in.");
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) { redirectToMain("User not found."); return; }
                    String role = doc.getString("role");
                    if (role != null && "admin".equals(role.trim())) {
                        buildAdminUI();
                    } else {
                        redirectToMain("Access denied. Admin role required.");
                    }
                })
                .addOnFailureListener(e -> redirectToMain("Auth check failed: " + e.getMessage()));
    }

    /** Sets up the full drawer-based admin UI after role verification passes. */
    private void buildAdminUI() {
        drawerLayout = findViewById(R.id.admin_drawer_layout);
        if (drawerLayout != null) {
            drawerLayout.setVisibility(android.view.View.VISIBLE);
        }
        tvAdminTitle = findViewById(R.id.tv_admin_title);

        // Hamburger button opens/closes drawer
        ImageView btnMenu = findViewById(R.id.btn_menu);
        btnMenu.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Sidebar NavigationView item selection
        NavigationView navView = findViewById(R.id.admin_nav_view);
        navView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.START);

            int id = item.getItemId();
            if (id == R.id.admin_nav_dashboard) {
                loadFragment(new AdminDashboardFragment(), "Dashboard");
            } else if (id == R.id.admin_nav_verification) {
                loadFragment(new AdminVerificationFragment(), "Verification Center");
            } else if (id == R.id.admin_nav_users) {
                loadFragment(new AdminUsersFragment(), "User Management");
            } else if (id == R.id.admin_nav_requests) {
                loadFragment(new AdminRequestsFragment(), "Requests");
            } else if (id == R.id.admin_nav_donations) {
                loadFragment(new AdminDonationsFragment(), "Donations");
            } else if (id == R.id.admin_nav_ngos) {
                loadFragment(new AdminNgosFragment(), "NGO Management");
            } else if (id == R.id.admin_nav_logout) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
            return true;
        });

        // Default screen: Dashboard
        if (getSupportFragmentManager().findFragmentById(R.id.admin_fragment_container) == null) {
            loadFragment(new AdminDashboardFragment(), "Dashboard");
            navView.setCheckedItem(R.id.admin_nav_dashboard);
        }
    }

    /** Loads a fragment into the admin fragment container and updates the toolbar title. */
    public void loadFragment(Fragment fragment, String title) {
        if (tvAdminTitle != null) tvAdminTitle.setText(title);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.admin_fragment_container, fragment)
                .commit();
    }

    private void redirectToMain(String reason) {
        Toast.makeText(this, reason, Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    /** Close drawer on back press if it's open, otherwise default back behaviour. */
    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
