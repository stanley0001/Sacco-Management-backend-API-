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
@Table(name = "savings_accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavingsAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Customer ID is required")
    @Column(nullable = false)
    private Long customerId;

    @NotNull(message = "Account number is required")
    @Column(nullable = false, unique = true, length = 50)
    private String accountNumber;

    @NotNull(message = "Product code is required")
    @Column(nullable = false, length = 50)
    private String productCode;

    @NotNull(message = "Product name is required")
    @Column(nullable = false, length = 100)
    private String productName;

    @DecimalMin(value = "0.0", inclusive = true, message = "Balance cannot be negative")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", inclusive = true, message = "Available balance cannot be negative")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal availableBalance = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", inclusive = true, message = "Interest rate cannot be negative")
    @Column(precision = 5, scale = 2)
    private BigDecimal interestRate = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", inclusive = true, message = "Minimum balance cannot be negative")
    @Column(precision = 15, scale = 2)
    private BigDecimal minimumBalance = BigDecimal.ZERO;

    @Column(length = 20)
    private String status = "ACTIVE"; // ACTIVE, DORMANT, CLOSED, SUSPENDED

    @Column(length = 50)
    private String accountType; // REGULAR, FIXED_DEPOSIT, JUNIOR, etc.

    @Column(nullable = false)
    private LocalDateTime openedDate = LocalDateTime.now();

    @Column
    private LocalDateTime closedDate;

    @Column
    private LocalDateTime lastTransactionDate;

    @Column(length = 50)
    private String branchCode;

    @Column(length = 100)
    private String createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt;

    @Column(length = 500)
    private String notes;
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
