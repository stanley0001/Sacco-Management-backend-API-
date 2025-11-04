package com.example.demo.accounting.controllers;

import com.example.demo.accounting.entities.Expense;
import com.example.demo.accounting.entities.ExpenseCategory;
import com.example.demo.accounting.services.ExpenseService;
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
@RequestMapping("/api/accounting/expenses")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "Expenses", description = "Expense Management & Tracking")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    @Operation(summary = "Create new expense")
    public ResponseEntity<?> createExpense(
            @RequestBody Expense expense,
            Authentication authentication) {
        try {
            String createdBy = authentication != null ? authentication.getName() : "system";
            Expense created = expenseService.createExpense(expense, createdBy);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error creating expense", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve expense")
    public ResponseEntity<?> approveExpense(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            String approvedBy = authentication != null ? authentication.getName() : "system";
            Expense approved = expenseService.approveExpense(id, approvedBy);
            return ResponseEntity.ok(approved);
        } catch (Exception e) {
            log.error("Error approving expense", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/pay")
    @Operation(summary = "Pay expense and create journal entry")
    public ResponseEntity<?> payExpense(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            String paidBy = authentication != null ? authentication.getName() : "system";
            Expense paid = expenseService.payExpense(id, paidBy);
            return ResponseEntity.ok(paid);
        } catch (Exception e) {
            log.error("Error paying expense", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject expense")
    public ResponseEntity<?> rejectExpense(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String reason = request.getOrDefault("reason", "No reason provided");
            Expense rejected = expenseService.rejectExpense(id, reason);
            return ResponseEntity.ok(rejected);
        } catch (Exception e) {
            log.error("Error rejecting expense", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    @Operation(summary = "Get expenses by date range")
    public ResponseEntity<List<Expense>> getExpenses(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Expense> expenses = expenseService.getExpensesByDateRange(startDate, endDate);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get expenses by status")
    public ResponseEntity<List<Expense>> getExpensesByStatus(@PathVariable Expense.ExpenseStatus status) {
        List<Expense> expenses = expenseService.getExpensesByStatus(status);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get expenses by category")
    public ResponseEntity<List<Expense>> getExpensesByCategory(@PathVariable Long categoryId) {
        List<Expense> expenses = expenseService.getExpensesByCategory(categoryId);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/total")
    @Operation(summary = "Get total expenses for period")
    public ResponseEntity<Map<String, Double>> getTotalExpenses(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Double total = expenseService.getTotalExpenses(startDate, endDate);
        return ResponseEntity.ok(Map.of("total", total));
    }

    // ========== Expense Categories ==========

    @PostMapping("/categories")
    @Operation(summary = "Create expense category")
    public ResponseEntity<?> createCategory(@RequestBody ExpenseCategory category) {
        try {
            ExpenseCategory created = expenseService.createCategory(category);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error creating category", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all expense categories")
    public ResponseEntity<List<ExpenseCategory>> getAllCategories() {
        List<ExpenseCategory> categories = expenseService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @PostMapping("/categories/initialize")
    @Operation(summary = "Initialize standard expense categories")
    public ResponseEntity<Map<String, String>> initializeCategories() {
        try {
            expenseService.initializeStandardCategories();
            return ResponseEntity.ok(Map.of(
                    "message", "Standard expense categories initialized successfully",
                    "status", "success"
            ));
        } catch (Exception e) {
            log.error("Error initializing categories", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage(), "status", "error"));
        }
    }
}
