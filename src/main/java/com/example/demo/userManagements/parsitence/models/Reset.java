package com.example.demo.userManagements.parsitence.models;

public class Reset {
    private String email;
    private String reason;

    // No-args constructor required for deserialization
    public Reset() {
    }

    public Reset(String email, String reason) {
        this.email = email;
        this.reason = reason;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
