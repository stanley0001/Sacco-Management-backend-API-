package com.example.demo.system.parsitence.models;

import com.example.demo.erp.customerManagement.parsistence.entities.Customer;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanAccount;

public class SReportData {
    private Customer client;
    private LoanAccount loanAccount;

    public SReportData() {
    }

    public SReportData(Customer client, LoanAccount loanAccount) {
        this.client = client;
        this.loanAccount = loanAccount;
    }

    public Customer getClient() {
        return client;
    }

    public void setClient(Customer client) {
        this.client = client;
    }

    public LoanAccount getLoanAccount() {
        return loanAccount;
    }

    public void setLoanAccount(LoanAccount loanAccount) {
        this.loanAccount = loanAccount;
    }

    @Override
    public String toString() {
        return "SReportData{" +
                "client=" + client +
                ", loanAccounts=" + loanAccount +
                '}';
    }
}
