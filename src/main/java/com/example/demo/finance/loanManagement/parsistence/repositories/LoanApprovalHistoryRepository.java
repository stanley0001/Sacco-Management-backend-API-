package com.example.demo.finance.loanManagement.parsistence.repositories;

import com.example.demo.finance.loanManagement.parsistence.entities.LoanApprovalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanApprovalHistoryRepository extends JpaRepository<LoanApprovalHistory, Long> {
    
    List<LoanApprovalHistory> findByLoanApplicationIdOrderByActionDateDesc(Long loanApplicationId);
    
    List<LoanApprovalHistory> findByLoanApplicationIdAndLevelNumberOrderByActionDateDesc(
        Long loanApplicationId, 
        Integer levelNumber
    );
    
    List<LoanApprovalHistory> findByApproverUserId(String approverUserId);
    
    long countByLoanApplicationIdAndLevelNumberAndAction(
        Long loanApplicationId,
        Integer levelNumber,
        LoanApprovalHistory.ApprovalAction action
    );
}
