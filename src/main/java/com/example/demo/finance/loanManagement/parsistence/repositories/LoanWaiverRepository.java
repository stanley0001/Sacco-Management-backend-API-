package com.example.demo.finance.loanManagement.parsistence.repositories;

import com.example.demo.finance.loanManagement.parsistence.entities.LoanWaiver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanWaiverRepository extends JpaRepository<LoanWaiver, Long> {
    
    List<LoanWaiver> findByLoanId(Long loanId);
    
    List<LoanWaiver> findByCustomerId(Long customerId);
    
    @Query("SELECT lw FROM LoanWaiver lw WHERE lw.loanId = :loanId AND lw.status = 'APPROVED'")
    List<LoanWaiver> findApprovedWaiversByLoan(@Param("loanId") Long loanId);
    
    @Query("SELECT SUM(lw.waivedInterest) FROM LoanWaiver lw WHERE lw.loanId = :loanId AND lw.status = 'APPROVED'")
    Double getTotalWaivedInterestByLoan(@Param("loanId") Long loanId);
    
    @Query("SELECT lw FROM LoanWaiver lw WHERE lw.autoWaived = true AND lw.status = 'APPROVED'")
    List<LoanWaiver> findAutoWaivedLoans();
}
