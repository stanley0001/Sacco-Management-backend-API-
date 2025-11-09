package com.example.demo.finance.loanManagement.services;

import com.example.demo.erp.communication.sms.SmsService;
import com.example.demo.erp.customerManagement.parsistence.entities.Customer;
import com.example.demo.erp.customerManagement.parsistence.repositories.CustomerRepository;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanAccount;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanRepaymentSchedule;
import com.example.demo.finance.loanManagement.parsistence.entities.loanTransactions;
import com.example.demo.finance.loanManagement.parsistence.repositories.LoanAccountRepo;
import com.example.demo.finance.loanManagement.parsistence.repositories.LoanRepaymentScheduleRepository;
import com.example.demo.finance.loanManagement.parsistence.repositories.TransactionsRepo;
import com.example.demo.finance.payments.entities.MpesaTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for processing loan payments
 * Works with actual LoanAccount entity structure
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoanPaymentService {
    
    private final LoanAccountRepo loanAccountRepo;
    private final TransactionsRepo transactionsRepo;
    private final LoanRepaymentScheduleRepository scheduleRepo;
    private final CustomerRepository customerRepository;
    private final SmsService smsService;
    private final LoanAccountingService loanAccountingService;
    
    /**
     * Process loan payment (from M-PESA or any payment method)
     * Uses actual LoanAccount fields: accountBalance, amount, payableAmount
     */
    @Transactional
    public loanTransactions processLoanPayment(Long loanId, BigDecimal paymentAmount, 
                                               String paymentMethod, String referenceNumber) {
        log.info("Processing loan payment: loanId={}, amount={}, method={}, ref={}", 
                loanId, paymentAmount, paymentMethod, referenceNumber);
        
        // Get loan account
        LoanAccount loan = loanAccountRepo.findById(loanId)
            .orElseThrow(() -> new RuntimeException("Loan not found: " + loanId));
        
        // Get current balance
        Float currentBalance = loan.getAccountBalance();
        if (currentBalance == null || currentBalance <= 0) {
            log.warn("Loan {} has no outstanding balance. Current balance: {}", loanId, currentBalance);
            throw new RuntimeException("Loan already paid off or has invalid balance");
        }
        
        // Allow payment on any loan with a balance, regardless of status
        log.info("Processing payment for loan {} with status: {} and balance: {}", 
                loanId, loan.getStatus(), currentBalance);
        
        // Calculate new balance
        Float paymentAmountFloat = paymentAmount.floatValue();
        Float newBalance = currentBalance - paymentAmountFloat;
        
        // Create transaction record using actual entity fields
        loanTransactions transaction = new loanTransactions();
        transaction.setLoanRef(loan.getLoanref());
        transaction.setCustomerId(loan.getCustomerId());
        transaction.setAmount(paymentAmountFloat);
        transaction.setTransactionType("PAYMENT");
        transaction.setPaymentMode(paymentMethod);
        transaction.setOtherRef(referenceNumber);
        transaction.setOtherResponses("Loan payment - " + referenceNumber);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setTransactionTime(LocalDateTime.now());
        
        // Set initial and final balances (stored as String in entity)
        transaction.setInitialBalance(String.format("%.2f", currentBalance));
        transaction.setFinalBalance(String.format("%.2f", Math.max(0, newBalance)));
        transaction.setAccountNumber(loan.getLoanref());
        
        // UPDATE REPAYMENT SCHEDULES
        List<LoanRepaymentSchedule> pendingSchedules = scheduleRepo
            .findByLoanAccountIdAndStatusNotOrderByDueDateAsc(loanId, LoanRepaymentSchedule.ScheduleStatus.PAID);
        
        BigDecimal remainingPayment = paymentAmount;
        List<LoanRepaymentSchedule> updatedSchedules = new ArrayList<>();
        
        if (!pendingSchedules.isEmpty()) {
            for (LoanRepaymentSchedule schedule : pendingSchedules) {
                if (remainingPayment.compareTo(BigDecimal.ZERO) <= 0) {
                    break; // Payment fully allocated
                }
                
                // Apply payment to this schedule (handles penalty → interest → principal order)
                BigDecimal amountApplied = schedule.applyPayment(remainingPayment, referenceNumber);
                remainingPayment = remainingPayment.subtract(amountApplied);
                
                updatedSchedules.add(schedule);
                
                log.info("Applied {} to installment {}. Status: {}. Remaining: {}", 
                    amountApplied, schedule.getInstallmentNumber(), schedule.getStatus(), remainingPayment);
            }
            
            // Save updated schedules
            scheduleRepo.saveAll(updatedSchedules);
            log.info("Updated {} repayment schedules", updatedSchedules.size());
        }
        
        // Update loan balance
        loan.setAccountBalance(Math.max(0, newBalance));
        
        // Check if loan is fully paid
        if (newBalance <= 0.01) { // Allow for rounding errors
            loan.setStatus("CLOSED");
            loan.setAccountBalance(0.0f);
            log.info("Loan {} fully paid and closed. Overpayment: {}", loanId, Math.abs(newBalance));
        } else if ("OVERDUE".equals(loan.getStatus()) || "DEFAULTED".equals(loan.getStatus())) {
            // Update status if payment was made on overdue or defaulted loan
            loan.setStatus("ACTIVE");
            log.info("Loan {} status updated from {} to ACTIVE", loanId, loan.getStatus());
        }
        
        // Save entities
        loanAccountRepo.save(loan);
        loanTransactions savedTransaction = transactionsRepo.save(transaction);
        
        // SEND SMS NOTIFICATION
        sendPaymentConfirmationSMS(loan, paymentAmount, referenceNumber, newBalance);
        
        // POST TO ACCOUNTING
        try {
            loanAccountingService.postLoanRepayment(savedTransaction, "system");
        } catch (Exception e) {
            log.error("Failed to post loan repayment to accounting", e);
        }
        
        log.info("Loan payment processed successfully: transactionId={}, newBalance={}", 
            savedTransaction.getTransactionId(), newBalance);
        
        return savedTransaction;
    }
    
    /**
     * Process M-PESA payment for loan
     */
    @Transactional
    public loanTransactions processMpesaPayment(MpesaTransaction mpesaTransaction) {
        if (mpesaTransaction.getLoanId() == null) {
            throw new RuntimeException("Loan ID not found in M-PESA transaction");
        }
        
        return processLoanPayment(
            mpesaTransaction.getLoanId(),
            mpesaTransaction.getAmount(),
            "MPESA",
            mpesaTransaction.getMpesaReceiptNumber()
        );
    }
    
    /**
     * Get loan payment summary
     */
    public PaymentSummary getPaymentSummary(Long loanId) {
        LoanAccount loan = loanAccountRepo.findById(loanId)
            .orElseThrow(() -> new RuntimeException("Loan not found: " + loanId));
        
        // Get all payments for this loan
        List<loanTransactions> transactions = transactionsRepo
            .findByLoanRefOrderByTransactionIdAsc(loan.getLoanref());
        
        // Calculate total paid (no status field in loanTransactions)
        Double totalPaid = transactions.stream()
            .filter(t -> "PAYMENT".equals(t.getTransactionType()))
            .mapToDouble(t -> t.getAmount() != null ? t.getAmount() : 0.0)
            .sum();
        
        // Build summary
        PaymentSummary summary = new PaymentSummary();
        summary.setLoanId(loanId);
        summary.setLoanReference(loan.getLoanref());
        summary.setCustomerId(loan.getCustomerId());
        summary.setOriginalAmount(loan.getAmount());
        summary.setPayableAmount(loan.getPayableAmount());
        summary.setCurrentBalance(loan.getAccountBalance());
        summary.setTotalPaid(totalPaid);
        summary.setStatus(loan.getStatus());
        summary.setStartDate(loan.getStartDate());
        summary.setDueDate(loan.getDueDate());
        summary.setPaymentsCount(transactions.size());
        
        return summary;
    }
    
    /**
     * Get all payments for a loan
     */
    public List<loanTransactions> getLoanPayments(String loanRef) {
        return transactionsRepo.findByLoanRefOrderByTransactionIdAsc(loanRef);
    }
    
    /**
     * Get all payments for a customer
     */
    public List<loanTransactions> getCustomerPayments(String customerId) {
        return transactionsRepo.findByCustomerIdOrderByTransactionIdDesc(customerId);
    }
    
    /**
     * DTO for payment summary
     */
    @lombok.Data
    public static class PaymentSummary {
        private Long loanId;
        private String loanReference;
        private String customerId;
        private Float originalAmount;
        private Float payableAmount;
        private Float currentBalance;
        private Double totalPaid;
        private String status;
        private LocalDateTime startDate;
        private LocalDateTime dueDate;
        private Integer paymentsCount;
    }
    
    /**
     * Send payment confirmation SMS
     */
    private void sendPaymentConfirmationSMS(LoanAccount loan, BigDecimal amount, String reference, Float newBalance) {
        try {
            Customer customer = customerRepository.findById(Long.valueOf(loan.getCustomerId()))
                    .orElse(null);
            if (customer != null && customer.getPhoneNumber() != null) {
                String message = String.format(
                    "Dear %s, we have received your loan repayment of KES %,.2f. " +
                    "Outstanding balance: KES %,.2f. Receipt: %s. Thank you.",
                    customer.getFirstName(),
                    amount.doubleValue(),
                    newBalance,
                    reference
                );
                smsService.sendSms(customer.getPhoneNumber(), message);
                log.info("Payment confirmation SMS sent to {}", customer.getPhoneNumber());
            }
        } catch (Exception e) {
            log.error("Failed to send payment confirmation SMS", e);
        }
    }
}
