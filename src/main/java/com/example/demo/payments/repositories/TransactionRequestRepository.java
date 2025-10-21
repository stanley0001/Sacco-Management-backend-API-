package com.example.demo.payments.repositories;

import com.example.demo.payments.entities.TransactionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRequestRepository extends JpaRepository<TransactionRequest, Long> {
    
    // Find by customer
    Page<TransactionRequest> findByCustomerId(Long customerId, Pageable pageable);
    
    // Find by customer and type
    Page<TransactionRequest> findByCustomerIdAndType(
        Long customerId, 
        TransactionRequest.TransactionType type, 
        Pageable pageable
    );
    
    // Find by customer and status
    Page<TransactionRequest> findByCustomerIdAndStatus(
        Long customerId, 
        TransactionRequest.RequestStatus status, 
        Pageable pageable
    );
    
    // Find by customer and date range
    @Query("SELECT t FROM TransactionRequest t WHERE t.customerId = :customerId " +
           "AND t.createdAt BETWEEN :startDate AND :endDate")
    Page<TransactionRequest> findByCustomerIdAndDateRange(
        @Param("customerId") Long customerId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
    
    // Find by type
    Page<TransactionRequest> findByType(TransactionRequest.TransactionType type, Pageable pageable);
    
    // Find by status
    Page<TransactionRequest> findByStatus(TransactionRequest.RequestStatus status, Pageable pageable);
    
    // Find by type and status
    Page<TransactionRequest> findByTypeAndStatus(
        TransactionRequest.TransactionType type,
        TransactionRequest.RequestStatus status,
        Pageable pageable
    );
    
    // Find all deposits
    @Query("SELECT t FROM TransactionRequest t WHERE t.type = 'DEPOSIT' ORDER BY t.createdAt DESC")
    Page<TransactionRequest> findAllDeposits(Pageable pageable);
    
    // Find all withdrawals
    @Query("SELECT t FROM TransactionRequest t WHERE t.type = 'WITHDRAWAL' ORDER BY t.createdAt DESC")
    Page<TransactionRequest> findAllWithdrawals(Pageable pageable);
    
    // Find pending requests
    @Query("SELECT t FROM TransactionRequest t WHERE t.status IN ('INITIATED', 'PROCESSING') ORDER BY t.createdAt DESC")
    Page<TransactionRequest> findPendingRequests(Pageable pageable);
    
    // Find by reference number
    List<TransactionRequest> findByReferenceNumber(String referenceNumber);
    
    // Find by M-PESA transaction ID
    TransactionRequest findByMpesaTransactionId(Long mpesaTransactionId);
    
    // Statistics queries
    @Query("SELECT COUNT(t) FROM TransactionRequest t WHERE t.type = :type AND t.status = :status")
    Long countByTypeAndStatus(
        @Param("type") TransactionRequest.TransactionType type,
        @Param("status") TransactionRequest.RequestStatus status
    );
    
    @Query("SELECT SUM(t.amount) FROM TransactionRequest t WHERE t.type = :type AND t.status = 'SUCCESS' " +
           "AND t.createdAt BETWEEN :startDate AND :endDate")
    Double sumAmountByTypeAndDateRange(
        @Param("type") TransactionRequest.TransactionType type,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
