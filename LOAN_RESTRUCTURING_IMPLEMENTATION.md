# 🔄 Loan Restructuring Implementation Guide

## Overview
Loan restructuring allows modifying existing loan terms (principal, interest rate, term, payment schedule) for customers facing payment difficulties or due to other circumstances.

---

## 📋 Restructuring Scenarios

### Common Restructuring Types:
1. **Term Extension** - Extend repayment period (reduce installment amount)
2. **Interest Rate Reduction** - Lower interest rate temporarily or permanently
3. **Principal Reduction** - Write off part of principal (rare, needs approval)
4. **Payment Holiday** - Skip payments for specified months
5. **Refinancing** - Combine multiple loans into one

---

## 🛠️ BACKEND IMPLEMENTATION

### Step 1: Create Loan Restructuring Entity

**File:** `src/main/java/com/example/demo/loanManagement/parsistence/entities/LoanRestructuring.java` (NEW)

```java
package com.example.demo.finance.loanManagement.parsistence.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "loan_restructuring")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanRestructuring {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long loanAccountId;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false, length = 50)
    private String restructuringType; // TERM_EXTENSION, INTEREST_REDUCTION, PRINCIPAL_REDUCTION, PAYMENT_HOLIDAY, REFINANCING

    // Original loan details
    private Double originalPrincipal;
    private Double originalInterestRate;
    private Integer originalTerm;
    private Double originalInstallment;
    private Double originalOutstanding;

    // New loan details after restructuring
    private Double newPrincipal;
    private Double newInterestRate;
    private Integer newTerm;
    private Double newInstallment;
    private LocalDateTime newStartDate;

    // Restructuring details
    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String justification;

    private String requestedBy;
    private LocalDateTime requestedAt;

    private String approvedBy;
    private LocalDateTime approvedAt;

    @Column(nullable = false, length = 20)
    private String status; // PENDING, APPROVED, REJECTED, IMPLEMENTED

    @Column(columnDefinition = "TEXT")
    private String approvalComments;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Accounting impact
    private Double principalWriteOff; // Amount of principal forgiven
    private Double interestWriteOff; // Amount of interest forgiven
    private Double totalCostToOrganization;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

---

### Step 2: Create Loan Restructuring Repository

**File:** `src/main/java/com/example/demo/loanManagement/parsistence/repositories/LoanRestructuringRepository.java` (NEW)

```java
package com.example.demo.finance.loanManagement.parsistence.repositories;

import com.example.demo.finance.loanManagement.parsistence.entities.LoanRestructuring;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRestructuringRepository extends JpaRepository<LoanRestructuring, Long> {

    List<LoanRestructuring> findByLoanAccountId(Long loanAccountId);

    List<LoanRestructuring> findByCustomerId(String customerId);

    List<LoanRestructuring> findByStatus(String status);

    @Query("SELECT r FROM LoanRestructuring r WHERE r.status = 'PENDING' ORDER BY r.requestedAt ASC")
    List<LoanRestructuring> findPendingRestructurings();

    @Query("SELECT r FROM LoanRestructuring r WHERE r.loanAccountId = ?1 AND r.status IN ('PENDING', 'APPROVED')")
    Optional<LoanRestructuring> findActiveRestructuringByLoanId(Long loanAccountId);
}
```

---

### Step 3: Create Loan Restructuring Service

**File:** `src/main/java/com/example/demo/loanManagement/services/LoanRestructuringService.java` (NEW)

```java
package com.example.demo.finance.loanManagement.services;

