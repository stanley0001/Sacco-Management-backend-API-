package com.example.demo.finance.payments.controllers;

import com.example.demo.finance.loanManagement.dto.ManualPaymentCommand;
import com.example.demo.finance.payments.entities.ManualPayment;
import com.example.demo.finance.payments.services.ManualPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments/manual")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Manual Payments", description = "Process manual payments (CASH, BANK, CHEQUE) with accounting integration")
public class ManualPaymentController {
    
    private final ManualPaymentService manualPaymentService;
    
    @PostMapping("/process")
    @Operation(summary = "Process a manual payment")
    public ResponseEntity<Map<String, Object>> processPayment(@RequestBody ManualPaymentCommand command) {
        try {
            ManualPayment payment = manualPaymentService.processManualPayment(command);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", payment.getRequiresApproval() ? 
                    "Payment submitted for approval" : "Payment processed successfully",
                "payment", payment,
                "requiresApproval", payment.getRequiresApproval()
            ));
        } catch (IllegalArgumentException e) {
            log.error("Invalid payment command", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error processing manual payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Failed to process payment: " + e.getMessage()
            ));
        }
    }
    
    @GetMapping("/pending-approval")
    @Operation(summary = "Get all payments pending approval")
    public ResponseEntity<List<ManualPayment>> getPendingApprovals() {
        return ResponseEntity.ok(manualPaymentService.getPendingApprovalPayments());
    }
    
    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve a pending manual payment")
    public ResponseEntity<Map<String, Object>> approvePayment(
            @PathVariable Long id,
            @RequestParam String approvedBy,
            @RequestParam(required = false) String comments) {
        try {
            ManualPayment payment = manualPaymentService.approvePayment(id, approvedBy, comments);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Payment approved and posted successfully",
                "payment", payment
            ));
        } catch (IllegalStateException e) {
            log.error("Invalid payment state", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error approving payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Failed to approve payment: " + e.getMessage()
            ));
        }
    }
    
    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject a pending manual payment")
    public ResponseEntity<Map<String, Object>> rejectPayment(
            @PathVariable Long id,
            @RequestParam String rejectedBy,
            @RequestParam String reason) {
        try {
            ManualPayment payment = manualPaymentService.rejectPayment(id, rejectedBy, reason);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Payment rejected successfully",
                "payment", payment
            ));
        } catch (IllegalStateException e) {
            log.error("Invalid payment state", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error rejecting payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Failed to reject payment: " + e.getMessage()
            ));
        }
    }
    
    @GetMapping("/history")
    @Operation(summary = "Get payment history with optional date range")
    public ResponseEntity<List<ManualPayment>> getPaymentHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(manualPaymentService.getPaymentHistory(startDate, endDate));
    }
    
    @GetMapping("/by-target")
    @Operation(summary = "Get payments by target (loan, account, etc.)")
    public ResponseEntity<List<ManualPayment>> getPaymentsByTarget(
            @RequestParam String targetType,
            @RequestParam Long targetId) {
        return ResponseEntity.ok(manualPaymentService.getPaymentsByTarget(targetType, targetId));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get payment details by ID")
    public ResponseEntity<ManualPayment> getPaymentById(@PathVariable Long id) {
        return manualPaymentService.getPaymentHistory(null, null).stream()
            .filter(p -> p.getId().equals(id))
            .findFirst()
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/stats")
    @Operation(summary = "Get payment statistics")
    public ResponseEntity<Map<String, Object>> getPaymentStats() {
        List<ManualPayment> allPayments = manualPaymentService.getPaymentHistory(null, null);
        List<ManualPayment> pendingApproval = manualPaymentService.getPendingApprovalPayments();
        
        long postedCount = allPayments.stream()
            .filter(p -> p.getStatus() == ManualPayment.PaymentStatus.POSTED)
            .count();
        
        long rejectedCount = allPayments.stream()
            .filter(p -> p.getStatus() == ManualPayment.PaymentStatus.REJECTED)
            .count();
        
        double totalAmount = allPayments.stream()
            .filter(p -> p.getStatus() == ManualPayment.PaymentStatus.POSTED)
            .mapToDouble(p -> p.getAmount().doubleValue())
            .sum();
        
        return ResponseEntity.ok(Map.of(
            "totalPayments", allPayments.size(),
            "pendingApproval", pendingApproval.size(),
            "posted", postedCount,
            "rejected", rejectedCount,
            "totalAmount", totalAmount
        ));
    }
}
