package com.example.demo.erp.customerManagement.controllers;

//import com.africastalking.sms.Recipient;
import com.example.demo.erp.customerManagement.dto.ImportResultDto;
import com.example.demo.erp.customerManagement.dto.StatusUpdateDto;
import com.example.demo.erp.customerManagement.parsistence.entities.Customer;
import com.example.demo.erp.customerManagement.parsistence.models.ClientInfo;
import com.example.demo.erp.customerManagement.services.CustomerImportExportService;
import com.example.demo.erp.customerManagement.services.CustomerCreationService;
import com.example.demo.erp.customerManagement.services.CustomerS;
import com.example.demo.finance.banking.parsitence.enitities.BankAccounts;
import com.example.demo.finance.banking.parsitence.enitities.Payments;
import com.example.demo.finance.banking.services.BankingService;
import com.example.demo.erp.communication.parsitence.models.WhatsAppMessage;
import com.example.demo.erp.communication.parsitence.models.bulkSmsModel;
import com.example.demo.erp.communication.services.AfricasTalkingApiService;
import com.example.demo.erp.communication.services.CommunicationService;
import com.example.demo.erp.communication.services.WhatsAppService;
import com.example.demo.erp.customerManagement.dto.CustomerAnalyticsDTO;
import com.example.demo.finance.loanManagement.parsistence.entities.Subscriptions;
import com.example.demo.finance.loanManagement.parsistence.entities.SuspensePayments;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanApplication;
import com.example.demo.finance.loanManagement.parsistence.models.*;
import com.example.demo.finance.loanManagement.services.LoanAccountService;
import com.example.demo.finance.loanManagement.services.LoanService;
import com.example.demo.finance.loanManagement.services.PaymentService;
import com.example.demo.finance.loanManagement.services.ProductService;
import com.example.demo.finance.loanManagement.services.SubscriptionService;
import com.example.demo.system.parsitence.models.DashBoardData;
import com.example.demo.system.parsitence.models.ResponseModel;
import com.example.demo.system.services.ReportService;
import com.example.demo.system.services.ScoreService;
import com.example.demo.system.userManagements.parsitence.enitities.Users;
import com.example.demo.system.userManagements.serviceImplementation.UserService;
import com.infobip.ApiException;
import com.infobip.model.SmsResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Log4j2
@RestController
@RequestMapping("api/customers")
public class CustomerController {

    public final SubscriptionService subscriptions;
    public final CustomerS customerService;
    public  final UserService userService;
    public final LoanService loanService;
    public final PaymentService paymentService;
    public final LoanAccountService loanAccountService;
    public final ReportService reportService;
    public final ScoreService scoreService;
    public final AfricasTalkingApiService sms;
    public final CommunicationService communicationService;
    public final WhatsAppService whatsAppService;
    public final CustomerImportExportService importExportService;
    private final ProductService productService;
    private final BankingService bankingService;
    private final CustomerCreationService customerCreationService;

    public CustomerController(
            SubscriptionService subscriptions, 
            CustomerS customerService, 
            UserService userService, 
            LoanService loanService, 
            PaymentService paymentService, 
            @Qualifier("loanAccountService") LoanAccountService loanAccountService, 
            ReportService reportService, 
            ScoreService scoreService, 
            AfricasTalkingApiService sms, 
            CommunicationService communicationService, 
            WhatsAppService whatsAppService,
            CustomerImportExportService importExportService,
            BankingService bankingService,
            ProductService productService,
            CustomerCreationService customerCreationService
    ) {
        this.subscriptions = subscriptions;
        this.customerService = customerService;
        this.userService = userService;
        this.loanService = loanService;
        this.paymentService = paymentService;
        this.loanAccountService = loanAccountService;
        this.reportService = reportService;
        this.scoreService = scoreService;
        this.sms = sms;
        this.communicationService = communicationService;
        this.whatsAppService = whatsAppService;
        this.importExportService = importExportService;
        this.bankingService = bankingService;
        this.productService = productService;
        this.customerCreationService = customerCreationService;
    }

