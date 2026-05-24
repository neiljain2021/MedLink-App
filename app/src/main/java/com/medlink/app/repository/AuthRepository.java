package com.medlink.app.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.medlink.app.models.Ngo;
import com.medlink.app.models.User;
import com.medlink.app.R;

// AdminRoleCallback is used to check if the current user has an admin role

public class AuthRepository {

    private final FirebaseAuth mAuth;
    private final FirebaseFirestore mFirestore;

    public AuthRepository() {
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public void logout() {
        mAuth.signOut();
    }

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(Exception e);
    }

    public void login(String email, String password, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> callback.onSuccess(authResult.getUser()))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Reads the "role" field from the current user's Firestore document.
     * Calls back with the role string (e.g. "admin", "user", or null if not set).
     */
    public void fetchUserRole(AdminRoleCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) { callback.onResult(null); return; }
        mFirestore.collection("Users").document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        callback.onResult(doc.getString("role"));
                    } else {
                        callback.onResult(null);
                    }
                })
                .addOnFailureListener(e -> callback.onResult(null));
    }

    public interface VerificationCallback {
        void onResult(boolean isVerified);
    }

    public void isUserVerified(VerificationCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) { callback.onResult(false); return; }
        mFirestore.collection("Users").document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Boolean verified = doc.getBoolean("verified");
                        callback.onResult(verified != null && verified);
                    } else {
                        callback.onResult(false);
                    }
                })
                .addOnFailureListener(e -> callback.onResult(false));
    }

    /** Convenience interface for role result. */
    public interface AdminRoleCallback {
        void onResult(String role); // role will be "admin" or null/other
    }

    public void register(String name, String email, String password, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser != null) {
                        // Create User profile in Firestore
                        User user = new User(firebaseUser.getUid(), name, email);
                        mFirestore.collection("Users").document(firebaseUser.getUid())
                                .set(user)
                                .addOnSuccessListener(aVoid -> callback.onSuccess(firebaseUser))
                                .addOnFailureListener(callback::onFailure);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void registerNgo(String ngoName, String email, String password, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser != null) {
                        User user = new User(firebaseUser.getUid(), ngoName, email);
                        user.setRole("ngo");
                        user.setNgoName(ngoName); // Link this auth user to the NGO entity
                        
                        mFirestore.collection("Users").document(firebaseUser.getUid())
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    // Create public NGO profile for app-wide visibility
                                    Ngo ngoEntry = new Ngo(ngoName, "General Medical Support", "Location Not Set", 5.0, R.drawable.ic_ngo_placeholder);
                                    ngoEntry.setNgoId(firebaseUser.getUid());
                                    ngoEntry.setVerified(false);
                                    
                                    mFirestore.collection("NGOs").document(firebaseUser.getUid())
                                            .set(ngoEntry)
                                            .addOnSuccessListener(aVoid2 -> callback.onSuccess(firebaseUser))
                                            .addOnFailureListener(callback::onFailure);
                                })
                                .addOnFailureListener(callback::onFailure);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }
}
