package com.example.demo.banking.parsitence.repositories;

import com.example.demo.banking.parsitence.enitities.BankAccounts;
import com.example.demo.customerManagement.parsistence.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankAccountRepo extends JpaRepository<BankAccounts,Long> {
   

    BankAccounts findByBankAccount(String accountNumber);

    Optional<List<BankAccounts>> findByCustomer(Customer customer);
}
