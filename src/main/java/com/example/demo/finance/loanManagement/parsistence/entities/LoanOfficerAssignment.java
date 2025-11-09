package com.example.demo.finance.loanManagement.parsistence.entities;

import com.example.demo.erp.customerManagement.parsistence.entities.Customer;
import com.example.demo.system.userManagements.parsitence.enitities.Users;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity to track loan officer assignments to clients
 * Allows loan officers to manage their portfolio
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "loan_officer_assignments")
public class LoanOfficerAssignment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_officer_id", nullable = false)
    private Users loanOfficer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @Column(name = "assignment_date")
    private LocalDateTime assignmentDate;
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "county")
    private String county; // For credit manager filtering
    
    @Column(name = "branch_code")
    private String branchCode;
    
    @Column(name = "assigned_by")
    private String assignedBy;
    
    @Column(name = "notes")
    private String notes;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        assignmentDate = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
