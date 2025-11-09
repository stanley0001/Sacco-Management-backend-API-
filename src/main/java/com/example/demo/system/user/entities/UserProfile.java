package com.example.demo.system.user.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "username", unique = true, nullable = false)
    private String username;
    
    @Column(name = "email", unique = true, nullable = false)
    private String email;
    
    @Column(name = "password", nullable = false)
    private String password;
    
    @Column(name = "first_name", nullable = false)
    private String firstName;
    
    @Column(name = "last_name", nullable = false)
    private String lastName;
    
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type")
    private UserType userType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private UserStatus status = UserStatus.ACTIVE;
    
    @Column(name = "branch_id")
    private Long branchId;
    
    @Column(name = "employee_id")
    private String employeeId;
    
    @Column(name = "department")
    private String department;
    
    @Column(name = "position")
    private String position;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    @Column(name = "password_expires_at")
    private LocalDateTime passwordExpiresAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "updated_by")
    private String updatedBy;
    
    public enum UserType {
        ADMIN,
        BRANCH_MANAGER,
        LOAN_OFFICER,
        TELLER,
        ACCOUNTANT,
        CUSTOMER_SERVICE,
        REGULAR_USER
    }
    
    public enum UserStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED,
        PENDING_ACTIVATION
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = UserStatus.PENDING_ACTIVATION;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Helper methods
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public boolean isActive() {
        return UserStatus.ACTIVE.equals(status);
    }
    
    public boolean isLoanOfficer() {
        return UserType.LOAN_OFFICER.equals(userType);
    }
    
    public boolean isAdmin() {
        return UserType.ADMIN.equals(userType);
    }
    
    public boolean isBranchManager() {
        return UserType.BRANCH_MANAGER.equals(userType);
    }
}
