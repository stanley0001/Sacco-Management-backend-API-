package com.example.demo.accounting.controllers;

import com.example.demo.accounting.entities.ChartOfAccounts;
import com.example.demo.accounting.entities.JournalEntry;
import com.example.demo.accounting.services.AccountingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounting")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "Accounting", description = "Professional financial data capture - Chart of Accounts and Journal Entries")
public class AccountingController {

    private final AccountingService accountingService;

    // ========== Chart of Accounts Endpoints ==========

    @PostMapping("/accounts")
    @Operation(summary = "Create new account in Chart of Accounts")
    public ResponseEntity<ChartOfAccounts> createAccount(
            @RequestBody ChartOfAccounts account,
            Authentication authentication) {
        try {
            String createdBy = authentication != null ? authentication.getName() : "system";
            account.setCreatedBy(createdBy);
            ChartOfAccounts created = accountingService.createAccount(account);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error creating account", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/accounts/{id}")
    @Operation(summary = "Update account in Chart of Accounts")
    public ResponseEntity<ChartOfAccounts> updateAccount(
            @PathVariable Long id,
            @RequestBody ChartOfAccounts account) {
        try {
            ChartOfAccounts updated = accountingService.updateAccount(id, account);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating account", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/accounts")
    @Operation(summary = "Get all accounts")
    public ResponseEntity<List<ChartOfAccounts>> getAllAccounts() {
        List<ChartOfAccounts> accounts = accountingService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/accounts/active")
    @Operation(summary = "Get all active accounts")
    public ResponseEntity<List<ChartOfAccounts>> getActiveAccounts() {
        List<ChartOfAccounts> accounts = accountingService.getActiveAccounts();
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/accounts/{accountCode}")
    @Operation(summary = "Get account by code")
    public ResponseEntity<ChartOfAccounts> getAccountByCode(@PathVariable String accountCode) {
        return accountingService.getAccountByCode(accountCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/accounts/type/{type}")
    @Operation(summary = "Get accounts by type")
    public ResponseEntity<List<ChartOfAccounts>> getAccountsByType(
            @PathVariable ChartOfAccounts.AccountType type) {
        List<ChartOfAccounts> accounts = accountingService.getAccountsByType(type);
        return ResponseEntity.ok(accounts);
    }

    @PostMapping("/accounts/initialize")
    @Operation(summary = "Initialize standard SACCO chart of accounts")
    public ResponseEntity<Map<String, String>> initializeChartOfAccounts(Authentication authentication) {
        try {
            String createdBy = authentication != null ? authentication.getName() : "system";
            accountingService.initializeStandardChartOfAccounts(createdBy);
            return ResponseEntity.ok(Map.of(
                    "message", "Standard chart of accounts initialized successfully",
                    "status", "success"
            ));
        } catch (Exception e) {
            log.error("Error initializing chart of accounts", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage(), "status", "error"));
        }
    }

    // ========== Journal Entry Endpoints ==========

    @PostMapping("/journal-entries")
    @Operation(summary = "Create new journal entry")
    public ResponseEntity<JournalEntry> createJournalEntry(
            @RequestBody JournalEntry entry,
            Authentication authentication) {
        try {
            String createdBy = authentication != null ? authentication.getName() : "system";
            JournalEntry created = accountingService.createJournalEntry(entry, createdBy);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error creating journal entry", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
    }

    @PostMapping("/journal-entries/{id}/post")
    @Operation(summary = "Post journal entry to ledger")
    public ResponseEntity<JournalEntry> postJournalEntry(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            String postedBy = authentication != null ? authentication.getName() : "system";
            JournalEntry posted = accountingService.postJournalEntry(id, postedBy);
            return ResponseEntity.ok(posted);
        } catch (Exception e) {
            log.error("Error posting journal entry", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/journal-entries/{id}/approve")
    @Operation(summary = "Approve journal entry")
    public ResponseEntity<JournalEntry> approveJournalEntry(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            String approvedBy = authentication != null ? authentication.getName() : "system";
            JournalEntry approved = accountingService.approveJournalEntry(id, approvedBy);
            return ResponseEntity.ok(approved);
        } catch (Exception e) {
            log.error("Error approving journal entry", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/journal-entries/{id}/reverse")
    @Operation(summary = "Reverse journal entry")
    public ResponseEntity<JournalEntry> reverseJournalEntry(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            String reversedBy = authentication != null ? authentication.getName() : "system";
            String reason = request.getOrDefault("reason", "No reason provided");
            JournalEntry reversed = accountingService.reverseJournalEntry(id, reason, reversedBy);
            return ResponseEntity.ok(reversed);
        } catch (Exception e) {
            log.error("Error reversing journal entry", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/journal-entries")
    @Operation(summary = "Get journal entries by date range")
    public ResponseEntity<List<JournalEntry>> getJournalEntries(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<JournalEntry> entries = accountingService.getJournalEntriesByDateRange(startDate, endDate);
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/journal-entries/status/{status}")
    @Operation(summary = "Get journal entries by status")
    public ResponseEntity<List<JournalEntry>> getJournalEntriesByStatus(
            @PathVariable JournalEntry.JournalStatus status) {
        List<JournalEntry> entries = accountingService.getJournalEntriesByStatus(status);
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/journal-entries/{id}")
    @Operation(summary = "Get journal entry by ID")
    public ResponseEntity<JournalEntry> getJournalEntryById(@PathVariable Long id) {
        return accountingService.getJournalEntryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
