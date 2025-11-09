package com.example.demo.finance.accounting.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payroll Detail - Individual employee salary details in a payroll run
 */
@Entity
@Table(name = "payroll_details")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_run_id", nullable = false)
    private PayrollRun payrollRun;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    // Earnings
    private Double basicSalary;
    private Double housingAllowance;
    private Double transportAllowance;
    private Double otherAllowances;
    private Double overtime;
    private Double bonus;

    // Deductions
    private Double paye; // Tax
    private Double nhif; // Health insurance
    private Double nssf; // Social security
    private Double loanDeductions;
    private Double advanceDeductions;
    private Double otherDeductions;

    // Calculated fields
    private Double grossSalary;
    private Double totalDeductions;
    private Double netSalary;

    private String paymentReference;
    private Boolean isPaid = false;

    public void calculateSalary() {
        this.grossSalary = (basicSalary != null ? basicSalary : 0) +
                          (housingAllowance != null ? housingAllowance : 0) +
                          (transportAllowance != null ? transportAllowance : 0) +
                          (otherAllowances != null ? otherAllowances : 0) +
                          (overtime != null ? overtime : 0) +
                          (bonus != null ? bonus : 0);

        this.totalDeductions = (paye != null ? paye : 0) +
                              (nhif != null ? nhif : 0) +
                              (nssf != null ? nssf : 0) +
                              (loanDeductions != null ? loanDeductions : 0) +
                              (advanceDeductions != null ? advanceDeductions : 0) +
                              (otherDeductions != null ? otherDeductions : 0);

        this.netSalary = grossSalary - totalDeductions;
    }

    public Double getGrossSalary() {
        return grossSalary != null ? grossSalary : 0.0;
    }

    public Double getTotalDeductions() {
        return totalDeductions != null ? totalDeductions : 0.0;
    }

    public Double getNetSalary() {
        return netSalary != null ? netSalary : 0.0;
    }
}
