package com.medlink.app.models;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class MedicineRequest {
    private String requestId;
    private String requesterId;
    private String medicineName;
    private String targetNgo;
    private String quantity;
    private String status;
    private java.util.List<String> rejectedBy = new java.util.ArrayList<>();
    @ServerTimestamp
    private Date timestamp;

    public MedicineRequest() {} // Required for Firestore

    public MedicineRequest(String requesterId, String medicineName, String targetNgo, String quantity) {
        this.requesterId = requesterId;
        this.medicineName = medicineName;
        this.targetNgo = targetNgo;
        this.quantity = quantity;
        this.status = "PENDING";
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getRequesterId() { return requesterId; }
    public void setRequesterId(String requesterId) { this.requesterId = requesterId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getTargetNgo() { return targetNgo; }
    public void setTargetNgo(String targetNgo) { this.targetNgo = targetNgo; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public java.util.List<String> getRejectedBy() { return rejectedBy; }
    public void setRejectedBy(java.util.List<String> rejectedBy) { this.rejectedBy = rejectedBy; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
