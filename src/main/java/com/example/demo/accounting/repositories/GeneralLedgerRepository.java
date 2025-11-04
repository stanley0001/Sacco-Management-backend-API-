package com.example.demo.accounting.repositories;

import com.example.demo.accounting.entities.GeneralLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface GeneralLedgerRepository extends JpaRepository<GeneralLedger, Long> {
    
    List<GeneralLedger> findByAccountCodeOrderByTransactionDateDesc(String accountCode);
    
    List<GeneralLedger> findByTransactionDateBetweenOrderByTransactionDateDesc(LocalDate startDate, LocalDate endDate);
    
    List<GeneralLedger> findByAccountCodeAndTransactionDateBetween(String accountCode, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT COALESCE(SUM(g.debit), 0.0) FROM GeneralLedger g WHERE g.accountCode = :accountCode")
    Double sumDebitByAccountCode(@Param("accountCode") String accountCode);
    
    @Query("SELECT COALESCE(SUM(g.credit), 0.0) FROM GeneralLedger g WHERE g.accountCode = :accountCode")
    Double sumCreditByAccountCode(@Param("accountCode") String accountCode);
    
    @Query("SELECT COALESCE(SUM(g.debit - g.credit), 0.0) FROM GeneralLedger g WHERE g.accountCode = :accountCode AND g.transactionDate <= :asOfDate")
    Double getAccountBalance(@Param("accountCode") String accountCode, @Param("asOfDate") LocalDate asOfDate);
}
