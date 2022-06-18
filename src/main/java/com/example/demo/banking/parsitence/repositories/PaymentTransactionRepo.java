package com.example.demo.banking.parsitence.repositories;

import com.example.demo.banking.parsitence.enitities.BankAccounts;
import com.example.demo.banking.parsitence.enitities.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentTransactionRepo extends JpaRepository<Transactions,Long> {
    List<Transactions> findAllByBankAccount(BankAccounts account);

    @Query("select sum(t.amount) from  Transactions t where t.transactionTime between :from and :to and t.bankAccount=:account")
    Double findAmountByAccount(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, @Param("account")BankAccounts bankAccount);
}
