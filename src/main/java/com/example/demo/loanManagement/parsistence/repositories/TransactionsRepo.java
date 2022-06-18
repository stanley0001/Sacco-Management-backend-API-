package com.example.demo.persistence.repository;

import com.example.demo.model.loanTransactions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionsRepo extends JpaRepository<loanTransactions, Long> {

    List<loanTransactions> findByLoanRefOrderByTransactionIdAsc(String loanRef);
}
