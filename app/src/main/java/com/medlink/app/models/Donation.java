package com.medlink.app.models;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Donation {
    private String donationId;
    private String donorId;
    private String donorName;
    private String medicineName;
    private String quantity;
    private String status;
    private String targetNgo;
    @ServerTimestamp
    private Date timestamp;

    public Donation() {} // Required for Firestore

    public Donation(String donorId, String donorName, String medicineName, String quantity) {
        this.donorId = donorId;
        this.donorName = donorName;
        this.medicineName = medicineName;
        this.quantity = quantity;
        this.status = "VERIFYING";
        this.targetNgo = "Any Verified NGO";
    }

    public String getDonationId() { return donationId; }
    public void setDonationId(String donationId) { this.donationId = donationId; }

    public String getDonorId() { return donorId; }
    public void setDonorId(String donorId) { this.donorId = donorId; }
    
    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTargetNgo() { return targetNgo; }
    public void setTargetNgo(String targetNgo) { this.targetNgo = targetNgo; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
