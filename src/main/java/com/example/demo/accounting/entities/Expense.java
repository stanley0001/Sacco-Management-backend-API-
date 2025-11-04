package com.example.demo.accounting.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Expense - Track all organizational expenses
 */
@Entity
@Table(name = "expenses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String expenseNumber;

    @Column(nullable = false)
    private LocalDate expenseDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private ExpenseCategory category;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String payee; // Who was paid

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseStatus status = ExpenseStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private String receiptUrl; // Path to uploaded receipt

    private String approvedBy;
    private LocalDateTime approvedAt;

    private String paidBy;
    private LocalDateTime paidAt;

    private String journalEntryId; // Reference to created journal entry

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private String createdBy;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public enum ExpenseStatus {
        PENDING,    // Submitted, awaiting approval
        APPROVED,   // Approved, ready for payment
        PAID,       // Payment completed
        REJECTED,   // Rejected
        CANCELLED   // Cancelled
    }

    public enum PaymentMethod {
        CASH,
        BANK_TRANSFER,
        CHEQUE,
        MPESA,
        CARD
    }
}
