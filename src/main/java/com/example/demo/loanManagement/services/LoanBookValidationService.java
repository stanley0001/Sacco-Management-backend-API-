package com.example.demo.loanManagement.services;

import com.example.demo.customerManagement.parsistence.entities.Customer;
import com.example.demo.customerManagement.parsistence.repositories.CustomerRepository;
import com.example.demo.loanManagement.dto.LoanBookUploadDTO;
import com.example.demo.loanManagement.parsistence.entities.Products;
import com.example.demo.loanManagement.parsistence.repositories.ProductRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanBookValidationService {
    
    private final CustomerRepository customerRepository;
    private final ProductRepo productRepo;
    
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(?:254|\\+254|0)?[17]\\d{8}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Set<String> VALID_STATUSES = Set.of("ACTIVE", "CLOSED", "DEFAULTED", "WRITTEN_OFF");
    
    /**
     * Validate list of loans
     */
    public List<LoanBookUploadDTO> validateLoans(List<LoanBookUploadDTO> loans) {
        log.info("Validating {} loans", loans.size());
        
        List<LoanBookUploadDTO> validatedLoans = new ArrayList<>();
        Set<String> processedCustomers = new HashSet<>();
        
        for (LoanBookUploadDTO loan : loans) {
            validateLoan(loan, processedCustomers);
            validatedLoans.add(loan);
        }
        
        log.info("Validation complete: {} loans processed", validatedLoans.size());
        return validatedLoans;
    }
    
    /**
     * Validate a single loan
     */
    private void validateLoan(LoanBookUploadDTO loan, Set<String> processedCustomers) {
        List<String> errors = new ArrayList<>();
        
        // Validate required fields
        validateRequiredFields(loan, errors);
        
        // Validate data formats
        if (loan.getPhoneNumber() != null) {
            validatePhoneNumber(loan.getPhoneNumber(), errors);
        }
        
        if (loan.getEmail() != null && !loan.getEmail().isEmpty()) {
            validateEmail(loan.getEmail(), errors);
        }
        
        // Validate numerical values
        validateNumericalValues(loan, errors);
        
        // Validate dates
        validateDates(loan, errors);
        
        // Validate status
        validateStatus(loan, errors);
        
        // Validate customer exists
        if (loan.getCustomerId() != null) {
            validateCustomer(loan, errors);
        }
        
        // Validate product exists
        if (loan.getProductCode() != null) {
            validateProduct(loan, errors);
        }
        
        // Check for duplicate customer in same upload
        if (loan.getCustomerId() != null && processedCustomers.contains(loan.getCustomerId())) {
            errors.add("Duplicate customer in upload. Customer " + loan.getCustomerId() + " appears multiple times");
        } else if (loan.getCustomerId() != null) {
            processedCustomers.add(loan.getCustomerId());
        }
        
        // Set validation result
        if (errors.isEmpty()) {
            loan.setIsValid(true);
            loan.setErrorMessage(null);
        } else {
            loan.setIsValid(false);
            loan.setErrorMessage(String.join("; ", errors));
        }
    }
    
    /**
     * Validate required fields
     */
    private void validateRequiredFields(LoanBookUploadDTO loan, List<String> errors) {
        if (isEmpty(loan.getCustomerId())) {
            errors.add("Customer ID is required");
        }
        if (isEmpty(loan.getCustomerName())) {
            errors.add("Customer Name is required");
        }
        if (isEmpty(loan.getPhoneNumber())) {
            errors.add("Phone Number is required");
        }
        if (isEmpty(loan.getProductCode())) {
            errors.add("Product Code is required");
        }
        if (loan.getPrincipal() == null) {
            errors.add("Principal is required");
        }
        if (loan.getInterestRate() == null) {
            errors.add("Interest Rate is required");
        }
        if (loan.getTerm() == null) {
            errors.add("Term is required");
        }
        if (loan.getDisbursementDate() == null) {
            errors.add("Disbursement Date is required");
        }
        if (isEmpty(loan.getStatus())) {
            errors.add("Status is required");
        }
    }
    
    /**
     * Validate phone number format
     */
    private void validatePhoneNumber(String phone, List<String> errors) {
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            errors.add("Invalid phone number format. Use format: 254XXXXXXXXX or 07XXXXXXXX");
        }
    }
    
    /**
     * Validate email format
     */
    private void validateEmail(String email, List<String> errors) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            errors.add("Invalid email format");
        }
    }
    
    /**
     * Validate numerical values
     */
    private void validateNumericalValues(LoanBookUploadDTO loan, List<String> errors) {
        if (loan.getPrincipal() != null && loan.getPrincipal() <= 0) {
            errors.add("Principal must be greater than 0");
        }
        
        if (loan.getInterestRate() != null && (loan.getInterestRate() < 0 || loan.getInterestRate() > 100)) {
            errors.add("Interest Rate must be between 0 and 100");
        }
        
        if (loan.getTerm() != null && loan.getTerm() <= 0) {
            errors.add("Term must be greater than 0");
        }
        
        if (loan.getOutstandingBalance() != null && loan.getOutstandingBalance() < 0) {
            errors.add("Outstanding Balance cannot be negative");
        }
        
        if (loan.getTotalPaid() != null && loan.getTotalPaid() < 0) {
            errors.add("Total Paid cannot be negative");
        }
        
        if (loan.getPaymentsMade() != null && loan.getPaymentsMade() < 0) {
            errors.add("Payments Made cannot be negative");
        }
        
        // Validate outstanding balance logic
        if (loan.getPrincipal() != null && loan.getOutstandingBalance() != null && 
            loan.getOutstandingBalance() > loan.getPrincipal()) {
            errors.add("Outstanding Balance cannot exceed Principal");
        }
    }
    
    /**
     * Validate dates
     */
    private void validateDates(LoanBookUploadDTO loan, List<String> errors) {
        if (loan.getDisbursementDate() != null) {
            if (loan.getDisbursementDate().isAfter(LocalDate.now())) {
                errors.add("Disbursement Date cannot be in the future");
            }
            
            // Check if date is too old (e.g., more than 10 years ago)
            if (loan.getDisbursementDate().isBefore(LocalDate.now().minusYears(10))) {
                errors.add("Disbursement Date is too old (more than 10 years ago)");
            }
        }
        
        if (loan.getLastPaymentDate() != null) {
            if (loan.getLastPaymentDate().isAfter(LocalDate.now())) {
                errors.add("Last Payment Date cannot be in the future");
            }
            
            if (loan.getDisbursementDate() != null && 
                loan.getLastPaymentDate().isBefore(loan.getDisbursementDate())) {
                errors.add("Last Payment Date cannot be before Disbursement Date");
            }
        }
    }
    
    /**
     * Validate status
     */
    private void validateStatus(LoanBookUploadDTO loan, List<String> errors) {
        if (loan.getStatus() != null && !VALID_STATUSES.contains(loan.getStatus().toUpperCase())) {
            errors.add("Invalid status. Must be one of: " + String.join(", ", VALID_STATUSES));
        }
        
        // Validate status logic
        if ("CLOSED".equals(loan.getStatus()) && loan.getOutstandingBalance() != null && loan.getOutstandingBalance() > 0) {
            errors.add("CLOSED loans must have Outstanding Balance of 0");
        }
    }
    
    /**
     * Validate customer exists
     */
    private void validateCustomer(LoanBookUploadDTO loan, List<String> errors) {
        try {
            Customer customer = customerRepository.findById(Long.parseLong(loan.getCustomerId()))
                .orElse(null);
            
            if (customer == null) {
                errors.add("Customer not found: " + loan.getCustomerId());
            } else {
                // Validate customer name matches
                String expectedName = (customer.getFirstName() + " " + customer.getLastName()).trim();
                if (!expectedName.equalsIgnoreCase(loan.getCustomerName().trim())) {
                    errors.add("Customer name mismatch. Expected: " + expectedName + ", Found: " + loan.getCustomerName());
                }
            }
        } catch (NumberFormatException e) {
            errors.add("Invalid Customer ID format");
        } catch (Exception e) {
            log.error("Error validating customer: {}", e.getMessage());
            errors.add("Error validating customer: " + e.getMessage());
        }
    }
    
    /**
     * Validate product exists
     */
    private void validateProduct(LoanBookUploadDTO loan, List<String> errors) {
        try {
            Products product = productRepo.getByCode(loan.getProductCode())
                .orElse(null);
            
            if (product == null) {
                errors.add("Product not found: " + loan.getProductCode());
            } else {
                // Validate loan amount against product limits
                if (loan.getPrincipal() != null) {
                    if (loan.getPrincipal() < product.getMinLimit()) {
                        errors.add("Principal below product minimum: " + product.getMinLimit());
                    }
                    if (loan.getPrincipal() > product.getMaxLimit()) {
                        errors.add("Principal exceeds product maximum: " + product.getMaxLimit());
                    }
                }
                
                // Set product name if not provided
                if (isEmpty(loan.getProductName())) {
                    loan.setProductName(product.getName());
                }
            }
        } catch (Exception e) {
            log.error("Error validating product: {}", e.getMessage());
            errors.add("Error validating product: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to check if string is empty
     */
    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
