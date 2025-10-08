# 🔧 SACCO Management System - Complete Backend Fix Guide

## ⚠️ Critical Error: Spring Boot Parameter Names

### **Error Diagnosis:**
```
java.lang.IllegalArgumentException: Name for argument of type [java.time.LocalDate] not specified, 
and parameter name information not available via reflection. 
Ensure that the compiler uses the '-parameters' flag.
```

**Root Cause**: Java compiler not preserving parameter names for reflection

---

## 🛠️ IMMEDIATE FIX - Add Compiler Parameters

### **Option 1: Fix in pom.xml (Maven)**

Add this to your `pom.xml` file:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <source>17</source>
                <target>17</target>
                <parameters>true</parameters>  <!-- ADD THIS LINE -->
                <compilerArgs>
                    <arg>-parameters</arg>  <!-- AND THIS -->
                </compilerArgs>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### **Option 2: Use @RequestParam Annotation**

**BEFORE (Causes Error):**
```java
@GetMapping("/reports")
public ResponseEntity<?> getReports(LocalDate startDate, LocalDate endDate) {
    // Missing parameter annotations
}
```

**AFTER (Fixed):**
```java
@GetMapping("/reports")
public ResponseEntity<?> getReports(
    @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
    @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
) {
    // Properly annotated
}
```

---

## 📋 COMPLETE SACCO MANAGEMENT API REQUIREMENTS

### **1. Reports Module APIs**

#### **A. Standard Reports**

```java
@RestController
@RequestMapping("/api/reports")
public class ReportsController {

    // General Reports
    @GetMapping("/loans")
    public ResponseEntity<LoanReportDTO> getLoanReport(
        @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(value = "status", required = false) String status
    ) {
        // Implementation
    }

    @GetMapping("/disbursements")
    public ResponseEntity<DisbursementReportDTO> getDisbursementReport(
        @RequestParam("startDate") LocalDate startDate,
        @RequestParam("endDate") LocalDate endDate
    ) {
        // Implementation
    }

    @GetMapping("/collections")
    public ResponseEntity<CollectionReportDTO> getCollectionReport(
        @RequestParam("startDate") LocalDate startDate,
        @RequestParam("endDate") LocalDate endDate
    ) {
        // Implementation
    }

    @GetMapping("/arrears")
    public ResponseEntity<ArrearsReportDTO> getArrearsReport(
        @RequestParam(value = "asOfDate", required = false) LocalDate asOfDate
    ) {
        // Implementation
    }

    @GetMapping("/portfolio")
    public ResponseEntity<PortfolioReportDTO> getPortfolioReport() {
        // Implementation
    }
}
```

#### **B. SASRA (Regulatory) Reports**

```java
@RestController
@RequestMapping("/api/reports/sasra")
public class SasraReportsController {

    @GetMapping("/cr1")
    public ResponseEntity<SasraCR1ReportDTO> getCR1Report(
        @RequestParam("reportingDate") LocalDate reportingDate
    ) {
        // SASRA CR1 - Loan Portfolio Report
    }

    @GetMapping("/cr2")
    public ResponseEntity<SasraCR2ReportDTO> getCR2Report(
        @RequestParam("reportingDate") LocalDate reportingDate
    ) {
        // SASRA CR2 - Classification of Loans
    }

    @GetMapping("/cr3")
    public ResponseEntity<SasraCR3ReportDTO> getCR3Report(
        @RequestParam("reportingDate") LocalDate reportingDate
    ) {
        // SASRA CR3 - Loan Loss Provisions
    }

    @GetMapping("/sr1")
    public ResponseEntity<SasraSR1ReportDTO> getSR1Report(
        @RequestParam("reportingDate") LocalDate reportingDate
    ) {
        // SASRA SR1 - Statistical Report
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportSasraReport(
        @RequestParam("reportType") String reportType,
        @RequestParam("reportingDate") LocalDate reportingDate
    ) {
        // Export to Excel
    }
}
```

