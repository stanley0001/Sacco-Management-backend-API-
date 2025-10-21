package com.example.demo.payments.services;

import com.example.demo.payments.entities.MpesaTransaction;
import com.example.demo.payments.entities.TransactionRequest;
import com.example.demo.payments.repositories.MpesaTransactionRepository;
import com.example.demo.payments.repositories.TransactionRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionRequestService {
    
    private final TransactionRequestRepository transactionRequestRepository;
    private final MpesaTransactionRepository mpesaTransactionRepository;
    private final MpesaService mpesaService;
    
    /**
     * Create a new deposit request
     */
    @Transactional
    public TransactionRequest createDepositRequest(
        Long customerId,
        String customerName,
        String phoneNumber,
        BigDecimal amount,
        String description,
        String initiatedBy
    ) {
        log.info("Creating deposit request: customerId={}, amount={}", customerId, amount);
        
        TransactionRequest request = new TransactionRequest();
        request.setType(TransactionRequest.TransactionType.DEPOSIT);
        request.setCustomerId(customerId);
        request.setCustomerName(customerName);
        request.setPhoneNumber(phoneNumber);
        request.setAmount(amount);
        request.setStatus(TransactionRequest.RequestStatus.INITIATED);
        request.setPaymentMethod(TransactionRequest.PaymentMethodType.MPESA);
        request.setDescription(description);
        request.setInitiatedBy(initiatedBy);
        request.setInitiatedAt(LocalDateTime.now());
        request.setPostedToAccount(false);
        request.setUtilizedForLoan(false);
        
        return transactionRequestRepository.save(request);
    }
    
    /**
     * Create a new withdrawal request
     */
    @Transactional
    public TransactionRequest createWithdrawalRequest(
        Long customerId,
        String customerName,
        String phoneNumber,
        BigDecimal amount,
        String description,
        String initiatedBy
    ) {
        log.info("Creating withdrawal request: customerId={}, amount={}", customerId, amount);
        
        TransactionRequest request = new TransactionRequest();
        request.setType(TransactionRequest.TransactionType.WITHDRAWAL);
        request.setCustomerId(customerId);
        request.setCustomerName(customerName);
        request.setPhoneNumber(phoneNumber);
        request.setAmount(amount);
        request.setStatus(TransactionRequest.RequestStatus.INITIATED);
        request.setPaymentMethod(TransactionRequest.PaymentMethodType.MPESA);
        request.setDescription(description);
        request.setInitiatedBy(initiatedBy);
        request.setInitiatedAt(LocalDateTime.now());
        request.setPostedToAccount(false);
        
        return transactionRequestRepository.save(request);
    }
    
    /**
     * Link M-PESA transaction to deposit request
     */
    @Transactional
    public TransactionRequest linkMpesaTransaction(Long requestId, Long mpesaTransactionId) {
        log.info("Linking M-PESA transaction {} to request {}", mpesaTransactionId, requestId);
        
        TransactionRequest request = transactionRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Transaction request not found: " + requestId));
        
        MpesaTransaction mpesaTransaction = mpesaTransactionRepository.findById(mpesaTransactionId)
            .orElseThrow(() -> new RuntimeException("M-PESA transaction not found: " + mpesaTransactionId));
        
        request.setMpesaTransactionId(mpesaTransactionId);
        request.setReferenceNumber(mpesaTransaction.getMpesaReceiptNumber());
        request.setStatus(TransactionRequest.RequestStatus.PROCESSING);
        
        if ("SUCCESS".equals(mpesaTransaction.getStatus().name())) {
            request.setStatus(TransactionRequest.RequestStatus.SUCCESS);
            request.setProcessedAt(LocalDateTime.now());
        } else if ("FAILED".equals(mpesaTransaction.getStatus().name())) {
            request.setStatus(TransactionRequest.RequestStatus.FAILED);
            request.setFailureReason(mpesaTransaction.getResultDesc());
        }
        
        return transactionRequestRepository.save(request);
    }
    
    /**
     * Update transaction status
     */
    @Transactional
    public TransactionRequest updateStatus(
        Long requestId,
        TransactionRequest.RequestStatus newStatus,
        String processedBy,
        String failureReason
    ) {
        log.info("Updating transaction {} status to {}", requestId, newStatus);
        
        TransactionRequest request = transactionRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Transaction request not found: " + requestId));
        
        request.setStatus(newStatus);
        request.setProcessedBy(processedBy);
        request.setProcessedAt(LocalDateTime.now());
        
        if (newStatus == TransactionRequest.RequestStatus.FAILED && failureReason != null) {
            request.setFailureReason(failureReason);
        }
        
        return transactionRequestRepository.save(request);
    }
    
    /**
     * Mark as posted to account
     */
    @Transactional
    public TransactionRequest markAsPostedToAccount(Long requestId, Long savingsAccountId) {
        log.info("Marking transaction {} as posted to account {}", requestId, savingsAccountId);
        
        TransactionRequest request = transactionRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Transaction request not found: " + requestId));
        
        request.setPostedToAccount(true);
        request.setPostedAt(LocalDateTime.now());
        request.setSavingsAccountId(savingsAccountId);
        request.setStatus(TransactionRequest.RequestStatus.POSTED_TO_ACCOUNT);
        
        return transactionRequestRepository.save(request);
    }
    
    /**
     * Mark as utilized for loan payment
     */
    @Transactional
    public TransactionRequest markAsUtilizedForLoan(Long requestId, Long loanId) {
        log.info("Marking transaction {} as utilized for loan {}", requestId, loanId);
        
        TransactionRequest request = transactionRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Transaction request not found: " + requestId));
        
        request.setUtilizedForLoan(true);
        request.setUtilizedLoanId(loanId);
        
        return transactionRequestRepository.save(request);
    }
    
    /**
     * Get all deposits with pagination
     */
    public Page<TransactionRequest> getAllDeposits(Pageable pageable) {
        return transactionRequestRepository.findAllDeposits(pageable);
    }
    
    /**
     * Get all withdrawals with pagination
     */
    public Page<TransactionRequest> getAllWithdrawals(Pageable pageable) {
        return transactionRequestRepository.findAllWithdrawals(pageable);
    }
    
    /**
     * Get pending requests
     */
    public Page<TransactionRequest> getPendingRequests(Pageable pageable) {
        return transactionRequestRepository.findPendingRequests(pageable);
    }
    
    /**
     * Get customer transactions with pagination
     */
    public Page<TransactionRequest> getCustomerTransactions(Long customerId, Pageable pageable) {
        return transactionRequestRepository.findByCustomerId(customerId, pageable);
    }
    
    /**
     * Get customer transactions by type
     */
    public Page<TransactionRequest> getCustomerTransactionsByType(
        Long customerId,
        TransactionRequest.TransactionType type,
        Pageable pageable
    ) {
        return transactionRequestRepository.findByCustomerIdAndType(customerId, type, pageable);
    }
    
    /**
     * Get customer transactions by date range
     */
    public Page<TransactionRequest> getCustomerTransactionsByDateRange(
        Long customerId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    ) {
        return transactionRequestRepository.findByCustomerIdAndDateRange(
            customerId, startDate, endDate, pageable
        );
    }
    
    /**
     * Get transaction details
     */
    public TransactionRequest getTransactionDetails(Long requestId) {
        return transactionRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Transaction request not found: " + requestId));
    }
    
    /**
     * Process deposit with M-PESA
     */
    @Transactional
    public TransactionRequest processDepositWithMpesa(Long requestId) {
        log.info("Processing deposit with M-PESA: requestId={}", requestId);
        
        TransactionRequest request = transactionRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Transaction request not found: " + requestId));
        
        // TODO: Initiate M-PESA STK push
        // This would call mpesaService.initiateSTKPush()
        
        request.setStatus(TransactionRequest.RequestStatus.PROCESSING);
        return transactionRequestRepository.save(request);
    }
    
    /**
     * Get statistics
     */
    public TransactionStatistics getStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        TransactionStatistics stats = new TransactionStatistics();
        
        stats.setTotalDeposits(transactionRequestRepository.sumAmountByTypeAndDateRange(
            TransactionRequest.TransactionType.DEPOSIT, startDate, endDate
        ));
        
        stats.setTotalWithdrawals(transactionRequestRepository.sumAmountByTypeAndDateRange(
            TransactionRequest.TransactionType.WITHDRAWAL, startDate, endDate
        ));
        
        stats.setPendingDeposits(transactionRequestRepository.countByTypeAndStatus(
            TransactionRequest.TransactionType.DEPOSIT,
            TransactionRequest.RequestStatus.INITIATED
        ));
        
        stats.setPendingWithdrawals(transactionRequestRepository.countByTypeAndStatus(
            TransactionRequest.TransactionType.WITHDRAWAL,
            TransactionRequest.RequestStatus.INITIATED
        ));
        
        return stats;
    }
    
    /**
     * Statistics DTO
     */
    @lombok.Data
    public static class TransactionStatistics {
        private Double totalDeposits;
        private Double totalWithdrawals;
        private Long pendingDeposits;
        private Long pendingWithdrawals;
    }
}
