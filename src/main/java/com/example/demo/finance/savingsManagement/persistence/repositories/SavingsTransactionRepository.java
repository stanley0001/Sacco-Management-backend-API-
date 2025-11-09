package com.example.demo.finance.savingsManagement.persistence.repositories;

import com.example.demo.finance.savingsManagement.persistence.entities.SavingsTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SavingsTransactionRepository extends JpaRepository<SavingsTransaction, Long> {
    
    Optional<SavingsTransaction> findByTransactionRef(String transactionRef);
    
    List<SavingsTransaction> findBySavingsAccountId(Long savingsAccountId);
    
    Page<SavingsTransaction> findBySavingsAccountId(Long savingsAccountId, Pageable pageable);
    
    Page<SavingsTransaction> findByTransactionType(String transactionType, Pageable pageable);
    
    @Query("SELECT st FROM SavingsTransaction st WHERE st.savingsAccountId = :accountId " +
           "AND st.transactionDate BETWEEN :startDate AND :endDate ORDER BY st.transactionDate DESC")
    List<SavingsTransaction> findByAccountAndDateRange(@Param("accountId") Long accountId,
                                                        @Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(st.amount) FROM SavingsTransaction st WHERE st.savingsAccountId = :accountId " +
           "AND st.transactionType = :type AND st.status = 'COMPLETED'")
    BigDecimal sumByAccountAndType(@Param("accountId") Long accountId, @Param("type") String type);
    
    @Query("SELECT st FROM SavingsTransaction st WHERE st.transactionDate BETWEEN :startDate AND :endDate " +
           "AND st.transactionType = :type ORDER BY st.transactionDate DESC")
    List<SavingsTransaction> findByDateRangeAndType(@Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate,
                                                     @Param("type") String type);
    
    @Query("SELECT COUNT(st) FROM SavingsTransaction st WHERE st.savingsAccountId = :accountId " +
           "AND st.transactionType = 'WITHDRAWAL' AND st.transactionDate >= :startDate")
    Integer countWithdrawalsSince(@Param("accountId") Long accountId, @Param("startDate") LocalDateTime startDate);
}
