package com.example.demo.finance.loanManagement.services;

import com.example.demo.erp.customerManagement.parsistence.entities.Customer;
import com.example.demo.erp.customerManagement.parsistence.repositories.CustomerRepository;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanApplication;
import com.example.demo.finance.loanManagement.parsistence.repositories.ProductRepo;
import com.example.demo.finance.loanManagement.dto.LoanApplicationCommand;
import com.example.demo.finance.loanManagement.dto.LoanApplicationResponse;
import com.example.demo.finance.loanManagement.parsistence.entities.Products;
import com.example.demo.finance.loanManagement.parsistence.entities.Subscriptions;
import com.example.demo.finance.loanManagement.parsistence.repositories.ApplicationRepo;
import com.example.demo.finance.loanManagement.parsistence.repositories.SubscriptionRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Centralized orchestrator for all loan application creation
 * Entry point for: Upload, Admin UI, Mobile App, Client Profile, API
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoanApplicationOrchestrator {
    
    private final ApplicationRepo applicationRepo;
    private final CustomerRepository customerRepository;
    private final ProductRepo productRepo;
    private final SubscriptionRepo subscriptionRepo;
    private final LoanCalculatorService loanCalculatorService;
    
    /**
     * Unified application creation - handles all sources
     */
    @Transactional
    public LoanApplicationResponse createApplication(LoanApplicationCommand command) {
        log.info("Creating loan application from source: {}, customer: {}", 
            command.getSource(), command.getCustomerId());
        
        List<String> warnings = new ArrayList<>();
        
        // 1. Resolve and validate customer
        Customer customer = resolveCustomer(command);
        
        // 2. Resolve and validate product
        Products product = resolveProduct(command);
        
        // 3. Validate subscription (if not upload)
        if (!Boolean.TRUE.equals(command.getIsUpload())) {
            validateSubscription(customer, product, warnings);
        }
        
        // 4. Calculate loan preview
        LoanCalculatorService.LoanCalculation calculation = calculateLoanPreview(command, product);
        
        // 5. Create application entity
        LoanApplication application = buildApplicationEntity(command, customer, product);
        
        // 6. Set initial status based on source
        setInitialStatus(application, command, warnings);
        
        // 7. Save application
        application = applicationRepo.save(application);
        
        log.info("Loan application created: ID={}, Status={}, Source={}", 
            application.getApplicationId(), application.getApplicationStatus(), command.getSource());
        
        // 8. Build response
        return buildResponse(application, calculation, warnings);
    }
    
    /**
     * Resolve customer from command - find existing or fail
     */
    private Customer resolveCustomer(LoanApplicationCommand command) {
        Customer customer = null;
        
        // Try by database ID first
        if (command.getCustomerId() != null) {
            customer = customerRepository.findById(command.getCustomerId())
                .orElse(null);
        }
        
        // Try by external ID (for uploads)
        if (customer == null && command.getCustomerExternalId() != null) {
            customer = customerRepository.findByExternalId(command.getCustomerExternalId())
                .orElse(null);
        }
        
        // Try by document number
        if (customer == null && command.getCustomerIdNumber() != null) {
            customer = customerRepository.findByDocumentNumber(command.getCustomerIdNumber())
                .orElse(null);
        }
        
        // Try by phone number
        if (customer == null && command.getCustomerMobileNumber() != null) {
            customer = customerRepository.findByPhoneNumber(command.getCustomerMobileNumber())
                .orElse(null);
        }
        
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found. Please ensure customer exists before creating loan application.");
        }
        
        log.debug("Resolved customer: ID={}, Name={} {}", 
            customer.getId(), customer.getFirstName(), customer.getLastName());
        
        return customer;
    }
    
    /**
     * Resolve product from command
     */
    private Products resolveProduct(LoanApplicationCommand command) {
        Products product = null;
        
        // Try by product ID first
        if (command.getProductId() != null) {
            product = productRepo.findById(command.getProductId())
                .orElse(null);
        }
        
        // Try by product code
        if (product == null && command.getProductCode() != null) {
            product = productRepo.getByCode(command.getProductCode())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + command.getProductCode()));
        }
        
        if (product == null) {
            throw new IllegalArgumentException("Product must be specified");
        }
        
        if (!product.getActive()) {
            throw new IllegalArgumentException("Product is not active: " + product.getCode());
        }
        
        log.debug("Resolved product: ID={}, Code={}, Name={}", 
            product.getId(), product.getCode(), product.getName());
        
        return product;
    }
    
    /**
     * Validate subscription exists for customer and product
     */
    private void validateSubscription(Customer customer, Products product, List<String> warnings) {
        Optional<Subscriptions> subscription = subscriptionRepo
            .findByCustomerIdAndProductCode(String.valueOf(customer.getId()), product.getCode());
        
        if (subscription.isEmpty()) {
            warnings.add("No active subscription found for this product. Application may require manual review.");
        } else if (!subscription.get().getStatus()) {
            warnings.add("Customer subscription is inactive. Application may require manual review.");
        }
    }
    
    /**
     * Calculate loan preview using LoanCalculatorService
     */
    private LoanCalculatorService.LoanCalculation calculateLoanPreview(LoanApplicationCommand command, Products product) {
        try {
            double amount = command.getLoanAmount().doubleValue();
            return loanCalculatorService.calculateLoan(amount, product, product.getInterestStrategy());
        } catch (Exception e) {
            log.warn("Could not calculate loan preview: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Build LoanApplication entity from command
     */
    private LoanApplication buildApplicationEntity(LoanApplicationCommand command, Customer customer, Products product) {
        LoanApplication application = new LoanApplication();
        
        // Generate unique loan number
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 90000) + 10000;
        application.setLoanNumber(timestamp + random);
        
        // Customer info
        application.setCustomerId(String.valueOf(customer.getId()));
        application.setCustomerIdNumber(customer.getDocumentNumber() != null ? 
            customer.getDocumentNumber() : command.getCustomerIdNumber());
        application.setCustomerMobileNumber(customer.getPhoneNumber());
        
        // Loan details
        application.setLoanAmount(command.getLoanAmount().toString());
        application.setProductCode(product.getCode());
        application.setProductId(product.getId());
        application.setLoanTerm(command.getTerm() != null ? command.getTerm().toString() : product.getTerm().toString());
        application.setLoanInterest(command.getInterestRate() != null ? 
            command.getInterestRate().toString() : product.getInterest().toString());
        application.setInstallments(command.getInstallments() != null ? 
            command.getInstallments() : command.getTerm().toString());
        
        // Set term and amount for new fields
        application.setTerm(command.getTerm() != null ? command.getTerm() : product.getTerm());
        application.setAmount(command.getLoanAmount().doubleValue());
        
        // Credit limit
        application.setCreditLimit(command.getLoanAmount().toString());
        
        // Disbursement details
        application.setDisbursementType(command.getDisbursementType() != null ? 
            command.getDisbursementType() : "MPESA");
        application.setDestinationAccount(command.getDestinationAccount() != null ? 
            command.getDestinationAccount() : customer.getPhoneNumber());
        
        // Timestamps
        application.setApplicationTime(command.getDisbursementDate() != null ? 
            command.getDisbursementDate().atStartOfDay() : LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());
        
        return application;
    }
    
    /**
     * Set initial status based on source and command flags
     */
    private void setInitialStatus(LoanApplication application, LoanApplicationCommand command, List<String> warnings) {
        // For uploads - mark as DISBURSED directly
        if (Boolean.TRUE.equals(command.getIsUpload())) {
            application.setApplicationStatus("DISBURSED");
            application.setDisbursementMethod("UPLOADED");
            log.info("Upload application - setting status to DISBURSED");
        } 
        // For other sources - start with NEW
        else {
            application.setApplicationStatus("NEW");
            log.info("New application - setting status to NEW for approval workflow");
        }
    }
    
    /**
     * Build response with calculation preview
     */
    private LoanApplicationResponse buildResponse(LoanApplication application, 
                                                  LoanCalculatorService.LoanCalculation calculation,
                                                  List<String> warnings) {
        LoanApplicationResponse.LoanApplicationResponseBuilder builder = LoanApplicationResponse.builder()
            .success(true)
            .applicationId(application.getApplicationId())
            .loanNumber(application.getLoanNumber())
            .applicationStatus(application.getApplicationStatus())
            .applicationTime(application.getApplicationTime())
            .warnings(warnings.isEmpty() ? null : warnings);
        
        // Set next action based on status
        if ("DISBURSED".equals(application.getApplicationStatus())) {
            builder.nextAction("LOAN_BOOKED")
                   .message("Loan application created and marked as disbursed (from upload)");
        } else if ("NEW".equals(application.getApplicationStatus())) {
            builder.nextAction("AWAITING_REVIEW")
                   .message("Loan application submitted successfully. Awaiting review and approval.")
                   .expectedDisbursementDate("After approval (2-3 business days)");
        }
        
        // Add calculation preview if available
        if (calculation != null) {
            builder.principalAmount(calculation.getPrincipal())
                   .totalInterest(calculation.getTotalInterest())
                   .totalRepayable(calculation.getTotalAmount())
                   .monthlyInstallment(calculation.getMonthlyPayment());
        }
        
        return builder.build();
    }
}
