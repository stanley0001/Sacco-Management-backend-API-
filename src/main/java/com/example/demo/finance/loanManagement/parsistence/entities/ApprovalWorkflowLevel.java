package com.example.demo.finance.loanManagement.parsistence.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Approval Workflow Level
 * Defines a single level in the approval workflow
 */
@Entity
@Table(name = "approval_workflow_levels")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalWorkflowLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_config_id", nullable = false)
    private ApprovalWorkflowConfig workflowConfig;

    @Column(nullable = false)
    private Integer levelNumber; // 1, 2, 3, etc.

    @Column(nullable = false, length = 100)
    private String levelName; // e.g., "Branch Manager", "Credit Officer", "CEO"

    @Column(length = 500)
    private String description;

    // Number of approvers required at this level (e.g., 1 out of 3, or 2 out of 5)
    @Column(nullable = false)
    private Integer requiredApprovers = 1;

    // Roles that can approve at this level (comma-separated role IDs or names)
    @Column(nullable = false, length = 500)
    private String allowedRoles;

    // Whether this level can be skipped
    @Column(nullable = false)
    private Boolean canSkip = false;

    // Auto-approve if no action taken within X hours
    @Column
    private Integer autoApproveAfterHours;

    // Send SMS notification when application reaches this level
    @Column(nullable = false)
    private Boolean sendSmsNotification = true;

    // Send email notification when application reaches this level
    @Column(nullable = false)
    private Boolean sendEmailNotification = false;

    // SMS template for this level
    @Column(length = 500)
    private String smsTemplate;

    /**
     * Check if a role is allowed to approve at this level
     */
    public boolean isRoleAllowed(String roleId) {
        if (allowedRoles == null || allowedRoles.isBlank()) {
            return false;
        }
        
        String[] roles = allowedRoles.split(",");
        for (String role : roles) {
            if (role.trim().equalsIgnoreCase(roleId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get allowed roles as array
     */
    public String[] getAllowedRolesArray() {
        if (allowedRoles == null || allowedRoles.isBlank()) {
            return new String[0];
        }
        return allowedRoles.split(",");
    }
}
