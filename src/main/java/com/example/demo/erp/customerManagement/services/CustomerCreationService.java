package com.example.demo.erp.customerManagement.services;

import com.example.demo.erp.customerManagement.parsistence.entities.Customer;
import com.example.demo.erp.customerManagement.parsistence.repositories.CustomerRepo;
import com.example.demo.finance.banking.parsitence.enitities.BankAccounts;
import com.example.demo.finance.banking.services.BankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Centralized service for customer/member creation across all channels
 * This ensures consistency in customer creation whether from:
 * 1. Web UI (admin creating customer)
 * 2. Excel/CSV Upload
 * 3. Loan Book Upload
 * 4. Mobile App Registration
 * 5. USSD Registration
 * 6. API Integration
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerCreationService {

    private final CustomerRepo customerRepo;
    private final BankingService bankingService;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * Source of customer creation for tracking and business logic
     */
    public enum CreationSource {
        ADMIN_UI,           // Created by admin through web UI
        CUSTOMER_UPLOAD,    // Uploaded via Excel/CSV
        LOAN_UPLOAD,        // Created during loan book upload
        MOBILE_APP,         // Self-registration from mobile app
        USSD,               // Self-registration from USSD
        API_INTEGRATION,    // Created via API
        DATA_SEEDING        // Created during initial data seeding
    }

    /**
     * DTO for customer creation request
     */
    @lombok.Data
    @lombok.Builder
    public static class CustomerCreationRequest {
        // Required fields
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private String documentNumber;

        // Optional fields
        private String middleName;
        private String email;
        private String address;
        private String occupation;
        private java.time.LocalDate dob;
        private String maritalStatus;
        private Float salary;
        private String employmentType;
        private String nextOfKin;
        private String nextOfKinPhone;
        private String nextOfKinRelationship;
        private String nextOfKinDocumentNumber;

        // Branch and officer assignment
        private String branchCode;
        private Long assignedLoanOfficerId;
        private String county;
        private String createdBy;

        // Channel-specific credentials (optional)
        private String webLogin;
        private String webPin;
        private Boolean enableWebChannel;

        private String mobileLogin;
        private String mobilePin;
        private Boolean enableMobileChannel;

        private String ussdLogin;
        private String ussdPin;
        private Boolean enableUssdChannel;

        // Initial deposit
        private Double initialDepositAmount;

        // Source tracking
        private CreationSource source;
        private String sourceReference;
    }

    /**
     * DTO for customer creation response
     */
    @lombok.Data
    @lombok.Builder
    public static class CustomerCreationResponse {
        private boolean success;
        private String message;
        private Customer customer;
        private List<BankAccounts> bankAccounts;
        private String memberNumber;
    }

    /**
     * Centralized method to create a customer
     * This is the ONLY method that should be used to create customers
     * 
     * @param request Customer creation request with all necessary data
     * @return CustomerCreationResponse with created customer and bank accounts
     */
    @Transactional
    public CustomerCreationResponse createCustomer(CustomerCreationRequest request) {
        log.info("Creating customer: {} {} from source: {}", 
            request.getFirstName(), request.getLastName(), request.getSource());

        try {
            // 1. Validate request
            validateCustomerRequest(request);

            // 2. Check for duplicates
            checkForDuplicates(request);

            // 3. Build customer entity
            Customer customer = buildCustomerEntity(request);

            // 4. Set channel-specific authentication
            setChannelAuthentication(customer, request);

            // 5. Generate member number if not exists
            if (customer.getMemberNumber() == null || customer.getMemberNumber().isEmpty()) {
                customer.setMemberNumber(generateMemberNumber());
            }

            // 6. Save customer
            Customer savedCustomer = customerRepo.save(customer);
            log.info("Customer saved with ID: {} and member number: {}", 
                savedCustomer.getId(), savedCustomer.getMemberNumber());

            // 7. Create bank accounts for the customer
            List<BankAccounts> bankAccounts = bankingService.createBankAccounts(savedCustomer);
            log.info("Created {} bank accounts for customer {}", 
                bankAccounts.size(), savedCustomer.getId());

            // 8. Process initial deposit if present
            if (request.getInitialDepositAmount() != null && request.getInitialDepositAmount() > 0) {
                savedCustomer.setInitialDepositAmount(request.getInitialDepositAmount());
                savedCustomer = customerRepo.save(savedCustomer);
                bankingService.processInitialDepositIfPresent(savedCustomer);
                log.info("Processed initial deposit of {} for customer {}", 
                    request.getInitialDepositAmount(), savedCustomer.getId());
            }

            // 9. Return success response
            return CustomerCreationResponse.builder()
                .success(true)
                .message("Customer created successfully")
                .customer(savedCustomer)
                .bankAccounts(bankAccounts)
                .memberNumber(savedCustomer.getMemberNumber())
                .build();

        } catch (Exception e) {
            log.error("Error creating customer: {}", e.getMessage(), e);
            return CustomerCreationResponse.builder()
                .success(false)
                .message("Failed to create customer: " + e.getMessage())
                .build();
        }
    }

    /**
     * Validate customer creation request
     */
    private void validateCustomerRequest(CustomerCreationRequest request) {
        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required");
        }
        if (request.getDocumentNumber() == null || request.getDocumentNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("ID/Document number is required");
        }

        // Validate phone number format (should be 254XXXXXXXXX)
        if (!request.getPhoneNumber().matches("^254\\d{9}$")) {
            throw new IllegalArgumentException(
                "Invalid phone number format. Expected: 254XXXXXXXXX, Got: " + request.getPhoneNumber());
        }

        // Validate email format if provided
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new IllegalArgumentException("Invalid email format");
            }
        }
    }

    /**
     * Check for duplicate customers
     */
    private void checkForDuplicates(CustomerCreationRequest request) {
        // Check phone number
        if (customerRepo.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException(
                "Customer with phone number " + request.getPhoneNumber() + " already exists");
        }

        // Check document number
        if (customerRepo.existsByDocumentNumber(request.getDocumentNumber())) {
            throw new IllegalArgumentException(
                "Customer with ID/Document number " + request.getDocumentNumber() + " already exists");
        }

        // Check email if provided
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (customerRepo.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException(
                    "Customer with email " + request.getEmail() + " already exists");
            }
        }
    }

    /**
     * Build customer entity from request
     */
    private Customer buildCustomerEntity(CustomerCreationRequest request) {
        Customer customer = new Customer();

        // Personal information
        customer.setFirstName(request.getFirstName().trim());
        customer.setLastName(request.getLastName().trim());
        customer.setMiddleName(request.getMiddleName() != null ? request.getMiddleName().trim() : null);
        customer.setPhoneNumber(request.getPhoneNumber().trim());
        customer.setDocumentNumber(request.getDocumentNumber().trim());
        customer.setEmail(request.getEmail() != null ? request.getEmail().trim() : null);

        // Additional information
        customer.setDob(request.getDob());
        customer.setAddress(request.getAddress());
        customer.setOccupation(request.getOccupation());
        customer.setMaritalStatus(request.getMaritalStatus());
        customer.setSalary(request.getSalary());
        customer.setEmploymentType(request.getEmploymentType());

        // Next of kin
        customer.setNextOfKin(request.getNextOfKin());
        customer.setNextOfKinPhone(request.getNextOfKinPhone());
        customer.setNextOfKinRelationship(request.getNextOfKinRelationship());
        customer.setNextOfKinDocumentNumber(request.getNextOfKinDocumentNumber());

        // Branch and assignment
        customer.setBranchCode(request.getBranchCode());
        customer.setAssignedLoanOfficerId(request.getAssignedLoanOfficerId());
        customer.setCounty(request.getCounty());
        customer.setCreatedBy(request.getCreatedBy() != null ? request.getCreatedBy() : "SYSTEM");

        // Status and timestamps
        customer.setAccountStatusFlag(true);
        customer.setAccountStatus("ACTIVE");
        customer.setStatus("PENDING_VERIFICATION"); // Default status
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        customer.setAccountBalance(0.0f);
        customer.setIsActive(true);

        // Set external ID to document number by default
        customer.setExternalId(request.getDocumentNumber());

        // Initialize channel flags
        customer.setWebChannelEnabled(false);
        customer.setMobileChannelEnabled(false);
        customer.setUssdChannelEnabled(false);

        return customer;
    }

    /**
     * Set channel-specific authentication credentials
     */
    private void setChannelAuthentication(Customer customer, CustomerCreationRequest request) {
        // Web Channel
        if (request.getEnableWebChannel() != null && request.getEnableWebChannel()) {
            customer.setWebLogin(request.getWebLogin() != null ? request.getWebLogin() : request.getEmail());
            if (request.getWebPin() != null && !request.getWebPin().isEmpty()) {
                customer.setWebPinHash(hashPin(request.getWebPin()));
            }
            customer.setWebChannelEnabled(true);
            customer.setWebFailedAttempts(0);
            log.info("Web channel enabled for customer with login: {}", customer.getWebLogin());
        }

        // Mobile Channel
        if (request.getEnableMobileChannel() != null && request.getEnableMobileChannel()) {
            customer.setMobileLogin(request.getMobileLogin() != null ? request.getMobileLogin() : request.getPhoneNumber());
            if (request.getMobilePin() != null && !request.getMobilePin().isEmpty()) {
                customer.setMobilePinHash(hashPin(request.getMobilePin()));
            }
            customer.setMobileChannelEnabled(true);
            customer.setMobileFailedAttempts(0);
            log.info("Mobile channel enabled for customer with login: {}", customer.getMobileLogin());
        }

        // USSD Channel
        if (request.getEnableUssdChannel() != null && request.getEnableUssdChannel()) {
            customer.setUssdLogin(request.getUssdLogin() != null ? request.getUssdLogin() : request.getPhoneNumber());
            if (request.getUssdPin() != null && !request.getUssdPin().isEmpty()) {
                customer.setUssdPinHash(hashPin(request.getUssdPin()));
            }
            customer.setUssdChannelEnabled(true);
            customer.setUssdFailedAttempts(0);
            log.info("USSD channel enabled for customer with login: {}", customer.getUssdLogin());
        }
    }

    /**
     * Hash PIN using BCrypt
     */
    private String hashPin(String pin) {
        return passwordEncoder.encode(pin);
    }

    /**
     * Generate unique member number
     * Format: MEM-YYYYMMDD-XXXX
     */
    private String generateMemberNumber() {
        String datePart = java.time.LocalDate.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uniquePart = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "MEM-" + datePart + "-" + uniquePart;
    }

    /**
     * Convenience method for simple customer creation (backward compatibility)
     */
    @Transactional
    public Customer createSimpleCustomer(Customer customer, CreationSource source) {
        CustomerCreationRequest request = CustomerCreationRequest.builder()
            .firstName(customer.getFirstName())
            .lastName(customer.getLastName())
            .middleName(customer.getMiddleName())
            .phoneNumber(customer.getPhoneNumber())
            .documentNumber(customer.getDocumentNumber())
            .email(customer.getEmail())
            .address(customer.getAddress())
            .occupation(customer.getOccupation())
            .dob(customer.getDob())
            .maritalStatus(customer.getMaritalStatus())
            .salary(customer.getSalary())
            .employmentType(customer.getEmploymentType())
            .nextOfKin(customer.getNextOfKin())
            .nextOfKinPhone(customer.getNextOfKinPhone())
            .nextOfKinRelationship(customer.getNextOfKinRelationship())
            .nextOfKinDocumentNumber(customer.getNextOfKinDocumentNumber())
            .branchCode(customer.getBranchCode())
            .assignedLoanOfficerId(customer.getAssignedLoanOfficerId())
            .county(customer.getCounty())
            .createdBy(customer.getCreatedBy())
            .initialDepositAmount(customer.getInitialDepositAmount())
            .source(source)
            .build();

        CustomerCreationResponse response = createCustomer(request);
        if (!response.isSuccess()) {
            throw new RuntimeException(response.getMessage());
        }
        return response.getCustomer();
    }
}
