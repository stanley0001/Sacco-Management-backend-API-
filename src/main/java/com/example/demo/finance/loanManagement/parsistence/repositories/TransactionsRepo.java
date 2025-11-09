package com.example.demo.finance.loanManagement.parsistence.repositories;

import com.example.demo.finance.loanManagement.parsistence.entities.loanTransactions;
import com.example.demo.finance.loanManagement.parsistence.entities.loanTransactions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionsRepo extends JpaRepository<loanTransactions, Long> {

    List<loanTransactions> findByLoanRefOrderByTransactionIdAsc(String loanRef);
    
    List<loanTransactions> findByCustomerIdOrderByTransactionIdDesc(String customerId);
    
    // Find transactions by loan account ID (used in Client Portal)
    List<loanTransactions> findByAccountIdOrderByTransactionIdDesc(Long accountId);
}
