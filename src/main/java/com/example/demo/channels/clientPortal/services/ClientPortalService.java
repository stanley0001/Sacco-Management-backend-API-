package com.example.demo.channels.clientPortal.services;

import com.example.demo.erp.customerManagement.parsistence.entities.Customer;
import com.example.demo.erp.customerManagement.parsistence.repositories.CustomerRepository;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanAccount;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanApplication;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanRepaymentSchedule;
import com.example.demo.finance.loanManagement.parsistence.entities.Products;
import com.example.demo.finance.loanManagement.parsistence.entities.loanTransactions;
import com.example.demo.finance.loanManagement.parsistence.repositories.LoanAccountRepository;
import com.example.demo.finance.loanManagement.parsistence.repositories.LoanApplicationRepository;
import com.example.demo.finance.loanManagement.parsistence.repositories.LoanRepaymentScheduleRepository;
import com.example.demo.finance.loanManagement.parsistence.repositories.ProductsRepository;
import com.example.demo.finance.loanManagement.parsistence.repositories.TransactionsRepo;
import com.example.demo.finance.loanManagement.services.LoanApplicationOrchestrator;
import com.example.demo.finance.loanManagement.services.PaymentProcessingHub;
import com.example.demo.finance.loanManagement.dto.LoanApplicationCommand;
import com.example.demo.finance.loanManagement.dto.LoanApplicationResponse;
import com.example.demo.finance.loanManagement.dto.PaymentCommand;
import com.example.demo.finance.banking.parsitence.enitities.BankAccounts;
import com.example.demo.finance.banking.parsitence.repositories.BankAccountRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Centralized service for all client-facing operations
 * Used by: Client Portal, Mobile App, USSD
 * Provides unified interface for all customer self-service operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClientPortalService {
    
    private final CustomerRepository customerRepository;
    private final LoanAccountRepository loanAccountRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanRepaymentScheduleRepository scheduleRepository;
    private final ProductsRepository productsRepository;
    private final TransactionsRepo transactionsRepository;
    private final BankAccountRepo bankAccountRepo;
    private final LoanApplicationOrchestrator loanApplicationOrchestrator;
    private final PaymentProcessingHub paymentProcessingHub;
    
    /**
     * Get customer dashboard summary
     */
    public Map<String, Object> getDashboardSummary(String customerId) {
        log.info("Getting dashboard summary for customer: {}", customerId);
        
        Customer customer = customerRepository.findById(Long.valueOf(customerId))
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        // Get all customer loans
        List<LoanAccount> loans = loanAccountRepository.findByCustomerId(customerId);
        
        // Calculate totals
        BigDecimal totalBorrowed = loans.stream()
            .map(LoanAccount::getPrincipalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalOutstanding = loans.stream()
            .map(LoanAccount::getTotalOutstanding)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long activeLoans = loans.stream()
            .filter(l -> "ACTIVE".equals(l.getStatus()))
            .count();
        
        // Get bank accounts balance (ALPHA, Shares, Deposits)
        List<BankAccounts> bankAccounts = bankAccountRepo.findByCustomer(customer)
            .orElse(new ArrayList<>());
        BigDecimal savingsBalance = bankAccounts.stream()
            .map(account -> BigDecimal.valueOf(account.getAccountBalance()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Get pending applications
        List<LoanApplication> pendingApps = loanApplicationRepository.findByCustomerId(Long.valueOf(customerId))
            .stream()
            .filter(app -> "NEW".equals(app.getApplicationStatus()) || 
                          "PENDING_REVIEW".equals(app.getApplicationStatus()))
            .collect(Collectors.toList());
        
        // Build summary
        Map<String, Object> summary = new HashMap<>();
        summary.put("customerName", customer.getFirstName() + " " + customer.getLastName());
        summary.put("customerId", customerId);
        summary.put("phoneNumber", customer.getPhoneNumber());
        summary.put("email", customer.getEmail());
        summary.put("totalLoans", loans.size());
        summary.put("activeLoans", activeLoans);
        summary.put("totalBorrowed", totalBorrowed);
        summary.put("totalOutstanding", totalOutstanding);
        summary.put("savingsBalance", savingsBalance);
        summary.put("pendingApplications", pendingApps.size());
        summary.put("memberSince", customer.getCreatedAt());
        
        return summary;
    }
    
    /**
     * Get all customer loans
     */
    public List<Map<String, Object>> getCustomerLoans(String customerId) {
        log.info("Getting loans for customer: {}", customerId);
        
        List<LoanAccount> loans = loanAccountRepository.findByCustomerId(customerId);
        
        return loans.stream().map(loan -> {
            Map<String, Object> loanInfo = new HashMap<>();
            loanInfo.put("id", loan.getId());
            loanInfo.put("loanReference", loan.getLoanReference());
            loanInfo.put("principalAmount", loan.getPrincipalAmount());
            loanInfo.put("totalAmount", loan.getTotalAmount());
            loanInfo.put("totalOutstanding", loan.getTotalOutstanding());
            loanInfo.put("interestRate", loan.getInterestRate());
            loanInfo.put("status", loan.getStatus());
            loanInfo.put("disbursementDate", loan.getDisbursementDate());
            loanInfo.put("maturityDate", loan.getMaturityDate());
            loanInfo.put("nextPaymentDate", loan.getNextPaymentDate());
            
            // Get product info
            if (loan.getProductId() != null) {
                productsRepository.findById(loan.getProductId()).ifPresent(product -> {
                    loanInfo.put("productName", product.getName());
                    loanInfo.put("productCode", product.getCode());
                });
            }
            
            return loanInfo;
        }).collect(Collectors.toList());
    }
    
    /**
     * Get loan details with schedule
     */
    public Map<String, Object> getLoanDetails(Long loanId, String customerId) {
        log.info("Getting loan details: loanId={}, customerId={}", loanId, customerId);
        
        LoanAccount loan = loanAccountRepository.findById(loanId)
            .orElseThrow(() -> new RuntimeException("Loan not found"));
        
        // Verify ownership
        if (!loan.getCustomerId().equals(customerId)) {
            throw new RuntimeException("Unauthorized access to loan");
        }
        
        // Get repayment schedule
        List<LoanRepaymentSchedule> schedules = scheduleRepository.findByLoanAccountIdOrderByInstallmentNumber(loanId);
        
        // Get payment history
        List<loanTransactions> transactions = transactionsRepository.findByAccountIdOrderByTransactionIdDesc(loanId);
        
        Map<String, Object> details = new HashMap<>();
        details.put("loan", loan);
        details.put("schedule", schedules);
        details.put("transactions", transactions);
        details.put("nextInstallment", getNextInstallment(schedules));
        details.put("overdueInstallments", getOverdueInstallments(schedules));
        
        return details;
    }
    
    /**
     * Get all loan applications for customer
     */
    public List<LoanApplication> getCustomerApplications(String customerId) {
        log.info("Getting applications for customer: {}", customerId);
        return loanApplicationRepository.findByCustomerId(Long.valueOf(customerId));
    }
    
    /**
     * Apply for a loan (self-service)
     */
    @Transactional
    public LoanApplicationResponse applyForLoan(String customerId, String productCode, 
                                               Double amount, Integer term, String purpose) {
        log.info("Customer {} applying for loan: product={}, amount={}", customerId, productCode, amount);
        
        // Verify customer exists
        Customer customer = customerRepository.findById(Long.valueOf(customerId))
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        // Build application command
        LoanApplicationCommand command = LoanApplicationCommand.builder()
            .customerId(Long.valueOf(customerId))
            .customerMobileNumber(customer.getPhoneNumber())
            .customerIdNumber(customer.getIdNumber())
            .productCode(productCode)
            .loanAmount(BigDecimal.valueOf(amount))
            .term(term)
            .loanPurpose(purpose)
            .source(LoanApplicationCommand.ApplicationSource.CLIENT_PORTAL)
            .build();
        
        // Use centralized orchestrator
        return loanApplicationOrchestrator.createApplication(command);
    }
    
    /**
     * Get available loan products
     */
    public List<Map<String, Object>> getAvailableProducts(String customerId) {
        log.info("Getting available products for customer: {}", customerId);
        
        List<Products> products = productsRepository.findAll();
        
        return products.stream()
            .filter(product -> Boolean.TRUE.equals(product.getActive()))
            .map(product -> {
                Map<String, Object> productInfo = new HashMap<>();
                productInfo.put("id", product.getId());
                productInfo.put("code", product.getCode());
                productInfo.put("name", product.getName());
                productInfo.put("minAmount", product.getMinLimit());
                productInfo.put("maxAmount", product.getMaxLimit());
                productInfo.put("interestRate", product.getInterest());
                productInfo.put("term", product.getTerm());
                return productInfo;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Make a loan payment (self-service)
     */
    @Transactional
    public loanTransactions makePayment(String customerId, Long loanId, BigDecimal amount, 
                                       String paymentMethod, String reference) {
        log.info("Customer {} making payment: loan={}, amount={}", customerId, loanId, amount);
        
        // Verify loan ownership
        LoanAccount loan = loanAccountRepository.findById(loanId)
            .orElseThrow(() -> new RuntimeException("Loan not found"));
        
        if (!loan.getCustomerId().equals(customerId)) {
            throw new RuntimeException("Unauthorized access to loan");
        }
        
        // Build payment command
        PaymentCommand command = PaymentCommand.builder()
            .loanReference(loan.getLoanReference())
            .amount(amount)
            .paymentMethod(paymentMethod)
            .referenceNumber(reference)
            .phoneNumber(getCustomerPhone(customerId))
            .build();
        
        // Use centralized payment hub
        return paymentProcessingHub.processPayment(command);
    }
    
    /**
     * Get transaction history
     */
    public List<loanTransactions> getTransactionHistory(String customerId, Integer limit) {
        log.info("Getting transaction history for customer: {}", customerId);
        
        // Get all customer loans
        List<LoanAccount> loans = loanAccountRepository.findByCustomerId(customerId);
        
        // Get transactions for all loans
        List<loanTransactions> allTransactions = new ArrayList<>();
        for (LoanAccount loan : loans) {
            allTransactions.addAll(transactionsRepository.findByAccountIdOrderByTransactionIdDesc(loan.getId()));
        }
        
        // Sort by date descending and limit
        return allTransactions.stream()
            .sorted(Comparator.comparing(loanTransactions::getTransactionDate).reversed())
            .limit(limit != null ? limit : 50)
            .collect(Collectors.toList());
    }
    
    /**
     * Get account statement
     */
    public Map<String, Object> getAccountStatement(String customerId, String startDate, String endDate) {
        log.info("Getting account statement for customer: {}", customerId);
        
        Customer customer = customerRepository.findById(Long.valueOf(customerId))
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        List<LoanAccount> loans = loanAccountRepository.findByCustomerId(customerId);
        List<loanTransactions> transactions = getTransactionHistory(customerId, null);
        
        Map<String, Object> statement = new HashMap<>();
        statement.put("customer", customer);
        statement.put("loans", loans);
        statement.put("transactions", transactions);
        statement.put("generatedDate", LocalDateTime.now());
        statement.put("startDate", startDate);
        statement.put("endDate", endDate);
        
        // Calculate summary
        BigDecimal totalPaid = transactions.stream()
            .map(t -> BigDecimal.valueOf(t.getAmount()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        statement.put("totalPaid", totalPaid);
        statement.put("currentOutstanding", loans.stream()
            .map(LoanAccount::getTotalOutstanding)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        return statement;
    }
    
    /**
     * Update customer profile (limited fields)
     */
    @Transactional
    public Customer updateProfile(String customerId, Map<String, String> updates) {
        log.info("Updating profile for customer: {}", customerId);
        
        Customer customer = customerRepository.findById(Long.valueOf(customerId))
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        // Only allow updating certain fields
        if (updates.containsKey("email")) {
            customer.setEmail(updates.get("email"));
        }
        if (updates.containsKey("phoneNumber")) {
            customer.setPhoneNumber(updates.get("phoneNumber"));
        }
        if (updates.containsKey("address")) {
            customer.setAddress(updates.get("address"));
        }
        
        customer.setUpdatedAt(LocalDateTime.now());
        
        return customerRepository.save(customer);
    }
    
    /**
     * Get bank accounts (ALPHA, Shares, Deposits)
     */
    public List<BankAccounts> getBankAccounts(String customerId) {
        log.info("Getting bank accounts for customer: {}", customerId);
        Customer customer = customerRepository.findById(Long.valueOf(customerId))
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        return bankAccountRepo.findByCustomer(customer).orElse(new ArrayList<>());
    }
    
    /**
     * Check loan eligibility
     */
    public Map<String, Object> checkLoanEligibility(String customerId, String productCode, Double amount) {
        log.info("Checking loan eligibility for customer: {}", customerId);
        
        Customer customer = customerRepository.findById(Long.valueOf(customerId))
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        Products product = productsRepository.findByCode(productCode)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        Map<String, Object> eligibility = new HashMap<>();
        eligibility.put("eligible", true);
        eligibility.put("reasons", new ArrayList<>());
        
        List<String> reasons = new ArrayList<>();
        
        // Check active status
        if (!customer.getIsActive()) {
            eligibility.put("eligible", false);
            reasons.add("Account is not active");
        }
        
        // Check amount limits
        if (product.getMinLimit() != null && amount < product.getMinLimit()) {
            eligibility.put("eligible", false);
            reasons.add("Amount below minimum limit of " + product.getMinLimit());
        }
        if (product.getMaxLimit() != null && amount > product.getMaxLimit()) {
            eligibility.put("eligible", false);
            reasons.add("Amount above maximum limit of " + product.getMaxLimit());
        }
        
        // Check existing loans
        List<LoanAccount> activeLoans = loanAccountRepository.findByCustomerId(customerId)
            .stream()
            .filter(l -> "ACTIVE".equals(l.getStatus()))
            .collect(Collectors.toList());
        
        if (activeLoans.size() >= 3) { // Max 3 active loans (configurable)
            eligibility.put("eligible", false);
            reasons.add("Maximum number of active loans reached");
        }
        
        eligibility.put("reasons", reasons);
        eligibility.put("maxEligibleAmount", product.getMaxLimit());
        eligibility.put("recommendedAmount", calculateRecommendedAmount(customer, product));
        
        return eligibility;
    }
    
    /**
     * Get notifications for customer
     */
    public List<Map<String, Object>> getNotifications(String customerId) {
        log.info("Getting notifications for customer: {}", customerId);
        
        List<Map<String, Object>> notifications = new ArrayList<>();
        
        // Get overdue loans
        List<LoanAccount> loans = loanAccountRepository.findByCustomerId(customerId);
        for (LoanAccount loan : loans) {
            List<LoanRepaymentSchedule> overdue = getOverdueInstallments(
                scheduleRepository.findByLoanAccountId(loan.getId()));
            
            if (!overdue.isEmpty()) {
                Map<String, Object> notification = new HashMap<>();
                notification.put("type", "OVERDUE_PAYMENT");
                notification.put("message", "You have " + overdue.size() + " overdue installment(s) for loan " + loan.getLoanReference());
                notification.put("severity", "HIGH");
                notification.put("loanId", loan.getId());
                notifications.add(notification);
            }
        }
        
        // Get pending applications
        List<LoanApplication> pending = loanApplicationRepository.findByCustomerId(Long.valueOf(customerId))
            .stream()
            .filter(app -> "PENDING_REVIEW".equals(app.getApplicationStatus()))
            .collect(Collectors.toList());
        
        if (!pending.isEmpty()) {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "APPLICATION_PENDING");
            notification.put("message", "You have " + pending.size() + " loan application(s) pending review");
            notification.put("severity", "MEDIUM");
            notifications.add(notification);
        }
        
        return notifications;
    }
    
    // Helper methods
    
    private Map<String, Object> getNextInstallment(List<LoanRepaymentSchedule> schedules) {
        return schedules.stream()
            .filter(s -> s.getStatus() == LoanRepaymentSchedule.ScheduleStatus.PENDING ||
                        s.getStatus() == LoanRepaymentSchedule.ScheduleStatus.OVERDUE)
            .min(Comparator.comparing(LoanRepaymentSchedule::getDueDate))
            .map(schedule -> {
                Map<String, Object> inst = new HashMap<>();
                inst.put("dueDate", schedule.getDueDate());
                inst.put("amount", schedule.getTotalAmount());
                inst.put("installmentNumber", schedule.getInstallmentNumber());
                return inst;
            })
            .orElse(null);
    }
    
    private List<LoanRepaymentSchedule> getOverdueInstallments(List<LoanRepaymentSchedule> schedules) {
        return schedules.stream()
            .filter(s -> s.getStatus() == LoanRepaymentSchedule.ScheduleStatus.OVERDUE)
            .collect(Collectors.toList());
    }
    
    private String getCustomerPhone(String customerId) {
        return customerRepository.findById(Long.valueOf(customerId))
            .map(Customer::getPhoneNumber)
            .orElse(null);
    }
    
    private BigDecimal calculateRecommendedAmount(Customer customer, Products product) {
        // Simple calculation - can be enhanced with credit scoring
        Integer maxLimit = product.getMaxLimit();
        if (maxLimit == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal maxAmount = BigDecimal.valueOf(maxLimit);
        return maxAmount.multiply(BigDecimal.valueOf(0.7)); // 70% of max as recommended
    }
}
