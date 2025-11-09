package com.example.demo.system.userManagements.parsitence.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class loginHistory  {



    @Id
    @Column(unique = true,updatable = false,nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String UserId;
    private LocalDateTime loginTime;
    private String loginStatus;
    private String ipAddress;
    private String geoLocation;

    public loginHistory() {
    }

    public loginHistory(Long id) {
        this.id = id;
    }

    public loginHistory(String userId, LocalDateTime loginTime, String loginStatus, String ipAddress, String geoLocation) {
        UserId = userId;
        this.loginTime = loginTime;
        this.loginStatus = loginStatus;
        this.ipAddress = ipAddress;
        this.geoLocation = geoLocation;
    }

    public loginHistory(Long id, String userId, LocalDateTime loginTime, String loginStatus, String ipAddress, String geoLocation) {
        this.id = id;
        UserId = userId;
        this.loginTime = loginTime;
        this.loginStatus = loginStatus;
        this.ipAddress = ipAddress;
        this.geoLocation = geoLocation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }

    public String getLoginStatus() {
        return loginStatus;
    }

    public void setLoginStatus(String loginStatus) {
        this.loginStatus = loginStatus;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(String geoLocation) {
        this.geoLocation = geoLocation;
    }

    @Override
    public String toString() {
        return "loginHistory{" +
                "id=" + id +
                ", UserId='" + UserId + '\'' +
                ", loginTime=" + loginTime +
                ", loginStatus='" + loginStatus + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", geoLocation='" + geoLocation + '\'' +
                '}';
    }
}
