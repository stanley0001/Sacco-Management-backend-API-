package com.example.demo.finance.payments.services;

import com.example.demo.finance.banking.parsitence.enitities.BankAccounts;
import com.example.demo.finance.banking.parsitence.enitities.Transactions;
import com.example.demo.finance.banking.parsitence.repositories.BankAccountRepo;
import com.example.demo.finance.banking.parsitence.repositories.PaymentTransactionRepo;
import com.example.demo.finance.loanManagement.parsistence.entities.SuspensePayments;
import com.example.demo.finance.loanManagement.parsistence.entities.loanTransactions;
import com.example.demo.finance.loanManagement.parsistence.repositories.SuspensePaymentRepo;
import com.example.demo.finance.loanManagement.services.LoanPaymentService;
import com.example.demo.finance.payments.entities.MpesaTransaction;
import com.example.demo.finance.payments.entities.TransactionRequest;
import com.example.demo.finance.payments.repositories.TransactionRequestRepository;
import com.example.demo.finance.savingsManagement.persistence.entities.SavingsTransaction;
import com.example.demo.finance.savingsManagement.services.SavingsAccountService;
import com.example.demo.erp.communication.sms.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionApprovalService {

    private final TransactionRequestRepository transactionRequestRepository;
    private final SavingsAccountService savingsAccountService;
    private final LoanPaymentService loanPaymentService;
    private final BankAccountRepo bankAccountRepo;
    private final PaymentTransactionRepo paymentTransactionRepo;
    private final SuspensePaymentRepo suspensePaymentRepo;
    private final SmsService smsService;

    @Transactional
    public TransactionRequest approveTransaction(Long requestId, String approvedBy, String referenceNumber) {
        TransactionRequest request = transactionRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Transaction request not found: " + requestId));

        if (request.getStatus() == TransactionRequest.RequestStatus.FAILED
            || request.getStatus() == TransactionRequest.RequestStatus.CANCELLED) {
            throw new IllegalStateException("Cannot approve a failed or cancelled request");
        }

        if (request.getStatus() == TransactionRequest.RequestStatus.POSTED_TO_ACCOUNT) {
            log.info("Transaction {} already posted, skipping approval.", requestId);
            return request;
        }

        try {
            if (request.getLoanId() != null
                || request.getTransactionCategory() == TransactionRequest.TransactionCategory.LOAN_REPAYMENT) {
                processLoanRepayment(request, referenceNumber, approvedBy);
            } else if (request.getTargetAccountId() != null) {
                processBankDeposit(request, referenceNumber, approvedBy);
            } else if (request.getSavingsAccountId() != null) {
                processSavingsDeposit(request, referenceNumber, approvedBy);
            } else {
                // No target account - record as suspense
                log.warn("No target account found for transaction {}. Recording as suspense.", requestId);
                recordSuspensePayment(request, referenceNumber, "NO_TARGET_ACCOUNT");
                throw new IllegalStateException("Transaction recorded as suspense - no target account");
            }
        } catch (Exception e) {
            // If processing fails, record as suspense for later reconciliation
            if (!e.getMessage().contains("suspense")) {
                log.error("Error processing transaction {}: {}. Recording as suspense.", requestId, e.getMessage());
                recordSuspensePayment(request, referenceNumber, "PROCESSING_ERROR: " + e.getMessage());
            }
            throw e;
        }

        request.setStatus(TransactionRequest.RequestStatus.POSTED_TO_ACCOUNT);
        request.setPostedToAccount(true);
        request.setProcessedAt(LocalDateTime.now());
        request.setProcessedBy(approvedBy);
        if (referenceNumber != null && !referenceNumber.isBlank()) {
            request.setReferenceNumber(referenceNumber);
        }

        TransactionRequest savedRequest = transactionRequestRepository.save(request);
        
        // Send SMS notification for approved manual payment (non-MPESA)
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()
            && request.getPaymentMethod() != TransactionRequest.PaymentMethodType.MPESA) {
            try {
                String paymentType = request.getTransactionCategory() != null 
                    ? request.getTransactionCategory().name() 
                    : request.getType().name();
                    
                String message = String.format(
                    "Payment approved! Amount: KES %,.2f. Type: %s. Method: %s. Reference: %s. Your payment has been posted to your account.",
                    request.getAmount(),
                    paymentType,
                    request.getPaymentMethod(),
                    request.getReferenceNumber()
                );
                
                smsService.sendSms(request.getPhoneNumber(), message);
                log.info("SMS notification sent for approved payment: {} to {}", 
                    request.getId(), request.getPhoneNumber());
            } catch (Exception smsEx) {
                // Don't fail the transaction if SMS fails
                log.error("Failed to send SMS for approved payment {}: {}", 
                    request.getId(), smsEx.getMessage());
            }
        }

        return savedRequest;
    }

    @Transactional
    public TransactionRequest rejectTransaction(Long requestId, String rejectedBy, String reason) {
        TransactionRequest request = transactionRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Transaction request not found: " + requestId));

        if (request.getStatus() == TransactionRequest.RequestStatus.POSTED_TO_ACCOUNT) {
            throw new IllegalStateException("Cannot reject a transaction that has already been posted");
        }

        request.setStatus(TransactionRequest.RequestStatus.FAILED);
        request.setFailureReason(reason);
        request.setProcessedAt(LocalDateTime.now());
        request.setProcessedBy(rejectedBy);

        return transactionRequestRepository.save(request);
    }

    @Transactional
    public TransactionRequest autoPostSuccessfulMpesa(MpesaTransaction mpesaTransaction) {
        if (mpesaTransaction.getTransactionRequestId() == null) {
            log.warn("M-PESA transaction {} has no linked transaction request", mpesaTransaction.getId());
            return null;
        }

        TransactionRequest request = transactionRequestRepository.findById(mpesaTransaction.getTransactionRequestId())
            .orElseThrow(() -> new RuntimeException("Transaction request not found: " + mpesaTransaction.getTransactionRequestId()));

        if (Boolean.TRUE.equals(request.getPostedToAccount())) {
            log.info("Transaction request {} already posted", request.getId());
            return request;
        }

        approveTransaction(request.getId(), "MPESA_CALLBACK", mpesaTransaction.getMpesaReceiptNumber());
        request.setStatus(TransactionRequest.RequestStatus.SUCCESS);
        request.setServiceProviderResponse(mpesaTransaction.getResultDesc());

        return transactionRequestRepository.save(request);
    }

    private void processSavingsDeposit(TransactionRequest request, String referenceNumber, String approvedBy) {
        if (request.getSavingsAccountId() == null) {
            throw new IllegalStateException("Savings deposit requires savings account ID");
        }

        SavingsTransaction transaction = savingsAccountService.deposit(
            request.getSavingsAccountId(),
            request.getAmount(),
            request.getPaymentMethod() != null ? request.getPaymentMethod().name() : "MANUAL",
            referenceNumber != null ? referenceNumber : request.getReferenceNumber(),
            request.getDescription(),
            approvedBy
        );

        request.setReferenceNumber(transaction.getPaymentReference());
        request.setPostedAt(LocalDateTime.now());
    }

    private void processBankDeposit(TransactionRequest request, String referenceNumber, String approvedBy) {
        if (request.getTargetAccountId() == null) {
            throw new IllegalStateException("Bank deposit requires bank account ID");
        }

        BankAccounts bankAccount = bankAccountRepo.findById(request.getTargetAccountId())
            .orElseThrow(() -> new RuntimeException("Bank account not found: " + request.getTargetAccountId()));

        log.info("Posting MPESA bank deposit for account {} approved by {}", bankAccount.getId(), approvedBy);

        double openingBalance = bankAccount.getAccountBalance() != null ? bankAccount.getAccountBalance() : 0.0d;
        double amount = request.getAmount() != null ? request.getAmount().doubleValue() : 0.0d;
        double closingBalance = openingBalance + amount;

        String effectiveReference = (referenceNumber != null && !referenceNumber.isBlank())
            ? referenceNumber
            : (request.getReferenceNumber() != null ? request.getReferenceNumber() : "BANK-" + System.currentTimeMillis());

        Transactions transaction = new Transactions();
        transaction.setTransactionTime(LocalDateTime.now());
        transaction.setTransactionType("DEPOSIT");
        transaction.setAmount(amount);
        transaction.setOpeningBalance(openingBalance);
        transaction.setClosingBalance(closingBalance);
        transaction.setOtherRef(effectiveReference);
        transaction.setBankAccount(bankAccount);

        paymentTransactionRepo.save(transaction);

        bankAccount.setAccountBalance(closingBalance);
        bankAccount.setUpdatedAt(LocalDateTime.now());
        bankAccountRepo.save(bankAccount);

        request.setReferenceNumber(effectiveReference);
        request.setPostedAt(LocalDateTime.now());
    }

    private void processLoanRepayment(TransactionRequest request, String referenceNumber, String approvedBy) {
        if (request.getLoanId() == null) {
            throw new IllegalStateException("Loan repayment requires loan ID");
        }

        loanTransactions loanTxn = loanPaymentService.processLoanPayment(
            request.getLoanId(),
            request.getAmount(),
            request.getPaymentMethod() != null ? request.getPaymentMethod().name() : "MANUAL",
            referenceNumber != null ? referenceNumber : request.getReferenceNumber()
        );

        request.setUtilizedForLoan(true);
        request.setUtilizedLoanId(request.getLoanId());
        request.setReferenceNumber(loanTxn.getOtherRef());
        request.setPostedAt(LocalDateTime.now());
    }

    /**
     * Create manual payment request that requires approval
     * For Cash, Cheque, Bank Transfer payments
     */
    @Transactional
    public TransactionRequest createManualPaymentRequest(
            Long customerId,
            String customerName,
            String phoneNumber,
            BigDecimal amount,
            TransactionRequest.PaymentMethodType paymentMethod,
            TransactionRequest.TransactionType transactionType,
            TransactionRequest.TransactionCategory transactionCategory,
            Long targetAccountId,
            Long loanId,
            Long savingsAccountId,
            String referenceNumber,
            String description,
            String initiatedBy) {
        
        log.info("Creating manual payment request: customerId={}, amount={}, method={}, type={}", 
                customerId, amount, paymentMethod, transactionType);
        
        // Validate payment method - only non-MPESA payments go through approval
        if (paymentMethod == TransactionRequest.PaymentMethodType.MPESA) {
            throw new IllegalArgumentException("M-PESA payments should use STK Push flow, not manual approval");
        }
        
        TransactionRequest request = new TransactionRequest();
        request.setType(transactionType);
        request.setTransactionCategory(transactionCategory);
        request.setCustomerId(customerId);
        request.setCustomerName(customerName);
        request.setPhoneNumber(phoneNumber);
        request.setAmount(amount);
        request.setPaymentMethod(paymentMethod);
        request.setPaymentChannel(TransactionRequest.PaymentChannel.MANUAL);
        request.setReferenceNumber(referenceNumber);
        request.setDescription(description);
        request.setInitiatedBy(initiatedBy);
        request.setInitiatedAt(LocalDateTime.now());
        
        // Set target account based on transaction type
        if (loanId != null) {
            request.setLoanId(loanId);
        }
        if (savingsAccountId != null) {
            request.setSavingsAccountId(savingsAccountId);
        }
        if (targetAccountId != null) {
            request.setTargetAccountId(targetAccountId);
        }
        
        // Set status to AWAITING_APPROVAL for manual payments
        request.setStatus(TransactionRequest.RequestStatus.AWAITING_APPROVAL);
        request.setPostedToAccount(false);
        
        TransactionRequest saved = transactionRequestRepository.save(request);
        log.info("Manual payment request created with ID: {} - Status: AWAITING_APPROVAL", saved.getId());
        
        return saved;
    }
    
    /**
     * Get all pending payment requests awaiting approval
     */
    public java.util.List<TransactionRequest> getPendingApprovals() {
        return transactionRequestRepository.findByStatusOrderByInitiatedAtDesc(
            TransactionRequest.RequestStatus.AWAITING_APPROVAL
        );
    }
    
    /**
     * Get pending approvals by customer
     */
    public java.util.List<TransactionRequest> getPendingApprovalsByCustomer(Long customerId) {
        return transactionRequestRepository.findByCustomerIdAndStatusOrderByInitiatedAtDesc(
            customerId,
            TransactionRequest.RequestStatus.AWAITING_APPROVAL
        );
    }
    
    /**
     * Get all transaction requests by status
     */
    public java.util.List<TransactionRequest> getTransactionsByStatus(TransactionRequest.RequestStatus status) {
        return transactionRequestRepository.findByStatusOrderByInitiatedAtDesc(status);
    }

    /**
     * Record payment as suspense for later reconciliation
     */
    private void recordSuspensePayment(TransactionRequest request, String referenceNumber, String exceptionType) {
        try {
            SuspensePayments suspense = new SuspensePayments();
            suspense.setAccountNumber(request.getPhoneNumber() != null ? request.getPhoneNumber() : "UNKNOWN");
            suspense.setAmount(request.getAmount() != null ? request.getAmount().toString() : "0");
            suspense.setStatus("NEW");
            suspense.setOtherRef(referenceNumber != null ? referenceNumber : "REQ-" + request.getId());
            suspense.setExceptionType(exceptionType);
            suspense.setDestinationAccount(request.getTargetAccountId() != null ? 
                request.getTargetAccountId().toString() : "NOT_SPECIFIED");
            suspense.setPaymentTime(LocalDateTime.now());
            
            suspensePaymentRepo.save(suspense);
            log.info("Suspense payment created: {} - Amount: {} - Type: {}", 
                suspense.getOtherRef(), suspense.getAmount(), exceptionType);
                
        } catch (Exception e) {
            log.error("Failed to create suspense payment record: {}", e.getMessage(), e);
        }
    }
}
