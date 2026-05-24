package com.medlink.app.models;

public class User {
    private String uid;
    private String name;
    private String email;
    private int livesHelped;
    private int medicinesDonated;
    private String phone;
    private String country;
    private String state;
    private String profileImageUrl;
    private boolean verified;
    private String role; // "admin" or "user" or "ngo"
    private String ngoName; // To link an NGO user to their targetNgo requests

    // Default constructor required for calls to DataSnapshot.getValue(User.class)
    public User() {
    }

    public User(String uid, String name, String email) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.livesHelped = 0;
        this.medicinesDonated = 0;
        this.role = "user";
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getLivesHelped() {
        return livesHelped;
    }

    public void setLivesHelped(int livesHelped) {
        this.livesHelped = livesHelped;
    }

    public int getMedicinesDonated() {
        return medicinesDonated;
    }

    public void setMedicinesDonated(int medicinesDonated) {
        this.medicinesDonated = medicinesDonated;
    }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getNgoName() { return ngoName; }
    public void setNgoName(String ngoName) { this.ngoName = ngoName; }
}
