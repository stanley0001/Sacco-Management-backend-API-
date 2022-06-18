package com.example.demo.loanManagement.parsistence.repositories;

import com.example.demo.loanManagement.parsistence.models.loanTransactions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionsRepo extends JpaRepository<loanTransactions, Long> {

    List<loanTransactions> findByLoanRefOrderByTransactionIdAsc(String loanRef);
}
