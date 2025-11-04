package com.example.demo.banking.services;

import com.example.demo.banking.parsitence.enitities.BankAccounts;
import com.example.demo.customerManagement.parsistence.entities.Customer;
import com.example.demo.loanManagement.parsistence.entities.Products;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@EnableAsync
public interface BankingService {
    void handleCallBack(JSONObject jsonObject) throws InterruptedException;


    List<BankAccounts> createBankAccounts(Customer customer);

    void refreshAllAccounts();

    void processInitialDepositIfPresent(Customer customer);

    BankAccounts createAccountForProduct(Customer customer, Products product, String customDescription);
}
