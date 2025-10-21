package com.example.demo.accounting.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Chart of Accounts - Foundation for double-entry bookkeeping
 */
@Entity
@Table(name = "chart_of_accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartOfAccounts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String accountCode;

    @Column(nullable = false, length = 200)
    private String accountName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountCategory accountCategory;

    private String parentAccountCode;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Boolean isSystemAccount = false; // Cannot be deleted if true

    private Double currentBalance = 0.0;

    @Enumerated(EnumType.STRING)
    private NormalBalance normalBalance; // DEBIT or CREDIT

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private String createdBy;

    public enum AccountType {
        ASSET,
        LIABILITY,
        EQUITY,
        REVENUE,
        EXPENSE
    }

    public enum AccountCategory {
        // Assets
        CURRENT_ASSET,
        FIXED_ASSET,
        INTANGIBLE_ASSET,
        OTHER_ASSET,

        // Liabilities
        CURRENT_LIABILITY,
        LONG_TERM_LIABILITY,
        OTHER_LIABILITY,

        // Equity
        CAPITAL,
        RETAINED_EARNINGS,
        DRAWINGS,

        // Revenue
        OPERATING_REVENUE,
        NON_OPERATING_REVENUE,

        // Expenses
        OPERATING_EXPENSE,
        ADMINISTRATIVE_EXPENSE,
        FINANCIAL_EXPENSE,
        OTHER_EXPENSE
    }

    public enum NormalBalance {
        DEBIT,  // Assets, Expenses
        CREDIT  // Liabilities, Equity, Revenue
    }
}
