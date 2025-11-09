package com.example.demo.erp.bulk.services;

import com.example.demo.erp.customerManagement.parsistence.entities.Customer;
import com.example.demo.erp.customerManagement.parsistence.repositories.CustomerRepository;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanAccount;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanApplication;
import com.example.demo.finance.loanManagement.parsistence.repositories.LoanAccountRepository;
import com.example.demo.finance.loanManagement.parsistence.repositories.LoanApplicationRepository;
import com.example.demo.finance.loanManagement.services.LoanDisbursementService;
import com.example.demo.finance.payments.services.UniversalPaymentService;
import com.example.demo.erp.communication.sms.SmsService;
import com.example.demo.system.user.entities.UserProfile;
import com.example.demo.system.user.services.UserManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile; 
import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkProcessingService {

    private final CustomerRepository customerRepository;
    private final LoanAccountRepository loanAccountRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final UserManagementService userManagementService;
    private final LoanDisbursementService loanDisbursementService;
    private final UniversalPaymentService universalPaymentService;
    private final SmsService smsService;
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    
    // CSV Headers for different entity types
    private static final String[] CUSTOMER_HEADERS = {
        "firstName", "lastName", "email", "phoneNumber", "idNumber", 
        "address", "branchId", "loanOfficerId", "accountType"
    };
    
    private static final String[] LOAN_HEADERS = {
        "customerId", "productId", "amount", "interestRate", "term", 
        "purpose", "productCode", "customerIdNumber", "customerMobileNumber", "creditLimit"
    };
    
    private static final String[] PAYMENT_HEADERS = {
        "customerId", "amount", "paymentType", "paymentMethod", 
        "reference", "description", "transactionDate"
    };
    
    private static final String[] USER_HEADERS = {
        "username", "email", "firstName", "lastName", "phoneNumber",
        "userType", "branchId", "employeeId", "department", "position"
    };

    /**
     * Bulk Customer Import
     */
    @Transactional
    public Map<String, Object> bulkImportCustomers(MultipartFile file, String importedBy) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        try {
            List<Map<String, String>> csvData = parseCsvFile(file);
            
            for (Map<String, String> row : csvData) {
                try {
                    // Validate required fields
                    if (row.get("firstName") == null || row.get("firstName").trim().isEmpty()) {
                        throw new IllegalArgumentException("First name is required");
                    }
                    if (row.get("lastName") == null || row.get("lastName").trim().isEmpty()) {
                        throw new IllegalArgumentException("Last name is required");
                    }
                    if (row.get("phoneNumber") == null || row.get("phoneNumber").trim().isEmpty()) {
                        throw new IllegalArgumentException("Phone number is required");
                    }
                    if (row.get("idNumber") == null || row.get("idNumber").trim().isEmpty()) {
                        throw new IllegalArgumentException("ID number is required");
                    }
                    
                    // Check if customer already exists
                    if (customerRepository.findByPhoneNumber(row.get("phoneNumber")).isPresent()) {
                        throw new IllegalArgumentException("Customer with phone number already exists");
                    }
                    if (customerRepository.findByDocumentNumber(row.get("idNumber")).isPresent()) {
                        throw new IllegalArgumentException("Customer with ID number already exists");
                    }
                    
                    Customer customer = new Customer();
                    customer.setFirstName(row.get("firstName").trim());
                    customer.setLastName(row.get("lastName").trim());
                    customer.setEmail(row.get("email") != null ? row.get("email").trim() : null);
                    customer.setPhoneNumber(row.get("phoneNumber").trim());
                    customer.setDocumentNumber(row.get("idNumber").trim());
                    customer.setDocumentType("NATIONAL_ID");
                    customer.setAddress(row.get("address") != null ? row.get("address").trim() : null);
                    customer.setAccountBalance(0.0f);
                    customer.setAccountStatus("ACTIVE");
                    customer.setAccountStatusFlag(true);
                    customer.setStatus("ACTIVE");
                    customer.setCreatedAt(LocalDateTime.now());
                    customer.setCreatedBy(importedBy);
                    customer.setIsActive(true);
                    
                    // Set branch and loan officer if provided
                    if (row.get("branchId") != null && !row.get("branchId").isEmpty()) {
                        customer.setBranchId(Long.valueOf(row.get("branchId")));
                    }
                    if (row.get("loanOfficerId") != null && !row.get("loanOfficerId").isEmpty()) {
                        customer.setAssignedLoanOfficerId(Long.valueOf(row.get("loanOfficerId")));
                    }
                    
                    customerRepository.save(customer);
                    successCount++;
                    
                    // Send welcome SMS
                    sendWelcomeSms(customer);
                    
                } catch (Exception e) {
                    failureCount++;
                    errors.add("Row " + (csvData.indexOf(row) + 1) + ": " + e.getMessage());
                    log.error("Error importing customer", e);
                }
            }
            
        } catch (Exception e) {
            log.error("Error processing bulk customer import", e);
            result.put("success", false);
            result.put("message", "Failed to process file: " + e.getMessage());
            return result;
        }

        result.put("success", true);
        result.put("message", String.format("Import completed. Success: %d, Failed: %d", successCount, failureCount));
        result.put("successCount", successCount);
        result.put("failureCount", failureCount);
        result.put("errors", errors);
        
        return result;
    }

    /**
     * Bulk Loan Applications Import
     */
    @Transactional
    public Map<String, Object> bulkImportLoanApplications(MultipartFile file, String importedBy) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        try {
            List<Map<String, String>> csvData = parseCsvFile(file);
            
            for (Map<String, String> row : csvData) {
                try {
                    // Validate required fields
                    if (row.get("customerId") == null || row.get("customerId").trim().isEmpty()) {
                        throw new IllegalArgumentException("Customer ID is required");
                    }
                    if (row.get("amount") == null || row.get("amount").trim().isEmpty()) {
                        throw new IllegalArgumentException("Loan amount is required");
                    }
                    if (row.get("productId") == null || row.get("productId").trim().isEmpty()) {
                        throw new IllegalArgumentException("Product ID is required");
                    }
                    
                    // Validate customer exists
                    String customerId = row.get("customerId").trim();
                    Customer customer = customerRepository.findById(Long.valueOf(customerId))
                        .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + customerId));
                    
                    // Validate loan amount
                    Double loanAmount;
                    try {
                        loanAmount = Double.valueOf(row.get("amount").trim().replaceAll(",", ""));
                        if (loanAmount <= 0) {
                            throw new IllegalArgumentException("Loan amount must be greater than 0");
                        }
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid loan amount format: " + row.get("amount"));
                    }
                    
                    // Validate term
                    Integer term;
                    try {
                        term = Integer.valueOf(row.get("term") != null ? row.get("term").trim() : "12");
                        if (term <= 0 || term > 60) {
                            throw new IllegalArgumentException("Loan term must be between 1 and 60 months");
                        }
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid term format: " + row.get("term"));
                    }
                    
                    LoanApplication application = new LoanApplication();
                    application.setCustomerId(customerId);
                    application.setProductId(Long.valueOf(row.get("productId").trim()));
                    application.setAmount(loanAmount);
                    application.setLoanAmount(String.valueOf(loanAmount));
                    application.setLoanInterest(row.get("interestRate") != null ? row.get("interestRate").trim() : "12.0");
                    application.setLoanTerm(String.valueOf(term));
                    application.setTerm(term);
                    application.setDestinationAccount(row.get("purpose") != null ? row.get("purpose").trim() : "SACCO_ACCOUNT");
                    application.setProductCode(row.get("productCode") != null ? row.get("productCode").trim() : "BULK_LOAN");
                    application.setCustomerIdNumber(customer.getDocumentNumber());
                    application.setCustomerMobileNumber(customer.getPhoneNumber());
                    application.setCreditLimit(row.get("creditLimit") != null ? row.get("creditLimit").trim() : String.valueOf(loanAmount));
                    application.setStatus("PENDING");
                    application.setApplicationTime(LocalDateTime.now());
                    application.setUpdatedAt(LocalDateTime.now());
                    
                    loanApplicationRepository.save(application);
                    successCount++;
                    
                } catch (Exception e) {
                    failureCount++;
                    errors.add("Row " + (csvData.indexOf(row) + 1) + ": " + e.getMessage());
                    log.error("Error importing loan application", e);
                }
            }
            
        } catch (Exception e) {
            log.error("Error processing bulk loan applications import", e);
            result.put("success", false);
            result.put("message", "Failed to process file: " + e.getMessage());
            return result;
        }

        result.put("success", true);
        result.put("message", String.format("Import completed. Success: %d, Failed: %d", successCount, failureCount));
        result.put("successCount", successCount);
        result.put("failureCount", failureCount);
        result.put("errors", errors);
        
        return result;
    }

    /**
     * Bulk Loan Disbursement
     */
    @Transactional
    public Map<String, Object> bulkDisburseLoan(List<Long> applicationIds, String disbursementMethod, String disbursedBy) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        List<String> successfulDisbursements = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        // Validate inputs
        if (applicationIds == null || applicationIds.isEmpty()) {
            result.put("success", false);
            result.put("message", "No loan applications provided for disbursement");
            return result;
        }
        
        if (disbursementMethod == null || disbursementMethod.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "Disbursement method is required");
            return result;
        }
        
        if (disbursedBy == null || disbursedBy.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "Disbursed by field is required");
            return result;
        }

        for (Long applicationId : applicationIds) {
            try {
                // Validate loan application exists and is in correct status
                LoanApplication application = loanApplicationRepository.findById(applicationId)
                    .orElseThrow(() -> new IllegalArgumentException("Loan application not found with ID: " + applicationId));
                
                if (!"PENDING".equals(application.getStatus()) && !"APPROVED".equals(application.getStatus())) {
                    throw new IllegalArgumentException("Loan application is not in PENDING or APPROVED status. Current status: " + application.getStatus());
                }
                
                // Generate unique reference number
                String reference = generateDisbursementReference(applicationId, disbursedBy);
                
                // Get customer for additional validation
                Customer customer = customerRepository.findById(Long.valueOf(application.getCustomerId()))
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found for application: " + applicationId));
                
                if (!customer.getIsActive()) {
                    throw new IllegalArgumentException("Customer account is not active: " + customer.getId());
                }
                
                // Call disbursement service
                loanDisbursementService.disburseLoan(applicationId, disbursedBy.trim(), reference, disbursementMethod.trim(), null);
                
                successCount++;
                successfulDisbursements.add(String.format("App ID: %d, Ref: %s, Customer: %s %s", 
                    applicationId, reference, customer.getFirstName(), customer.getLastName()));
                
            } catch (Exception e) {
                failureCount++;
                errors.add("Application " + applicationId + ": " + e.getMessage());
                log.error("Error disbursing loan {}", applicationId, e);
            }
        }

        result.put("success", true);
        result.put("message", String.format("Bulk disbursement completed. Success: %d, Failed: %d", successCount, failureCount));
        result.put("successCount", successCount);
        result.put("failureCount", failureCount);
        result.put("errors", errors);
        result.put("successfulDisbursements", successfulDisbursements);
        
        return result;
    }

    /**
     * Bulk Payment Processing
     */
    @Transactional
    public Map<String, Object> bulkProcessPayments(MultipartFile file, String processedBy) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        try {
            List<Map<String, String>> csvData = parseCsvFile(file);
            
            // Process payments in parallel for better performance
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            
            for (Map<String, String> row : csvData) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        // Validate required fields
                        if (row.get("customerId") == null || row.get("customerId").trim().isEmpty()) {
                            throw new IllegalArgumentException("Customer ID is required");
                        }
                        if (row.get("amount") == null || row.get("amount").trim().isEmpty()) {
                            throw new IllegalArgumentException("Amount is required");
                        }
                        if (row.get("paymentMethod") == null || row.get("paymentMethod").trim().isEmpty()) {
                            throw new IllegalArgumentException("Payment method is required");
                        }
                        
                        // Validate customer exists and is active
                        Long customerId = Long.valueOf(row.get("customerId").trim());
                        Customer customer = customerRepository.findById(customerId)
                            .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + customerId));
                        
                        if (!customer.getIsActive()) {
                            throw new IllegalArgumentException("Customer account is not active: " + customerId);
                        }
                        
                        // Validate amount
                        BigDecimal amount = new BigDecimal(row.get("amount").trim().replaceAll(",", ""));
                        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                            throw new IllegalArgumentException("Amount must be greater than 0");
                        }
                        
                        // Create payment request
                        Map<String, Object> paymentRequest = new HashMap<>();
                        paymentRequest.put("customerId", customerId);
                        paymentRequest.put("amount", amount);
                        paymentRequest.put("paymentMethod", row.get("paymentMethod").trim());
                        paymentRequest.put("transactionType", row.get("paymentType") != null ? row.get("paymentType").trim() : "DEPOSIT");
                        paymentRequest.put("description", row.get("description") != null ? row.get("description").trim() : "Bulk payment processing");
                        paymentRequest.put("referenceNumber", row.get("reference") != null ? row.get("reference").trim() : generatePaymentReference(customerId));
                        paymentRequest.put("initiatedBy", processedBy);
                        paymentRequest.put("sourceModule", "BULK_PROCESSING");
                        
                        // Process payment through universal payment service
                        universalPaymentService.processPaymentRequest(paymentRequest);
                        
                        synchronized (result) {
                            int current = (int) result.getOrDefault("successCount", 0);
                            result.put("successCount", current + 1);
                        }
                        
                    } catch (Exception e) {
                        synchronized (result) {
                            int current = (int) result.getOrDefault("failureCount", 0);
                            result.put("failureCount", current + 1);
                            
                            @SuppressWarnings("unchecked")
                            List<String> errorList = (List<String>) result.getOrDefault("errors", new ArrayList<>());
                            errorList.add("Row " + (csvData.indexOf(row) + 1) + ": " + e.getMessage());
                            result.put("errors", errorList);
                        }
                        log.error("Error processing payment", e);
                    }
                }, executorService);
                
                futures.add(future);
            }
            
            // Wait for all payments to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            successCount = (int) result.getOrDefault("successCount", 0);
            failureCount = (int) result.getOrDefault("failureCount", 0);
            errors = (List<String>) result.getOrDefault("errors", new ArrayList<>());
            
        } catch (Exception e) {
            log.error("Error processing bulk payments", e);
            result.put("success", false);
            result.put("message", "Failed to process file: " + e.getMessage());
            return result;
        }

        result.put("success", true);
        result.put("message", String.format("Bulk payment processing completed. Success: %d, Failed: %d", successCount, failureCount));
        
        return result;
    }

    /**
     * Bulk User Import
     */
    @Transactional
    public Map<String, Object> bulkImportUsers(MultipartFile file, String importedBy) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        try {
            List<Map<String, String>> csvData = parseCsvFile(file);
            
            for (Map<String, String> row : csvData) {
                try {
                    UserProfile user = new UserProfile();
                    user.setUsername(row.get("username"));
                    user.setEmail(row.get("email"));
                    user.setFirstName(row.get("firstName"));
                    user.setLastName(row.get("lastName"));
                    user.setPhoneNumber(row.get("phoneNumber"));
                    user.setEmployeeId(row.get("employeeId"));
                    user.setDepartment(row.get("department"));
                    user.setPosition(row.get("position"));
                    user.setPassword("DefaultPassword123!"); // Should be changed on first login
                    
                    // Set user type and branch
                    if (row.get("userType") != null && !row.get("userType").isEmpty()) {
                        user.setUserType(UserProfile.UserType.valueOf(row.get("userType").toUpperCase()));
                    } else {
                        user.setUserType(UserProfile.UserType.REGULAR_USER);
                    }
                    
                    if (row.get("branchId") != null && !row.get("branchId").isEmpty()) {
                        user.setBranchId(Long.valueOf(row.get("branchId")));
                    }
                    
                    userManagementService.createUser(user, importedBy);
                    successCount++;
                    
                } catch (Exception e) {
                    failureCount++;
                    errors.add("Row " + (csvData.indexOf(row) + 1) + ": " + e.getMessage());
                    log.error("Error importing user", e);
                }
            }
            
        } catch (Exception e) {
            log.error("Error processing bulk user import", e);
            result.put("success", false);
            result.put("message", "Failed to process file: " + e.getMessage());
            return result;
        }

        result.put("success", true);
        result.put("message", String.format("Import completed. Success: %d, Failed: %d", successCount, failureCount));
        result.put("successCount", successCount);
        result.put("failureCount", failureCount);
        result.put("errors", errors);
        
        return result;
    }

    /**
     * Export Customers to CSV
     */
    public String exportCustomersToCSV(Long branchId, boolean includeInactive) {
        StringBuilder csv = new StringBuilder();
        
        // Add headers
        csv.append(String.join(",", CUSTOMER_HEADERS)).append("\n");
        
        List<Customer> customers;
        if (branchId != null) {
            customers = customerRepository.findByBranchId(branchId);
        } else {
            customers = customerRepository.findAll();
        }
        
        // Filter by active status if needed
        if (!includeInactive) {
            customers = customers.stream()
                .filter(Customer::getIsActive)
                .toList();
        }
        
        // Add data rows
        for (Customer customer : customers) {
            csv.append(String.format("%s,%s,%s,%s,%s,%s,%d,%d,%s\n",
                escapeCsvValue(customer.getFirstName()),
                escapeCsvValue(customer.getLastName()),
                escapeCsvValue(customer.getEmail()),
                escapeCsvValue(customer.getPhoneNumber()),
                escapeCsvValue(customer.getIdNumber()),
                escapeCsvValue(customer.getAddress()),
                customer.getBranchId() != null ? customer.getBranchId() : 0,
                customer.getAssignedLoanOfficerId() != null ? customer.getAssignedLoanOfficerId() : 0,
                "SAVINGS"
            ));
        }
        
        return csv.toString();
    }

    /**
     * Export Loan Accounts to CSV
     */
    public String exportLoanAccountsToCSV(Long branchId, String status) {
        StringBuilder csv = new StringBuilder();
        
        // Add headers
        String[] loanExportHeaders = {
            "accountId", "customerId", "customerName", "loanReference", "principalAmount",
            "interestRate", "term", "totalAmount", "outstandingAmount", "status", "disbursementDate"
        };
        csv.append(String.join(",", loanExportHeaders)).append("\n");
        
        List<LoanAccount> loanAccounts = loanAccountRepository.findAll();
        
        // Filter by status if provided
        if (status != null && !status.isEmpty()) {
            loanAccounts = loanAccounts.stream()
                .filter(account -> status.equalsIgnoreCase(account.getStatus()))
                .toList();
        }
        
        // Add data rows
        for (LoanAccount account : loanAccounts) {
            // Get customer name
            Customer customer = customerRepository.findById(Long.valueOf(account.getCustomerId())).orElse(null);
            String customerName = customer != null ? 
                customer.getFirstName() + " " + customer.getLastName() : "Unknown";
            
            csv.append(String.format("%d,%d,%s,%s,%.2f,%.2f,%d,%.2f,%.2f,%s,%s\n",
                account.getId(),
                account.getCustomerId(),
                escapeCsvValue(customerName),
                escapeCsvValue(account.getLoanReference()),
                account.getPrincipalAmount(),
                account.getInterestRate(),
                account.getTerm(),
                account.getTotalAmount(),
                account.getTotalOutstanding(),
                escapeCsvValue(account.getStatus()),
                account.getDisbursementDate() != null ? account.getDisbursementDate().toString() : ""
            ));
        }
        
        return csv.toString();
    }

    /**
     * Generate CSV Template for entity type
     */
    public String generateCsvTemplate(String entityType) {
        switch (entityType.toLowerCase()) {
            case "customers":
                return String.join(",", CUSTOMER_HEADERS) + "\n" +
                       "# CUSTOMER IMPORT TEMPLATE - Replace with actual customer data\n" +
                       "# firstName,lastName,email,phoneNumber,idNumber,address,branchId,loanOfficerId,accountType\n" +
                       "# Example: Mary,Wanjiku,mary.wanjiku@email.com,+254712345678,30123456,Nairobi CBD,1,2,SAVINGS\n";
            
            case "loans":
                return String.join(",", LOAN_HEADERS) + "\n" +
                       "# LOAN APPLICATION IMPORT TEMPLATE - Replace with actual loan data\n" +
                       "# customerId,productId,amount,interestRate,term,purpose,productCode,customerIdNumber,customerMobileNumber,creditLimit\n" +
                       "# Example: 123,1,50000,15.0,24,Business Expansion,MICRO_LOAN,30123456,+254712345678,100000\n";
            
            case "payments":
                return String.join(",", PAYMENT_HEADERS) + "\n" +
                       "# PAYMENT IMPORT TEMPLATE - Replace with actual payment data\n" +
                       "# customerId,amount,paymentType,paymentMethod,reference,description,transactionDate\n" +
                       "# Example: 123,5000,LOAN_REPAYMENT,MPESA,ABC123456,Monthly payment,2024-11-03\n";
            
            case "users":
                return String.join(",", USER_HEADERS) + "\n" +
                       "# USER IMPORT TEMPLATE - Replace with actual user data\n" +
                       "# username,email,firstName,lastName,phoneNumber,userType,branchId,employeeId,department,position\n" +
                       "# Example: jane.kamau,jane.kamau@helasuite.com,Jane,Kamau,+254712345678,LOAN_OFFICER,1,EMP002,Credit,Loan Officer\n";
            
            default:
                throw new IllegalArgumentException("Unknown entity type: " + entityType);
        }
    }

    /**
     * Get Bulk Processing Statistics
     */
    public Map<String, Object> getBulkProcessingStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Customer stats
        long totalCustomers = customerRepository.count();
        long activeCustomers = customerRepository.countByIsActiveTrue();
        
        // Loan stats
        long totalLoans = loanAccountRepository.count();
        long activeLoans = loanAccountRepository.countByStatus("ACTIVE");
        
        // Recent imports (last 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long recentCustomers = customerRepository.countByCreatedAtAfter(thirtyDaysAgo);
        
        stats.put("customerStats", Map.of(
            "total", totalCustomers,
            "active", activeCustomers,
            "recentImports", recentCustomers
        ));
        
        stats.put("loanStats", Map.of(
            "total", totalLoans,
            "active", activeLoans
        ));
        
        return stats;
    }

    // Helper methods
    
    /**
     * Generate unique disbursement reference number
     */
    private String generateDisbursementReference(Long applicationId, String disbursedBy) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String userPrefix = disbursedBy.length() > 3 ? disbursedBy.substring(0, 3).toUpperCase() : disbursedBy.toUpperCase();
        return String.format("BULK_%s_%s_%d", userPrefix, timestamp, applicationId);
    }
    
    /**
     * Generate unique payment reference number
     */
    private String generatePaymentReference(Long customerId) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        return String.format("BULKPAY_%s_%d", timestamp, customerId);
    }
    
    private List<Map<String, String>> parseCsvFile(MultipartFile file) throws IOException {
        List<Map<String, String>> result = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("Empty CSV file");
            }
            
            String[] headers = headerLine.split(",");
            
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",", -1); // -1 to include empty trailing fields
                
                Map<String, String> row = new HashMap<>();
                for (int i = 0; i < headers.length && i < values.length; i++) {
                    row.put(headers[i].trim(), values[i].trim());
                }
                result.add(row);
            }
        }
        
        return result;
    }
    
    private String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }
        
        // Escape quotes and wrap in quotes if contains comma, quote, or newline
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }
    
    private void sendWelcomeSms(Customer customer) {
        try {
            String message = String.format(
                "Welcome to HelaSuite SACCO, %s! Your account has been created successfully. " +
                "Visit our nearest branch or contact us for more information.",
                customer.getFirstName()
            );
            
            smsService.sendSms(customer.getPhoneNumber(), message);
        } catch (Exception e) {
            log.error("Failed to send welcome SMS to {}", customer.getPhoneNumber(), e);
            // Don't fail the import due to SMS failure
        }
    }
}
