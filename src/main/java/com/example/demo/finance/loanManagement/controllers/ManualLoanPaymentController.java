package com.example.demo.finance.loanManagement.controllers;

import com.example.demo.finance.loanManagement.parsistence.entities.ManualLoanPayment;
import com.example.demo.finance.loanManagement.services.LoanPaymentService;
import com.example.demo.finance.loanManagement.services.ManualLoanPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loans/manual-payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Manual Loan Payments", description = "Submit and approve manual loan payments (Bank, Cash, Cheque)")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class ManualLoanPaymentController {

    private final ManualLoanPaymentService manualPaymentService;
    private final LoanPaymentService loanPaymentService;

    /**
     * Submit a manual loan payment for approval
     */
    @PostMapping("/submit")
    @Operation(summary = "Submit manual loan payment (Bank Transfer, Cash, Cheque)")
    public ResponseEntity<?> submitManualPayment(
        @RequestBody ManualPaymentRequest request,
        Authentication authentication
    ) {
        try {
            String submittedBy = authentication != null ? authentication.getName() : "system";
            
            ManualLoanPayment payment = new ManualLoanPayment();
            payment.setAmount(request.getAmount());
            payment.setPaymentMethod(request.getPaymentMethod());
            payment.setReferenceNumber(request.getReferenceNumber());
            payment.setBankName(request.getBankName());
            payment.setBankBranch(request.getBankBranch());
            payment.setDepositorName(request.getDepositorName());
            payment.setDepositorIdNumber(request.getDepositorIdNumber());
            payment.setDescription(request.getDescription());
            payment.setSubmittedBy(submittedBy);
            
            ManualLoanPayment savedPayment = manualPaymentService.submitPayment(request.getLoanAccountId(), payment);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Manual payment submitted for approval",
                "paymentId", savedPayment.getId(),
                "status", savedPayment.getStatus()
            ));
        } catch (Exception e) {
            log.error("Error submitting manual payment", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Get pending manual payments for approval
     */
    @GetMapping("/pending")
    @Operation(summary = "Get pending manual loan payments")
    public ResponseEntity<?> getPendingPayments() {
        try {
            List<ManualLoanPayment> pending = manualPaymentService.getPendingPayments();
            return ResponseEntity.ok(pending);
        } catch (Exception e) {
            log.error("Error fetching pending payments", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Approve a manual loan payment
     */
    @PostMapping("/{paymentId}/approve")
    @Operation(summary = "Approve manual loan payment and process to loan account")
    public ResponseEntity<?> approvePayment(
        @PathVariable Long paymentId,
        @RequestBody Map<String, String> request,
        Authentication authentication
    ) {
        try {
            String approvedBy = authentication != null ? authentication.getName() : "system";
            String comments = request.get("comments");
            
            // Approve the payment
            ManualLoanPayment payment = manualPaymentService.approvePayment(paymentId, approvedBy, comments);
            
            // Process the actual payment to loan account
            loanPaymentService.processLoanPayment(
                payment.getLoanAccountId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getReferenceNumber()
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Payment approved and posted to loan account successfully",
                "paymentId", paymentId,
                "loanAccountId", payment.getLoanAccountId()
            ));
        } catch (Exception e) {
            log.error("Error approving payment", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Reject a manual loan payment
     */
    @PostMapping("/{paymentId}/reject")
    @Operation(summary = "Reject manual loan payment")
    public ResponseEntity<?> rejectPayment(
        @PathVariable Long paymentId,
        @RequestBody Map<String, String> request,
        Authentication authentication
    ) {
        try {
            String rejectedBy = authentication != null ? authentication.getName() : "system";
            String reason = request.get("reason");
            
            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Rejection reason is required"
                ));
            }
            
            ManualLoanPayment payment = manualPaymentService.rejectPayment(paymentId, rejectedBy, reason);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Payment rejected",
                "paymentId", paymentId
            ));
        } catch (Exception e) {
            log.error("Error rejecting payment", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Get payment history
     */
    @GetMapping("/history")
    @Operation(summary = "Get manual payment history with optional filters")
    public ResponseEntity<?> getPaymentHistory(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) Long loanAccountId
    ) {
        try {
            List<ManualLoanPayment> history = manualPaymentService.getPaymentHistory(status, loanAccountId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error fetching payment history", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Data
    public static class ManualPaymentRequest {
        private Long loanAccountId;
        private BigDecimal amount;
        private String paymentMethod; // BANK_TRANSFER, CASH, CHEQUE
        private String referenceNumber;
        private String bankName;
        private String bankBranch;
        private String depositorName;
        private String depositorIdNumber;
        private String description;
    }
}
