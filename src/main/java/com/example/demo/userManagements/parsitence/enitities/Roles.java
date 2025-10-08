package com.example.demo.userManagements.parsitence.enitities;


import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
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
    @ManyToMany(fetch =FetchType.EAGER)
    private List<rolePermissions> permissions =new ArrayList<>();


    public Roles() {
    }

    public Roles(Long roleId) {
        this.roleId = roleId;
    }

    public Roles(Long roleId, String roleName, String description, String roleType, String roleStatus, LocalDateTime createdAt, String addedBy, List<rolePermissions> permissions) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.description = description;
        this.roleType = roleType;
        this.roleStatus = roleStatus;
        this.createdAt = createdAt;
        this.addedBy = addedBy;
        this.permissions = permissions;
    }

    public Long getId() {
        return roleId;
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
                ", permissions=" + permissions +
                '}';
    }
}
