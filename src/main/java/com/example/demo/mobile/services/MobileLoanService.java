package com.example.demo.mobile.services;

import com.example.demo.customerManagement.parsistence.entities.Customer;
import com.example.demo.customerManagement.parsistence.repositories.CustomerRepository;
import com.example.demo.loanManagement.parsistence.entities.LoanAccount;
import com.example.demo.loanManagement.parsistence.entities.LoanApplication;
import com.example.demo.loanManagement.parsistence.entities.Products;
import com.example.demo.loanManagement.parsistence.repositories.ApplicationRepo;
import com.example.demo.loanManagement.parsistence.repositories.LoanAccountRepo;
import com.example.demo.loanManagement.parsistence.repositories.ProductRepo;
import com.example.demo.mobile.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MobileLoanService {

    private final LoanAccountRepo loanAccountRepo;
    private final ApplicationRepo applicationRepo;
    private final ProductRepo productRepo;
    private final CustomerRepository customerRepository;

    public List<LoanSummaryDto> getMemberLoans(String memberId) {
        log.info("Fetching loans for member: {}", memberId);
        
        List<LoanAccount> loans = loanAccountRepo.findByCustomerId(memberId);
        
        return loans.stream()
                .map(this::convertToLoanSummary)
                .collect(Collectors.toList());
    }

    public LoanDetailDto getLoanDetails(String loanId, String memberId) {
        log.info("Fetching loan details for loan: {}", loanId);
        
        LoanAccount loan = loanAccountRepo.findById(Long.valueOf(loanId))
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        
        // Verify ownership
        if (!loan.getCustomerId().equals(memberId)) {
            throw new RuntimeException("Unauthorized access to loan");
        }
        
        return convertToLoanDetail(loan);
    }

    public List<RepaymentScheduleDto> getRepaymentSchedule(String loanId, String memberId) {
        log.info("Fetching repayment schedule for loan: {}", loanId);
        
        LoanAccount loan = loanAccountRepo.findById(Long.valueOf(loanId))
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        
        if (!loan.getCustomerId().equals(memberId)) {
            throw new RuntimeException("Unauthorized access");
        }
        
        return generateRepaymentSchedule(loan);
    }

    public List<LoanProductDto> getAvailableLoanProducts() {
        log.info("Fetching available loan products");
        
        List<Products> products = productRepo.findAll().stream()
                .filter(Products::getActive)
                .collect(Collectors.toList());
        
        return products.stream()
                .map(this::convertToLoanProductDto)
                .collect(Collectors.toList());
    }

    public EligibilityResponseDto checkLoanEligibility(String memberId, String productId) {
        log.info("Checking loan eligibility for member: {}, product: {}", memberId, productId);
        
        Customer customer = customerRepository.findById(Long.valueOf(memberId))
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        Products product = productRepo.findById(Long.valueOf(productId))
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Check existing loans
        List<LoanAccount> existingLoans = loanAccountRepo.findByCustomerId(memberId);
        long activeLoans = existingLoans.stream()
                .filter(loan -> "ACTIVE".equalsIgnoreCase(loan.getStatus()))
                .count();
        
        BigDecimal existingBalance = existingLoans.stream()
                .filter(loan -> "ACTIVE".equalsIgnoreCase(loan.getStatus()))
                .map(loan -> BigDecimal.valueOf(loan.getAccountBalance()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        boolean eligible = true;
        List<String> missingRequirements = new ArrayList<>();
        String reason = "Eligible for loan";
        
        // Check eligibility criteria
        if (activeLoans >= 3) {
            eligible = false;
            reason = "Maximum number of active loans reached (3)";
            missingRequirements.add("Clear at least one existing loan");
        }
        
        if (existingBalance.compareTo(BigDecimal.valueOf(500000)) > 0) {
            eligible = false;
            reason = "Outstanding loan balance too high";
            missingRequirements.add("Reduce existing loan balance below KES 500,000");
        }
        
        // Calculate max loan amount (3x savings balance - simplified)
        BigDecimal maxLoanAmount = BigDecimal.valueOf(product.getMaxLimit());
        BigDecimal recommendedAmount = maxLoanAmount.multiply(BigDecimal.valueOf(0.7));
        
        List<String> requirements = Arrays.asList(
                "Active SACCO membership",
                "Minimum 6 months membership",
                "Regular savings contributions",
                "Good repayment history",
                "Valid identification"
        );
        
        return EligibilityResponseDto.builder()
                .eligible(eligible)
                .maxLoanAmount(maxLoanAmount)
                .recommendedAmount(recommendedAmount)
                .reason(reason)
                .requirements(requirements)
                .missingRequirements(missingRequirements)
                .existingLoanBalance(existingBalance)
                .creditScore(eligible ? 750 : 650)
                .build();
    }

    @Transactional
    public LoanApplicationResponseDto applyForLoan(String memberId, LoanApplicationRequest request) {
        log.info("Processing loan application for member: {}, amount: {}", memberId, request.getAmount());
        
        // Verify customer exists
        Customer customer = customerRepository.findById(Long.valueOf(memberId))
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        // Verify product exists
        Products product = productRepo.findById(Long.valueOf(request.getProductId()))
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Validate amount within limits
        if (request.getAmount().compareTo(BigDecimal.valueOf(product.getMinLimit())) < 0 ||
            request.getAmount().compareTo(BigDecimal.valueOf(product.getMaxLimit())) > 0) {
            throw new RuntimeException("Amount outside product limits");
        }
        
        // Create loan application
        LoanApplication application = new LoanApplication();
        application.setCustomerId(memberId);
        application.setCustomerIdNumber(customer.getDocumentNumber());
        application.setCustomerMobileNumber(customer.getPhoneNumber());
        application.setLoanAmount(request.getAmount().toString());
        application.setProductCode(product.getCode());
        application.setLoanTerm(request.getTerm().toString());
        application.setLoanInterest(product.getInterest().toString());
        application.setApplicationStatus("NEW");
        application.setApplicationTime(LocalDateTime.now());
        application.setLoanNumber(System.currentTimeMillis());
        application.setDestinationAccount(customer.getPhoneNumber());
        application.setDisbursementType("MPESA");
        application.setCreditLimit("0");
        
        application = applicationRepo.save(application);
        
        log.info("Loan application created successfully: {}", application.getApplicationId());
        
        return LoanApplicationResponseDto.builder()
                .success(true)
                .applicationId(application.getApplicationId().toString())
                .applicationNumber(application.getLoanNumber().toString())
                .message("Loan application submitted successfully")
                .status("SUBMITTED")
                .requestedAmount(request.getAmount())
                .approvedAmount(null)
                .productName(product.getName())
                .applicationDate(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .expectedDisbursementDate("Within 2-3 business days")
                .build();
    }

    @Transactional
    public TransactionResponseDto makeLoanRepayment(String loanId, String memberId, LoanRepaymentRequest request) {
        log.info("Processing loan repayment for loan: {}, amount: {}", loanId, request.getAmount());
        
        // Verify customer and PIN
        Customer customer = customerRepository.findById(Long.valueOf(memberId))
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        if (!verifyPin(request.getPin(), customer.getPinHash())) {
            throw new RuntimeException("Invalid PIN");
        }
        
        // Get loan
        LoanAccount loan = loanAccountRepo.findById(Long.valueOf(loanId))
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        
        if (!loan.getCustomerId().equals(memberId)) {
            throw new RuntimeException("Unauthorized access");
        }
        
        // Validate repayment amount
        if (request.getAmount().compareTo(BigDecimal.valueOf(loan.getAccountBalance())) > 0) {
            throw new RuntimeException("Repayment amount exceeds outstanding balance");
        }
        
        // Update loan balance
        float newBalance = loan.getAccountBalance() - request.getAmount().floatValue();
        loan.setAccountBalance(newBalance);
        
        // Update status if fully paid
        if (newBalance <= 0) {
            loan.setStatus("COMPLETED");
        }
        
        loanAccountRepo.save(loan);
        
        String transactionRef = "LRP" + System.currentTimeMillis();
        
        log.info("Loan repayment successful. New balance: {}", newBalance);
        
        return TransactionResponseDto.builder()
                .success(true)
                .transactionId(UUID.randomUUID().toString())
                .transactionRef(transactionRef)
                .message("Loan repayment successful")
                .amount(request.getAmount())
                .newBalance(BigDecimal.valueOf(newBalance))
                .transactionDate(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .receiptNumber(transactionRef)
                .build();
    }

    public List<TransactionDto> getLoanTransactions(String loanId, String memberId) {
        log.info("Fetching transactions for loan: {}", loanId);
        
        LoanAccount loan = loanAccountRepo.findById(Long.valueOf(loanId))
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        
        if (!loan.getCustomerId().equals(memberId)) {
            throw new RuntimeException("Unauthorized access");
        }
        
        // Mock transactions - in production, fetch from transaction table
        return generateMockLoanTransactions(loan);
    }

    @Transactional
    public LoanApplicationResponseDto requestLoanTopUp(String loanId, String memberId, LoanTopUpRequest request) {
        log.info("Processing loan top-up request for loan: {}, amount: {}", loanId, request.getTopUpAmount());
        
        // Verify customer and PIN
        Customer customer = customerRepository.findById(Long.valueOf(memberId))
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        if (!verifyPin(request.getPin(), customer.getPinHash())) {
            throw new RuntimeException("Invalid PIN");
        }
        
        // Get existing loan
        LoanAccount loan = loanAccountRepo.findById(Long.valueOf(loanId))
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        
        if (!loan.getCustomerId().equals(memberId)) {
            throw new RuntimeException("Unauthorized access");
        }
        
        // Check if top-up is allowed
        if (!"ACTIVE".equalsIgnoreCase(loan.getStatus())) {
            throw new RuntimeException("Top-up only allowed for active loans");
        }
        
        // Create top-up application
        LoanApplication application = new LoanApplication();
        application.setCustomerId(memberId);
        application.setCustomerIdNumber(customer.getDocumentNumber());
        application.setCustomerMobileNumber(customer.getPhoneNumber());
        application.setLoanAmount(request.getTopUpAmount().toString());
        application.setApplicationStatus("NEW");
        application.setApplicationTime(LocalDateTime.now());
        application.setLoanNumber(System.currentTimeMillis());
        application.setDestinationAccount(customer.getPhoneNumber());
        application.setDisbursementType("MPESA");
        
        application = applicationRepo.save(application);
        
        return LoanApplicationResponseDto.builder()
                .success(true)
                .applicationId(application.getApplicationId().toString())
                .applicationNumber(application.getLoanNumber().toString())
                .message("Loan top-up request submitted successfully")
                .status("PENDING_APPROVAL")
                .requestedAmount(request.getTopUpAmount())
                .applicationDate(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .expectedDisbursementDate("Within 2-3 business days after approval")
                .build();
    }

    // Helper methods
    private LoanSummaryDto convertToLoanSummary(LoanAccount loan) {
        return LoanSummaryDto.builder()
                .loanId(loan.getAccountId().toString())
                .loanNumber(loan.getLoanref())
                .productName("Loan Product") // In production, fetch from product
                .productCode("LN" + loan.getAccountId())
                .principalAmount(BigDecimal.valueOf(loan.getAmount()))
                .outstandingBalance(BigDecimal.valueOf(loan.getAccountBalance()))
                .totalRepayable(BigDecimal.valueOf(loan.getPayableAmount()))
                .paidAmount(BigDecimal.valueOf(loan.getPayableAmount() - loan.getAccountBalance()))
                .interestRate(BigDecimal.valueOf(10)) // Mock rate
                .status(loan.getStatus())
                .disbursementDate(loan.getStartDate() != null ? 
                        loan.getStartDate().toString() : null)
                .maturityDate(loan.getDueDate() != null ? 
                        loan.getDueDate().toString() : null)
                .nextPaymentDate(loan.getDueDate() != null ? 
                        loan.getDueDate().toString() : null)
                .nextPaymentAmount(BigDecimal.valueOf(loan.getAccountBalance() / 12))
                .installmentsPaid(0)
                .totalInstallments(12)
                .daysOverdue(0)
                .build();
    }

    private LoanDetailDto convertToLoanDetail(LoanAccount loan) {
        return LoanDetailDto.builder()
                .loanId(loan.getAccountId().toString())
                .loanNumber(loan.getLoanref())
                .productCode("LN" + loan.getAccountId())
                .productName("Loan Product")
                .principalAmount(BigDecimal.valueOf(loan.getAmount()))
                .totalInterest(BigDecimal.valueOf(loan.getPayableAmount() - loan.getAmount()))
                .totalRepayable(BigDecimal.valueOf(loan.getPayableAmount()))
                .paidAmount(BigDecimal.valueOf(loan.getPayableAmount() - loan.getAccountBalance()))
                .outstandingBalance(BigDecimal.valueOf(loan.getAccountBalance()))
                .interestRate(BigDecimal.valueOf(10))
                .interestType("REDUCING_BALANCE")
                .loanTerm(12)
                .termUnit("MONTHS")
                .status(loan.getStatus())
                .disbursementDate(loan.getStartDate() != null ? 
                        loan.getStartDate().toString() : null)
                .maturityDate(loan.getDueDate() != null ? 
                        loan.getDueDate().toString() : null)
                .penaltyAmount(BigDecimal.ZERO)
                .daysOverdue(0)
                .nextPaymentDate(loan.getDueDate() != null ? 
                        loan.getDueDate().toString() : null)
                .nextPaymentAmount(BigDecimal.valueOf(loan.getAccountBalance() / 12))
                .installmentsPaid(0)
                .totalInstallments(12)
                .repaymentSchedule(generateRepaymentSchedule(loan))
                .build();
    }

    private LoanProductDto convertToLoanProductDto(Products product) {
        return LoanProductDto.builder()
                .productId(product.getId().toString())
                .productCode(product.getCode())
                .productName(product.getName())
                .description("Flexible loan product with competitive rates")
                .minAmount(BigDecimal.valueOf(product.getMinLimit()))
                .maxAmount(BigDecimal.valueOf(product.getMaxLimit()))
                .interestRate(BigDecimal.valueOf(product.getInterest()))
                .interestType(product.getInterestStrategy() != null ? 
                        product.getInterestStrategy().toString() : "REDUCING_BALANCE")
                .minTerm(1)
                .maxTerm(product.getTerm())
                .termUnit(product.getTimeSpan())
                .eligibilityCriteria(Arrays.asList(
                        "Active membership",
                        "Regular savings",
                        "Good credit history"
                ))
                .processingFee(BigDecimal.valueOf(500))
                .processingFeeRate(BigDecimal.valueOf(1))
                .topUpAllowed(product.getTopUp())
                .earlyRepaymentAllowed(product.getAllowEarlyRepayment())
                .earlyRepaymentPenalty(BigDecimal.valueOf(product.getEarlyRepaymentPenalty()))
                .isActive(product.getActive())
                .build();
    }

    private List<RepaymentScheduleDto> generateRepaymentSchedule(LoanAccount loan) {
        List<RepaymentScheduleDto> schedule = new ArrayList<>();
        int installments = 12;
        float monthlyPayment = loan.getPayableAmount() / installments;
        float balance = loan.getPayableAmount();
        
        for (int i = 1; i <= installments; i++) {
            balance -= monthlyPayment;
            schedule.add(RepaymentScheduleDto.builder()
                    .installmentNumber(i)
                    .dueDate(LocalDateTime.now().plusMonths(i).format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .principalAmount(BigDecimal.valueOf(loan.getAmount() / installments))
                    .interestAmount(BigDecimal.valueOf(monthlyPayment - (loan.getAmount() / installments)))
                    .totalPayment(BigDecimal.valueOf(monthlyPayment))
                    .balanceAfterPayment(BigDecimal.valueOf(Math.max(0, balance)))
                    .status(i <= 2 ? "PAID" : "PENDING")
                    .amountPaid(i <= 2 ? BigDecimal.valueOf(monthlyPayment) : BigDecimal.ZERO)
                    .amountDue(i <= 2 ? BigDecimal.ZERO : BigDecimal.valueOf(monthlyPayment))
                    .paymentDate(i <= 2 ? LocalDateTime.now().minusMonths(3 - i)
                            .format(DateTimeFormatter.ISO_LOCAL_DATE) : null)
                    .build());
        }
        
        return schedule;
    }

    private List<TransactionDto> generateMockLoanTransactions(LoanAccount loan) {
        List<TransactionDto> transactions = new ArrayList<>();
        BigDecimal balance = BigDecimal.valueOf(loan.getPayableAmount());
        
        // Disbursement
        transactions.add(TransactionDto.builder()
                .transactionId(UUID.randomUUID().toString())
                .transactionDate(LocalDateTime.now().minusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .transactionType("LOAN_DISBURSEMENT")
                .description("Loan disbursed to M-Pesa")
                .amount(BigDecimal.valueOf(loan.getAmount()))
                .debitCredit("CR")
                .balanceBefore(BigDecimal.ZERO)
                .balanceAfter(BigDecimal.valueOf(loan.getPayableAmount()))
                .reference("DISB" + loan.getAccountId())
                .status("COMPLETED")
                .channel("MPESA")
                .build());
        
        // Repayments
        for (int i = 1; i <= 2; i++) {
            BigDecimal payment = BigDecimal.valueOf(loan.getPayableAmount() / 12);
            balance = balance.subtract(payment);
            
            transactions.add(TransactionDto.builder()
                    .transactionId(UUID.randomUUID().toString())
                    .transactionDate(LocalDateTime.now().minusDays(30 - (i * 10))
                            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .transactionType("LOAN_REPAYMENT")
                    .description("Monthly installment payment")
                    .amount(payment)
                    .debitCredit("DR")
                    .balanceBefore(balance.add(payment))
                    .balanceAfter(balance)
                    .reference("REP" + loan.getAccountId() + "_" + i)
                    .status("COMPLETED")
                    .channel("MPESA")
                    .build());
        }
        
        return transactions;
    }

    private boolean verifyPin(String pin, String hashedPin) {
        if (hashedPin == null) return false;
        return BCrypt.checkpw(pin, hashedPin);
    }
}
