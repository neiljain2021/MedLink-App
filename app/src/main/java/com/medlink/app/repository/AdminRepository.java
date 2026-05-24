package com.medlink.app.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.medlink.app.models.AdminLog;
import com.medlink.app.models.Ngo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AdminRepository — central Firestore access layer for all admin panel operations.
 *
 * Collections touched:
 *   Users, Requests, Donations, NGOs, AdminLogs
 *
 * Soft delete strategy:
 *   Sets isDeleted=true + deletedAt timestamp on the document.
 *   Normal app queries do NOT filter by isDeleted, so deleted items are effectively hidden
 *   (they don't appear in user-facing lists which use the regular queries).
 *   Admin panel shows ALL items (deleted or not) so the admin can restore them.
 */
public class AdminRepository {

    private final FirebaseFirestore db;
    private final String adminUid;

    // Firestore collection names
    private static final String COL_USERS     = "Users";
    private static final String COL_REQUESTS  = "Requests";
    private static final String COL_DONATIONS = "Donations";
    private static final String COL_NGOS      = "NGOs";
    private static final String COL_LOGS      = "AdminLogs";

    // Page size for paginated queries
    public static final int PAGE_SIZE = 20;

    public AdminRepository() {
        db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        adminUid = (auth.getCurrentUser() != null) ? auth.getCurrentUser().getUid() : "unknown";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Generic callback interface
    // ─────────────────────────────────────────────────────────────────────────

    public interface Callback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DASHBOARD STATS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Fetches document counts for all 4 main collections simultaneously.
     * Returns a Map with keys: "users", "requests", "donations", "ngos"
     */
    public void getDashboardStats(Callback<Map<String, Integer>> callback) {
        Map<String, Integer> stats = new HashMap<>();
        final int[] pending = {4}; // Number of async tasks to wait for

        Runnable checkDone = () -> {
            pending[0]--;
            if (pending[0] == 0) callback.onSuccess(stats);
        };

        db.collection(COL_USERS).get().addOnSuccessListener(s -> {
            stats.put("users", s.size()); checkDone.run();
        }).addOnFailureListener(e -> { stats.put("users", 0); checkDone.run(); });

        db.collection(COL_REQUESTS).get().addOnSuccessListener(s -> {
            stats.put("requests", s.size()); checkDone.run();
        }).addOnFailureListener(e -> { stats.put("requests", 0); checkDone.run(); });

        db.collection(COL_DONATIONS).get().addOnSuccessListener(s -> {
            stats.put("donations", s.size()); checkDone.run();
        }).addOnFailureListener(e -> { stats.put("donations", 0); checkDone.run(); });

        db.collection(COL_NGOS).get().addOnSuccessListener(s -> {
            stats.put("ngos", s.size()); checkDone.run();
        }).addOnFailureListener(e -> { stats.put("ngos", 0); checkDone.run(); });
    }

    /**
     * Fetches the 10 most recent admin log entries for the activity feed.
     */
    public void getRecentActivity(Callback<List<AdminLog>> callback) {
        db.collection(COL_LOGS)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(snap -> {
                    List<AdminLog> logs = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        AdminLog log = doc.toObject(AdminLog.class);
                        log.setLogId(doc.getId());
                        logs.add(log);
                    }
                    callback.onSuccess(logs);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Counts requests by status for the dashboard bar chart.
     * Returns Map: { "PENDING": n, "PROCESSING": n, "READY": n, "REJECTED": n }
     */
    public void getRequestStatusBreakdown(Callback<Map<String, Integer>> callback) {
        db.collection(COL_REQUESTS).get()
                .addOnSuccessListener(snap -> {
                    Map<String, Integer> counts = new HashMap<>();
                    counts.put("PENDING", 0);
                    counts.put("PROCESSING", 0);
                    counts.put("READY", 0);
                    counts.put("REJECTED", 0);
                    for (QueryDocumentSnapshot doc : snap) {
                        String status = doc.getString("status");
                        if (status != null && counts.containsKey(status)) {
                            counts.put(status, counts.get(status) + 1);
                        }
                    }
                    callback.onSuccess(counts);
                })
                .addOnFailureListener(callback::onFailure);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // USER MANAGEMENT
    // ─────────────────────────────────────────────────────────────────────────

    /** Fetch all users (paginated - first page). */
    public void getAllUsers(String roleFilter, Callback<List<DocumentSnapshot>> callback) {
        db.collection(COL_USERS)
                .limit(100) // Fetch a larger batch to filter locally
                .get()
                .addOnSuccessListener(snap -> {
                    List<DocumentSnapshot> filtered = new java.util.ArrayList<>();
                    for (DocumentSnapshot doc : snap) {
                        String role = doc.getString("role");
                        // If we want "user" role: include explicit "user" OR null (legacy)
                        if ("user".equals(roleFilter)) {
                            if (role == null || "user".equals(role)) {
                                filtered.add(doc);
                            }
                        } else {
                            // Otherwise match the specific role (e.g. "ngo")
                            if (roleFilter.equals(role)) {
                                filtered.add(doc);
                            }
                        }
                    }
                    callback.onSuccess(filtered);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /** Fetch next page. (Simplified for local filtering) */
    public void getUsersAfter(String roleFilter, DocumentSnapshot lastDoc, Callback<List<DocumentSnapshot>> callback) {
        // For simplicity with local filtering, we just reload or use larger initial fetch
        getAllUsers(roleFilter, callback);
    }

    /** Update specific user fields. */
    public void updateUser(String uid, Map<String, Object> fields, Callback<Void> callback) {
        db.collection(COL_USERS).document(uid)
                .update(fields)
                .addOnSuccessListener(v -> {
                    logAction("UPDATE_USER", "User", uid, "Updated fields: " + fields.keySet());
                    callback.onSuccess(null);
                })
                .addOnFailureListener(callback::onFailure);
    }


    /** Toggle blocked state on a user. */
    public void setUserBlocked(String uid, boolean blocked, Callback<Void> callback) {
        Map<String, Object> fields = new HashMap<>();
        fields.put("blocked", blocked);
        db.collection(COL_USERS).document(uid)
                .update(fields)
                .addOnSuccessListener(v -> {
                    logAction(blocked ? "BLOCK_USER" : "UNBLOCK_USER", "User", uid, "blocked=" + blocked);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /** Soft-delete a user by setting isDeleted=true + deletedAt timestamp. */
    public void softDeleteUser(String uid, Callback<Void> callback) {
        Map<String, Object> fields = new HashMap<>();
        fields.put("isDeleted", true);
        fields.put("deletedAt", new Date());
        db.collection(COL_USERS).document(uid)
                .update(fields)
                .addOnSuccessListener(v -> {
                    logAction("DELETE_USER", "User", uid, "Soft deleted");
                    callback.onSuccess(null);
                })
                .addOnFailureListener(callback::onFailure);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REQUEST MANAGEMENT
    // ─────────────────────────────────────────────────────────────────────────

    /** Fetch all requests, optionally filtered by status ("ALL" means no filter). */
    public void getAllRequests(String statusFilter, Callback<List<DocumentSnapshot>> callback) {
        Query query = db.collection(COL_REQUESTS).orderBy("timestamp", Query.Direction.DESCENDING);
        if (statusFilter != null && !statusFilter.equals("ALL")) {
            query = query.whereEqualTo("status", statusFilter);
        }
        query.limit(50).get()
                .addOnSuccessListener(snap -> callback.onSuccess(snap.getDocuments()))
                .addOnFailureListener(callback::onFailure);
    }

    /** Update the status field on a request. */
    public void updateRequestStatus(String requestId, String newStatus, Callback<Void> callback) {
        db.collection(COL_REQUESTS).document(requestId)
                .update("status", newStatus)
                .addOnSuccessListener(v -> {
                    logAction("UPDATE_REQUEST_STATUS", "Request", requestId, "status=" + newStatus);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /** Soft-delete a request. */
    public void softDeleteRequest(String requestId, Callback<Void> callback) {
        Map<String, Object> fields = new HashMap<>();
        fields.put("isDeleted", true);
        fields.put("deletedAt", new Date());
        db.collection(COL_REQUESTS).document(requestId)
                .update(fields)
                .addOnSuccessListener(v -> {
                    logAction("DELETE_REQUEST", "Request", requestId, "Soft deleted");
                    callback.onSuccess(null);
                })
                .addOnFailureListener(callback::onFailure);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DONATION MANAGEMENT
    // ─────────────────────────────────────────────────────────────────────────

    /** Fetch all donations, optionally filtered by status. */
    public void getAllDonations(String statusFilter, Callback<List<DocumentSnapshot>> callback) {
        Query query = db.collection(COL_DONATIONS).orderBy("timestamp", Query.Direction.DESCENDING);
        if (statusFilter != null && !statusFilter.equals("ALL")) {
            query = query.whereEqualTo("status", statusFilter);
        }
        query.limit(50).get()
                .addOnSuccessListener(snap -> callback.onSuccess(snap.getDocuments()))
                .addOnFailureListener(callback::onFailure);
    }

    /** Update the status on a donation. */
    public void updateDonationStatus(String donationId, String newStatus, Callback<Void> callback) {
        db.collection(COL_DONATIONS).document(donationId)
                .update("status", newStatus)
                .addOnSuccessListener(v -> {
                    logAction("UPDATE_DONATION_STATUS", "Donation", donationId, "status=" + newStatus);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /** Soft-delete a donation. */
    public void softDeleteDonation(String donationId, Callback<Void> callback) {
        Map<String, Object> fields = new HashMap<>();
        fields.put("isDeleted", true);
        fields.put("deletedAt", new Date());
        db.collection(COL_DONATIONS).document(donationId)
                .update(fields)
                .addOnSuccessListener(v -> {
                    logAction("DELETE_DONATION", "Donation", donationId, "Soft deleted");
                    callback.onSuccess(null);
                })
                .addOnFailureListener(callback::onFailure);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NGO MANAGEMENT
    // ─────────────────────────────────────────────────────────────────────────

    /** Fetch all NGOs. */
    public void getAllNgos(Callback<List<DocumentSnapshot>> callback) {
        db.collection(COL_NGOS)
                .orderBy("name")
                .get()
                .addOnSuccessListener(snap -> callback.onSuccess(snap.getDocuments()))
                .addOnFailureListener(callback::onFailure);
    }

    /** Add a new NGO document. */
    public void addNgo(Ngo ngo, Callback<Void> callback) {
        db.collection(COL_NGOS).add(ngo)
                .addOnSuccessListener(ref -> {
                    ngo.setNgoId(ref.getId());
                    ref.set(ngo);
                    logAction("ADD_NGO", "NGO", ref.getId(), "Added: " + ngo.getName());
                    callback.onSuccess(null);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /** Update specific NGO fields. */
    public void updateNgo(String ngoId, Map<String, Object> fields, Callback<Void> callback) {
        db.collection(COL_NGOS).document(ngoId)
                .update(fields)
                .addOnSuccessListener(v -> {
                    logAction("UPDATE_NGO", "NGO", ngoId, "Updated: " + fields.keySet());
                    callback.onSuccess(null);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /** Toggle the verified flag on an NGO. */
    public void toggleNgoVerified(String ngoId, boolean verified, Callback<Void> callback) {
        db.collection(COL_NGOS).document(ngoId)
                .update("verified", verified)
                .addOnSuccessListener(v -> {
                    logAction("TOGGLE_NGO_VERIFIED", "NGO", ngoId, "verified=" + verified);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /** Soft-delete an NGO. */
    public void softDeleteNgo(String ngoId, Callback<Void> callback) {
        Map<String, Object> fields = new HashMap<>();
        fields.put("isDeleted", true);
        fields.put("deletedAt", new Date());
        db.collection(COL_NGOS).document(ngoId)
                .update(fields)
                .addOnSuccessListener(v -> {
                    logAction("DELETE_NGO", "NGO", ngoId, "Soft deleted");
                    callback.onSuccess(null);
                })
                .addOnFailureListener(callback::onFailure);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AUDIT LOGGING
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Write an audit log entry to AdminLogs collection.
     * Called automatically by every mutating operation above.
     */
    public void logAction(String action, String targetType, String targetId, String details) {
        AdminLog log = new AdminLog(adminUid, action, targetType, targetId, details);
        db.collection(COL_LOGS).add(log)
                .addOnSuccessListener(ref -> {
                    // Update the logId field inside the document itself
                    ref.update("logId", ref.getId());
                })
                .addOnFailureListener(e -> {
                    // Logging failure is non-fatal — silently ignore
                });
    }
}