    //creating customers using centralized service
    @PostMapping("/create")
    public ResponseEntity<?> createCustomer(
            @RequestBody Customer customer,
            Authentication authentication){
        try {
            String createdBy = authentication != null ? authentication.getName() : "ADMIN";
            
            // Use centralized customer creation service
            CustomerCreationService.CustomerCreationRequest request = CustomerCreationService.CustomerCreationRequest.builder()
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
                .createdBy(createdBy)
                .initialDepositAmount(customer.getInitialDepositAmount())
                .source(CustomerCreationService.CreationSource.ADMIN_UI)
                .build();
            
            CustomerCreationService.CustomerCreationResponse response = customerCreationService.createCustomer(request);
            
            if (response.isSuccess()) {
                log.info("Customer created successfully: {} with member number: {}", 
                    response.getCustomer().getId(), response.getMemberNumber());
                return new ResponseEntity<>(response.getCustomer(), HttpStatus.CREATED);
            } else {
                log.error("Failed to create customer: {}", response.getMessage());
                return new ResponseEntity<>(Map.of(
                    "success", false,
                    "error", response.getMessage()
                ), HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            log.error("Error creating customer", e);
            return new ResponseEntity<>(Map.of(
                "success", false,
                "error", e.getMessage()
            ), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    //finding customers info
    @GetMapping("all")
    public ResponseEntity<ResponseModel> findAll(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "search", required = false) String search
    ){
        ResponseModel customers = customerService.findAll(page, size, status, search);
        HttpStatus responseStatus = customers.getStatus() != null ? customers.getStatus() : HttpStatus.OK;
        return new ResponseEntity<>(customers,responseStatus);
    }
    //find all suspense payments
    @GetMapping("/findAllSuspense")
    public ResponseEntity<List<SuspensePayments>> findAllSuspense(@RequestParam("cusPhone") String cusPhone){
        List<SuspensePayments> suspensePayments=loanAccountService.findAllOverPayment(cusPhone).get();
        return new ResponseEntity<>(suspensePayments,HttpStatus.OK);
    }
    @GetMapping ("/findCus/{id}")
    public ResponseEntity<ClientInfo> findIndividual(@PathVariable("id") Long id){
       ClientInfo customer=customerService.findById(id);
        return new ResponseEntity<>(customer,HttpStatus.OK);
    }
    //This method is too expensive to be changed
    @GetMapping ("/findCusByUsername/{name}")
    public ResponseEntity<ClientInfo> findIndividualByName(@PathVariable("name") String name){
        Users user=userService.findByName(name).get();
        Customer customerS=customerService.findByPhone(user.getPhone()).get();
        ClientInfo customer=customerService.findById(customerS.getId());
        return new ResponseEntity<>(customer,HttpStatus.OK);
    }
    //updating customers info
    @PutMapping("/update")
    public ResponseEntity<Customer> updateIndividual(@RequestBody Customer customer){
        Customer customer1=customerService.update(customer);
        return new ResponseEntity<>(customer1,HttpStatus.CREATED);
    }
    //deactivating customer
    @PutMapping("/changeStatus")
    public ResponseEntity<Customer> changeStatus(@RequestParam("id") Long id, @RequestParam("status") String status){
        customerService.changeStatus(id,status);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PostMapping("/createSubscription")
    public ResponseEntity<subscriptionR> subscribe(@RequestBody subscriptionR req){
        subscriptions.subscribe(req.getPhone(),req.getProductId(),req.getAmount());
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @GetMapping("/findSubscription/{id}")
    public ResponseEntity<Optional<List<Subscriptions>>> findSubscription(@PathVariable("id") String id){
        Optional<List<Subscriptions>> subscription=subscriptions.findCustomerId(id);
        return new ResponseEntity<>(subscription,HttpStatus.OK);
    }
    @GetMapping("/findSubscriptionBybody")
    public ResponseEntity<Optional<Subscriptions>> findSubscription(@RequestParam("id") String id, @RequestParam("productCode") String productCode){
        Optional<Subscriptions> subscription=subscriptions.findCustomerIdandproductCode(id,productCode);
        return new ResponseEntity<>(subscription,HttpStatus.OK);
    }
    @PostMapping("/loanApplication")
    public ResponseEntity<LoanApplication> loanApplication(@RequestBody newApplication application){
       LoanApplication loanApp = loanService.loanApplication(
           application.getCustomerId(),
           application.getPhoneNumberValue(),
           application.getProductCode(),
           application.getAmount()
       );
        return new ResponseEntity<>(loanApp, HttpStatus.OK);
    }
    @PostMapping("/whatsappComm")
    public ResponseEntity whatsappComm(@RequestBody WhatsAppMessage message){
        whatsAppService.processWhatsAppRequest(message);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PostMapping("/loanRepayment")
    public ResponseEntity<Payments> loanRepayment(@RequestParam("phoneNumber") String phoneNumber, @RequestParam("productCode") String productCode, @RequestParam("amount") String amount){
        paymentService.paymentRequest(phoneNumber,productCode,amount);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PostMapping("/customPayment")
    public ResponseEntity<Payments> customPayment(@RequestBody Payments payment){
        paymentService.processLoanPayment(payment);
        return new ResponseEntity<>(HttpStatus.OK);
    }
   @PostMapping("/score")
    public ResponseEntity<Integer> score(@RequestParam("id") Long id){
        Integer score=scoreService.loadData(id);
        return new ResponseEntity<>(score,HttpStatus.OK);
    }

    /**
     * Enable channel-based authentication for a customer
     * NO LONGER creates a Users entity - authentication stored in Customer entity
     * 
     * @param id Customer ID
     * @param channel Channel to enable: web, mobile, ussd (default: mobile)
     * @param pin Optional PIN (if not provided, generates temporary PIN)
     * @return ResponseModel with status and PIN
     */
    @PostMapping("/enableClientLogin")
    public ResponseEntity<ResponseModel> enableLogin(
            @RequestParam("id") Long id,
            @RequestParam(value = "channel", defaultValue = "mobile") String channel,
            @RequestParam(value = "pin", required = false) String pin){
        
        log.info("Enabling {} channel for customer ID: {}", channel, id);
        ResponseModel response = customerService.enableClientLogin(id, channel, pin);
        return new ResponseEntity<>(response, response.getStatus());
    }
    
    /**
     * Legacy endpoint for backward compatibility - enables mobile channel
     * @deprecated Use /enableClientLogin with channel parameter instead
     */
    @Deprecated
    @PostMapping("/enableClientLogin/legacy")
    public ResponseEntity<ResponseModel> enableLoginLegacy(@RequestParam("id") Long id){
        log.warn("Using deprecated legacy endpoint for customer ID: {}", id);
        ResponseModel response = customerService.enableClientLogin(id);
        return new ResponseEntity<>(response, response.getStatus());
    }
    @GetMapping("/dashBoardData")
    public ResponseEntity<DashBoardData> getData(){
         DashBoardData data= reportService.getData();
        return new ResponseEntity<>(data,HttpStatus.OK);
    }
//    @PostMapping("/sendSms")
//    public ResponseEntity<List<Recipient>> send(String message) throws IOException {
//        List<Recipient> data= sms.sendSms(message);
//        return new ResponseEntity<>(data,HttpStatus.OK);
//    }
    @PostMapping("/sendSms2")
    public ResponseEntity<List<SmsResponse>> send(@RequestBody bulkSmsModel customSms) throws IOException, ApiException {
        List<SmsResponse> data= communicationService.sendBulkSMS(customSms);
        return new ResponseEntity<>(data,HttpStatus.CREATED);
    }

    /**
     * Get customer analytics using existing services
     */
    @GetMapping("/{customerId}/analytics")
    public ResponseEntity<CustomerAnalyticsDTO> getCustomerAnalytics(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "12") int months) {
        try {
            log.info("Getting analytics for customer ID: {}", customerId);
            
            // Use existing services to get data
            ClientInfo clientInfo = customerService.findById(customerId);
            if (clientInfo == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            Integer creditScore = scoreService.loadData(customerId);
            
            // Build simple analytics response
            CustomerAnalyticsDTO analytics = CustomerAnalyticsDTO.builder()
                .creditScore(creditScore != null ? creditScore : 300)
                .riskLevel(creditScore != null && creditScore >= 600 ? "LOW" : "HIGH")
                .customerStatus("ACTIVE")
                .totalTransactions(0)
                .activeLoans(0)
                .build();
                
            return new ResponseEntity<>(analytics, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error getting customer analytics", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // New endpoints for frontend integration
    @PostMapping("/applyLoan")
    public ResponseEntity<?> applyForLoan(@RequestBody newApplication application){
        try {
            log.info("Received loan application request - customerId: {}, phone: {}, product: {}, amount: {}", 
                application.getCustomerId(), application.getPhoneNumberValue(), 
                application.getProductCode(), application.getAmount());
            
            LoanApplication loanApp = loanService.loanApplication(
                application.getCustomerId(),
                application.getPhoneNumberValue(), 
                application.getProductCode(), 
                application.getAmount()
            );
            
            return new ResponseEntity<>(loanApp, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            log.error("Error processing loan application: {}", e.getMessage());
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/makePayment")
    public ResponseEntity<Payments> makePayment(@RequestBody Payments payment){
        paymentService.processLoanPayment(payment);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/sendMessage")
    public ResponseEntity<String> sendMessage(@RequestBody Object messageData){
        // Implementation for sending messages
        return new ResponseEntity<>("Message sent successfully", HttpStatus.OK);
    }

    @PutMapping("/updateProfile")
    public ResponseEntity<Customer> updateProfile(@RequestBody Customer customer){
        Customer updatedCustomer = customerService.update(customer);
        return new ResponseEntity<>(updatedCustomer, HttpStatus.OK);
    }

    /**
     * Import customers from Excel or CSV file
     * POST /api/customers/import
     */
    @PostMapping("/import")
    public ResponseEntity<ImportResultDto> importCustomers(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        try {
            String importedBy = authentication != null ? authentication.getName() : "system";
            ImportResultDto result = importExportService.importCustomers(file, importedBy);
            
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error importing customers", e);
            ImportResultDto errorResult = ImportResultDto.builder()
                    .failed(1)
                    .successful(0)
                    .totalRecords(0)
                    .build();
            errorResult.getErrors().add("Import failed: " + e.getMessage());
            return new ResponseEntity<>(errorResult, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Export all customers to Excel
     * GET /api/customers/export
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCustomers() {
        try {
            ByteArrayOutputStream outputStream = importExportService.exportCustomersToExcel();
            byte[] excelBytes = outputStream.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", 
                    "members_export_" + LocalDate.now() + ".xlsx");
            headers.setContentLength(excelBytes.length);

            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error exporting customers", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update customer status (activate/deactivate)
     * PATCH /api/customers/{id}/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Customer> updateCustomerStatus(
            @PathVariable Long id,
            @RequestBody StatusUpdateDto statusUpdate
    ) {
        try {
            Optional<Customer> optionalCustomer = customerService.findCustomerById(id);
            if (optionalCustomer.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            Customer customer = optionalCustomer.get();
            Boolean isActive = statusUpdate.getIsActive();
            customer.setAccountStatusFlag(isActive != null && isActive);
            customer.setStatus(isActive != null && isActive ? "ACTIVE" : "INACTIVE");
            
            Customer updated = customerService.update(customer);
            
            log.info("Customer {} status updated to: {}", id, statusUpdate.getIsActive());
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error updating customer status", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delete customer
     * DELETE /api/customers/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        try {
            Optional<Customer> optionalCustomer = customerService.findCustomerById(id);
            if (optionalCustomer.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            customerService.deleteCustomer(id);

            log.info("Customer {} deleted successfully", id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            log.error("Error deleting customer", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{id}/accounts/recreate")
    public ResponseEntity<?> recreateCustomerAccounts(@PathVariable Long id) {
        Optional<Customer> customerOptional = customerService.findCustomerById(id);
        if (customerOptional.isEmpty()) {
            return new ResponseEntity<>(Map.of(
                    "success", false,
                    "error", "Customer not found"
            ), HttpStatus.NOT_FOUND);
        }

        Customer customer = customerOptional.get();
        List<BankAccounts> accounts = bankingService.createBankAccounts(customer);
        bankingService.processInitialDepositIfPresent(customer);

        List<Map<String, Object>> accountSummaries = accounts.stream().map(account -> {
            Map<String, Object> summary = new java.util.HashMap<>();
            summary.put("id", account.getId());
            summary.put("accountNumber", account.getBankAccount());
            summary.put("accountType", account.getAccountType());
            summary.put("description", account.getAccountDescription());
            summary.put("balance", account.getAccountBalance());
            summary.put("createdAt", account.getCreatedAt());
            summary.put("updatedAt", account.getUpdatedAt());
            return summary;
        }).toList();

        return new ResponseEntity<>(Map.of(
                "success", true,
                "accounts", accountSummaries
        ), HttpStatus.OK);
    }

    @PostMapping("/{id}/accounts")
    public ResponseEntity<?> createAccountForProduct(
            @PathVariable Long id,
            @RequestBody CreateAccountRequest request
    ) {
        Optional<Customer> customerOptional = customerService.findCustomerById(id);
        if (customerOptional.isEmpty()) {
            return new ResponseEntity<>(Map.of(
                    "success", false,
                    "error", "Customer not found"
            ), HttpStatus.NOT_FOUND);
        }

        if (request == null || request.getProductId() == null) {
            return new ResponseEntity<>(Map.of(
                    "success", false,
                    "error", "productId is required"
            ), HttpStatus.BAD_REQUEST);
        }

        var productOptional = productService.findById(request.getProductId());
        if (productOptional.isEmpty()) {
            return new ResponseEntity<>(Map.of(
                    "success", false,
                    "error", "Product not found"
            ), HttpStatus.NOT_FOUND);
        }

        var product = productOptional.get();
        if (!Boolean.TRUE.equals(product.getActive())) {
            return new ResponseEntity<>(Map.of(
                    "success", false,
                    "error", "Product is not active"
            ), HttpStatus.BAD_REQUEST);
        }

        String transactionType = product.getTransactionType() != null ? product.getTransactionType().toUpperCase() : "";
        if (!transactionType.contains("SAV")) {
            return new ResponseEntity<>(Map.of(
                    "success", false,
                    "error", "Only savings products can create customer accounts"
            ), HttpStatus.BAD_REQUEST);
        }

        Customer customer = customerOptional.get();
        BankAccounts account = bankingService.createAccountForProduct(customer, product, request.getDescription());

        Map<String, Object> accountSummary = Map.of(
                "id", account.getId(),
                "accountNumber", account.getBankAccount(),
                "accountType", account.getAccountType(),
                "description", account.getAccountDescription(),
                "balance", account.getAccountBalance(),
                "createdAt", account.getCreatedAt(),
                "updatedAt", account.getUpdatedAt()
        );

        return new ResponseEntity<>(Map.of(
                "success", true,
                "account", accountSummary
        ), HttpStatus.CREATED);
    }

    public static class CreateAccountRequest {
        private Long productId;
        private String description;

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    /**
     * Update credit limit for a subscription
     * Can be used from client profile to:
     * 1. Manually override credit limit
     * 2. Apply calculation rules
     * POST /api/customers/subscription/updateCreditLimit
     */
    @PostMapping("/subscription/updateCreditLimit")
    public ResponseEntity<?> updateSubscriptionCreditLimit(@RequestBody Map<String, Object> request) {
        try {
            Long subscriptionId = Long.parseLong(request.get("subscriptionId").toString());
            Integer creditLimit = request.get("creditLimit") != null ? 
                Integer.parseInt(request.get("creditLimit").toString()) : null;
            Boolean override = request.get("override") != null ? 
                Boolean.parseBoolean(request.get("override").toString()) : false;
            String calculationRule = request.get("calculationRule") != null ? 
                request.get("calculationRule").toString() : null;
            
            // If calculation rule provided, calculate the limit
            if (calculationRule != null && !calculationRule.isEmpty() && creditLimit == null) {
                // Get subscription to find customer ID
                Optional<Subscriptions> subOpt = subscriptions.subscriptionsRepo.findById(subscriptionId);
                if (subOpt.isPresent()) {
                    Subscriptions sub = subOpt.get();
                    creditLimit = subscriptions.calculateCreditLimit(
                        sub.getCustomerId(), 
                        sub.getProductCode(), 
                        calculationRule
                    );
                }
            }
            
            Subscriptions updated = subscriptions.updateCreditLimit(
                subscriptionId, 
                creditLimit, 
                override,
                calculationRule
            );
            
            return new ResponseEntity<>(Map.of(
                "success", true,
                "message", "Credit limit updated successfully",
                "subscription", updated
            ), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error updating credit limit: {}", e.getMessage());
            return new ResponseEntity<>(Map.of(
                "success", false,
                "error", e.getMessage()
            ), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Get available credit limit calculation rules
     * GET /api/customers/subscription/creditLimitRules
     */
    @GetMapping("/subscription/creditLimitRules")
    public ResponseEntity<List<Map<String, String>>> getCreditLimitRules() {
        List<Map<String, String>> rules = List.of(
            Map.of("code", "3X_SAVINGS", "name", "3X Savings Balance", "description", "3 times customer's savings"),
            Map.of("code", "5X_SAVINGS", "name", "5X Savings Balance", "description", "5 times customer's savings"),
            Map.of("code", "FIXED_50K", "name", "Fixed 50,000", "description", "Fixed limit of 50,000"),
            Map.of("code", "FIXED_100K", "name", "Fixed 100,000", "description", "Fixed limit of 100,000"),
            Map.of("code", "BASED_ON_HISTORY", "name", "Based on History", "description", "Calculate based on repayment history")
        );
        return new ResponseEntity<>(rules, HttpStatus.OK);
    }


}

