# Loan Centralization - Quick Start Guide

## For Developers

### Creating a Loan Application (Any Source)

```java
@Autowired
private LoanApplicationOrchestrator applicationOrchestrator;

// Build command
LoanApplicationCommand command = LoanApplicationCommand.builder()
    .source(ApplicationSource.ADMIN_UI) // or UPLOAD, MOBILE_APP, CLIENT_PROFILE, API
    .customerId(123L) // or use customerExternalId for uploads
    .productCode("PERSONAL_LOAN")
    .loanAmount(BigDecimal.valueOf(50000))
    .term(12) // months
    .disbursementType("MPESA")
    .destinationAccount("254712345678")
    .requestedBy("admin@system.com")
    .build();

// Create application
LoanApplicationResponse response = applicationOrchestrator.createApplication(command);

// Response contains:
// - applicationId
// - loanNumber
// - applicationStatus (NEW or DISBURSED for uploads)
// - calculation preview (principal, interest, total, monthly payment)
// - warnings (if any)
```

### Approving an Application

```java
@Autowired
private LoanWorkflowService workflowService;

// Approve
LoanApplication approved = workflowService.approveApplication(
    applicationId, 
    "approver@system.com",
    "Application meets all criteria"
);

// Or Reject
LoanApplication rejected = workflowService.rejectApplication(
    applicationId,
    "approver@system.com", 
    "Insufficient collateral"
);
```

### Booking/Disbursing a Loan

```java
@Autowired
private LoanBookingService bookingService;

// Build booking command
LoanBookingCommand command = LoanBookingCommand.builder()
    .applicationId(applicationId)
    .disbursementMethod("MPESA_B2C") // or BANK_TRANSFER, CASH, SACCO_ACCOUNT
    .disbursementReference("DISB-2024-001")
    .disbursedBy("disbursement@system.com")
    .phoneNumber("254712345678") // for MPESA
    .postToAccounting(true)
    .build();

// Book the loan
LoanAccount account = bookingService.bookLoan(command);

// Account now has:
// - Loan reference
// - Repayment schedules
// - Accounting entries
// - Status: ACTIVE
```

### Processing a Payment

```java
@Autowired
private PaymentProcessingHub paymentHub;

// M-PESA Payment
loanTransactions txn = paymentHub.processMpesaPayment(
    loanId,
    1000.0, // amount
    "QXY123ABC", // MPESA receipt
    "254712345678" // phone
);

// Manual Payment
loanTransactions txn = paymentHub.processManualPayment(
    loanId,
    1000.0,
    "BANK", // payment method
    "BNK-REF-001",
    "cashier@system.com" // approved by
);

// Mobile App Payment
loanTransactions txn = paymentHub.processMobilePayment(
    loanId,
    1000.0,
    "MPESA",
    "MOBILE-TXN-001"
);
```

## Migration Checklist

### For Existing Services

#### 1. LoanService (Legacy)
- [ ] Replace `loanApplication()` with `applicationOrchestrator.createApplication()`
- [ ] Remove direct account creation logic
- [ ] Use `bookingService.bookLoan()` for loan accounts

#### 2. MobileLoanService
- [ ] Update `applyForLoan()` to use orchestrator
- [ ] Update `makeLoanRepayment()` to use `paymentHub.processMobilePayment()`

#### 3. LoanDisbursementService
- [ ] Integrate with `bookingService.bookLoan()`
- [ ] Use `workflowService` for status updates

#### 4. TransactionApprovalService
- [ ] Update `processLoanRepayment()` to use payment hub
- [ ] Remove direct loan payment logic

### For Frontend

#### Angular Services to Update
- [ ] Merge `loan.service.ts` and `loan-application.service.ts`
- [ ] Add new status badges (NEW, PENDING_REVIEW, APPROVED, READY_FOR_DISBURSEMENT, DISBURSED)
- [ ] Update approval workflow UI

## Common Patterns

### Pattern 1: Upload Flow
```java
// Upload creates application + books loan in one transaction
LoanApplicationCommand cmd = buildFromUploadDTO(uploadDTO);
cmd.setIsUpload(true); // Auto-marks as DISBURSED
LoanApplicationResponse app = orchestrator.createApplication(cmd);
LoanBookingCommand bookCmd = buildBookingCommand(app, uploadDTO);
LoanAccount account = bookingService.bookLoan(bookCmd);
```

