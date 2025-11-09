package com.example.demo.channels.mobile.controllers;

import com.example.demo.channels.mobile.dto.*;
import com.example.demo.channels.mobile.services.MobileLoanService;
import com.example.demo.channels.mobile.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mobile/loans")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearer-token")
@Tag(name = "Mobile Loans", description = "Mobile loan management endpoints")
public class MobileLoanController {

    private final MobileLoanService loanService;

    @GetMapping
    @Operation(summary = "Get all loans", description = "Get all loans for authenticated member")
    public ResponseEntity<List<LoanSummaryDto>> getLoans(
            @AuthenticationPrincipal String memberId) {
        log.info("Fetching loans for member: {}", memberId);
        List<LoanSummaryDto> loans = loanService.getMemberLoans(memberId);
        return ResponseEntity.ok(loans);
    }

    @GetMapping("/{loanId}")
    @Operation(summary = "Get loan details", description = "Get detailed information about a loan")
    public ResponseEntity<LoanDetailDto> getLoanDetails(
            @PathVariable String loanId,
            @AuthenticationPrincipal String memberId) {
        log.info("Fetching loan details: {}", loanId);
        LoanDetailDto loan = loanService.getLoanDetails(loanId, memberId);
        return ResponseEntity.ok(loan);
    }

    @GetMapping("/{loanId}/schedule")
    @Operation(summary = "Get repayment schedule", description = "Get loan repayment schedule")
    public ResponseEntity<List<RepaymentScheduleDto>> getRepaymentSchedule(
            @PathVariable String loanId,
            @AuthenticationPrincipal String memberId) {
        log.info("Fetching repayment schedule for loan: {}", loanId);
        List<RepaymentScheduleDto> schedule = loanService.getRepaymentSchedule(loanId, memberId);
        return ResponseEntity.ok(schedule);
    }

    @GetMapping("/products")
    @Operation(summary = "Get loan products", description = "Get available loan products")
    public ResponseEntity<List<LoanProductDto>> getLoanProducts() {
        log.info("Fetching loan products");
        List<LoanProductDto> products = loanService.getAvailableLoanProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/eligibility/{productId}")
    @Operation(summary = "Check loan eligibility", description = "Check if member is eligible for a loan product")
    public ResponseEntity<EligibilityResponseDto> checkEligibility(
            @PathVariable String productId,
            @AuthenticationPrincipal String memberId) {
        log.info("Checking loan eligibility for member: {} product: {}", memberId, productId);
        EligibilityResponseDto eligibility = loanService.checkLoanEligibility(memberId, productId);
        return ResponseEntity.ok(eligibility);
    }

    @PostMapping("/apply")
    @Operation(summary = "Apply for loan", description = "Submit loan application")
    public ResponseEntity<LoanApplicationResponseDto> applyForLoan(
            @Valid @RequestBody LoanApplicationRequest request,
            @AuthenticationPrincipal String memberId) {
        log.info("Loan application from member: {}", memberId);
        LoanApplicationResponseDto response = loanService.applyForLoan(memberId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{loanId}/repay")
    @Operation(summary = "Make loan repayment", description = "Make payment towards loan")
    public ResponseEntity<TransactionResponseDto> makeLoanRepayment(
            @PathVariable String loanId,
            @Valid @RequestBody LoanRepaymentRequest request,
            @AuthenticationPrincipal String memberId) {
        log.info("Loan repayment for loan: {}", loanId);
        TransactionResponseDto response = loanService.makeLoanRepayment(loanId, memberId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{loanId}/transactions")
    @Operation(summary = "Get loan transactions", description = "Get transaction history for loan")
    public ResponseEntity<List<TransactionDto>> getLoanTransactions(
            @PathVariable String loanId,
            @AuthenticationPrincipal String memberId) {
        log.info("Fetching transactions for loan: {}", loanId);
        List<TransactionDto> transactions = loanService.getLoanTransactions(loanId, memberId);
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/{loanId}/top-up")
    @Operation(summary = "Request loan top-up", description = "Request additional funds on existing loan")
    public ResponseEntity<LoanApplicationResponseDto> requestTopUp(
            @PathVariable String loanId,
            @Valid @RequestBody LoanTopUpRequest request,
            @AuthenticationPrincipal String memberId) {
        log.info("Loan top-up request for loan: {}", loanId);
        LoanApplicationResponseDto response = loanService.requestLoanTopUp(loanId, memberId, request);
        return ResponseEntity.ok(response);
    }
}
