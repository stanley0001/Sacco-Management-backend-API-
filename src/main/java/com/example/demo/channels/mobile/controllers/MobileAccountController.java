package com.example.demo.channels.mobile.controllers;

import com.example.demo.channels.mobile.dto.*;
import com.example.demo.channels.mobile.services.MobileAccountService;
import com.example.demo.channels.mobile.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mobile/accounts")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearer-token")
@Tag(name = "Mobile Accounts", description = "Mobile account management endpoints")
public class MobileAccountController {

    private final MobileAccountService accountService;

    @GetMapping
    @Operation(summary = "Get all accounts", description = "Get all accounts for authenticated member")
    public ResponseEntity<List<AccountSummaryDto>> getAccounts(
            @AuthenticationPrincipal String memberId) {
        log.info("Fetching accounts for member: {}", memberId);
        List<AccountSummaryDto> accounts = accountService.getMemberAccounts(memberId);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{accountId}/balance")
    @Operation(summary = "Get account balance", description = "Get current balance for specific account")
    public ResponseEntity<BalanceDto> getBalance(
            @PathVariable String accountId,
            @AuthenticationPrincipal String memberId) {
        log.info("Fetching balance for account: {}", accountId);
        BalanceDto balance = accountService.getAccountBalance(accountId, memberId);
        return ResponseEntity.ok(balance);
    }

    @GetMapping("/{accountId}/statement")
    @Operation(summary = "Get account statement", description = "Get transaction statement for account")
    public ResponseEntity<Page<TransactionDto>> getStatement(
            @PathVariable String accountId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @AuthenticationPrincipal String memberId,
            Pageable pageable) {
        log.info("Fetching statement for account: {}", accountId);
        Page<TransactionDto> statement = accountService.getAccountStatement(
                accountId, memberId, startDate, endDate, pageable);
        return ResponseEntity.ok(statement);
    }

    @GetMapping("/{accountId}/mini-statement")
    @Operation(summary = "Get mini statement", description = "Get last 5 transactions")
    public ResponseEntity<List<TransactionDto>> getMiniStatement(
            @PathVariable String accountId,
            @AuthenticationPrincipal String memberId) {
        log.info("Fetching mini statement for account: {}", accountId);
        List<TransactionDto> transactions = accountService.getMiniStatement(accountId, memberId);
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/{accountId}/deposit")
    @Operation(summary = "Make deposit", description = "Deposit money to account")
    public ResponseEntity<TransactionResponseDto> deposit(
            @PathVariable String accountId,
            @Valid @RequestBody DepositRequest request,
            @AuthenticationPrincipal String memberId) {
        log.info("Deposit request for account: {}", accountId);
        TransactionResponseDto response = accountService.makeDeposit(accountId, memberId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{accountId}/withdraw")
    @Operation(summary = "Make withdrawal", description = "Withdraw money from account")
    public ResponseEntity<TransactionResponseDto> withdraw(
            @PathVariable String accountId,
            @Valid @RequestBody WithdrawalRequest request,
            @AuthenticationPrincipal String memberId) {
        log.info("Withdrawal request for account: {}", accountId);
        TransactionResponseDto response = accountService.makeWithdrawal(accountId, memberId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer funds", description = "Transfer money between accounts")
    public ResponseEntity<TransactionResponseDto> transfer(
            @Valid @RequestBody TransferRequest request,
            @AuthenticationPrincipal String memberId) {
        log.info("Transfer request from {} to {}", request.getFromAccountId(), request.getToAccountId());
        TransactionResponseDto response = accountService.transferFunds(memberId, request);
        return ResponseEntity.ok(response);
    }
}
