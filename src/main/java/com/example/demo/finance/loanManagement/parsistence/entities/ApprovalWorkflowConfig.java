package com.example.demo.finance.loanManagement.parsistence.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Approval Workflow Configuration
 * Defines the approval levels and requirements for loan applications
 */
@Entity
@Table(name = "approval_workflow_configs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalWorkflowConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String workflowName;

    @Column(length = 500)
    private String description;

    // Loan amount range this workflow applies to
    @Column(precision = 15, scale = 2)
    private BigDecimal minAmount;

    @Column(precision = 15, scale = 2)
    private BigDecimal maxAmount;

    // Product codes this workflow applies to (comma-separated)
    @Column(length = 500)
    private String applicableProducts;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Boolean isDefault = false;

    @Column(nullable = false)
    private Integer priority = 1; // Higher priority workflows checked first

    // Total number of approval levels required
    @Column(nullable = false)
    private Integer totalLevels = 1;

    // Workflow levels (one-to-many relationship)
    @OneToMany(mappedBy = "workflowConfig", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("levelNumber ASC")
    @Builder.Default
    private List<ApprovalWorkflowLevel> levels = new ArrayList<>();

    @Column(length = 100)
    private String createdBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(length = 100)
    private String updatedBy;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Check if this workflow applies to a loan application
     */
    public boolean appliesToLoan(BigDecimal loanAmount, String productCode) {
        // Check if active
        if (!isActive) {
            return false;
        }

        // Check amount range
        if (minAmount != null && loanAmount.compareTo(minAmount) < 0) {
            return false;
        }
        if (maxAmount != null && loanAmount.compareTo(maxAmount) > 0) {
            return false;
        }

        // Check product code
        if (applicableProducts != null && !applicableProducts.isBlank()) {
            String[] products = applicableProducts.split(",");
            boolean productMatches = false;
            for (String product : products) {
                if (product.trim().equalsIgnoreCase(productCode)) {
                    productMatches = true;
                    break;
                }
            }
            if (!productMatches) {
                return false;
            }
        }

        return true;
    }

    /**
     * Get level by number
     */
    public ApprovalWorkflowLevel getLevel(int levelNumber) {
        return levels.stream()
            .filter(level -> level.getLevelNumber() == levelNumber)
            .findFirst()
            .orElse(null);
    }
}