---

### **2. Missing APIs by Module**

#### **Loans Module**

```java
@RestController
@RequestMapping("/api/loans")
public class LoanController {

    @PostMapping("/apply")
    public ResponseEntity<LoanApplicationResponse> applyForLoan(@RequestBody LoanApplicationRequest request) {
        // Loan application submission
    }

    @PostMapping("/approve")
    public ResponseEntity<LoanApprovalResponse> approveLoan(
        @RequestParam("loanId") Long loanId,
        @RequestParam("approvedAmount") BigDecimal approvedAmount
    ) {
        // Loan approval
    }

    @PostMapping("/disburse")
    public ResponseEntity<DisbursementResponse> disburseLoan(
        @RequestParam("loanId") Long loanId
    ) {
        // Loan disbursement
    }

    @PostMapping("/reject")
    public ResponseEntity<String> rejectLoan(
        @RequestParam("loanId") Long loanId,
        @RequestParam("reason") String reason
    ) {
        // Loan rejection
    }

    @GetMapping("/schedule/{loanId}")
    public ResponseEntity<RepaymentScheduleDTO> getRepaymentSchedule(@PathVariable Long loanId) {
        // Get repayment schedule
    }

    @GetMapping("/arrears")
    public ResponseEntity<List<LoanArrearsDTO>> getLoansInArrears() {
        // Get loans in arrears
    }

    @PostMapping("/restructure")
    public ResponseEntity<LoanRestructureResponse> restructureLoan(@RequestBody LoanRestructureRequest request) {
        // Loan restructuring
    }

    @PostMapping("/writeoff")
    public ResponseEntity<String> writeOffLoan(@RequestParam("loanId") Long loanId) {
        // Loan write-off
    }
}
```

#### **Payments Module**

```java
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @PostMapping("")
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest request) {
        // Process loan payment
    }

    @PostMapping("/bulk")
    public ResponseEntity<BulkPaymentResponse> processBulkPayments(@RequestBody List<PaymentRequest> requests) {
        // Process multiple payments
    }

    @GetMapping("/history")
    public ResponseEntity<List<PaymentHistoryDTO>> getPaymentHistory(
        @RequestParam("customerId") Long customerId,
        @RequestParam(value = "startDate", required = false) LocalDate startDate,
        @RequestParam(value = "endDate", required = false) LocalDate endDate
    ) {
        // Get payment history
    }

    @PostMapping("/reverse")
    public ResponseEntity<String> reversePayment(
        @RequestParam("paymentId") Long paymentId,
        @RequestParam("reason") String reason
    ) {
        // Payment reversal
    }

    @GetMapping("/mpesa/callback")
    public ResponseEntity<String> mpesaCallback(@RequestBody String callbackData) {
        // M-Pesa payment callback
    }
}
```

#### **Customers Module**

```java
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @PostMapping("/create")
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        // Create customer
    }

    @PutMapping("/update")
    public ResponseEntity<Customer> updateCustomer(@RequestBody Customer customer) {
        // Update customer
    }

    @GetMapping("/findCus/{id}")
    public ResponseEntity<ClientProfile> getCustomerProfile(@PathVariable Long id) {
        // Get customer profile
    }

    @GetMapping("/all")
    public ResponseEntity<Page<Customer>> getAllCustomers(
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        // Get all customers with pagination
    }

    @PostMapping("/createSubscription")
    public ResponseEntity<Subscription> createSubscription(@RequestBody SubscriptionRequest request) {
        // Create product subscription
    }

    @GetMapping("/search")
    public ResponseEntity<List<Customer>> searchCustomers(@RequestParam("query") String query) {
        // Search customers
    }

    @GetMapping("/dashboard/{customerId}")
    public ResponseEntity<CustomerDashboardDTO> getCustomerDashboard(@PathVariable Long customerId) {
        // Customer dashboard data
    }

    @PutMapping("/deactivate/{customerId}")
    public ResponseEntity<String> deactivateCustomer(@PathVariable Long customerId) {
        // Deactivate customer
    }

    @PutMapping("/activate/{customerId}")
    public ResponseEntity<String> activateCustomer(@PathVariable Long customerId) {
        // Activate customer
    }
}
```

