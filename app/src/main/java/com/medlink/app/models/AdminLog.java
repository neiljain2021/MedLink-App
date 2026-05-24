package com.medlink.app.models;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

/**
 * AdminLog — represents one admin action stored in the Firestore "AdminLogs" collection.
 * Every CRUD operation performed by the admin is recorded here for audit purposes.
 */
public class AdminLog {

    private String logId;
    private String adminUid;   // UID of the admin who performed the action
    private String action;     // e.g. "DELETE_USER", "APPROVE_REQUEST", "BLOCK_USER"
    private String targetType; // e.g. "User", "Request", "Donation", "NGO"
    private String targetId;   // Firestore document ID of the affected record
    private String details;    // Optional human-readable detail string

    @ServerTimestamp
    private Date timestamp;

    public AdminLog() {} // Required for Firestore deserialization

    public AdminLog(String adminUid, String action, String targetType, String targetId, String details) {
        this.adminUid   = adminUid;
        this.action     = action;
        this.targetType = targetType;
        this.targetId   = targetId;
        this.details    = details;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────

    public String getLogId()                    { return logId; }
    public void   setLogId(String logId)        { this.logId = logId; }

    public String getAdminUid()                 { return adminUid; }
    public void   setAdminUid(String adminUid)  { this.adminUid = adminUid; }

    public String getAction()                   { return action; }
    public void   setAction(String action)      { this.action = action; }

    public String getTargetType()               { return targetType; }
    public void   setTargetType(String t)       { this.targetType = t; }

    public String getTargetId()                 { return targetId; }
    public void   setTargetId(String targetId)  { this.targetId = targetId; }

    public String getDetails()                  { return details; }
    public void   setDetails(String details)    { this.details = details; }

    public Date getTimestamp()                  { return timestamp; }
    public void setTimestamp(Date timestamp)    { this.timestamp = timestamp; }
}
