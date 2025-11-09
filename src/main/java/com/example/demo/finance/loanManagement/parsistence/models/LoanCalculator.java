package com.example.demo.finance.loanManagement.parsistence.models;

import lombok.Data;

@Data
public class LoanCalculator {
    private Integer productId;
    private Integer amount;
    private Integer numberOfInstallments;
}
