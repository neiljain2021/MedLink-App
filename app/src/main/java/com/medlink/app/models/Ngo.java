package com.medlink.app.models;

public class Ngo implements java.io.Serializable {
    private String ngoId;
    private String name;
    private String category;
    private String location;
    private double rating;
    private int iconResId; // Using local drawable ID for now
    private boolean verified;
    private java.util.Map<String, Integer> inventory;


    public Ngo() {} // Required for Firestore

    public Ngo(String name, String category, String location, double rating, int iconResId) {
        this.name = name;
        this.category = category;
        this.location = location;
        this.rating = rating;
        this.iconResId = iconResId;
    }



    public String getNgoId() { return ngoId; }
    public void setNgoId(String ngoId) { this.ngoId = ngoId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getIconResId() { return iconResId; }
    public void setIconResId(int iconResId) { this.iconResId = iconResId; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public java.util.Map<String, Integer> getInventory() { return inventory; }
    public void setInventory(java.util.Map<String, Integer> inventory) { this.inventory = inventory; }
}
