package com.example.demo.finance.payments.controllers;

import com.example.demo.finance.payments.dto.*;
import com.example.demo.finance.payments.entities.TransactionRequest;
import com.example.demo.finance.payments.services.TransactionRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transaction Requests", description = "Manage deposits, withdrawals, and disbursements")
@CrossOrigin(originPatterns = "*", maxAge = 3600, allowCredentials = "true")
public class TransactionRequestController {
    
    private final TransactionRequestService transactionRequestService;

    /**
     * Initiate M-PESA deposit (STK push)
     */
    @PostMapping("/deposits/mpesa")
    @Operation(summary = "Initiate M-PESA deposit via STK push")
    @PreAuthorize("hasAnyAuthority('TRANSACTION_CREATE', 'PAYMENT_INITIATE', 'ADMIN_ACCESS')")
    public ResponseEntity<MpesaDepositResponse> initiateMpesaDeposit(@RequestBody MpesaDepositRequest request) {
        log.info("API: Initiating M-PESA deposit for customer {}", request.getCustomerId());

        try {
            MpesaDepositResponse response = transactionRequestService.initiateMpesaDeposit(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error initiating M-PESA deposit", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get M-PESA deposit status
     */
    @GetMapping("/deposits/mpesa/{checkoutRequestId}/status")
    @Operation(summary = "Get status of an M-PESA deposit")
    @PreAuthorize("hasAnyAuthority('TRANSACTION_VIEW', 'PAYMENT_VIEW', 'ADMIN_ACCESS')")
    public ResponseEntity<MpesaDepositStatusResponse> getMpesaDepositStatus(
        @PathVariable String checkoutRequestId
    ) {
        log.info("API: Getting status for checkoutRequestId {}", checkoutRequestId);

        try {
            MpesaDepositStatusResponse status = transactionRequestService.getMpesaDepositStatus(checkoutRequestId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Error fetching M-PESA deposit status", e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get all deposits with pagination
     */
    @GetMapping("/deposits")
    @Operation(summary = "Get all deposit requests")
    @PreAuthorize("hasAnyAuthority('TRANSACTION_VIEW', 'ADMIN_ACCESS')")
    public ResponseEntity<Page<TransactionRequest>> getAllDeposits(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String sortDir
    ) {
        log.info("API: Getting all deposits - page: {}, size: {}", page, size);
        
        Sort sort = sortDir.equalsIgnoreCase("asc") ? 
            Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<TransactionRequest> deposits = transactionRequestService.getAllDeposits(pageable);
        return ResponseEntity.ok(deposits);
    }
    
    /**
     * Get all withdrawals with pagination
     */
    @GetMapping("/withdrawals")
    @Operation(summary = "Get all withdrawal requests")
    @PreAuthorize("hasAnyAuthority('TRANSACTION_VIEW', 'ADMIN_ACCESS')")
    public ResponseEntity<Page<TransactionRequest>> getAllWithdrawals(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String sortDir
    ) {
        log.info("API: Getting all withdrawals - page: {}, size: {}", page, size);
        
        Sort sort = sortDir.equalsIgnoreCase("asc") ? 
            Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<TransactionRequest> withdrawals = transactionRequestService.getAllWithdrawals(pageable);
        return ResponseEntity.ok(withdrawals);
    }
    
    /**
     * Get pending requests
     */
    @GetMapping("/pending")
    @Operation(summary = "Get all pending transaction requests")
    @PreAuthorize("hasAnyAuthority('TRANSACTION_VIEW', 'ADMIN_ACCESS')")
    public ResponseEntity<Page<TransactionRequest>> getPendingRequests(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        log.info("API: Getting pending requests");
        
        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionRequest> pending = transactionRequestService.getPendingRequests(pageable);
        return ResponseEntity.ok(pending);
    }
    
    /**
     * Get transaction details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get transaction request details")
    @PreAuthorize("hasAnyAuthority('TRANSACTION_VIEW', 'ADMIN_ACCESS')")
    public ResponseEntity<TransactionRequest> getTransactionDetails(
        @PathVariable @Parameter(description = "Transaction request ID") Long id
    ) {
        log.info("API: Getting transaction details: {}", id);
        
        try {
            TransactionRequest request = transactionRequestService.getTransactionDetails(id);
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            log.error("Error getting transaction details", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create transfer request
     */
    @PostMapping("/transfers")
    @Operation(summary = "Create a new internal transfer request")
    @PreAuthorize("hasAnyAuthority('TRANSACTION_CREATE', 'ADMIN_ACCESS')")
    public ResponseEntity<?> createTransferRequest(@RequestBody TransferRequestDTO request) {
        log.info("API: Creating transfer request for customer: {}", request.getCustomerId());

        try {
            TransactionRequest created = transactionRequestService.createTransferRequest(
                request.getCustomerId(),
                request.getCustomerName(),
                request.getPhoneNumber(),
                request.getAmount(),
                request.getSourceAccountId(),
                request.getTargetAccountId(),
                request.getDescription(),
                request.getInitiatedBy()
            );
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            log.error("Error creating transfer request", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Create deposit request
     */
    @PostMapping("/deposits")
    @Operation(summary = "Create a new deposit request")
    @PreAuthorize("hasAnyAuthority('TRANSACTION_CREATE', 'ADMIN_ACCESS')")
    public ResponseEntity<?> createDepositRequest(@RequestBody DepositRequestDTO request) {
        log.info("API: Creating deposit request for customer: {}", request.getCustomerId());
        
        try {
            DepositRequestCommand command = DepositRequestCommand.builder()
                .customerId(request.getCustomerId())
                .customerName(request.getCustomerName())
                .phoneNumber(request.getPhoneNumber())
                .amount(request.getAmount())
                .description(request.getDescription())
                .initiatedBy(request.getInitiatedBy())
                .savingsAccountId(request.getSavingsAccountId())
                .targetAccountId(request.getTargetAccountId())
                .loanId(request.getLoanId())
                .loanReference(request.getLoanReference())
                .paymentMethod(request.getPaymentMethod())
                .transactionCategory(request.getTransactionCategory())
                .transactionType(request.getTransactionType())
                .paymentChannel(request.getPaymentChannel())
                .providerConfigId(request.getProviderConfigId())
                .referenceNumber(request.getReferenceNumber())
                .initialStatus(request.getPaymentChannel() == TransactionRequest.PaymentChannel.MANUAL
                    ? TransactionRequest.RequestStatus.AWAITING_APPROVAL
                    : TransactionRequest.RequestStatus.INITIATED)
                .build();

            TransactionRequest created = transactionRequestService.createDepositRequest(command);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            log.error("Error creating deposit request", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Create withdrawal request
     */
    @PostMapping("/withdrawals")
    @Operation(summary = "Create a new withdrawal request")
    @PreAuthorize("hasAnyAuthority('TRANSACTION_CREATE', 'ADMIN_ACCESS')")
    public ResponseEntity<?> createWithdrawalRequest(@RequestBody WithdrawalRequestDTO request) {
        log.info("API: Creating withdrawal request for customer: {}", request.getCustomerId());
        
        try {
            TransactionRequest created = transactionRequestService.createWithdrawalRequest(
                request.getCustomerId(),
                request.getCustomerName(),
                request.getPhoneNumber(),
                request.getAmount(),
                request.getDescription(),
                request.getInitiatedBy()
            );
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            log.error("Error creating withdrawal request", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Update transaction status
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "Update transaction request status")
    @PreAuthorize("hasAnyAuthority('TRANSACTION_UPDATE', 'ADMIN_ACCESS')")
    public ResponseEntity<?> updateStatus(
        @PathVariable Long id,
        @RequestBody StatusUpdateDTO request
    ) {
        log.info("API: Updating transaction {} status to {}", id, request.getStatus());
        
        try {
            TransactionRequest updated = transactionRequestService.updateStatus(
                id,
                request.getStatus(),
                request.getProcessedBy(),
                request.getFailureReason()
            );
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating transaction status", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Mark as posted to account
     */
    @PutMapping("/{id}/post-to-account")
    @Operation(summary = "Mark transaction as posted to account")
    @PreAuthorize("hasAnyAuthority('TRANSACTION_UPDATE', 'ADMIN_ACCESS')")
    public ResponseEntity<?> markAsPostedToAccount(
        @PathVariable Long id,
        @RequestParam Long savingsAccountId
    ) {
        log.info("API: Marking transaction {} as posted to account {}", id, savingsAccountId);
        
        try {
            TransactionRequest updated = transactionRequestService.markAsPostedToAccount(id, savingsAccountId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error marking as posted", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Mark as utilized for loan
     */
    @PutMapping("/{id}/utilize-for-loan")
    @Operation(summary = "Mark transaction as utilized for loan payment")
    @PreAuthorize("hasAnyAuthority('TRANSACTION_UPDATE', 'ADMIN_ACCESS')")
    public ResponseEntity<?> markAsUtilizedForLoan(
        @PathVariable Long id,
        @RequestParam Long loanId
    ) {
        log.info("API: Marking transaction {} as utilized for loan {}", id, loanId);
        
        try {
            TransactionRequest updated = transactionRequestService.markAsUtilizedForLoan(id, loanId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error marking as utilized", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get customer transactions
     */
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get customer transaction requests")
    @PreAuthorize("hasAnyAuthority('TRANSACTION_VIEW', 'CUSTOMER_READ', 'ADMIN_ACCESS')")
    public ResponseEntity<Page<TransactionRequest>> getCustomerTransactions(
        @PathVariable Long customerId,
        @RequestParam(required = false) String type,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String sortDir
    ) {
        log.info("API: Getting customer {} transactions", customerId);
        
        Sort sort = sortDir.equalsIgnoreCase("asc") ? 
            Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<TransactionRequest> transactions;
        
        if (startDate != null && endDate != null) {
            transactions = transactionRequestService.getCustomerTransactionsByDateRange(
                customerId, startDate, endDate, pageable
            );
        } else if (type != null) {
            TransactionRequest.TransactionType transactionType = 
                TransactionRequest.TransactionType.valueOf(type.toUpperCase());
            transactions = transactionRequestService.getCustomerTransactionsByType(
                customerId, transactionType, pageable
            );
        } else {
            transactions = transactionRequestService.getCustomerTransactions(customerId, pageable);
        }
        
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * Get statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get transaction statistics")
    @PreAuthorize("hasAnyAuthority('TRANSACTION_VIEW', 'ADMIN_ACCESS')")
    public ResponseEntity<TransactionRequestService.TransactionStatistics> getStatistics(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        log.info("API: Getting transaction statistics");
        
        TransactionRequestService.TransactionStatistics stats = 
            transactionRequestService.getStatistics(startDate, endDate);
        return ResponseEntity.ok(stats);
    }
    
    // DTOs
    @lombok.Data
    public static class DepositRequestDTO {
        private Long customerId;
        private String customerName;
        private String phoneNumber;
        private BigDecimal amount;
        private String description;
        private String initiatedBy;
        private Long savingsAccountId;
        private Long targetAccountId;
        private TransactionRequest.PaymentMethodType paymentMethod;
        private TransactionRequest.TransactionCategory transactionCategory;
        private TransactionRequest.TransactionType transactionType;
        private TransactionRequest.PaymentChannel paymentChannel;
        private Long providerConfigId;
        private String providerCode;
        private String referenceNumber;
        private Long loanId;
        private String loanReference;
    }
    
    @lombok.Data
    public static class WithdrawalRequestDTO {
        private Long customerId;
        private String customerName;
        private String phoneNumber;
        private BigDecimal amount;
        private String description;
        private String initiatedBy;
    }

    @lombok.Data
    public static class TransferRequestDTO {
        private Long customerId;
        private String customerName;
        private String phoneNumber;
        private BigDecimal amount;
        private Long sourceAccountId;
        private Long targetAccountId;
        private String description;
        private String initiatedBy;
    }

    @lombok.Data
    public static class ManualPostingDTO {
        private Long savingsAccountId;
        private BigDecimal amount;
        private String description;
        private String initiatedBy;
    }
    
    @lombok.Data
    public static class StatusUpdateDTO {
        private TransactionRequest.RequestStatus status;
        private String processedBy;
        private String failureReason;
    }
}
