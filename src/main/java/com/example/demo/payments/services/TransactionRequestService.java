package com.example.demo.payments.services;

import com.example.demo.payments.dto.DepositRequestCommand;
import com.example.demo.payments.dto.MpesaDepositRequest;
import com.example.demo.payments.dto.MpesaDepositResponse;
import com.example.demo.payments.dto.MpesaDepositStatusResponse;
import com.example.demo.payments.dto.STKPushRequest;
import com.example.demo.payments.dto.STKPushResponse;
import com.example.demo.payments.entities.MpesaConfig;
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
    private final MpesaConfigService mpesaConfigService;
    
    /**
     * Create a new deposit request
     */
    @Transactional
    public TransactionRequest createDepositRequest(
        Long customerId,
        String customerName,
        String phoneNumber,
        java.math.BigDecimal amount,
        String description,
        String initiatedBy,
        Long savingsAccountId,
        Long targetAccountId,
        TransactionRequest.PaymentMethodType paymentMethod
    ) {
        log.info("Creating deposit request: customerId={}, amount={}", customerId, amount);

        DepositRequestCommand command = DepositRequestCommand.builder()
            .customerId(customerId)
            .customerName(customerName)
            .phoneNumber(phoneNumber)
            .amount(amount)
            .description(description)
            .initiatedBy(initiatedBy)
            .savingsAccountId(savingsAccountId)
            .targetAccountId(targetAccountId)
            .paymentMethod(paymentMethod)
            .transactionType(TransactionRequest.TransactionType.DEPOSIT)
            .transactionCategory(savingsAccountId != null ? TransactionRequest.TransactionCategory.SAVINGS_DEPOSIT : null)
            .paymentChannel(paymentMethod == TransactionRequest.PaymentMethodType.MPESA ? TransactionRequest.PaymentChannel.MPESA : TransactionRequest.PaymentChannel.MANUAL)
            .initialStatus(TransactionRequest.RequestStatus.INITIATED)
            .build();

        return createDepositRequest(command);
    }

    @Transactional
    public TransactionRequest createDepositRequest(DepositRequestCommand command) {
        TransactionRequest.TransactionType transactionType = command.getTransactionType() != null
            ? command.getTransactionType()
            : TransactionRequest.TransactionType.DEPOSIT;

        TransactionRequest.TransactionCategory category = command.getTransactionCategory();
        if (category == null) {
            category = command.getLoanId() != null
                ? TransactionRequest.TransactionCategory.LOAN_REPAYMENT
                : TransactionRequest.TransactionCategory.SAVINGS_DEPOSIT;
        }

        TransactionRequest.PaymentMethodType paymentMethod = command.getPaymentMethod() != null
            ? command.getPaymentMethod()
            : TransactionRequest.PaymentMethodType.MPESA;

        TransactionRequest.PaymentChannel paymentChannel = command.getPaymentChannel();
        if (paymentChannel == null) {
            paymentChannel = paymentMethod == TransactionRequest.PaymentMethodType.MPESA
                ? TransactionRequest.PaymentChannel.MPESA
                : TransactionRequest.PaymentChannel.MANUAL;
        }

        TransactionRequest.RequestStatus status = command.getInitialStatus() != null
            ? command.getInitialStatus()
            : TransactionRequest.RequestStatus.INITIATED;

        TransactionRequest request = new TransactionRequest();
        request.setType(transactionType);
        request.setTransactionCategory(category);
        request.setCustomerId(command.getCustomerId());
        request.setCustomerName(command.getCustomerName());
        request.setPhoneNumber(command.getPhoneNumber());
        request.setAmount(command.getAmount());
        request.setStatus(status);
        request.setPaymentMethod(paymentMethod);
        request.setPaymentChannel(paymentChannel);
        request.setDescription(command.getDescription());
        request.setReferenceNumber(command.getReferenceNumber());
        request.setInitiatedBy(command.getInitiatedBy());
        request.setInitiatedAt(LocalDateTime.now());
        request.setPostedToAccount(false);
        request.setUtilizedForLoan(false);
        request.setSavingsAccountId(command.getSavingsAccountId());
        request.setTargetAccountId(command.getTargetAccountId());
        request.setLoanId(command.getLoanId());
        request.setLoanReference(command.getLoanReference());
        request.setProviderConfigId(command.getProviderConfigId());

        return transactionRequestRepository.save(request);
    }

    /**
     * Create a new account transfer request
     */
    @Transactional
    public TransactionRequest createTransferRequest(
        Long customerId,
        String customerName,
        String phoneNumber,
        BigDecimal amount,
        Long sourceAccountId,
        Long targetAccountId,
        String description,
        String initiatedBy
    ) {
        log.info("Creating transfer request: customerId={}, amount={} source={} target={}", customerId, amount, sourceAccountId, targetAccountId);

        TransactionRequest request = new TransactionRequest();
        request.setType(TransactionRequest.TransactionType.TRANSFER);
        request.setCustomerId(customerId);
        request.setCustomerName(customerName);
        request.setPhoneNumber(phoneNumber);
        request.setAmount(amount);
        request.setStatus(TransactionRequest.RequestStatus.INITIATED);
        request.setPaymentMethod(TransactionRequest.PaymentMethodType.INTERNAL_TRANSFER);
        request.setDescription(description);
        request.setInitiatedBy(initiatedBy);
        request.setInitiatedAt(LocalDateTime.now());
        request.setPostedToAccount(false);
        request.setUtilizedForLoan(false);
        request.setSourceAccountId(sourceAccountId);
        request.setTargetAccountId(targetAccountId);

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
        request.setPaymentChannel(TransactionRequest.PaymentChannel.MPESA);
        
        if (MpesaTransaction.TransactionStatus.SUCCESS.equals(mpesaTransaction.getStatus())) {
            request.setStatus(TransactionRequest.RequestStatus.SUCCESS);
            request.setProcessedAt(LocalDateTime.now());
        } else if (MpesaTransaction.TransactionStatus.FAILED.equals(mpesaTransaction.getStatus())) {
            request.setStatus(TransactionRequest.RequestStatus.FAILED);
            request.setFailureReason(mpesaTransaction.getResultDesc());
        }

        if (mpesaTransaction.getTransactionRequestId() == null) {
            mpesaTransaction.setTransactionRequestId(requestId);
            mpesaTransactionRepository.save(mpesaTransaction);
        }
        
        return transactionRequestRepository.save(request);
    }

    /**
     * Initiate M-PESA deposit via STK push
     */
    @Transactional
    public MpesaDepositResponse initiateMpesaDeposit(MpesaDepositRequest depositRequest) {
        log.info("Initiating M-PESA deposit for customer: {} amount: {}", depositRequest.getCustomerId(), depositRequest.getAmount());

        DepositRequestCommand command = DepositRequestCommand.builder()
            .customerId(depositRequest.getCustomerId())
            .customerName(depositRequest.getCustomerName())
            .phoneNumber(depositRequest.getPhoneNumber())
            .amount(depositRequest.getAmount())
            .description(depositRequest.getDescription())
            .initiatedBy(depositRequest.getInitiatedBy())
            .savingsAccountId(depositRequest.getSavingsAccountId())
            .loanId(depositRequest.getLoanId())
            .loanReference(depositRequest.getLoanReference())
            .targetAccountId(depositRequest.getTargetAccountId())
            .paymentMethod(TransactionRequest.PaymentMethodType.MPESA)
            .transactionCategory(depositRequest.getTransactionCategory())
            .transactionType(depositRequest.getTransactionType())
            .paymentChannel(depositRequest.getPaymentChannel() != null ? depositRequest.getPaymentChannel() : TransactionRequest.PaymentChannel.MPESA)
            .initialStatus(TransactionRequest.RequestStatus.PROCESSING)
            .providerConfigId(depositRequest.getProviderConfigId())
            .referenceNumber(depositRequest.getReferenceNumber())
            .build();

        TransactionRequest savedRequest = createDepositRequest(command);

        STKPushRequest stkPushRequest = STKPushRequest.builder()
            .phoneNumber(depositRequest.getPhoneNumber())
            .amount(depositRequest.getAmount())
            .accountReference(resolveAccountReference(depositRequest))
            .transactionDesc(resolveTransactionDescription(depositRequest))
            .customerId(depositRequest.getCustomerId())
            .loanId(depositRequest.getLoanId())
            .savingsAccountId(depositRequest.getSavingsAccountId())
            .providerConfigId(resolveProviderConfigId(depositRequest, savedRequest))
            .providerCode(resolveProviderCode(depositRequest, savedRequest))
            .transactionRequestId(savedRequest.getId())
            .build();

        STKPushResponse stkResponse = mpesaService.initiateSTKPush(stkPushRequest);

        MpesaTransaction mpesaTransaction = mpesaTransactionRepository
            .findByCheckoutRequestId(stkResponse.getCheckoutRequestId())
            .orElseThrow(() -> new RuntimeException("M-PESA transaction not persisted"));

        if (mpesaTransaction.getTransactionRequestId() == null) {
            mpesaTransaction.setTransactionRequestId(savedRequest.getId());
            mpesaTransactionRepository.save(mpesaTransaction);
        }

        savedRequest.setMpesaTransactionId(mpesaTransaction.getId());
        savedRequest.setStatus(TransactionRequest.RequestStatus.PROCESSING);
        savedRequest.setServiceProviderResponse(stkResponse.getResponseDescription());
        savedRequest.setReferenceNumber(stkResponse.getCheckoutRequestId());
        transactionRequestRepository.save(savedRequest);

        return MpesaDepositResponse.builder()
            .merchantRequestId(stkResponse.getMerchantRequestId())
            .checkoutRequestId(stkResponse.getCheckoutRequestId())
            .responseCode(stkResponse.getResponseCode())
            .responseDescription(stkResponse.getResponseDescription())
            .customerMessage(stkResponse.getCustomerMessage())
            .transactionRequestId(savedRequest.getId())
            .mpesaTransactionId(mpesaTransaction.getId())
            .requestStatus(savedRequest.getStatus())
            .transactionStatus(mpesaTransaction.getStatus())
            .build();
    }

    /**
     * Query latest status for a checkout request ID
     */
    public MpesaDepositStatusResponse getMpesaDepositStatus(String checkoutRequestId) {
        MpesaTransaction transaction = mpesaTransactionRepository
            .findByCheckoutRequestId(checkoutRequestId)
            .orElseThrow(() -> new RuntimeException("M-PESA transaction not found for CheckoutRequestID: " + checkoutRequestId));

        TransactionRequest linkedRequest = null;
        if (transaction.getTransactionRequestId() != null) {
            linkedRequest = transactionRequestRepository.findById(transaction.getTransactionRequestId()).orElse(null);
        }
        if (linkedRequest == null) {
            linkedRequest = transactionRequestRepository.findByMpesaTransactionId(transaction.getId());
        }

        return MpesaDepositStatusResponse.builder()
            .merchantRequestId(transaction.getMerchantRequestId())
            .checkoutRequestId(transaction.getCheckoutRequestId())
            .resultCode(transaction.getResultCode())
            .resultDesc(transaction.getResultDesc())
            .transactionStatus(transaction.getStatus())
            .requestStatus(linkedRequest != null ? linkedRequest.getStatus() : null)
            .transactionRequestId(linkedRequest != null ? linkedRequest.getId() : transaction.getTransactionRequestId())
            .mpesaTransactionId(transaction.getId())
            .build();
    }

    private String resolveAccountReference(MpesaDepositRequest request) {
        if (request.getLoanReference() != null && !request.getLoanReference().isBlank()) {
            return request.getLoanReference();
        }
        if (request.getLoanId() != null) {
            return "LOAN-" + request.getLoanId();
        }
        if (request.getSavingsAccountId() != null) {
            return "SAV-" + request.getSavingsAccountId();
        }
        return String.valueOf(request.getCustomerId());
    }

    private String resolveTransactionDescription(MpesaDepositRequest request) {
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            return request.getDescription();
        }
        return request.getLoanId() != null ? "Loan repayment" : "Savings deposit";
    }

    private Long resolveProviderConfigId(MpesaDepositRequest request, TransactionRequest savedRequest) {
        if (request.getProviderConfigId() != null) {
            return request.getProviderConfigId();
        }
        if (savedRequest.getProviderConfigId() != null) {
            return savedRequest.getProviderConfigId();
        }
        return mpesaConfigService.getDefaultConfiguration().getId();
    }

    private String resolveProviderCode(MpesaDepositRequest request, TransactionRequest savedRequest) {
        if (request.getProviderCode() != null && !request.getProviderCode().isBlank()) {
            return request.getProviderCode();
        }
        if (savedRequest.getProviderConfigId() != null) {
            MpesaConfig config = mpesaConfigService.getActiveConfiguration(savedRequest.getProviderConfigId(), null);
            return config.getConfigName();
        }
        return mpesaConfigService.getDefaultConfiguration().getConfigName();
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
