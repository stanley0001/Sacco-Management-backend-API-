package com.example.demo.customerManagement.parsistence.models;

import com.example.demo.banking.parsitence.enitities.BankAccounts;
import com.example.demo.banking.parsitence.enitities.Payments;
import com.example.demo.communication.parsitence.models.Email;
import com.example.demo.customerManagement.parsistence.entities.Customer;
import com.example.demo.loanManagement.parsistence.models.Subscriptions;
import com.example.demo.loanManagement.parsistence.models.loanApplication;
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
    private List<loanApplication> loanApplications;
    private List<BankAccounts> bankAccounts;
    private List<Payments> customerPayments;

}
