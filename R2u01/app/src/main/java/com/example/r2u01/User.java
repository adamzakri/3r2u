package com.example.r2u01;

public class User {
    private String username;
    private String email;
    private String userType;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email, String userType) {
        this.username = username;
        this.email = email;
        this.userType = userType;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getUserType() {
        return userType;
    }
}
