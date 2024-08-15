package com.example.demo.loanManagement.parsistence.models;

import lombok.Data;

import java.util.List;

@Data
public class LoanCalculatorResponse {
    private LoanAccountModel loanAccount;
    private List<RepaymentSchedules> schedules;
    private float totalRepayment;
    private float totalInterest;
}
