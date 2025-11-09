package com.example.demo.finance.loanManagement.parsistence.repositories;

import com.example.demo.finance.loanManagement.parsistence.entities.ApprovalWorkflowConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalWorkflowConfigRepository extends JpaRepository<ApprovalWorkflowConfig, Long> {
    
    List<ApprovalWorkflowConfig> findByIsActiveTrue();
    
    Optional<ApprovalWorkflowConfig> findByWorkflowName(String workflowName);
    
    Optional<ApprovalWorkflowConfig> findByIsDefaultTrueAndIsActiveTrue();
    
    @Query("SELECT w FROM ApprovalWorkflowConfig w WHERE w.isActive = true " +
           "AND (w.minAmount IS NULL OR w.minAmount <= :amount) " +
           "AND (w.maxAmount IS NULL OR w.maxAmount >= :amount) " +
           "AND (w.applicableProducts IS NULL OR w.applicableProducts = '' OR LOWER(w.applicableProducts) LIKE LOWER(CONCAT('%', :productCode, '%'))) " +
           "ORDER BY w.priority DESC")
    List<ApprovalWorkflowConfig> findApplicableWorkflows(
        @Param("amount") BigDecimal amount,
        @Param("productCode") String productCode
    );
}
