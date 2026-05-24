package com.medlink.app.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.medlink.app.R;
import com.medlink.app.fragments.DonateFragment;
import com.medlink.app.fragments.HomeFragment;
import com.medlink.app.fragments.NgoListFragment;
import com.medlink.app.fragments.ProfileFragment;
import com.medlink.app.fragments.RequestsFragment;
import com.medlink.app.repository.DataRepository;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Seed database if empty or missing fields
        android.widget.Toast.makeText(this, "Synchronizing NGO data…", android.widget.Toast.LENGTH_SHORT).show();
        new DataRepository().seedNgosIfEmpty();

        bottomNav = findViewById(R.id.bottom_nav);

        // Load home fragment by default
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment(), false);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selected = new HomeFragment();
            } else if (id == R.id.nav_requests) {
                selected = new RequestsFragment();
            } else if (id == R.id.nav_donate) {
                selected = new DonateFragment();
            } else if (id == R.id.nav_ngos) {
                selected = new NgoListFragment();
            } else if (id == R.id.nav_profile) {
                selected = new ProfileFragment();
            }

            if (selected != null) {
                loadFragment(selected, false);
                return true;
            }
            return false;
        });
    }

    public void setSelectedTab(int itemId) {
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(itemId);
        }
    }

    public void loadFragment(Fragment fragment, boolean addToBackstack) {
        androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment);
        
        if (addToBackstack) {
            transaction.addToBackStack(null);
        }
        
        transaction.commit();
    }
}
