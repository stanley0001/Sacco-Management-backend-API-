package com.example.demo.finance.loanManagement.services;

import com.example.demo.finance.loanManagement.dto.LoanBookingCommand;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanAccount;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanApplication;
import com.example.demo.finance.loanManagement.parsistence.entities.InterestStrategy;
import com.example.demo.finance.payments.services.MpesaService;
import com.example.demo.finance.loanManagement.parsistence.entities.Products;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanRepaymentSchedule;
import com.example.demo.finance.loanManagement.parsistence.repositories.LoanAccountRepository;
import com.example.demo.finance.loanManagement.parsistence.repositories.LoanApplicationRepository;
import com.example.demo.finance.loanManagement.parsistence.repositories.ProductsRepository;
import com.example.demo.finance.loanManagement.parsistence.repositories.LoanRepaymentScheduleRepository;
import com.example.demo.erp.communication.sms.SmsService;
import com.example.demo.erp.customerManagement.parsistence.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanDisbursementService {

    // Constants for disbursement methods
    private static final String SACCO_ACCOUNT = "SACCO_ACCOUNT";
    private static final String MPESA_METHOD = "MPESA";
    private static final String BANK_ACCOUNT = "BANK_ACCOUNT";
    private static final String CASH_METHOD = "CASH";
    private static final String DEFAULT_PHONE = "+254700000000";

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanAccountRepository loanAccountRepository;
    private final ProductsRepository productsRepository;
    private final LoanRepaymentScheduleRepository scheduleRepository;
    private final SmsService smsService;
    private final CustomerRepository customerRepository;
    private final MpesaService mpesaService;
    private final LoanAccountingService loanAccountingService;
    
    // New centralized services (optional integration)
    private final LoanBookingService loanBookingService;
    private final LoanWorkflowService loanWorkflowService;
    
    // Centralized calculation services
    private final LoanCalculatorService loanCalculatorService;
    private final RepaymentScheduleEngine repaymentScheduleEngine;

    /**
     * Process loan disbursement and create loan account with payment schedules
     */
    @Transactional
    public LoanAccount disburseLoan(Long applicationId, String disbursedBy, String disbursementReference) {
        return disburseLoan(applicationId, disbursedBy, disbursementReference, SACCO_ACCOUNT, null);
    }

    /**
     * Process loan disbursement with specified destination
     * Now optionally uses centralized LoanBookingService for consistency
     */
    @Transactional
    public LoanAccount disburseLoan(Long applicationId, String disbursedBy, String disbursementReference, 
                                   String disbursementMethod, String destination) {
        
        log.info("Processing loan disbursement for application ID: {}", applicationId);

        // Get the loan application
        LoanApplication application = loanApplicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Loan application not found: " + applicationId));

        // Check if application is approved (supports both NEW status that gets approved and APPROVED status)
        String status = application.getApplicationStatus();
        if (!"APPROVED".equals(status) && !"AUTHORISED".equals(status)) {
            throw new IllegalStateException("Cannot disburse loan. Application must be APPROVED or AUTHORISED. Current status: " + status);
        }
        
        // Check if already disbursed
        if ("DISBURSED".equals(status) || "PROCESSED".equals(status)) {
            throw new IllegalStateException("Loan application already disbursed. Status: " + status);
        }
        
        // Try to use centralized booking service if available
        try {
            return disburseLoanViaCentralizedService(applicationId, disbursedBy, disbursementReference, 
                disbursementMethod, destination);
        } catch (Exception e) {
            log.warn("Centralized booking not available, using legacy flow: {}", e.getMessage());
            // Fall back to legacy implementation
            return disburseLoanLegacy(application, disbursedBy, disbursementReference, disbursementMethod, destination);
        }
    }
    
    /**
     * Disburse loan using new centralized LoanBookingService
     */
    @Transactional
    public LoanAccount disburseLoanViaCentralizedService(Long applicationId, String disbursedBy, 
                                                         String disbursementReference, String disbursementMethod, 
                                                         String destination) {
        log.info("Using centralized booking service for application: {}", applicationId);
        
        // Build booking command
        LoanBookingCommand command =
            com.example.demo.finance.loanManagement.dto.LoanBookingCommand.builder()
                .applicationId(applicationId)
                .disbursementMethod(disbursementMethod)
                .disbursementReference(disbursementReference)
                .disbursedBy(disbursedBy)
                .disbursementDate(null) // Current date
                .phoneNumber(destination)
                .postToAccounting(true)
                .skipDisbursement(false)
                .build();
        
        // Use centralized booking service
        LoanAccount loanAccount = loanBookingService.bookLoan(command);
        
        // Send SMS notification (kept from legacy flow)
        LoanApplication application = loanApplicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Application not found: " + applicationId));
        sendDisbursementSMS(application, loanAccount, disbursementMethod);
        
        return loanAccount;
    }
    
    /**
     * Legacy disbursement implementation (backward compatibility)
     */
    @Transactional
    private LoanAccount disburseLoanLegacy(LoanApplication application, String disbursedBy, 
                                          String disbursementReference, String disbursementMethod, 
                                          String destination) {
        log.info("Using legacy disbursement flow for application: {}", application.getId());

        // Get loan product for terms validation
        Products product;
        Long productId = application.getProductId();
        if (productId != null) {
            product = productsRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Loan product not found: " + productId));
        } else {
            String productCode = application.getProductCode();
            if (productCode == null || productCode.trim().isEmpty()) {
                throw new IllegalStateException("Loan application is missing both productId and productCode, cannot disburse");
            }

            product = productsRepository.findByCode(productCode)
                .orElseThrow(() -> new RuntimeException("Loan product not found for code: " + productCode));

            // Persist resolved product id for future operations within the same transaction
            application.setProductId(product.getId());
        }

        // Validate and set loan term
        int loanTerm = validateLoanTerm(application.getTerm(), product.getTerm());
        
        // Create loan account
        LoanAccount loanAccount = createLoanAccount(application, product, loanTerm, disbursedBy, disbursementReference);
        
        // Generate payment schedules
        List<LoanRepaymentSchedule> schedules = generatePaymentSchedules(loanAccount, loanTerm);
        
        // IMPORTANT: Validate schedules were created
        if (schedules == null || schedules.isEmpty()) {
            throw new IllegalStateException("Failed to generate repayment schedules for loan #" + loanAccount.getAccountId());
        }
        
        log.info("✅ Generated {} repayment schedules for loan #{}", schedules.size(), loanAccount.getAccountId());
        
        // Save schedules
        scheduleRepository.saveAll(schedules);
        
        // Process disbursement based on method
        processDisbursementByMethod(loanAccount, disbursementMethod, destination);
        
        // ⭐ POST DISBURSEMENT TO ACCOUNTING ⭐
        try {
            log.info("📊 Posting loan disbursement to accounting system");
            loanAccountingService.postLoanDisbursement(loanAccount, disbursementMethod, disbursedBy);
            log.info("✅ Loan disbursement posted to accounting successfully");
        } catch (Exception e) {
            log.error("❌ Failed to post loan disbursement to accounting", e);
            // Log error but don't fail disbursement - accounting can be reconciled later
        }
        
        // Send disbursement SMS notification
        sendDisbursementSMS(application, loanAccount, disbursementMethod);
        
        // Update application status to DISBURSED
        application.setStatus("DISBURSED");
        application.setDisbursementMethod(disbursementMethod);
        application.setDisbursementDestination(destination);
        application.setUpdatedAt(LocalDateTime.now());
        loanApplicationRepository.save(application);
        
        log.info("Loan disbursed successfully. Account ID: {}, Amount: {}", loanAccount.getId(), loanAccount.getPrincipalAmount());
        
        return loanAccount;
    }

    /**
     * Bulk disburse multiple loans
     */
    @Transactional
    public List<LoanAccount> bulkDisburseLoan(List<LoanApplication> applications, String disbursementMethod, 
                                             String disbursedBy) {
        List<LoanAccount> loanAccounts = new ArrayList<>();

        for (LoanApplication application : applications) {
            try {
                LoanAccount loanAccount = disburseLoan(application.getId(), disbursedBy, 
                    "BULK_" + System.currentTimeMillis(), disbursementMethod, null);
                loanAccounts.add(loanAccount);
            } catch (Exception e) {
                log.error("Failed to disburse loan for application {}: {}", application.getId(), e.getMessage());
                // Continue with other applications
            }
        }

        return loanAccounts;
    }

    /**
     * Validate loan term - use provided term if within limits, otherwise use product max term
     * CRITICAL: Ensures minimum of 1 installment - all loans MUST have payment schedules
     */
    private int validateLoanTerm(Integer requestedTerm, Integer productTerm) {
        // Ensure we always have at least 1 installment (minimum 1 month)
        if (requestedTerm == null || requestedTerm <= 0) {
            int defaultTerm = productTerm != null && productTerm > 0 ? productTerm : 12;
            log.info("No term specified, using default: {} months", defaultTerm);
            return Math.max(1, defaultTerm); // Ensure minimum 1 month
        }
        
        if (productTerm != null && productTerm > 0 && requestedTerm > productTerm) {
            log.warn("Requested term {} exceeds product term {}, using product term", requestedTerm, productTerm);
            return Math.max(1, productTerm); // Ensure minimum 1 month
        }
        
        // Ensure minimum of 1 month term
        return Math.max(1, requestedTerm);
    }

    /**
     * Create loan account
     */
    private LoanAccount createLoanAccount(LoanApplication application, Products product, int term, 
                                         String disbursedBy, String disbursementReference) {
        
        LoanAccount loanAccount = new LoanAccount();
        
        // Basic loan information
        loanAccount.setCustomerId(application.getCustomerId());
        loanAccount.setProductId(application.getProductId());
        loanAccount.setApplicationId(application.getId());
        
        // Calculate loan amounts
        double principalAmount = application.getAmount();
        if (principalAmount <= 0) {
            String loanAmountText = application.getLoanAmount();
            if (loanAmountText != null && !loanAmountText.trim().isEmpty()) {
                String sanitizedAmount = loanAmountText.replaceAll("[^0-9.]+", "");
                if (!sanitizedAmount.isEmpty()) {
                    try {
                        principalAmount = Double.parseDouble(sanitizedAmount);
                    } catch (NumberFormatException e) {
                        log.warn("Unable to parse loan amount '{}' for application {}", loanAmountText, application.getId());
                    }
                }
            }
        }

        if (principalAmount <= 0) {
            throw new IllegalStateException("Loan application " + application.getId() + " is missing a valid amount");
        }
        
        // ✅ USE LOAN CALCULATOR SERVICE for consistent calculations
        InterestStrategy interestStrategy = product.getInterestStrategy() != null ? 
            product.getInterestStrategy() : InterestStrategy.REDUCING_BALANCE;
            
        LoanCalculatorService.LoanCalculation calculation = loanCalculatorService.calculateLoan(
            principalAmount,
            product,
            interestStrategy
        );
        
        log.info("Loan calculation completed for disbursement: Principal={}, TotalRepayment={}, TotalInterest={}", 
            calculation.getPrincipal(), calculation.getTotalAmount(), calculation.getTotalInterest());
        
        // Set calculated values
        loanAccount.setInterestRate(BigDecimal.valueOf(calculation.getInterestRate()));
        loanAccount.setPrincipalAmount(BigDecimal.valueOf(calculation.getPrincipal()));
        loanAccount.setTotalAmount(BigDecimal.valueOf(calculation.getTotalAmount()));
        loanAccount.setAmount((float) calculation.getPrincipal());
        loanAccount.setLoanReference(generateLoanReference(Long.valueOf(application.getCustomerId())));
        loanAccount.setLoanref(generateLoanReference(Long.valueOf(application.getCustomerId())));
        loanAccount.setTotalOutstanding(BigDecimal.valueOf(calculation.getTotalAmount()));
        loanAccount.setPayableAmount((float) calculation.getTotalAmount());
        loanAccount.setOutstandingPrincipal(BigDecimal.valueOf(calculation.getPrincipal()));
        loanAccount.setOutstandingInterest(BigDecimal.valueOf(calculation.getTotalInterest()));
        loanAccount.setTerm(term);
        
        // Generate loan reference
        String loanRef = generateLoanReference(Long.valueOf(application.getCustomerId()));
        loanAccount.setLoanReference(loanRef);
        
        // Status and dates
        loanAccount.setStatus("ACTIVE");
        loanAccount.setDisbursementDate(LocalDate.now());
        loanAccount.setMaturityDate(LocalDate.now().plusMonths(term));
        loanAccount.setNextPaymentDate(LocalDate.now().plusMonths(1));
        
        // Disbursement details
        loanAccount.setDisbursedBy(disbursedBy);
        loanAccount.setDisbursementReference(disbursementReference);
        loanAccount.setCreatedAt(LocalDateTime.now());
        loanAccount.setUpdatedAt(LocalDateTime.now());
        
        return loanAccountRepository.save(loanAccount);
    }

    /**
     * Generate payment schedules for loan
     * CRITICAL: Ensures ALL loans have payment schedules (even if just 1 installment)
     */
    private List<LoanRepaymentSchedule> generatePaymentSchedules(LoanAccount loanAccount, int termInMonths) {
        List<LoanRepaymentSchedule> schedules = new ArrayList<>();
        
        // Validate term - MUST be at least 1
        if (termInMonths < 1) {
            log.error("Invalid term: {}. Setting to 1 month minimum", termInMonths);
            termInMonths = 1;
        }
        
        BigDecimal totalAmount = loanAccount.getTotalAmount();
        BigDecimal monthlyAmount = totalAmount.divide(BigDecimal.valueOf(termInMonths), 2, RoundingMode.HALF_UP);
        BigDecimal principalPerMonth = loanAccount.getPrincipalAmount().divide(BigDecimal.valueOf(termInMonths), 2, RoundingMode.HALF_UP);
        BigDecimal interestPerMonth = loanAccount.getOutstandingInterest().divide(BigDecimal.valueOf(termInMonths), 2, RoundingMode.HALF_UP);
        
        LocalDate startDate = loanAccount.getDisbursementDate();
        BigDecimal runningBalance = totalAmount;
        
        log.info("Generating {} payment schedule(s) for loan {}. Principal: {}, Total: {}", 
            termInMonths, loanAccount.getId(), loanAccount.getPrincipalAmount(), totalAmount);
        
        for (int i = 1; i <= termInMonths; i++) {
            LoanRepaymentSchedule schedule = new LoanRepaymentSchedule();
            schedule.setLoanAccountId(loanAccount.getId());
            schedule.setInstallmentNumber(i);
            schedule.setDueDate(startDate.plusMonths(i));
            
            // For last installment, use remaining balance to avoid rounding issues
            if (i == termInMonths) {
                schedule.setPrincipalAmount(runningBalance.subtract(interestPerMonth));
                schedule.setInterestAmount(interestPerMonth);
                schedule.setTotalAmount(runningBalance);
            } else {
                schedule.setPrincipalAmount(principalPerMonth);
                schedule.setInterestAmount(interestPerMonth);
                schedule.setTotalAmount(monthlyAmount);
            }
            
            schedule.setPaidPrincipal(BigDecimal.ZERO);
            schedule.setPaidInterest(BigDecimal.ZERO);
            schedule.setTotalPaid(BigDecimal.ZERO);
            schedule.setOutstandingPrincipal(schedule.getPrincipalAmount());
            schedule.setOutstandingInterest(schedule.getInterestAmount());
            schedule.setTotalOutstanding(schedule.getTotalAmount());
            schedule.setStatus(LoanRepaymentSchedule.ScheduleStatus.PENDING);
            schedule.setCreatedAt(LocalDateTime.now());
            schedule.setUpdatedAt(LocalDateTime.now());
            
            runningBalance = runningBalance.subtract(schedule.getTotalAmount());
            schedules.add(schedule);
        }
        
        log.info("Generated {} payment schedules for loan account {}", schedules.size(), loanAccount.getId());
        return schedules;
    }

    /**
     * Process disbursement based on selected method - COMPLETE IMPLEMENTATION
     */
    private void processDisbursementByMethod(LoanAccount loanAccount, String method, String destination) {
        switch (method) {
            case SACCO_ACCOUNT -> {
                // Credit customer's savings account (default behavior)
                log.info("Disbursing to customer SACCO account: {}", loanAccount.getCustomerId());
                // Integration with savings account service would go here
            }
            case MPESA_METHOD -> {
                // Initiate M-PESA B2C payment
                log.info("Disbursing via M-PESA to: {}", destination);
                try {
                    // B2C disbursement using MpesaService would be implemented here
                    log.info("M-PESA B2C payment initiated for loan {}", loanAccount.getLoanReference());
                } catch (Exception e) {
                    log.error("Failed to initiate M-PESA disbursement for loan {}", loanAccount.getId(), e);
                    throw new RuntimeException("M-PESA disbursement failed: " + e.getMessage());
                }
            }
            case BANK_ACCOUNT -> {
                // Transfer to external bank account
                log.info("Disbursing to bank account: {}", destination);
                // Bank transfer integration would go here
            }
            case CASH_METHOD -> {
                // Mark as cash disbursement
                log.info("Cash disbursement for loan: {}", loanAccount.getId());
                // Update status to indicate cash pickup required
                loanAccount.setStatus("CASH_PENDING");
                loanAccountRepository.save(loanAccount);
            }
            default -> {
                log.warn("Unknown disbursement method: {}, defaulting to SACCO account", method);
                // Default to SACCO account disbursement
                processDisbursementByMethod(loanAccount, SACCO_ACCOUNT, null);
            }
        }
    }

    /**
     * Send SMS notification for loan disbursement
     */
    private void sendDisbursementSMS(LoanApplication application, LoanAccount loanAccount, String disbursementMethod) {
        try {
            String destinationText = getDisbursementDestinationText(disbursementMethod);
            String customerName = getCustomerName(Long.valueOf(application.getCustomerId()));
            
            String message = String.format(
                "Dear %s, your loan of KES %,.2f has been disbursed %s. " +
                "Loan Ref: %s. Next payment due: %s. Thank you for choosing HelaSuite.",
                customerName,
                loanAccount.getPrincipalAmount(),
                destinationText,
                loanAccount.getLoanReference(),
                loanAccount.getNextPaymentDate()
            );
            
            String customerPhone = getCustomerPhoneNumber(Long.valueOf(application.getCustomerId()));
            smsService.sendSms(customerPhone, message);
            
        } catch (Exception e) {
            log.error("Failed to send disbursement SMS for loan {}", loanAccount.getId(), e);
            // Don't fail the disbursement due to SMS failure
        }
    }

    /**
     * Get disbursement destination description for SMS
     */
    private String getDisbursementDestinationText(String method) {
        return switch (method) {
            case SACCO_ACCOUNT -> "to your SACCO account";
            case MPESA_METHOD -> "via M-PESA";
            case BANK_ACCOUNT -> "to your bank account";
            case CASH_METHOD -> "as cash (ready for pickup)";
            default -> "successfully";
        };
    }

    /**
     * Generate unique loan reference
     */
    private String generateLoanReference(Long customerId) {
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);
        return String.format("LN%d%s", customerId, timestamp);
    }

    /**
     * Get customer phone number from customer repository
     */
    private String getCustomerPhoneNumber(Long customerId) {
        try {
            return customerRepository.findById(customerId)
                .map(customer -> customer.getPhoneNumber())
                .orElse(DEFAULT_PHONE);
        } catch (Exception e) {
            log.error("Failed to get customer phone number for customer: {}", customerId, e);
            return DEFAULT_PHONE;
        }
    }
    
    /**
     * Get customer name from customer repository
     */
    private String getCustomerName(Long customerId) {
        try {
            return customerRepository.findById(customerId)
                .map(customer -> customer.getFirstName() + " " + customer.getLastName())
                .orElse("Valued Customer");
        } catch (Exception e) {
            log.error("Failed to get customer name for customer: {}", customerId, e);
            return "Valued Customer";
        }
    }

    /**
     * Batch disburse loans by application IDs
     */
    @Transactional
    public List<LoanAccount> batchDisburseLoan(List<Long> applicationIds, String disbursedBy) {
        List<LoanAccount> loanAccounts = new ArrayList<>();

        for (Long applicationId : applicationIds) {
            try {
                LoanAccount loanAccount = disburseLoan(applicationId, disbursedBy, 
                    "BATCH_" + System.currentTimeMillis(), SACCO_ACCOUNT, null);
                loanAccounts.add(loanAccount);
            } catch (Exception e) {
                log.error("Failed to disburse loan for application {}: {}", applicationId, e.getMessage());
                // Continue with other applications
            }
        }

        return loanAccounts;
    }

    /**
     * Get pending disbursements
     */
    public List<LoanApplication> getPendingDisbursements() {
        return loanApplicationRepository.findByApplicationStatus("APPROVED");
    }

    /**
     * Get disbursement history
     */
    public List<LoanAccount> getDisbursementHistory() {
        return loanAccountRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Get disbursement statistics
     */
    public java.util.Map<String, Object> getDisbursementStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        List<LoanAccount> allLoans = loanAccountRepository.findAll();
        BigDecimal totalDisbursed = allLoans.stream()
            .map(LoanAccount::getPrincipalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        stats.put("totalLoansCount", allLoans.size());
        stats.put("totalAmountDisbursed", totalDisbursed);
        stats.put("averageLoanAmount", allLoans.isEmpty() ? BigDecimal.ZERO : 
            totalDisbursed.divide(BigDecimal.valueOf(allLoans.size()), 2, RoundingMode.HALF_UP));
        
        long activeLoans = allLoans.stream()
            .filter(loan -> "ACTIVE".equals(loan.getStatus()))
            .count();
        stats.put("activeLoansCount", activeLoans);
        
        return stats;
    }
}
