package com.example.demo.finance.accounting.controllers;

import com.example.demo.finance.accounting.entities.Employee;
import com.example.demo.finance.accounting.entities.PayrollRun;
import com.example.demo.finance.accounting.services.PayrollService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounting/payroll")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "Payroll", description = "Payroll & Salary Management")
public class PayrollController {

    private final PayrollService payrollService;

    // ========== Employee Management ==========

    @PostMapping("/employees")
    @Operation(summary = "Create new employee")
    public ResponseEntity<?> createEmployee(
            @RequestBody Employee employee,
            Authentication authentication) {
        try {
            String createdBy = authentication != null ? authentication.getName() : "system";
            Employee created = payrollService.createEmployee(employee, createdBy);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error creating employee", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/employees/{id}")
    @Operation(summary = "Update employee")
    public ResponseEntity<?> updateEmployee(
            @PathVariable Long id,
            @RequestBody Employee employee) {
        try {
            Employee updated = payrollService.updateEmployee(id, employee);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating employee", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/employees")
    @Operation(summary = "Get all employees")
    public ResponseEntity<List<Employee>> getAllEmployees() {
        List<Employee> employees = payrollService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/employees/active")
    @Operation(summary = "Get active employees")
    public ResponseEntity<List<Employee>> getActiveEmployees() {
        List<Employee> employees = payrollService.getActiveEmployees();
        return ResponseEntity.ok(employees);
    }

    // ========== Payroll Run Management ==========

    @PostMapping("/runs")
    @Operation(summary = "Create payroll run for a month")
    public ResponseEntity<?> createPayrollRun(
            @RequestParam Integer month,
            @RequestParam Integer year,
            Authentication authentication) {
        try {
            String createdBy = authentication != null ? authentication.getName() : "system";
            PayrollRun created = payrollService.createPayrollRun(month, year, createdBy);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error creating payroll run", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/runs/{id}/calculate")
    @Operation(summary = "Calculate payroll (taxes, deductions)")
    public ResponseEntity<?> calculatePayroll(@PathVariable Long id) {
        try {
            PayrollRun calculated = payrollService.calculatePayroll(id);
            return ResponseEntity.ok(calculated);
        } catch (Exception e) {
            log.error("Error calculating payroll", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/runs/{id}/approve")
    @Operation(summary = "Approve payroll")
    public ResponseEntity<?> approvePayroll(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            String approvedBy = authentication != null ? authentication.getName() : "system";
            PayrollRun approved = payrollService.approvePayroll(id, approvedBy);
            return ResponseEntity.ok(approved);
        } catch (Exception e) {
            log.error("Error approving payroll", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/runs/{id}/process-payment")
    @Operation(summary = "Process salary payment and create journal entry")
    public ResponseEntity<?> processPayment(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            String processedBy = authentication != null ? authentication.getName() : "system";
            PayrollRun paid = payrollService.processPayment(id, processedBy);
            return ResponseEntity.ok(paid);
        } catch (Exception e) {
            log.error("Error processing payment", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/runs")
    @Operation(summary = "Get all payroll runs")
    public ResponseEntity<List<PayrollRun>> getAllPayrollRuns() {
        List<PayrollRun> runs = payrollService.getAllPayrollRuns();
        return ResponseEntity.ok(runs);
    }

    @GetMapping("/runs/{id}")
    @Operation(summary = "Get payroll run by ID")
    public ResponseEntity<?> getPayrollRunById(@PathVariable Long id) {
        try {
            // This would need to be added to the service
            return ResponseEntity.ok(Map.of("message", "Feature coming soon"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
