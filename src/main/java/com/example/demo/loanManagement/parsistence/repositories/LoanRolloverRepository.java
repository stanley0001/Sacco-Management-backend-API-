package com.example.demo.loanManagement.parsistence.repositories;

import com.example.demo.loanManagement.parsistence.entities.LoanRollover;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRolloverRepository extends JpaRepository<LoanRollover, Long> {
    
    List<LoanRollover> findByOriginalLoanId(Long originalLoanId);
    
    List<LoanRollover> findByNewLoanId(Long newLoanId);
    
    List<LoanRollover> findByCustomerId(Long customerId);
    
    @Query("SELECT lr FROM LoanRollover lr WHERE lr.customerId = :customerId AND lr.status = :status")
    List<LoanRollover> findByCustomerIdAndStatus(@Param("customerId") Long customerId, @Param("status") String status);
    
    @Query("SELECT COUNT(lr) FROM LoanRollover lr WHERE lr.customerId = :customerId AND lr.status = 'COMPLETED'")
    Long countRolloversByCustomer(@Param("customerId") Long customerId);
}
