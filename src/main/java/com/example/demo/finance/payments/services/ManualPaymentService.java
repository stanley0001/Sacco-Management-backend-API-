package com.example.demo.finance.payments.services;

import com.example.demo.finance.accounting.entities.JournalEntryLine;
import com.example.demo.finance.accounting.entities.JournalEntry;
import com.example.demo.finance.accounting.repositories.ChartOfAccountsRepo;
import com.example.demo.finance.accounting.services.AccountingService;
import com.example.demo.finance.loanManagement.dto.ManualPaymentCommand;
import com.example.demo.finance.loanManagement.parsistence.entities.loanTransactions;
import com.example.demo.finance.loanManagement.services.PaymentProcessingHub;
import com.example.demo.finance.payments.entities.ManualPayment;
import com.example.demo.finance.payments.repositories.ManualPaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for processing manual payments (CASH, BANK_TRANSFER, CHEQUE)
 * with full accounting integration
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ManualPaymentService {
    
    private final ManualPaymentRepository manualPaymentRepository;
    private final PaymentProcessingHub paymentProcessingHub;
    private final AccountingService accountingService;
    private final ChartOfAccountsRepo chartOfAccountsRepo;
    
    /**
     * Process manual payment
     */
    @Transactional
    public ManualPayment processManualPayment(ManualPaymentCommand command) {
        log.info("Processing manual payment: target={}, amount={}, method={}", 
            command.getTarget(), command.getAmount(), command.getPaymentMethod());
        
        // Validate command
        validatePaymentCommand(command);
        
        // Create manual payment record
        ManualPayment payment = createPaymentRecord(command);
        
        // Determine if approval is required
        boolean requiresApproval = requiresApproval(command);
        
        if (requiresApproval) {
            payment.setStatus(ManualPayment.PaymentStatus.PENDING_APPROVAL);
            payment.setRequiresApproval(true);
            log.info("Payment requires approval: {}", payment.getId());
        } else {
            // Process immediately
            payment.setStatus(ManualPayment.PaymentStatus.APPROVED);
            payment.setApprovedBy(command.getApprovedBy() != null ? command.getApprovedBy() : command.getReceivedBy());
            payment.setApprovedAt(LocalDateTime.now());
            
            // Process the actual payment
            processPaymentTransaction(payment, command);
            
            // Post to accounting if enabled
            if (command.isPostToAccounting()) {
                postToAccounting(payment, command);
            }
            
            payment.setStatus(ManualPayment.PaymentStatus.POSTED);
            log.info("Payment processed and posted: {}", payment.getId());
        }
        
        return manualPaymentRepository.save(payment);
    }
    
    /**
     * Approve pending manual payment
     */
    @Transactional
    public ManualPayment approvePayment(Long paymentId, String approvedBy, String comments) {
        log.info("Approving manual payment: {}", paymentId);
        
        ManualPayment payment = manualPaymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalStateException("Payment not found: " + paymentId));
        
        if (payment.getStatus() != ManualPayment.PaymentStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Payment is not pending approval: " + payment.getStatus());
        }
        
        payment.setStatus(ManualPayment.PaymentStatus.APPROVED);
        payment.setApprovedBy(approvedBy);
        payment.setApprovedAt(LocalDateTime.now());
        payment.setApprovalComments(comments);
        
        // Rebuild command for processing
        ManualPaymentCommand command = buildCommandFromPayment(payment);
        
        // Process the payment transaction
        processPaymentTransaction(payment, command);
        
        // Post to accounting
        if (payment.getPostToAccounting()) {
            postToAccounting(payment, command);
        }
        
        payment.setStatus(ManualPayment.PaymentStatus.POSTED);
        payment.setPostedAt(LocalDateTime.now());
        
        log.info("Payment approved and posted: {}", paymentId);
        return manualPaymentRepository.save(payment);
    }
    
    /**
     * Reject pending manual payment
     */
    @Transactional
    public ManualPayment rejectPayment(Long paymentId, String rejectedBy, String reason) {
        log.info("Rejecting manual payment: {}", paymentId);
        
        ManualPayment payment = manualPaymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalStateException("Payment not found: " + paymentId));
        
        if (payment.getStatus() != ManualPayment.PaymentStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Payment is not pending approval: " + payment.getStatus());
        }
        
        payment.setStatus(ManualPayment.PaymentStatus.REJECTED);
        payment.setRejectedBy(rejectedBy);
        payment.setRejectedAt(LocalDateTime.now());
        payment.setRejectionReason(reason);
        
        return manualPaymentRepository.save(payment);
    }
    
    /**
     * Get pending approval payments
     */
    public List<ManualPayment> getPendingApprovalPayments() {
        return manualPaymentRepository.findByStatus(ManualPayment.PaymentStatus.PENDING_APPROVAL);
    }
    
    /**
     * Get payment history
     */
    public List<ManualPayment> getPaymentHistory(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null) {
            return manualPaymentRepository.findByPaymentDateBetween(startDate.toLocalDate(), endDate.toLocalDate());
        }
        return manualPaymentRepository.findAll();
    }
    
    /**
     * Get payments by target
     */
    public List<ManualPayment> getPaymentsByTarget(String targetType, Long targetId) {
        return manualPaymentRepository.findByTargetTypeAndTargetId(targetType, targetId);
    }
    
    // ===== PRIVATE HELPER METHODS =====
    
    private void validatePaymentCommand(ManualPaymentCommand command) {
        if (command.getTarget() == null) {
            throw new IllegalArgumentException("Payment target is required");
        }
        if (command.getAmount() == null || command.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valid payment amount is required");
        }
        if (command.getPaymentMethod() == null || command.getPaymentMethod().trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method is required");
        }
        if (command.getReceivedBy() == null || command.getReceivedBy().trim().isEmpty()) {
            throw new IllegalArgumentException("Received by is required");
        }
    }
    
    private boolean requiresApproval(ManualPaymentCommand command) {
        // Cheques always require approval
        if ("CHEQUE".equalsIgnoreCase(command.getPaymentMethod())) {
            return true;
        }
        
        // Large amounts require approval (threshold: 100,000)
        if (command.getAmount().compareTo(new BigDecimal("100000")) > 0) {
            return true;
        }
        
        // If explicitly set
        return command.isRequireApproval();
    }
    
    private ManualPayment createPaymentRecord(ManualPaymentCommand command) {
        ManualPayment payment = new ManualPayment();
        
        // Target info
        payment.setTargetType(command.getTarget().name());
        payment.setTargetId(command.getTargetId());
        payment.setTargetReference(command.getTargetReference());
        
        // Payment details
        payment.setAmount(command.getAmount());
        payment.setPaymentMethod(command.getPaymentMethod());
        payment.setReferenceNumber(command.getReferenceNumber());
        payment.setPaymentDate(command.getPaymentDate() != null ? command.getPaymentDate() : java.time.LocalDate.now());
        
        // Cheque details
        payment.setChequeNumber(command.getChequeNumber());
        payment.setChequeBank(command.getChequeBank());
        payment.setChequeDate(command.getChequeDate());
        
        // Bank transfer details
        payment.setBankName(command.getBankName());
        payment.setBankBranch(command.getBankBranch());
        payment.setBankAccountNumber(command.getBankAccountNumber());
        payment.setSenderName(command.getSenderName());
        
        // Approval info
        payment.setReceivedBy(command.getReceivedBy());
        payment.setComments(command.getComments());
        
        // Accounting
        payment.setDebitAccountId(command.getDebitAccountId());
        payment.setCreditAccountId(command.getCreditAccountId());
        payment.setPostToAccounting(command.isPostToAccounting());
        
        // Audit
        payment.setCreatedAt(LocalDateTime.now());
        
        return payment;
    }
    
    private void processPaymentTransaction(ManualPayment payment, ManualPaymentCommand command) {
        try {
            if (ManualPaymentCommand.PaymentTarget.LOAN_REPAYMENT.name().equals(payment.getTargetType())) {
                // Process loan repayment through hub
                loanTransactions txn = paymentProcessingHub.processManualPayment(
                    payment.getTargetId(),
                    payment.getAmount().doubleValue(),
                    payment.getPaymentMethod(),
                    payment.getReferenceNumber(),
                    payment.getApprovedBy()
                );
                
                payment.setTransactionReference(txn.getTransactionId().toString());
                log.info("Loan repayment processed: transactionId={}", txn.getTransactionId());
            } else {
                // Handle other payment targets (account deposits, etc.)
                // This will be implemented based on your other modules
                log.info("Processing {} payment for target {}", payment.getTargetType(), payment.getTargetId());
            }
        } catch (Exception e) {
            log.error("Error processing payment transaction", e);
            payment.setStatus(ManualPayment.PaymentStatus.FAILED);
            payment.setErrorMessage(e.getMessage());
            throw new RuntimeException("Failed to process payment: " + e.getMessage(), e);
        }
    }
    
    private void postToAccounting(ManualPayment payment, ManualPaymentCommand command) {
        try {
            // Create journal entry for manual payment
            String description = String.format("Manual Payment - %s - %s - Ref: %s",
                payment.getPaymentMethod(),
                payment.getTargetType(),
                payment.getReferenceNumber()
            );
            
            // Create journal entry entity
            JournalEntry journalEntry =
                JournalEntry.builder()
                    .description(description)
                    .journalType(JournalEntry.JournalType.PAYMENT)
                    .reference(payment.getReferenceNumber() != null ? payment.getReferenceNumber() : "MANUAL-PAY")
                    .transactionDate(payment.getPaymentDate())
                    .createdBy(payment.getApprovedBy())
                    .build();
            
            // Create journal lines
            java.util.List<JournalEntryLine> lines = new java.util.ArrayList<>();
            
            // Debit line (Source account - Cash/Bank)
            String debitAccountCode = chartOfAccountsRepo.findById(command.getDebitAccountId())
                .orElseThrow(() -> new IllegalStateException("Debit account not found: " + command.getDebitAccountId()))
                .getAccountCode();
                
            JournalEntryLine debitLine =
                new JournalEntryLine();
            debitLine.setJournalEntry(journalEntry);
            debitLine.setAccountCode(debitAccountCode);
            debitLine.setType(JournalEntryLine.EntryType.DEBIT);
            debitLine.setAmount(payment.getAmount().doubleValue());
            debitLine.setDescription(description);
            debitLine.setLineNumber(1);
            lines.add(debitLine);
            
            // Credit line (Destination account - Loan/Customer)
            String creditAccountCode = chartOfAccountsRepo.findById(command.getCreditAccountId())
                .orElseThrow(() -> new IllegalStateException("Credit account not found: " + command.getCreditAccountId()))
                .getAccountCode();
                
            JournalEntryLine creditLine =
                new JournalEntryLine();
            creditLine.setJournalEntry(journalEntry);
            creditLine.setAccountCode(creditAccountCode);
            creditLine.setType(JournalEntryLine.EntryType.CREDIT);
            creditLine.setAmount(payment.getAmount().doubleValue());
            creditLine.setDescription(description);
            creditLine.setLineNumber(2);
            lines.add(creditLine);
            
            journalEntry.setLines(lines);
            
            // Post using accounting service
            JournalEntry posted =
                accountingService.createJournalEntry(journalEntry, payment.getApprovedBy());
            accountingService.postJournalEntry(posted.getId(), payment.getApprovedBy());
            
            payment.setPostedToAccounting(true);
            payment.setPostedAt(LocalDateTime.now());
            log.info("Payment posted to accounting: paymentId={}, journalId={}", payment.getId(), posted.getId());
            
        } catch (Exception e) {
            log.error("Error posting to accounting", e);
            payment.setPostingError(e.getMessage());
            // Don't fail the whole payment if accounting fails
        }
    }
    
    private ManualPaymentCommand buildCommandFromPayment(ManualPayment payment) {
        return ManualPaymentCommand.builder()
            .target(ManualPaymentCommand.PaymentTarget.valueOf(payment.getTargetType()))
            .targetId(payment.getTargetId())
            .targetReference(payment.getTargetReference())
            .amount(payment.getAmount())
            .paymentMethod(payment.getPaymentMethod())
            .referenceNumber(payment.getReferenceNumber())
            .paymentDate(payment.getPaymentDate())
            .chequeNumber(payment.getChequeNumber())
            .chequeBank(payment.getChequeBank())
            .chequeDate(payment.getChequeDate())
            .bankName(payment.getBankName())
            .bankBranch(payment.getBankBranch())
            .bankAccountNumber(payment.getBankAccountNumber())
            .senderName(payment.getSenderName())
            .receivedBy(payment.getReceivedBy())
            .approvedBy(payment.getApprovedBy())
            .comments(payment.getComments())
            .debitAccountId(payment.getDebitAccountId())
            .creditAccountId(payment.getCreditAccountId())
            .postToAccounting(payment.getPostToAccounting())
            .build();
    }
}
