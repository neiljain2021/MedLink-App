package com.medlink.app.models;

public class ActivityItem {
    public String title;
    public String description;
    public long timestamp;
    public String type; // "CLAIM" or "REQUEST"
    
    public ActivityItem() {}
    
    public ActivityItem(String title, String description, long timestamp, String type) {
        this.title = title;
        this.description = description;
        this.timestamp = timestamp;
        this.type = type;
    }
}
