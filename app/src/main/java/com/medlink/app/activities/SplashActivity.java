package com.medlink.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.medlink.app.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences("MedLinkPrefs", MODE_PRIVATE);
            boolean isFirstRun = prefs.getBoolean("isFirstRun", true);

            Intent intent;
            if (isFirstRun) {
                intent = new Intent(SplashActivity.this, OnboardingActivity.class);
            } else if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null) {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            } else {
                // User is logged in, check role for redirection
                com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        Intent targetIntent;
                        if (documentSnapshot.exists()) {
                            String role = documentSnapshot.getString("role");
                            if (role != null && "ngo".equals(role.trim())) {
                                targetIntent = new Intent(SplashActivity.this, NgoActivity.class);
                            } else if (role != null && "admin".equals(role.trim())) {
                                targetIntent = new Intent(SplashActivity.this, AdminActivity.class);
                            } else {
                                targetIntent = new Intent(SplashActivity.this, MainActivity.class);
                            }
                        } else {
                            targetIntent = new Intent(SplashActivity.this, MainActivity.class);
                        }
                        startActivity(targetIntent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        finish();
                    });
                return;
            }
            startActivity(intent);
            finish();
        }, 1500); // 1.5 second splash
    }
}
