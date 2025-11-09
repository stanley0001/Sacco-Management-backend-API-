package com.example.demo.finance.accounting.entities;

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
 * Employee - Staff member details for payroll
 */
@Entity
@Table(name = "employees")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String employeeCode;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String middleName;

    @Column(nullable = false, unique = true)
    private String nationalId;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String position; // Job title

    private String department;

    @Column(nullable = false)
    private Double basicSalary;

    private Double housingAllowance;
    private Double transportAllowance;
    private Double otherAllowances;

    // Tax & Statutory Deductions
    private String kraPin; // Tax PIN
    private String nhifNumber;
    private String nssfNumber;

    // Bank Details
    private String bankName;
    private String bankBranch;
    private String bankAccountNumber;

    @Column(nullable = false)
    private LocalDate dateOfJoining;

    private LocalDate dateOfLeaving;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private String createdBy;

    public enum EmployeeStatus {
        ACTIVE,
        ON_LEAVE,
        SUSPENDED,
        TERMINATED,
        RETIRED
    }

    public Double getGrossSalary() {
        return basicSalary + 
               (housingAllowance != null ? housingAllowance : 0) +
               (transportAllowance != null ? transportAllowance : 0) +
               (otherAllowances != null ? otherAllowances : 0);
    }
}
