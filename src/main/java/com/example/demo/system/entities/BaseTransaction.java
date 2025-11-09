package com.example.demo.system.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Base Transaction Entity - Parent for all transaction types
 * Uses inheritance to share common transaction logic
 * All specific transaction types extend this
 */
@MappedSuperclass
@Data
@NoArgsConstructor
@SuperBuilder
public abstract class BaseTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Common transaction fields - shared by all transaction types
     */
    @Column(nullable = false, unique = true)
    private String transactionReference;
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @Column(nullable = false)
    private LocalDateTime transactionDate;
    
    @Column(nullable = false)
    private String status; // PENDING, COMPLETED, FAILED, REVERSED
    
    @Column(nullable = false)
    private String customerId;
    
    private String paymentMethod; // CASH, MPESA, BANK, CHEQUE
    
    @Column(length = 1000)
    private String description;
    
    /**
     * Accounting integration fields
     */
    private Long journalEntryId;
    
    private Boolean postedToAccounting = false;
    
    private LocalDateTime postedToAccountingAt;
    
    /**
     * Audit fields
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private String processedBy;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    /**
     * Common business methods - inherited by all transactions
     */
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (transactionDate == null) {
            transactionDate = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Mark transaction as completed
     */
    public void markCompleted() {
        this.status = "COMPLETED";
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Mark transaction as failed
     */
    public void markFailed(String reason) {
        this.status = "FAILED";
        this.notes = (this.notes != null ? this.notes + "\n" : "") + "Failed: " + reason;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Mark as posted to accounting
     */
    public void markPostedToAccounting(Long journalEntryId) {
        this.journalEntryId = journalEntryId;
        this.postedToAccounting = true;
        this.postedToAccountingAt = LocalDateTime.now();
    }
    
    /**
     * Check if transaction is pending
     */
    public boolean isPending() {
        return "PENDING".equals(this.status);
    }
    
    /**
     * Check if transaction is completed
     */
    public boolean isCompleted() {
        return "COMPLETED".equals(this.status);
    }
    
    /**
     * Check if transaction can be reversed
     */
    public boolean canBeReversed() {
        return isCompleted() && !postedToAccounting;
    }
}