### Pattern 2: Standard Approval Flow
```java
// Step 1: Create application
LoanApplicationCommand cmd = buildFromUserInput();
LoanApplicationResponse app = orchestrator.createApplication(cmd);
// Status: NEW

// Step 2: Review and approve
workflow.moveToPendingReview(app.getApplicationId(), reviewer, comments);
// Status: PENDING_REVIEW

workflow.approveApplication(app.getApplicationId(), approver, comments);
// Status: APPROVED

// Step 3: Disburse
LoanBookingCommand bookCmd = buildDisbursementCommand(app);
LoanAccount account = bookingService.bookLoan(bookCmd);
// Status: DISBURSED, Account: ACTIVE
```

### Pattern 3: Payment Processing
```java
// All payment sources funnel through hub
PaymentCommand cmd = PaymentCommand.builder()
    .source(PaymentSource.MOBILE_APP)
    .loanId(loanId)
    .amount(BigDecimal.valueOf(amount))
    .paymentMethod("MPESA")
    .referenceNumber(reference)
    .build();

loanTransactions txn = paymentHub.processPayment(cmd);
```

## Status Reference

### Application Statuses
- `NEW` - Just created, awaiting review
- `PENDING_REVIEW` - Under review by loan officer
- `APPROVED` - Approved, ready for disbursement
- `READY_FOR_DISBURSEMENT` - Queued for disbursement
- `DISBURSED` - Funds disbursed, loan active
- `REJECTED` - Application rejected
- `CLOSED` - Loan fully paid and closed
- `DEFAULTED` - Loan defaulted

### Account Statuses
- `INIT` - Initial state (legacy)
- `ACTIVE` - Loan disbursed and active
- `CURRENT` - Payments up to date
- `OVERDUE` - Past due date
- `CLOSED` - Fully paid
- `DEFAULTED` - Defaulted

### Schedule Statuses
- `PENDING` - Not yet due
- `CURRENT` - Due now
- `OVERDUE` - Past due
- `PAID` - Fully paid
- `PARTIALLY_PAID` - Partially paid

## Troubleshooting

### Issue: "Customer not found"
**Solution:** Ensure customer exists before creating application. Orchestrator doesn't auto-create customers.

### Issue: "Invalid state transition"
**Solution:** Check current application status. Only specific transitions are allowed (see workflow service).

### Issue: "Product not found"
**Solution:** Ensure product code matches exactly. Products must be active.

### Issue: "Subscription not found" (Warning)
**Solution:** This is a warning for non-upload flows. Create subscription before application or proceed with manual review.

### Issue: "Cannot book loan - application not approved"
**Solution:** Application must be in APPROVED, READY_FOR_DISBURSEMENT, or DISBURSED status.

## Testing Commands

### Test Upload Flow
```bash
curl -X POST http://localhost:8080/api/loan-book/upload \
  -F "file=@loan_book.csv"
```

### Test Application Creation
```bash
curl -X POST http://localhost:8080/api/loan-applications/apply \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "123",
    "productCode": "PERSONAL_LOAN",
    "amount": 50000,
    "term": 12
  }'
```

### Test Payment
```bash
curl -X POST http://localhost:8080/api/loans/payments/process \
  -H "Content-Type: application/json" \
  -d '{
    "loanId": 456,
    "amount": 1000,
    "paymentMethod": "MPESA",
    "referenceNumber": "QXY123ABC"
  }'
```

## Next Steps

1. **Review Implementation**: Check `LOAN_CENTRALIZATION_IMPLEMENTATION.md`
2. **Test Existing Flows**: Verify uploads and M-PESA still work
3. **Migrate Services**: Update remaining services one by one
4. **Update Frontend**: Consolidate Angular services
5. **Document APIs**: Update Swagger/API documentation
6. **Train Team**: Share this guide with developers

---

**Status:** ✅ Core Infrastructure Complete
**Ready For:** Integration testing and service migration
