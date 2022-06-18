package com.example.demo.banking.services;

import com.example.demo.banking.parsitence.enitities.BankAccounts;
import com.example.demo.customerManagement.parsistence.entities.Customer;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface BankingService {
    void handleCallBack(JSONObject jsonObject) throws InterruptedException;


    List<BankAccounts> createBankAccounts(Customer customer);

    void refreshAllAccounts();
}
