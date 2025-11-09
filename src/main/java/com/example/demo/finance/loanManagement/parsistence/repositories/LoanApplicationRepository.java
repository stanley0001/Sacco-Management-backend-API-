package com.example.demo.finance.loanManagement.parsistence.repositories;

import com.example.demo.finance.loanManagement.parsistence.entities.LoanApplication;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    
    /**
     * Find loan applications by status
     */
    List<LoanApplication> findByApplicationStatus(String applicationStatus);
    
    /**
     * Find loan applications by customer ID
     */
//    List<LoanApplication> findByCustomerId(Long customerId);
    @Query("SELECT l FROM LoanApplication l WHERE CAST(l.customerId AS string) = CAST(:customerId AS string)")
    List<LoanApplication> findByCustomerId(@Param("customerId") Long customerId);


    /**
     * Find loan applications by product ID
     */
    List<LoanApplication> findByProductId(Long productId);
    
    /**
     * Find loan applications by status and customer ID
     */
//    List<LoanApplication> findByApplicationStatusAndCustomerId(String applicationStatus, Long customerId);
    @Query("""
    SELECT la FROM LoanApplication la
    WHERE la.applicationStatus = :applicationStatus
      AND CAST(la.customerId AS string) = CAST(:customerId AS string)
""")
    List<LoanApplication> findByApplicationStatusAndCustomerId(
            @Param("applicationStatus") String applicationStatus,
            @Param("customerId") Long customerId
    );


    /**
     * Find loan applications by amount range
     */
    @Query("SELECT la FROM LoanApplication la WHERE la.amount BETWEEN :minAmount AND :maxAmount")
    List<LoanApplication> findByAmountRange(@Param("minAmount") BigDecimal minAmount, @Param("maxAmount") BigDecimal maxAmount);
    
    /**
     * Find loan applications created within date range
     */
    @Query("SELECT la FROM LoanApplication la WHERE la.applicationTime BETWEEN :startDate AND :endDate")
    List<LoanApplication> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find pending loan applications
     */
    @Query("SELECT la FROM LoanApplication la WHERE la.applicationStatus = 'PENDING' ORDER BY la.applicationTime ASC")
    List<LoanApplication> findPendingApplications();
    
    /**
     * Find approved loan applications waiting for disbursement
     */
    @Query("SELECT la FROM LoanApplication la WHERE la.applicationStatus = 'APPROVED' ORDER BY la.applicationTime ASC")
    List<LoanApplication> findApprovedApplications();
    
    /**
     * Count applications by status
     */
    Long countByApplicationStatus(String applicationStatus);
    
    /**
     * Find applications by customer and status
     */
    @Query("""
    SELECT la FROM LoanApplication la
    WHERE CAST(la.customerId AS string) = CAST(:customerId AS string)
      AND la.applicationStatus IN :statuses
""")
    List<LoanApplication> findByCustomerIdAndStatusIn(
            @Param("customerId") Long customerId,
            @Param("statuses") List<String> statuses
    );

}
