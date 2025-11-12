package com.example.demo.finance.loanManagement.services;

import com.example.demo.erp.customerManagement.parsistence.entities.Customer;
import com.example.demo.erp.customerManagement.parsistence.repositories.CustomerRepository;
import com.example.demo.erp.customerManagement.services.CustomerCreationService;
import com.example.demo.finance.loanManagement.parsistence.repositories.ProductRepo;
import com.example.demo.finance.loanManagement.dto.LoanBookUploadDTO;
import com.example.demo.finance.loanManagement.parsistence.entities.Products;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanBookValidationService {
    
    private final CustomerRepository customerRepository;
    private final ProductRepo productRepo;
    private final CustomerCreationService customerCreationService;
    
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(?:254|\\+254|0)?[17]\\d{8}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Set<String> VALID_STATUSES = Set.of("ACTIVE", "CLOSED", "DEFAULTED", "WRITTEN_OFF");
    
    /**
     * Validate list of loans
     */
    public List<LoanBookUploadDTO> validateLoans(List<LoanBookUploadDTO> loans) {
        log.info("Validating {} loans", loans.size());
        
        List<LoanBookUploadDTO> validatedLoans = new ArrayList<>();
        
        for (LoanBookUploadDTO loan : loans) {
            validateLoan(loan);
            validatedLoans.add(loan);
        }
        
        log.info("Validation complete: {} loans processed", validatedLoans.size());
        return validatedLoans;
    }
    
    /**
     * Validate a single loan
     */
    private void validateLoan(LoanBookUploadDTO loan) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
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
        
        // Validate status (with warnings)
        validateStatus(loan, warnings);
        
        // Validate customer exists (warnings only)
        if (loan.getCustomerId() != null) {
            validateCustomer(loan, errors, warnings);
        }
        
        // Validate product exists (warnings only)
        if (loan.getProductCode() != null) {
            validateProduct(loan, errors, warnings);
        }
        
        // Set validation result
        if (errors.isEmpty()) {
            loan.setIsValid(true);
            loan.setErrorMessage(null);
        } else {
            loan.setIsValid(false);
            loan.setErrorMessage(String.join("; ", errors));
        }
        
        // Set warnings (non-blocking)
        if (!warnings.isEmpty()) {
            loan.setWarningMessage(String.join("; ", warnings));
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
        
        // Note: Outstanding balance CAN exceed principal due to interest, penalties, and late fees
        // This is a valid business scenario, so we don't validate against it
    }
    
    /**
     * Validate dates (supports backdating for loan book imports)
     */
    private void validateDates(LoanBookUploadDTO loan, List<String> errors) {
        if (loan.getDisbursementDate() != null) {
            if (loan.getDisbursementDate().isAfter(LocalDate.now())) {
                errors.add("Disbursement Date cannot be in the future");
            }
            
            // Allow backdating without restriction for importing historical loans
            // Check if date is unreasonably old (e.g., before 1900)
            if (loan.getDisbursementDate().isBefore(LocalDate.of(1900, 1, 1))) {
                errors.add("Disbursement Date is unrealistically old (before 1900)");
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
    private void validateStatus(LoanBookUploadDTO loan, List<String> warnings) {
        if (loan.getStatus() != null) {
            String originalStatus = loan.getStatus();
            String normalizedStatus = loan.getStatus().toUpperCase().trim();
            
            // Transform CURRENT to ACTIVE
            if ("CURRENT".equals(normalizedStatus)) {
                loan.setStatus("ACTIVE");
                warnings.add("Status 'CURRENT' transformed to 'ACTIVE'");
                return;
            }
            
            // Check if status is valid
            if (!VALID_STATUSES.contains(normalizedStatus)) {
                // Invalid status - set to ACTIVE as default and warn
                loan.setStatus("ACTIVE");
                warnings.add("Invalid status '" + originalStatus + "' changed to 'ACTIVE'. Valid statuses: " + String.join(", ", VALID_STATUSES));
                return;
            }
            
            // Set normalized status
            loan.setStatus(normalizedStatus);
        }
        
        // Validate status logic
        if ("CLOSED".equals(loan.getStatus()) && loan.getOutstandingBalance() != null && loan.getOutstandingBalance() > 0) {
            warnings.add("CLOSED loan has Outstanding Balance > 0. Consider reviewing this loan.");
        }
    }
    
    /**
     * Validate customer exists
     */
    private void validateCustomer(LoanBookUploadDTO loan, List<String> errors, List<String> warnings) {
        try {
            Optional<Customer> optionalCustomer = Optional.empty();

            if (!isEmpty(loan.getCustomerId())) {
                // First, try to find by external_id (the uploaded customer ID)
                optionalCustomer = customerRepository.findByExternalId(loan.getCustomerId());
                
                // If not found by external_id, try by database ID
                if (optionalCustomer.isEmpty()) {
                    try {
                        optionalCustomer = customerRepository.findById(Long.parseLong(loan.getCustomerId()));
                    } catch (NumberFormatException e) {
                        // Not a valid Long ID, skip this check
                    }
                }

                // If still not found, try by document number
                if (optionalCustomer.isEmpty()) {
                    optionalCustomer = customerRepository.findByDocumentNumber(loan.getCustomerId());
                }
            }

            // Only create customer if not found by any method
            if (optionalCustomer.isEmpty()) {
                optionalCustomer = createCustomerFromLoanUpload(loan);
            }

            Customer customer = optionalCustomer.orElse(null);

            if (customer == null) {
                errors.add("Customer not found: " + loan.getCustomerId());
                return;
            }

            String expectedName = (customer.getFirstName() + " " + customer.getLastName()).trim();
            if (!expectedName.equalsIgnoreCase(loan.getCustomerName().trim())) {
                // Name mismatch is a warning, not an error - use stored customer data
                warnings.add("Customer name mismatch. Expected: " + expectedName + ", Found: " + loan.getCustomerName() + ". Using stored customer.");
            }
        } catch (Exception e) {
            log.error("Error validating customer: {}", e.getMessage());
            errors.add("Error validating customer: " + e.getMessage());
        }
    }

    /**
     * Validate product exists
     */
    private void validateProduct(LoanBookUploadDTO loan, List<String> errors, List<String> warnings) {
        try {
            Optional<Products> optionalProduct = productRepo.getByCode(loan.getProductCode());

            if (optionalProduct.isEmpty()) {
                optionalProduct = createProductFromLoanUpload(loan);
            }

            Products product = optionalProduct.orElse(null);

            if (product == null) {
                errors.add("Product not found: " + loan.getProductCode());
            } else {
                if (loan.getPrincipal() != null) {
                    if (product.getMinLimit() != null && loan.getPrincipal() < product.getMinLimit()) {
                        // Product limit violations are warnings, not errors - allow upload to proceed
                        warnings.add("Principal below product minimum: " + product.getMinLimit() + ". Proceeding with upload.");
                    }
                    if (product.getMaxLimit() != null && loan.getPrincipal() > product.getMaxLimit()) {
                        // Product limit violations are warnings, not errors - allow upload to proceed
                        warnings.add("Principal exceeds product maximum: " + product.getMaxLimit() + ". Proceeding with upload.");
                    }
                }

                if (isEmpty(loan.getProductName())) {
                    loan.setProductName(product.getName());
                }
            }
        } catch (Exception e) {
            log.error("Error validating product: {}", e.getMessage());
            errors.add("Error validating product: " + e.getMessage());
        }
    }

    private Optional<Customer> createCustomerFromLoanUpload(LoanBookUploadDTO loan) {
        try {
            // Parse customer name - handle single name, two names, or three names
            String firstName = "N/A";
            String middleName = null;
            String lastName = "N/A";
            
            if (loan.getCustomerName() != null && !loan.getCustomerName().trim().isEmpty()) {
                String[] parts = loan.getCustomerName().trim().split("\\s+");
                
                if (parts.length == 1) {
                    firstName = parts[0];
                    lastName = parts[0];
                } else if (parts.length == 2) {
                    firstName = parts[0];
                    lastName = parts[1];
                } else if (parts.length >= 3) {
                    firstName = parts[0];
                    middleName = parts[1];
                    lastName = String.join(" ", java.util.Arrays.copyOfRange(parts, 2, parts.length));
                }
            }
            
            // Build creation request using centralized service
            CustomerCreationService.CustomerCreationRequest request = 
                CustomerCreationService.CustomerCreationRequest.builder()
                    .firstName(firstName)
                    .lastName(lastName)
                    .middleName(middleName)
                    .phoneNumber(loan.getPhoneNumber())
                    .documentNumber(loan.getCustomerId())
                    .email(loan.getEmail())
                    .branchCode(loan.getBranchCode())
                    .createdBy("LOAN_VALIDATION")
                    .source(CustomerCreationService.CreationSource.LOAN_UPLOAD)
                    .sourceReference(loan.getLoanId())
                    .build();
            
            // Create customer using centralized service (includes bank account creation)
            CustomerCreationService.CustomerCreationResponse response = 
                customerCreationService.createCustomer(request);
            
            if (!response.isSuccess()) {
                log.error("Failed to create customer during validation: {}", response.getMessage());
                return Optional.empty();
            }
            
            log.info("Created customer during validation via centralized service: ID={}, MemberNumber={}, BankAccounts={}", 
                response.getCustomer().getId(), 
                response.getMemberNumber(),
                response.getBankAccounts().size());
            
            return Optional.of(response.getCustomer());
        } catch (Exception e) {
            log.error("Error creating customer from loan upload: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    private Optional<Products> createProductFromLoanUpload(LoanBookUploadDTO loan) {
        try {
            if (isEmpty(loan.getProductCode())) {
                return Optional.empty();
            }
            
            // Check if product with this name already exists
            String name = isEmpty(loan.getProductName())
                ? "Auto-" + loan.getProductCode().trim()
                : loan.getProductName().trim();
            
            Optional<Products> existingByName = productRepo.findByName(name);
            if (existingByName.isPresent()) {
                log.info("Product with name '{}' already exists, using existing product", name);
                return existingByName;
            }

            Products product = new Products();
            product.setCode(loan.getProductCode().trim());
            product.setName(name);

            product.setActive(true);
            product.setTransactionType("LOAN");
            product.setTimeSpan("MONTHS");

            if (loan.getTerm() != null && loan.getTerm() > 0) {
                product.setTerm(loan.getTerm());
            }

            if (loan.getInterestRate() != null) {
                product.setInterest((int) Math.round(loan.getInterestRate()));
            }

            if (loan.getPrincipal() != null && loan.getPrincipal() > 0) {
                int principal = (int) Math.round(loan.getPrincipal());
                product.setMinLimit(principal);
                product.setMaxLimit(principal);
            }

            product.setTopUp(Boolean.FALSE);
            product.setRollOver(Boolean.FALSE);
            product.setDailyInterest(Boolean.FALSE);
            product.setInterestUpfront(Boolean.FALSE);

            if (!isEmpty(loan.getBranchCode())) {
                product.setBranchCode(loan.getBranchCode());
            }

            Products saved = productRepo.save(product);
            return Optional.ofNullable(saved);
        } catch (Exception e) {
            log.error("Error creating product from loan upload: {}", e.getMessage());
            return Optional.empty();
        }
    }

    // Removed obsolete fromLoanUpload method - now using centralized CustomerCreationService

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
