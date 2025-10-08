package com.example.demo.savingsManagement.persistence.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "savings_products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavingsProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @NotBlank(message = "Product code is required")
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(length = 500)
    private String description;

    @DecimalMin(value = "0.0", inclusive = true)
    @Column(precision = 5, scale = 2)
    private BigDecimal interestRate = BigDecimal.ZERO;

    @Column(length = 20)
    private String interestCalculationMethod = "DAILY_BALANCE"; // DAILY_BALANCE, MONTHLY_AVERAGE

    @Column(length = 20)
    private String interestPostingFrequency = "MONTHLY"; // MONTHLY, QUARTERLY, ANNUALLY

    @DecimalMin(value = "0.0", inclusive = true)
    @Column(precision = 15, scale = 2)
    private BigDecimal minimumBalance = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", inclusive = true)
    @Column(precision = 15, scale = 2)
    private BigDecimal minimumOpeningBalance = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", inclusive = true)
    @Column(precision = 15, scale = 2)
    private BigDecimal maximumBalance;

    @DecimalMin(value = "0.0", inclusive = true)
    @Column(precision = 15, scale = 2)
    private BigDecimal withdrawalFee = BigDecimal.ZERO;

    @Column
    private Integer maxWithdrawalsPerMonth;

    @DecimalMin(value = "0.0", inclusive = true)
    @Column(precision = 15, scale = 2)
    private BigDecimal monthlyMaintenanceFee = BigDecimal.ZERO;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Boolean allowsWithdrawals = true;

    @Column(nullable = false)
    private Boolean allowsDeposits = true;

    @Column(nullable = false)
    private Boolean allowsOverdraft = false;

    @DecimalMin(value = "0.0", inclusive = true)
    @Column(precision = 15, scale = 2)
    private BigDecimal overdraftLimit = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt;

    @Column(length = 100)
    private String createdBy;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
