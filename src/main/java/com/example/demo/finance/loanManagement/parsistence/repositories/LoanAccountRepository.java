package com.example.demo.finance.loanManagement.parsistence.repositories;

import com.example.demo.finance.loanManagement.parsistence.entities.LoanAccount;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanAccountRepository extends JpaRepository<LoanAccount, Long> {
    
    /**
     * Find loan account by loan reference
     */
    Optional<LoanAccount> findByLoanReference(String loanReference);
    
    /**
     * Find loan accounts by customer ID
     */
    List<LoanAccount> findByCustomerId(String customerId);
    
    /**
     * Find loan accounts by status
     */
    List<LoanAccount> findByStatus(String status);
    
    /**
     * Find loan accounts by customer ID and status
     */
    List<LoanAccount> findByCustomerIdAndStatus(String customerId, String status);
    
    /**
     * Find active loan accounts for a customer
     */
    @Query("SELECT la FROM LoanAccount la WHERE la.customerId = :customerId AND la.status IN ('ACTIVE', 'CURRENT')")
    List<LoanAccount> findActiveByCustomerId(@Param("customerId") String customerId);
    
    /**
     * Find overdue loan accounts
     */
    @Query("SELECT la FROM LoanAccount la WHERE la.nextPaymentDate < CURRENT_DATE AND la.totalOutstanding > 0")
    List<LoanAccount> findOverdueAccounts();
    
    /**
     * Find loan accounts by disbursement date range
     */
    @Query("SELECT la FROM LoanAccount la WHERE la.disbursementDate BETWEEN :startDate AND :endDate")
    List<LoanAccount> findByDisbursementDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find loan accounts with outstanding balance greater than amount
     */
    @Query("SELECT la FROM LoanAccount la WHERE la.totalOutstanding >= :amount")
    List<LoanAccount> findByTotalOutstandingGreaterThanEqual(@Param("amount") BigDecimal amount);
    
    /**
     * Count loan accounts by status
     */
    Long countByStatus(String status);
    
    /**
     * Find all ordered by creation date descending
     */
    List<LoanAccount> findAllByOrderByCreatedAtDesc();
    
    /**
     * Find loan accounts by product ID
     */
    List<LoanAccount> findByProductId(Long productId);
    
    /**
     * Find loan accounts created within date range
     */
    @Query("SELECT la FROM LoanAccount la WHERE la.createdAt BETWEEN :startDate AND :endDate ORDER BY la.createdAt DESC")
    List<LoanAccount> findByCreatedAtBetweenOrderByCreatedAtDesc(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Get total outstanding amount for a customer
     */
    @Query("SELECT COALESCE(SUM(la.totalOutstanding), 0) FROM LoanAccount la WHERE la.customerId = :customerId AND la.status IN ('ACTIVE', 'CURRENT')")
    BigDecimal getTotalOutstandingByCustomerId(@Param("customerId") String customerId);
}
