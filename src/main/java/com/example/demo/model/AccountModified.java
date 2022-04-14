package com.example.demo.model;

import lombok.Data;

import java.util.List;

@Data
public class AccountModified {

    private LoanAccount account;
    private List<loanTransactions>   transactions;
}
