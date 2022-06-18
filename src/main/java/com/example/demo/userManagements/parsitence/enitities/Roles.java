package com.example.demo.userManagements.parsitence.enitities;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
public class UserGroup {
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
    @OneToMany
    Set<rolePermissions> permissions = new HashSet<>();

    public UserGroup() {
    }

    public UserGroup(Long roleId) {
        this.roleId = roleId;
    }

    public UserGroup(Long roleId, String roleName, String description, String roleType, String roleStatus, LocalDateTime createdAt, String addedBy, Set<rolePermissions> permissions) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.description = description;
        this.roleType = roleType;
        this.roleStatus = roleStatus;
        this.createdAt = createdAt;
        this.addedBy = addedBy;
        this.permissions = permissions;
    }
}
