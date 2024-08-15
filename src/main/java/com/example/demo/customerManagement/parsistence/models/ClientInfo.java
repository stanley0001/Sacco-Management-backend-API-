package com.example.demo.customerManagement.parsistence.models;

import com.example.demo.banking.parsitence.enitities.BankAccounts;
import com.example.demo.banking.parsitence.enitities.Payments;
import com.example.demo.communication.parsitence.models.Email;
import com.example.demo.customerManagement.parsistence.entities.Customer;
import com.example.demo.loanManagement.parsistence.entities.Subscriptions;
import com.example.demo.loanManagement.parsistence.entities.LoanApplication;
import com.example.demo.userManagements.parsitence.enitities.Users;
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
