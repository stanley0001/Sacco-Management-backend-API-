package com.example.demo.finance.loanManagement.controllers;

import com.example.demo.finance.loanManagement.parsistence.entities.LoanStandingOrder;
import com.example.demo.finance.loanManagement.services.AutoLoanDeductionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/loan-standing-orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Loan Standing Orders", description = "Automatic loan deduction management")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LoanStandingOrderController {

    private final AutoLoanDeductionService autoLoanDeductionService;

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get customer's standing orders")
    public ResponseEntity<List<LoanStandingOrder>> getCustomerStandingOrders(
        @PathVariable Long customerId
    ) {
        return ResponseEntity.ok(autoLoanDeductionService.getCustomerStandingOrders(customerId));
    }

    @PostMapping
    @Operation(summary = "Create standing order")
    public ResponseEntity<LoanStandingOrder> createStandingOrder(
        @RequestBody StandingOrderRequest request
    ) {
        log.info("Creating standing order for customer: {}, loan: {}",
            request.getCustomerId(), request.getLoanAccountId());

        LoanStandingOrder order = autoLoanDeductionService.createStandingOrder(
            request.getCustomerId(),
            request.getLoanAccountId(),
            request.getSavingsAccountId(),
            request.getDeductionType(),
            request.getAmount(),
            request.getPercentage(),
            request.getCreatedBy()
        );

        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate standing order")
    public ResponseEntity<String> deactivateStandingOrder(
        @PathVariable Long id,
        @RequestParam(required = false) String updatedBy
    ) {
        autoLoanDeductionService.deactivateStandingOrder(id, updatedBy);
        return ResponseEntity.ok("Standing order deactivated successfully");
    }

    @GetMapping("/status")
    @Operation(summary = "Check if auto deduction is enabled")
    public ResponseEntity<StatusResponse> getStatus() {
        boolean enabled = autoLoanDeductionService.isAutoDeductionEnabled();
        return ResponseEntity.ok(new StatusResponse(enabled));
    }

    @Data
    public static class StandingOrderRequest {
        private Long customerId;
        private Long loanAccountId;
        private Long savingsAccountId;
        private LoanStandingOrder.DeductionType deductionType;
        private BigDecimal amount;
        private BigDecimal percentage;
        private String createdBy;
    }

    @Data
    public static class StatusResponse {
        private final boolean autoDeductionEnabled;
    }
}
