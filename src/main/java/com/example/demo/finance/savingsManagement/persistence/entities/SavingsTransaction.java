package com.example.demo.finance.savingsManagement.persistence.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "savings_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavingsTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Savings account ID is required")
    @Column(nullable = false)
    private Long savingsAccountId;

    @NotNull(message = "Transaction reference is required")
    @Column(nullable = false, unique = true, length = 100)
    private String transactionRef;

    @NotNull(message = "Transaction type is required")
    @Column(nullable = false, length = 50)
    private String transactionType; // DEPOSIT, WITHDRAWAL, INTEREST_CREDIT, FEE_DEBIT, TRANSFER_IN, TRANSFER_OUT

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(precision = 15, scale = 2)
    private BigDecimal balanceBefore;

    @Column(precision = 15, scale = 2)
    private BigDecimal balanceAfter;

    @Column(length = 50)
    private String paymentMethod; // CASH, MPESA, BANK_TRANSFER, CHEQUE, etc.

    @Column(length = 100)
    private String paymentReference;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDateTime transactionDate = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime valueDate = LocalDateTime.now();

    @Column(length = 100)
    private String postedBy;

    @Column(length = 100)
    private String approvedBy;

    @Column(length = 20)
    private String status = "COMPLETED"; // PENDING, COMPLETED, REVERSED, FAILED

    @Column(length = 50)
    private String branchCode;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
