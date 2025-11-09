package com.example.demo.finance.loanManagement.services;

import com.example.demo.finance.loanManagement.parsistence.entities.LoanAccount;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanApplication;
import com.example.demo.finance.loanManagement.parsistence.repositories.ProductRepo;
import com.example.demo.finance.loanManagement.dto.LoanBookingCommand;
import com.example.demo.finance.loanManagement.parsistence.entities.InterestStrategy;
import com.example.demo.finance.loanManagement.parsistence.entities.*;
import com.example.demo.finance.loanManagement.parsistence.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Centralized service for booking/creating loan accounts from approved applications
 * Handles account creation, schedule generation, and accounting posting
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoanBookingService {
    
    private final ApplicationRepo applicationRepo;
    private final LoanAccountRepo loanAccountRepo;
    private final ProductRepo productRepo;
    private final LoanRepaymentScheduleRepository scheduleRepository;
    private final LoanCalculatorService loanCalculatorService;
    private final LoanAccountingService loanAccountingService;
    private final LoanWorkflowService workflowService;
    private final RepaymentScheduleEngine scheduleEngine;
    
    /**
     * Book a loan account from an approved application
     */
    @Transactional
    public LoanAccount bookLoan(LoanBookingCommand command) {
        log.info("Booking loan for application: {}", command.getApplicationId());
        
        // 1. Get and validate application
        LoanApplication application = applicationRepo.findById(command.getApplicationId())
            .orElseThrow(() -> new IllegalStateException("Application not found: " + command.getApplicationId()));
        
        // 2. Validate application status (allow APPROVED or DISBURSED for uploads)
        validateApplicationStatus(application);
        
        // 3. Get product configuration
        Products product = getProductConfiguration(application);
        
        // 4. Calculate loan terms using calculator
        LoanCalculatorService.LoanCalculation calculation = calculateLoanTerms(application, product);
        
        // 5. Create loan account
        LoanAccount loanAccount = createLoanAccount(application, product, calculation, command);
        
        // 6. Save loan account
        loanAccount = loanAccountRepo.save(loanAccount);
        log.info("Loan account created: ID={}, Reference={}", loanAccount.getAccountId(), loanAccount.getLoanref());
        
        // 7. Generate and save repayment schedules
        List<LoanRepaymentSchedule> schedules = scheduleEngine.generateSchedules(loanAccount, application, product, calculation);
        if (!schedules.isEmpty()) {
            scheduleRepository.saveAll(schedules);
            log.info("Saved {} repayment schedules for loan {}", schedules.size(), loanAccount.getAccountId());
        }
        
        // 8. Post to accounting (if not skipped)
        if (!Boolean.FALSE.equals(command.getPostToAccounting())) {
            postToAccounting(loanAccount, command);
        }
        
        // 9. Update application status to DISBURSED
        workflowService.markDisbursed(application.getApplicationId(), 
            command.getDisbursementMethod(), command.getDisbursementReference());
        
        log.info("Loan booking completed successfully for application {}", command.getApplicationId());
        return loanAccount;
    }
    
    /**
     * Validate application status
     */
    private void validateApplicationStatus(LoanApplication application) {
        String status = application.getApplicationStatus();
        
        // Allow APPROVED, READY_FOR_DISBURSEMENT, or DISBURSED (for uploads)
        if (!"APPROVED".equals(status) && 
            !"READY_FOR_DISBURSEMENT".equals(status) &&
            !"DISBURSED".equals(status)) {
            throw new IllegalStateException(
                "Cannot book loan. Application must be APPROVED, READY_FOR_DISBURSEMENT, or DISBURSED. Current status: " + status);
        }
    }
    
    /**
     * Get product configuration
     */
    private Products getProductConfiguration(LoanApplication application) {
        // Try by productId first
        if (application.getProductId() != null) {
            return productRepo.findById(application.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + application.getProductId()));
        }
        
        // Fall back to product code
        return productRepo.getByCode(application.getProductCode())
            .orElseThrow(() -> new RuntimeException("Product not found: " + application.getProductCode()));
    }
    
    /**
     * Calculate loan terms using LoanCalculatorService
     */
    private LoanCalculatorService.LoanCalculation calculateLoanTerms(LoanApplication application, Products product) {
        double principal = application.getAmount() != null ? 
            application.getAmount() : Double.parseDouble(application.getLoanAmount());
        
        InterestStrategy strategy = product.getInterestStrategy() != null ? 
            product.getInterestStrategy() : InterestStrategy.REDUCING_BALANCE;
        
        return loanCalculatorService.calculateLoan(principal, product, strategy);
    }
    
    /**
     * Create loan account entity
     */
    private LoanAccount createLoanAccount(LoanApplication application, Products product, 
                                         LoanCalculatorService.LoanCalculation calculation,
                                         LoanBookingCommand command) {
        LoanAccount account = new LoanAccount();
        
        // Generate loan reference
        account.setLoanref(generateLoanReference());
        
        // Link to application
        account.setApplicationId(application.getApplicationId());
        
        // Customer info
        account.setCustomerId(application.getCustomerId());
        
        // Product info
        account.setProductId(product.getId());
        
        // Amounts (use both old float fields and new BigDecimal fields)
        account.setAmount((float) calculation.getPrincipal());
        account.setPrincipalAmount(BigDecimal.valueOf(calculation.getPrincipal()));
        account.setPayableAmount((float) calculation.getTotalAmount());
        account.setTotalAmount(BigDecimal.valueOf(calculation.getTotalAmount()));
        account.setAccountBalance((float) calculation.getTotalAmount());
        account.setTotalOutstanding(BigDecimal.valueOf(calculation.getTotalAmount()));
        account.setOutstandingPrincipal(BigDecimal.valueOf(calculation.getPrincipal()));
        account.setOutstandingInterest(BigDecimal.valueOf(calculation.getTotalInterest()));
        
        // Interest
        account.setInterestRate(BigDecimal.valueOf(product.getInterest()));
        
        // Term
        account.setTerm(application.getTerm());
        account.setInstallments(application.getTerm());
        
        // Dates
        LocalDate disbursementDate = command.getDisbursementDate() != null ? 
            command.getDisbursementDate() : LocalDate.now();
        account.setStartDate(disbursementDate.atStartOfDay());
        account.setDisbursementDate(disbursementDate);
        
        LocalDate maturityDate = disbursementDate.plusMonths(application.getTerm());
        account.setDueDate(maturityDate.atTime(23, 59));
        account.setMaturityDate(maturityDate);
        
        // Next payment date (1 month from disbursement)
        account.setNextPaymentDate(disbursementDate.plusMonths(1));
        
        // Status
        account.setStatus("ACTIVE");
        
        // Disbursement details
        account.setDisbursedBy(command.getDisbursedBy());
        account.setDisbursementReference(command.getDisbursementReference());
        
        // Timestamps
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        
        // Other reference
        account.setOtherRef("LOAN-" + account.getLoanref() + "-" + System.currentTimeMillis());
        
        log.debug("Created loan account entity: Principal={}, Total={}, Term={} months", 
            calculation.getPrincipal(), calculation.getTotalAmount(), application.getTerm());
        
        return account;
    }
    
    /**
     * Generate unique loan reference
     */
    private String generateLoanReference() {
        return "LN" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * Post loan disbursement to accounting
     */
    private void postToAccounting(LoanAccount loanAccount, LoanBookingCommand command) {
        try {
            log.info("Posting loan disbursement to accounting");
            loanAccountingService.postLoanDisbursement(
                loanAccount, 
                command.getDisbursementMethod(), 
                command.getDisbursedBy()
            );
            log.info("Loan disbursement posted to accounting successfully");
        } catch (Exception e) {
            log.error("Failed to post loan disbursement to accounting: {}", e.getMessage(), e);
            // Don't fail the booking if accounting fails - can be reconciled later
        }
    }
}
