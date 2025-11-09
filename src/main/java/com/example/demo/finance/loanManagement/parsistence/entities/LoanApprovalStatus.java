package com.example.demo.finance.loanManagement.parsistence.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Loan Approval Status
 * Tracks current approval status and level for a loan application
 */
@Entity
@Table(name = "loan_approval_status")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApprovalStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long loanApplicationId;

    @Column(nullable = false)
    private Long workflowConfigId;

    @Column(nullable = false)
    private Integer currentLevel = 1;

    @Column(nullable = false)
    private Integer totalLevels;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING_LEVEL_1;

    // Number of approvals received at current level
    @Column(nullable = false)
    private Integer approvalsAtCurrentLevel = 0;

    // Number of approvals required at current level
    @Column(nullable = false)
    private Integer approvalsRequiredAtCurrentLevel = 1;

    // Comma-separated list of user IDs who have approved at current level
    @Column(length = 500)
    private String approvedByAtCurrentLevel;

    @Column
    private LocalDateTime levelStartTime;

    @Column
    private LocalDateTime lastActionTime;

    @Column
    private LocalDateTime completedTime;

    @Column(nullable = false)
    private Boolean isComplete = false;

    @Column(nullable = false)
    private Boolean isFinallyApproved = false;

    @Column(length = 1000)
    private String currentLevelComments;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum Status {
        PENDING_LEVEL_1,
        PENDING_LEVEL_2,
        PENDING_LEVEL_3,
        PENDING_LEVEL_4,
        PENDING_LEVEL_5,
        FULLY_APPROVED,
        REJECTED,
        RETURNED_FOR_CORRECTION,
        CANCELLED
    }

    /**
     * Move to next level
     */
    public void moveToNextLevel(int totalLevels, int requiredApprovers) {
        this.currentLevel++;
        this.approvalsAtCurrentLevel = 0;
        this.approvalsRequiredAtCurrentLevel = requiredApprovers;
        this.approvedByAtCurrentLevel = null;
        this.levelStartTime = LocalDateTime.now();
        this.lastActionTime = LocalDateTime.now();
        
        // Update status based on level
        if (currentLevel > totalLevels) {
            this.status = Status.FULLY_APPROVED;
            this.isComplete = true;
            this.isFinallyApproved = true;
            this.completedTime = LocalDateTime.now();
        } else {
            this.status = Status.valueOf("PENDING_LEVEL_" + currentLevel);
        }
    }

    /**
     * Add approval at current level
     */
    public void addApprovalAtCurrentLevel(String userId) {
        this.approvalsAtCurrentLevel++;
        
        // Add to approved list
        if (approvedByAtCurrentLevel == null || approvedByAtCurrentLevel.isBlank()) {
            this.approvedByAtCurrentLevel = userId;
        } else {
            this.approvedByAtCurrentLevel += "," + userId;
        }
        
        this.lastActionTime = LocalDateTime.now();
    }

    /**
     * Check if current level is complete
     */
    public boolean isCurrentLevelComplete() {
        return approvalsAtCurrentLevel >= approvalsRequiredAtCurrentLevel;
    }

    /**
     * Mark as rejected
     */
    public void markAsRejected() {
        this.status = Status.REJECTED;
        this.isComplete = true;
        this.isFinallyApproved = false;
        this.completedTime = LocalDateTime.now();
        this.lastActionTime = LocalDateTime.now();
    }

    /**
     * Mark as returned for correction
     */
    public void markAsReturned() {
        this.status = Status.RETURNED_FOR_CORRECTION;
        this.lastActionTime = LocalDateTime.now();
    }
}
