package com.example.demo.finance.payments.controllers;

import com.example.demo.finance.payments.entities.TransactionRequest;
import com.example.demo.finance.payments.dto.ManualPaymentRequest;
import com.example.demo.finance.payments.dto.PaymentApprovalRequest;
import com.example.demo.finance.payments.entities.TransactionRequest;
import com.example.demo.finance.payments.services.TransactionApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments/approvals")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(originPatterns = "*", maxAge = 3600, allowCredentials = "true")
public class PaymentApprovalController {

    private final TransactionApprovalService approvalService;

    /**
     * Create manual payment request (Cash, Cheque, Bank Transfer)
     * Goes to AWAITING_APPROVAL status
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createManualPayment(
            @RequestBody ManualPaymentRequest request,
            Authentication authentication) {
        
        try {
            String initiatedBy = authentication != null ? authentication.getName() : "SYSTEM";
            
            log.info("Creating manual payment request: customer={}, amount={}, method={}", 
                    request.getCustomerId(), request.getAmount(), request.getPaymentMethod());
            
            TransactionRequest transactionRequest = approvalService.createManualPaymentRequest(
                request.getCustomerId(),
                request.getCustomerName(),
                request.getPhoneNumber(),
                request.getAmount(),
                request.getPaymentMethod(),
                request.getTransactionType(),
                request.getTransactionCategory(),
                request.getTargetAccountId(),
                request.getLoanId(),
                request.getSavingsAccountId(),
                request.getReferenceNumber(),
                request.getDescription(),
                initiatedBy
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Payment request created successfully and awaiting approval");
            response.put("requestId", transactionRequest.getId());
            response.put("status", transactionRequest.getStatus().name());
            response.put("referenceNumber", transactionRequest.getReferenceNumber());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid payment request: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            log.error("Error creating manual payment request", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create payment request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get all pending payment approvals
     */
    @GetMapping("/pending")
    public ResponseEntity<List<TransactionRequest>> getPendingApprovals() {
        try {
            List<TransactionRequest> pendingApprovals = approvalService.getPendingApprovals();
            return ResponseEntity.ok(pendingApprovals);
        } catch (Exception e) {
            log.error("Error fetching pending approvals", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get pending approvals for a specific customer
     */
    @GetMapping("/pending/customer/{customerId}")
    public ResponseEntity<List<TransactionRequest>> getPendingApprovalsByCustomer(
            @PathVariable Long customerId) {
        try {
            List<TransactionRequest> pendingApprovals = 
                approvalService.getPendingApprovalsByCustomer(customerId);
            return ResponseEntity.ok(pendingApprovals);
        } catch (Exception e) {
            log.error("Error fetching pending approvals for customer {}", customerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get transactions by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TransactionRequest>> getTransactionsByStatus(
            @PathVariable TransactionRequest.RequestStatus status) {
        try {
            List<TransactionRequest> transactions = approvalService.getTransactionsByStatus(status);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            log.error("Error fetching transactions by status {}", status, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Approve a payment request
     */
    @PostMapping("/approve/{requestId}")
    public ResponseEntity<Map<String, Object>> approvePayment(
            @PathVariable Long requestId,
            @RequestBody PaymentApprovalRequest request,
            Authentication authentication) {
        
        try {
            String approvedBy = authentication != null ? authentication.getName() : "ADMIN";
            
            log.info("Approving payment request {} by {}", requestId, approvedBy);
            
            TransactionRequest approved = approvalService.approveTransaction(
                requestId, 
                approvedBy, 
                request.getReferenceNumber()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Payment approved and posted successfully");
            response.put("requestId", approved.getId());
            response.put("status", approved.getStatus().name());
            response.put("referenceNumber", approved.getReferenceNumber());
            response.put("postedAt", approved.getPostedAt());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            log.error("Cannot approve payment {}: {}", requestId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            log.error("Error approving payment request {}", requestId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to approve payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Reject a payment request
     */
    @PostMapping("/reject/{requestId}")
    public ResponseEntity<Map<String, Object>> rejectPayment(
            @PathVariable Long requestId,
            @RequestBody PaymentApprovalRequest request,
            Authentication authentication) {
        
        try {
            String rejectedBy = authentication != null ? authentication.getName() : "ADMIN";
            
            log.info("Rejecting payment request {} by {}", requestId, rejectedBy);
            
            TransactionRequest rejected = approvalService.rejectTransaction(
                requestId, 
                rejectedBy, 
                request.getRejectionReason()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Payment rejected");
            response.put("requestId", rejected.getId());
            response.put("status", rejected.getStatus().name());
            response.put("rejectionReason", rejected.getFailureReason());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            log.error("Cannot reject payment {}: {}", requestId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            log.error("Error rejecting payment request {}", requestId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to reject payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get payment request details
     */
    @GetMapping("/{requestId}")
    public ResponseEntity<TransactionRequest> getPaymentRequest(@PathVariable Long requestId) {
        try {
            // You'll need to add a method in the service to get by ID
            // For now, we can get it through the repository directly
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error fetching payment request {}", requestId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