#### **Products Module**

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @GetMapping("")
    public ResponseEntity<List<Product>> getAllProducts() {
        // Get all products
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        // Get product by ID
    }

    @PostMapping("/create")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        // Create product
    }

    @PutMapping("/update")
    public ResponseEntity<Product> updateProduct(@RequestBody Product product) {
        // Update product
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        // Delete product
    }

    @PutMapping("/toggle/{id}")
    public ResponseEntity<Product> toggleProductStatus(@PathVariable Long id) {
        // Toggle product active status
    }

    @GetMapping("/active")
    public ResponseEntity<List<Product>> getActiveProducts() {
        // Get only active products
    }

    @GetMapping("/calculate")
    public ResponseEntity<LoanCalculationDTO> calculateLoan(
        @RequestParam("productId") Long productId,
        @RequestParam("amount") BigDecimal amount,
        @RequestParam("term") Integer term
    ) {
        // Calculate loan details
    }
}
```

#### **Communications Module**

```java
@RestController
@RequestMapping("/api/communications")
public class CommunicationController {

    @PostMapping("/send")
    public ResponseEntity<CommunicationResponse> sendMessage(@RequestBody CommunicationRequest request) {
        // Send SMS/Email
    }

    @PostMapping("/bulk")
    public ResponseEntity<BulkCommunicationResponse> sendBulkMessage(@RequestBody BulkCommunicationRequest request) {
        // Send bulk messages
    }

    @GetMapping("/history/{customerId}")
    public ResponseEntity<List<CommunicationHistoryDTO>> getHistory(@PathVariable Long customerId) {
        // Get communication history
    }

    @GetMapping("/templates")
    public ResponseEntity<List<MessageTemplate>> getTemplates() {
        // Get message templates
    }

    @PostMapping("/templates")
    public ResponseEntity<MessageTemplate> createTemplate(@RequestBody MessageTemplate template) {
        // Create message template
    }
}
```

---

### **3. DTOs (Data Transfer Objects) Needed**

```java
// Loan DTOs
public class LoanApplicationRequest {
    private Long customerId;
    private Long productId;
    private BigDecimal amount;
    private Integer term;
    private String purpose;
    private String phoneNumber;
}

public class LoanApplicationResponse {
    private String applicationId;
    private String status;
    private String message;
    private BigDecimal approvedAmount;
    private LocalDate expectedDisbursementDate;
}

// Report DTOs
public class LoanReportDTO {
    private Integer totalLoans;
    private BigDecimal totalDisbursed;
    private BigDecimal totalRepaid;
    private BigDecimal outstandingBalance;
    private BigDecimal portfolioAtRisk;
    private List<LoanDetailDTO> loans;
}

public class ArrearsReportDTO {
    private Integer totalLoansInArrears;
    private BigDecimal totalArrearsAmount;
    private List<LoanArrearsDetailDTO> arrearsDetails;
}

// SASRA DTOs
public class SasraCR1ReportDTO {
    private LocalDate reportingDate;
    private BigDecimal totalLoanPortfolio;
    private BigDecimal performingLoans;
    private BigDecimal nonPerformingLoans;
    private Double nplRatio;
    private List<LoanClassificationDTO> classifications;
}

// Payment DTOs
public class PaymentRequest {
    private Long customerId;
    private Long loanId;
    private BigDecimal amount;
    private String paymentMethod;
    private String reference;
    private String notes;
}