import com.example.demo.finance.loanManagement.parsistence.entities.LoanAccount;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanRepaymentSchedule;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanRestructuring;
import com.example.demo.finance.loanManagement.parsistence.repositories.LoanAccountRepo;
import com.example.demo.finance.loanManagement.parsistence.repositories.LoanRepaymentScheduleRepository;
import com.example.demo.finance.loanManagement.parsistence.repositories.LoanRestructuringRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanRestructuringService {

    private final LoanRestructuringRepository restructuringRepository;
    private final LoanAccountRepo loanAccountRepo;
    private final LoanRepaymentScheduleRepository scheduleRepository;
    private final LoanAccountingService loanAccountingService;

    /**
     * Request loan restructuring
     */
    @Transactional
    public LoanRestructuring requestRestructuring(
            Long loanAccountId,
            String restructuringType,
            Double newPrincipal,
            Double newInterestRate,
            Integer newTerm,
            String reason,
            String requestedBy) {

        log.info("Processing restructuring request for loan ID: {}", loanAccountId);

        // Get existing loan
        LoanAccount loanAccount = loanAccountRepo.findById(loanAccountId)
            .orElseThrow(() -> new IllegalStateException("Loan account not found"));

        // Check if loan can be restructured
        if ("CLOSED".equals(loanAccount.getStatus()) || "WRITTEN_OFF".equals(loanAccount.getStatus())) {
            throw new IllegalStateException("Cannot restructure a " + loanAccount.getStatus() + " loan");
        }

        // Check for existing pending restructuring
        restructuringRepository.findActiveRestructuringByLoanId(loanAccountId)
            .ifPresent(r -> {
                throw new IllegalStateException("Loan already has a pending restructuring request");
            });

        // Calculate new installment
        double newInstallment = calculateNewInstallment(newPrincipal, newInterestRate, newTerm);

        // Calculate cost to organization
        double originalTotal = loanAccount.getAmount() != null ? loanAccount.getAmount() : 0.0;
        double newTotal = newPrincipal * (1 + (newInterestRate / 100));
        double costToOrg = originalTotal - newTotal;

        // Create restructuring request
        LoanRestructuring restructuring = LoanRestructuring.builder()
            .loanAccountId(loanAccountId)
            .customerId(loanAccount.getCustomerId())
            .restructuringType(restructuringType)
            // Original details
            .originalPrincipal(loanAccount.getAmount().doubleValue())
            .originalInterestRate(loanAccount.getInterestRate() != null ? 
                loanAccount.getInterestRate().doubleValue() : 0.0)
            .originalTerm(loanAccount.getTerm())
            .originalOutstanding(loanAccount.getAccountBalance().doubleValue())
            // New details
            .newPrincipal(newPrincipal)
            .newInterestRate(newInterestRate)
            .newTerm(newTerm)
            .newInstallment(newInstallment)
            .newStartDate(LocalDateTime.now().plusMonths(1))
            // Request details
            .reason(reason)
            .requestedBy(requestedBy)
            .requestedAt(LocalDateTime.now())
            .status("PENDING")
            .totalCostToOrganization(costToOrg > 0 ? costToOrg : 0.0)
            .build();

        restructuring = restructuringRepository.save(restructuring);
        
        log.info("✅ Restructuring request created: ID={}", restructuring.getId());
        return restructuring;
    }

    /**
     * Approve restructuring
     */
    @Transactional
    public LoanRestructuring approveRestructuring(Long restructuringId, String approvedBy, String comments) {
        log.info("Approving restructuring ID: {}", restructuringId);

        LoanRestructuring restructuring = restructuringRepository.findById(restructuringId)
            .orElseThrow(() -> new IllegalStateException("Restructuring request not found"));

        if (!"PENDING".equals(restructuring.getStatus())) {
            throw new IllegalStateException("Only PENDING restructurings can be approved");
        }

        restructuring.setStatus("APPROVED");
        restructuring.setApprovedBy(approvedBy);
        restructuring.setApprovedAt(LocalDateTime.now());
        restructuring.setApprovalComments(comments);

        restructuring = restructuringRepository.save(restructuring);
        
        log.info("✅ Restructuring approved: ID={}", restructuringId);
        return restructuring;
    }

    /**
     * Implement approved restructuring
     */
    @Transactional
    public LoanAccount implementRestructuring(Long restructuringId, String implementedBy) {
        log.info("Implementing restructuring ID: {}", restructuringId);

        LoanRestructuring restructuring = restructuringRepository.findById(restructuringId)
            .orElseThrow(() -> new IllegalStateException("Restructuring request not found"));

        if (!"APPROVED".equals(restructuring.getStatus())) {
            throw new IllegalStateException("Only APPROVED restructurings can be implemented");
        }

        // Get loan account
        LoanAccount loanAccount = loanAccountRepo.findById(restructuring.getLoanAccountId())
            .orElseThrow(() -> new IllegalStateException("Loan account not found"));

        // Archive old schedules (mark as CANCELLED)
        List<LoanRepaymentSchedule> oldSchedules = 
            scheduleRepository.findByLoanAccountIdOrderByInstallmentNumberAsc(loanAccount.getAccountId());
        for (LoanRepaymentSchedule schedule : oldSchedules) {
            if ("PENDING".equals(schedule.getStatus())) {
                schedule.setStatus("CANCELLED");
            }
        }
        scheduleRepository.saveAll(oldSchedules);

        // Update loan account
        if (loanAccount.getPrincipalAmount() != null) {
            loanAccount.setPrincipalAmount(java.math.BigDecimal.valueOf(restructuring.getNewPrincipal()));
        }
        loanAccount.setAmount(restructuring.getNewPrincipal().floatValue());
        
        if (loanAccount.getInterestRate() != null) {
            loanAccount.setInterestRate(java.math.BigDecimal.valueOf(restructuring.getNewInterestRate()));
        }
        loanAccount.setTerm(restructuring.getNewTerm());
        loanAccount.setStatus("RESTRUCTURED");
        
        loanAccount = loanAccountRepo.save(loanAccount);

        // Generate new schedules
        List<LoanRepaymentSchedule> newSchedules = generateNewSchedules(
            loanAccount,
            restructuring.getNewPrincipal(),
            restructuring.getNewInterestRate(),
            restructuring.getNewTerm()
        );
        scheduleRepository.saveAll(newSchedules);

        // Post to accounting (if there's a write-off)
        if (restructuring.getTotalCostToOrganization() > 0) {
            try {
                loanAccountingService.postLoanWriteOff(
                    loanAccount,
                    restructuring.getTotalCostToOrganization(),
                    "Loan Restructuring - " + restructuring.getRestructuringType(),
                    implementedBy
                );
            } catch (Exception e) {
                log.error("Failed to post restructuring to accounting", e);
            }
        }

        // Update restructuring status
        restructuring.setStatus("IMPLEMENTED");
        restructuring.setUpdatedAt(LocalDateTime.now());
        restructuringRepository.save(restructuring);

        log.info("✅ Restructuring implemented successfully for loan ID: {}", loanAccount.getAccountId());
        return loanAccount;
    }

    /**
     * Reject restructuring
     */
    @Transactional
    public LoanRestructuring rejectRestructuring(Long restructuringId, String rejectedBy, String reason) {
        LoanRestructuring restructuring = restructuringRepository.findById(restructuringId)
            .orElseThrow(() -> new IllegalStateException("Restructuring request not found"));

        restructuring.setStatus("REJECTED");
        restructuring.setApprovalComments(reason);
        restructuring.setUpdatedAt(LocalDateTime.now());

        return restructuringRepository.save(restructuring);
    }

    /**
     * Get all pending restructurings
     */
    public List<LoanRestructuring> getPendingRestructurings() {
        return restructuringRepository.findPendingRestructurings();
    }

    /**
     * Get restructuring history for a loan
     */
    public List<LoanRestructuring> getRestructuringHistory(Long loanAccountId) {
        return restructuringRepository.findByLoanAccountId(loanAccountId);
    }

    // Helper methods
    private double calculateNewInstallment(double principal, double interestRate, int term) {
        double totalAmount = principal * (1 + (interestRate / 100));
        return totalAmount / term;
    }

    private List<LoanRepaymentSchedule> generateNewSchedules(
            LoanAccount loanAccount, 
            double principal, 
            double interestRate, 
            int term) {
        
        List<LoanRepaymentSchedule> schedules = new ArrayList<>();
        double installmentAmount = calculateNewInstallment(principal, interestRate, term);
        double principalPerInstallment = principal / term;
        double interestPerInstallment = (principal * (interestRate / 100)) / term;

        LocalDateTime startDate = LocalDateTime.now().plusMonths(1);

        for (int i = 1; i <= term; i++) {
            LoanRepaymentSchedule schedule = new LoanRepaymentSchedule();
            schedule.setLoanAccountId(loanAccount.getAccountId());
            schedule.setInstallmentNumber(i);
            schedule.setInstallmentAmount((float) installmentAmount);
            schedule.setPrincipalPortion((float) principalPerInstallment);
            schedule.setInterestPortion((float) interestPerInstallment);
            schedule.setDueDate(startDate.plusMonths(i - 1));
            schedule.setStatus("PENDING");
            schedules.add(schedule);
        }

        return schedules;
    }
}
```

Continue in next message...
