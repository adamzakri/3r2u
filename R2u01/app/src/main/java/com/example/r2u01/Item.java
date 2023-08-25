package com.example.r2u01;

public class Item {
    private String itemId;
    private String title;
    private String description;
    private String imgUrl;
    private double latitude;
    private double longitude;
    private String ownerUid;

    public Item() {
        // Default constructor required for Firebase
    }

    public Item(String itemId, String title, String description, String imgUrl, double latitude, double longitude,String ownerUid) {
        this.itemId = itemId;
        this.title = title;
        this.description = description;
        this.imgUrl = imgUrl;
        this.latitude = latitude;
        this.longitude = longitude;
        this.ownerUid = ownerUid;
    }

    public String getOwnerUid() {
        return ownerUid;
    }

    public void setOwnerUid(String ownerUid) {
        this.ownerUid = ownerUid;
    }

    public String getItemId() {
        return itemId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
