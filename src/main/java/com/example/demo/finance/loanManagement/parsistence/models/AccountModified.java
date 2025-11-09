package com.example.demo.finance.loanManagement.parsistence.models;

import com.example.demo.finance.loanManagement.parsistence.entities.LoanAccount;
import com.example.demo.finance.loanManagement.parsistence.entities.loanTransactions;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanAccount;
import com.example.demo.finance.loanManagement.parsistence.entities.loanTransactions;
import lombok.Data;

import java.util.List;

@Data
public class AccountModified {

    private LoanAccount account;
    private List<loanTransactions>   transactions;
}