public class PaymentResponse {
    private String transactionId;
    private String status;
    private BigDecimal newBalance;
    private String receiptNumber;
}
```

---

### **4. Database Schema Updates Needed**

```sql
-- Loan Applications Table
CREATE TABLE IF NOT EXISTS loan_applications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    application_ref VARCHAR(50) UNIQUE NOT NULL,
    customer_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    requested_amount DECIMAL(15,2) NOT NULL,
    approved_amount DECIMAL(15,2),
    term INT NOT NULL,
    purpose VARCHAR(200),
    status VARCHAR(50) DEFAULT 'PENDING',
    application_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved_date TIMESTAMP,
    disbursed_date TIMESTAMP,
    rejected_date TIMESTAMP,
    rejection_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Payments Table
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_id VARCHAR(50) UNIQUE NOT NULL,
    customer_id BIGINT NOT NULL,
    loan_id BIGINT NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    payment_method VARCHAR(50) NOT NULL,
    reference VARCHAR(100),
    status VARCHAR(50) DEFAULT 'COMPLETED',
    reversed BOOLEAN DEFAULT FALSE,
    reversal_reason TEXT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (loan_id) REFERENCES loan_accounts(id)
);

-- Communications Table
CREATE TABLE IF NOT EXISTS communications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    message_id VARCHAR(50) UNIQUE NOT NULL,
    customer_id BIGINT,
    message_type VARCHAR(50) NOT NULL,
    subject VARCHAR(200),
    message TEXT NOT NULL,
    recipient VARCHAR(100) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    failed_at TIMESTAMP,
    failure_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- Loan Repayment Schedule
CREATE TABLE IF NOT EXISTS repayment_schedule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    loan_id BIGINT NOT NULL,
    installment_number INT NOT NULL,
    due_date DATE NOT NULL,
    principal_due DECIMAL(15,2) NOT NULL,
    interest_due DECIMAL(15,2) NOT NULL,
    total_due DECIMAL(15,2) NOT NULL,
    principal_paid DECIMAL(15,2) DEFAULT 0,
    interest_paid DECIMAL(15,2) DEFAULT 0,
    total_paid DECIMAL(15,2) DEFAULT 0,
    status VARCHAR(50) DEFAULT 'PENDING',
    paid_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (loan_id) REFERENCES loan_accounts(id)
);
```

---

## 🚀 DEPLOYMENT STEPS

### **1. Fix Backend Compilation**
```bash
cd s:/code/PERSONAL/java/Sacco-Management-backend-API-

# Clean and rebuild with parameters
mvn clean compile -DskipTests

# Rebuild entire project
mvn clean install -DskipTests
```

### **2. Restart Application**
```bash
# Stop current instance
# Start with updated configuration
mvn spring-boot:run
```

---

## ✅ VALIDATION CHECKLIST

- [ ] Backend compiles with `-parameters` flag
- [ ] All controller methods have @RequestParam annotations
- [ ] All LocalDate parameters have @DateTimeFormat
- [ ] Reports API endpoints working
- [ ] SASRA reports generating correctly
- [ ] Loan application flow complete
- [ ] Payment processing working
- [ ] Communication APIs functional
- [ ] All database tables created
- [ ] Indexes created for performance

---

## 📊 TESTING COMMANDS

```bash
# Test Reports API
curl -X GET "http://localhost:8080/api/reports/loans?startDate=2025-01-01&endDate=2025-10-08" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Test SASRA Report
curl -X GET "http://localhost:8080/api/reports/sasra/cr1?reportingDate=2025-10-08" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Test Loan Application
curl -X POST "http://localhost:8080/api/loans/apply" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "customerId": 1,
    "productId": 1,
    "amount": 50000,
    "term": 12,
    "purpose": "Business",
    "phoneNumber": "0712345678"
  }'
```

---

**Priority**: CRITICAL  
**Estimated Fix Time**: 30 minutes  
**Full Implementation**: 2-3 days

---

Created: 2025-10-08  
Version: 1.0  
Status: Ready for Implementation
