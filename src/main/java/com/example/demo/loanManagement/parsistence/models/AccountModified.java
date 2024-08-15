package com.example.demo.loanManagement.parsistence.models;

import com.example.demo.loanManagement.parsistence.entities.LoanAccount;
import com.example.demo.loanManagement.parsistence.entities.loanTransactions;
import lombok.Data;

import java.util.List;

@Data
public class AccountModified {

    private LoanAccount account;
    private List<loanTransactions>   transactions;
}
