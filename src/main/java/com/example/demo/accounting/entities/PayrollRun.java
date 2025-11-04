package com.example.demo.accounting.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Payroll Run - Monthly salary processing
 */
@Entity
@Table(name = "payroll_runs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String payrollNumber;

    @Column(nullable = false)
    private Integer periodMonth; // 1-12

    @Column(nullable = false)
    private Integer periodYear;

    @OneToMany(mappedBy = "payrollRun", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PayrollDetail> details = new ArrayList<>();

    private Double totalGrossSalary = 0.0;
    private Double totalDeductions = 0.0;
    private Double totalNetSalary = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayrollStatus status = PayrollStatus.DRAFT;

    private LocalDateTime processedAt;
    private String processedBy;

    private LocalDateTime approvedAt;
    private String approvedBy;

    private String journalEntryId; // Reference to journal entry

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private String createdBy;

    public enum PayrollStatus {
        DRAFT,       // Being prepared
        PROCESSED,   // Calculated
        APPROVED,    // Approved for payment
        PAID,        // Salaries paid
        CANCELLED    // Cancelled
    }

    public void calculateTotals() {
        this.totalGrossSalary = details.stream().mapToDouble(PayrollDetail::getGrossSalary).sum();
        this.totalDeductions = details.stream().mapToDouble(PayrollDetail::getTotalDeductions).sum();
        this.totalNetSalary = details.stream().mapToDouble(PayrollDetail::getNetSalary).sum();
    }
}
