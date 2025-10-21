package com.example.demo.payments.repositories;

import com.example.demo.payments.entities.MpesaTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MpesaTransactionRepository extends JpaRepository<MpesaTransaction, Long> {
    
    Optional<MpesaTransaction> findByMerchantRequestId(String merchantRequestId);
    
    Optional<MpesaTransaction> findByCheckoutRequestId(String checkoutRequestId);
    
    Optional<MpesaTransaction> findByMpesaReceiptNumber(String mpesaReceiptNumber);
    
    List<MpesaTransaction> findByCustomerId(Long customerId);
    
    List<MpesaTransaction> findByLoanId(Long loanId);
    
    List<MpesaTransaction> findBySavingsAccountId(Long savingsAccountId);
    
    List<MpesaTransaction> findByStatus(MpesaTransaction.TransactionStatus status);
    
    List<MpesaTransaction> findByPhoneNumber(String phoneNumber);
    
    List<MpesaTransaction> findByTransactionType(MpesaTransaction.TransactionType type);
    
    List<MpesaTransaction> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    List<MpesaTransaction> findByCustomerIdAndCreatedAtBetween(
        Long customerId, LocalDateTime start, LocalDateTime end
    );
    
    List<MpesaTransaction> findByCustomerIdAndStatus(
        Long customerId, MpesaTransaction.TransactionStatus status
    );
    
    @Query("SELECT t FROM MpesaTransaction t WHERE t.status = :status AND t.callbackReceived = false")
    List<MpesaTransaction> findPendingTransactions(@Param("status") MpesaTransaction.TransactionStatus status);
    
    @Query("SELECT t FROM MpesaTransaction t WHERE t.customerId = :customerId ORDER BY t.createdAt DESC")
    List<MpesaTransaction> findRecentTransactionsByCustomer(@Param("customerId") Long customerId);
    
    @Query("SELECT SUM(t.amount) FROM MpesaTransaction t WHERE t.customerId = :customerId AND t.status = 'SUCCESS' AND t.createdAt BETWEEN :start AND :end")
    Double getTotalSuccessfulPaymentsByCustomer(
        @Param("customerId") Long customerId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
}
