package com.example.demo.finance.loanManagement.parsistence.repositories;

import com.example.demo.finance.loanManagement.parsistence.entities.LoanStandingOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanStandingOrderRepository extends JpaRepository<LoanStandingOrder, Long> {
    
    List<LoanStandingOrder> findByCustomerIdAndIsActiveTrue(Long customerId);
    
    List<LoanStandingOrder> findByLoanAccountIdAndIsActiveTrue(Long loanAccountId);
    
    List<LoanStandingOrder> findBySavingsAccountIdAndIsActiveTrue(Long savingsAccountId);
    
    Optional<LoanStandingOrder> findByCustomerIdAndLoanAccountIdAndIsActiveTrue(Long customerId, Long loanAccountId);
    
    List<LoanStandingOrder> findByIsActiveTrue();
}
