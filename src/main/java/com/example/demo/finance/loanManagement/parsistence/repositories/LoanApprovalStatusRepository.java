package com.example.demo.finance.loanManagement.parsistence.repositories;

import com.example.demo.finance.loanManagement.parsistence.entities.LoanApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanApprovalStatusRepository extends JpaRepository<LoanApprovalStatus, Long> {
    
    Optional<LoanApprovalStatus> findByLoanApplicationId(Long loanApplicationId);
    
    List<LoanApprovalStatus> findByStatusAndIsCompleteFalse(LoanApprovalStatus.Status status);
    
    List<LoanApprovalStatus> findByIsCompleteFalseOrderByCreatedAtAsc();
    
    List<LoanApprovalStatus> findByCurrentLevelAndIsCompleteFalse(Integer currentLevel);
}
