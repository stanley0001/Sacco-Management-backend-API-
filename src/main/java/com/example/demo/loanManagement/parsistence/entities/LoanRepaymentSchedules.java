package com.example.demo.loanManagement.parsistence.entities;

import com.example.demo.enums.Statuses;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@Entity
public class LoanRepaymentSchedules {
    @Id
    @Column(nullable = false,unique = true,updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private float amount;
    private Double amountPaid;
    private Integer loanAccount;
    private Integer installmentNumber;
    private Double balance;
    private Statuses status;
    private LocalDate commencementDate;
    private LocalDate dueDate;
    private LocalDate createdAt;
    private LocalDate updatedAt;

    public LoanRepaymentSchedules() {
        this.createdAt = LocalDate.now();
        this.updatedAt = LocalDate.now();
    }
}
