package com.example.demo.erp.customerManagement.parsistence.models;

import com.example.demo.finance.banking.parsitence.enitities.BankAccounts;
import com.example.demo.finance.banking.parsitence.enitities.Payments;
import com.example.demo.erp.communication.parsitence.models.Email;
import com.example.demo.erp.customerManagement.parsistence.entities.Customer;
import com.example.demo.finance.loanManagement.parsistence.entities.Subscriptions;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanApplication;
import com.example.demo.system.userManagements.parsitence.enitities.Users;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ClientInfo {
    private Customer client;
    private Users user;
    private List<Subscriptions> subscriptions;
    private List<Email> communications;
    private List<LoanApplication> loanApplications;
    private List<BankAccounts> bankAccounts;
    private List<Payments> customerPayments;

}
