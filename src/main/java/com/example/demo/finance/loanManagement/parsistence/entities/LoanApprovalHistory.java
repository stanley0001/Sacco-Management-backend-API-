package com.example.demo.finance.loanManagement.parsistence.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Loan Approval History
 * Tracks all approval actions on a loan application
 */
@Entity
@Table(name = "loan_approval_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApprovalHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long loanApplicationId;

    @Column(nullable = false)
    private Long workflowConfigId;

    @Column(nullable = false)
    private Integer levelNumber;

    @Column(nullable = false, length = 100)
    private String levelName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalAction action;

    @Column(nullable = false, length = 100)
    private String approverUserId;

    @Column(length = 200)
    private String approverName;

    @Column(length = 100)
    private String approverRole;

    @Column(length = 1000)
    private String comments;

    @Column(length = 500)
    private String rejectionReason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime actionDate;

    @Column(length = 50)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    public enum ApprovalAction {
        SUBMITTED,      // Application submitted
        APPROVED,       // Approved at this level
        REJECTED,       // Rejected at this level
        RETURNED,       // Returned for corrections
        ESCALATED,      // Escalated to next level
        AUTO_APPROVED,  // Auto-approved due to timeout
        SKIPPED         // Level skipped based on rules
    }
}
