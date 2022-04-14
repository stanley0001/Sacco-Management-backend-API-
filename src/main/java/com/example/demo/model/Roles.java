package com.example.demo.model;


import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Roles  {
    @Id
    @Column(nullable = false,unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long roleId;
    private String roleName;
    private String description;
    private String roleType;
    private String roleStatus;
    private LocalDateTime createdAt;
    private String addedBy;
    private String ipAddress;

    public Roles() {
    }

    public Roles(Long roleId) {
        this.roleId = roleId;
    }

    public Roles(String roleName, String description, String roleType, String roleStatus, LocalDateTime createdAt, String addedBy, String ipAddress) {
        this.roleName = roleName;
        this.description = description;
        this.roleType = roleType;
        this.roleStatus = roleStatus;
        this.createdAt = createdAt;
        this.addedBy = addedBy;
        this.ipAddress = ipAddress;
    }

    public Roles(Long roleId, String roleName, String description, String roleType, String roleStatus, LocalDateTime createdAt, String addedBy, String ipAddress) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.description = description;
        this.roleType = roleType;
        this.roleStatus = roleStatus;
        this.createdAt = createdAt;
        this.addedBy = addedBy;
        this.ipAddress = ipAddress;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRoleType() {
        return roleType;
    }

    public void setRoleType(String roleType) {
        this.roleType = roleType;
    }

    public String getRoleStatus() {
        return roleStatus;
    }

    public void setRoleStatus(String roleStatus) {
        this.roleStatus = roleStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Override
    public String toString() {
        return "Roles{" +
                "roleId=" + roleId +
                ", roleName='" + roleName + '\'' +
                ", description='" + description + '\'' +
                ", roleType='" + roleType + '\'' +
                ", roleStatus='" + roleStatus + '\'' +
                ", createdAt=" + createdAt +
                ", addedBy='" + addedBy + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                '}';
    }
}
