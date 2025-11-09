package com.example.demo.system.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Base Account Entity - Parent for all account types
 * Loan accounts, Savings accounts, etc. extend this
 */
@MappedSuperclass
@Data
@NoArgsConstructor
@SuperBuilder
public abstract class BaseAccount {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Common account fields
     */
    @Column(nullable = false, unique = true)
    private String accountNumber;
    
    @Column(nullable = false)
    private String customerId;
    
    @Column(nullable = false)
    private BigDecimal balance;
    
    @Column(nullable = false)
    private String status; // ACTIVE, CLOSED, SUSPENDED, DORMANT
    
    private LocalDate openingDate;
    
    private LocalDate closingDate;
    
    /**
     * Audit fields
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private String createdBy;
    
    private String updatedBy;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    /**
     * Lifecycle hooks
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (openingDate == null) {
            openingDate = LocalDate.now();
        }
        if (status == null) {
            status = "ACTIVE";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Common business methods
     */
    
    public void activate() {
        this.status = "ACTIVE";
        this.updatedAt = LocalDateTime.now();
    }
    
    public void suspend() {
        this.status = "SUSPENDED";
        this.updatedAt = LocalDateTime.now();
    }
    
    public void close() {
        this.status = "CLOSED";
        this.closingDate = LocalDate.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isActive() {
        return "ACTIVE".equals(this.status);
    }
    
    public boolean isClosed() {
        return "CLOSED".equals(this.status);
    }
    
    /**
     * Abstract methods - must be implemented by subclasses
     */
    public abstract String getAccountType();
    
    public abstract void credit(BigDecimal amount);
    
    public abstract void debit(BigDecimal amount);
}
