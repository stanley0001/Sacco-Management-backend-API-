package com.example.demo.userManagements.parsitence.enitities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class rolePermissions  {
    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String value;
    private String permissionStatus;
    private LocalDateTime createdAt;
    private String addedBy;
    private String ipAddress;


    public rolePermissions() {
    }

    public rolePermissions(Long id) {
        this.id = id;
    }

    public rolePermissions(Long id, String name, String value, String permissionStatus, LocalDateTime createdAt, String addedBy, String ipAddress) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.permissionStatus = permissionStatus;
        this.createdAt = createdAt;
        this.addedBy = addedBy;
        this.ipAddress = ipAddress;
    }

    @Override
    public String toString() {
        return "rolePermissions{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", permissionStatus='" + permissionStatus + '\'' +
                ", createdAt=" + createdAt +
                ", addedBy='" + addedBy + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                '}';
    }
}
