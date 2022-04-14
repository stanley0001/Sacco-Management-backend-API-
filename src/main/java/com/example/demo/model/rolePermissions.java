package com.example.demo.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class rolePermissions  {
    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String roleId;
    private String permission;
    private String permissionStatus;
    private LocalDateTime createdAt;
    private String addedBy;
    private String ipAddress;

    public rolePermissions() {
    }

    public rolePermissions(Long id) {
        this.id = id;
    }

    public rolePermissions(String roleId, String permission, String permissionStatus, LocalDateTime createdAt, String addedBy, String ipAddress) {
        this.roleId = roleId;
        this.permission = permission;
        this.permissionStatus = permissionStatus;
        this.createdAt = createdAt;
        this.addedBy = addedBy;
        this.ipAddress = ipAddress;
    }

    public rolePermissions(Long id, String roleId, String permission, String permissionStatus, LocalDateTime createdAt, String addedBy, String ipAddress) {
        this.id = id;
        this.roleId = roleId;
        this.permission = permission;
        this.permissionStatus = permissionStatus;
        this.createdAt = createdAt;
        this.addedBy = addedBy;
        this.ipAddress = ipAddress;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getPermissionStatus() {
        return permissionStatus;
    }

    public void setPermissionStatus(String permissionStatus) {
        this.permissionStatus = permissionStatus;
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
        return "rolePermissions{" +
                "id=" + id +
                ", roleId='" + roleId + '\'' +
                ", permission='" + permission + '\'' +
                ", permissionStatus='" + permissionStatus + '\'' +
                ", createdAt=" + createdAt +
                ", addedBy='" + addedBy + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                '}';
    }
}
