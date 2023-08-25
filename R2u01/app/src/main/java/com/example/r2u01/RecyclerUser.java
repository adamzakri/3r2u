package com.example.r2u01;

public class RecyclerUser {
    private String uid;
    private String username;
    private String imgUrl;
    private String userType;

    public RecyclerUser() {
        // Default constructor required for Firebase
    }

    public RecyclerUser(String uid, String username, String imgUrl) {
        this.uid = uid;
        this.username = username;
        this.imgUrl = imgUrl;
        this.userType = userType;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
