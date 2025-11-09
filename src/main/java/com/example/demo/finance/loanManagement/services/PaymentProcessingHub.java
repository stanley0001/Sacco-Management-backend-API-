package com.example.demo.finance.loanManagement.services;

import com.example.demo.finance.loanManagement.dto.PaymentCommand;
import com.example.demo.finance.loanManagement.parsistence.entities.loanTransactions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Centralized hub for processing all loan payments
 * Entry point for: MPESA, Manual approvals, Mobile app, Admin UI, Bank deposits
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessingHub {
    
    private final LoanPaymentService loanPaymentService;
    
    /**
     * Process loan payment from any source
     */
    @Transactional
    public loanTransactions processPayment(PaymentCommand command) {
        log.info("Processing loan payment from source: {}, loan: {}, amount: {}", 
            command.getSource(), command.getLoanId(), command.getAmount());
        
        // Validate command
        validatePaymentCommand(command);
        
        // Determine reference number based on source
        String referenceNumber = resolveReferenceNumber(command);
        
        // Process payment using existing LoanPaymentService
        loanTransactions transaction = loanPaymentService.processLoanPayment(
            command.getLoanId(),
            command.getAmount(),
            command.getPaymentMethod(),
            referenceNumber
        );
        
        log.info("Payment processed successfully: transactionId={}, source={}", 
            transaction.getTransactionId(), command.getSource());
        
        // Additional post-processing based on source
        postProcessPayment(command, transaction);
        
        return transaction;
    }
    
    /**
     * Process MPESA payment
     */
    @Transactional
    public loanTransactions processMpesaPayment(Long loanId, Double amount, String mpesaReceipt, String phoneNumber) {
        PaymentCommand command = PaymentCommand.builder()
            .source(PaymentCommand.PaymentSource.MPESA_CALLBACK)
            .loanId(loanId)
            .amount(java.math.BigDecimal.valueOf(amount))
            .paymentMethod("MPESA")
            .mpesaReceiptNumber(mpesaReceipt)
            .phoneNumber(phoneNumber)
            .referenceNumber(mpesaReceipt)
            .build();
        
        return processPayment(command);
    }
    
    /**
     * Process manual payment (from admin UI)
     */
    @Transactional
    public loanTransactions processManualPayment(Long loanId, Double amount, String paymentMethod, 
                                                 String reference, String approvedBy) {
        PaymentCommand command = PaymentCommand.builder()
            .source(PaymentCommand.PaymentSource.MANUAL_APPROVAL)
            .loanId(loanId)
            .amount(java.math.BigDecimal.valueOf(amount))
            .paymentMethod(paymentMethod)
            .referenceNumber(reference)
            .approvedBy(approvedBy)
            .build();
        
        return processPayment(command);
    }
    
    /**
     * Process mobile app payment
     */
    @Transactional
    public loanTransactions processMobilePayment(Long loanId, Double amount, String paymentMethod, 
                                                String reference) {
        PaymentCommand command = PaymentCommand.builder()
            .source(PaymentCommand.PaymentSource.MOBILE_APP)
            .loanId(loanId)
            .amount(java.math.BigDecimal.valueOf(amount))
            .paymentMethod(paymentMethod)
            .referenceNumber(reference)
            .build();
        
        return processPayment(command);
    }
    
    /**
     * Validate payment command
     */
    private void validatePaymentCommand(PaymentCommand command) {
        if (command.getLoanId() == null) {
            throw new IllegalArgumentException("Loan ID is required");
        }
        
        if (command.getAmount() == null || command.getAmount().doubleValue() <= 0) {
            throw new IllegalArgumentException("Valid payment amount is required");
        }
        
        if (command.getPaymentMethod() == null || command.getPaymentMethod().trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method is required");
        }
    }
    
    /**
     * Resolve reference number from command
     */
    private String resolveReferenceNumber(PaymentCommand command) {
        // Use MPESA receipt if available
        if (command.getMpesaReceiptNumber() != null && !command.getMpesaReceiptNumber().isEmpty()) {
            return command.getMpesaReceiptNumber();
        }
        
        // Use provided reference
        if (command.getReferenceNumber() != null && !command.getReferenceNumber().isEmpty()) {
            return command.getReferenceNumber();
        }
        
        // Generate reference based on source
        String prefix = switch (command.getSource()) {
            case MPESA_CALLBACK, MPESA_STK -> "MPESA";
            case MOBILE_APP -> "MOBILE";
            case MANUAL_APPROVAL -> "MANUAL";
            case BANK_DEPOSIT -> "BANK";
            case CASH_DEPOSIT -> "CASH";
            default -> "PAY";
        };
        
        return prefix + "-" + System.currentTimeMillis();
    }
    
    /**
     * Post-process payment based on source
     */
    private void postProcessPayment(PaymentCommand command, loanTransactions transaction) {
        // Future enhancements:
        // - Send SMS notification based on source
        // - Update mobile app notification queue
        // - Trigger accounting journal entries
        // - Update payment schedules
        
        log.debug("Post-processing payment from source: {}", command.getSource());
    }
}
