package com.example.demo.finance.accounting.services;

import com.example.demo.finance.accounting.entities.*;
import com.example.demo.finance.accounting.repositories.EmployeeRepository;
import com.example.demo.finance.accounting.repositories.PayrollRunRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class PayrollService {

    private final EmployeeRepository employeeRepo;
    private final PayrollRunRepository payrollRunRepo;
    private final AccountingService accountingService;

    // ========== Employee Management ==========

    @Transactional
    public Employee createEmployee(Employee employee, String createdBy) {
        if (employeeRepo.existsByEmployeeCode(employee.getEmployeeCode())) {
            throw new RuntimeException("Employee code already exists: " + employee.getEmployeeCode());
        }
        if (employeeRepo.existsByNationalId(employee.getNationalId())) {
            throw new RuntimeException("National ID already exists: " + employee.getNationalId());
        }

        employee.setCreatedBy(createdBy);
        employee.setStatus(Employee.EmployeeStatus.ACTIVE);

        log.info("Creating employee: {} {}", employee.getFirstName(), employee.getLastName());
        return employeeRepo.save(employee);
    }

    @Transactional
    public Employee updateEmployee(Long id, Employee employee) {
        Employee existing = employeeRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + id));

        existing.setFirstName(employee.getFirstName());
        existing.setLastName(employee.getLastName());
        existing.setMiddleName(employee.getMiddleName());
        existing.setPhoneNumber(employee.getPhoneNumber());
        existing.setEmail(employee.getEmail());
        existing.setPosition(employee.getPosition());
        existing.setDepartment(employee.getDepartment());
        existing.setBasicSalary(employee.getBasicSalary());
        existing.setHousingAllowance(employee.getHousingAllowance());
        existing.setTransportAllowance(employee.getTransportAllowance());
        existing.setOtherAllowances(employee.getOtherAllowances());
        existing.setBankName(employee.getBankName());
        existing.setBankBranch(employee.getBankBranch());
        existing.setBankAccountNumber(employee.getBankAccountNumber());
        existing.setStatus(employee.getStatus());

        return employeeRepo.save(existing);
    }

    public List<Employee> getActiveEmployees() {
        return employeeRepo.findByStatusOrderByEmployeeCodeAsc(Employee.EmployeeStatus.ACTIVE);
    }

    public List<Employee> getAllEmployees() {
        return employeeRepo.findAll();
    }

    // ========== Payroll Run Management ==========

    @Transactional
    public PayrollRun createPayrollRun(Integer month, Integer year, String createdBy) {
        // Check if payroll already exists for this period
        if (payrollRunRepo.findByPeriodMonthAndPeriodYear(month, year).isPresent()) {
            throw new RuntimeException("Payroll already exists for " + month + "/" + year);
        }

        PayrollRun payrollRun = PayrollRun.builder()
                .payrollNumber(generatePayrollNumber(month, year))
                .periodMonth(month)
                .periodYear(year)
                .status(PayrollRun.PayrollStatus.DRAFT)
                .createdBy(createdBy)
                .details(new ArrayList<>())
                .build();

        // Add all active employees
        List<Employee> activeEmployees = getActiveEmployees();
        for (Employee employee : activeEmployees) {
            PayrollDetail detail = PayrollDetail.builder()
                    .payrollRun(payrollRun)
                    .employee(employee)
                    .basicSalary(employee.getBasicSalary())
                    .housingAllowance(employee.getHousingAllowance())
                    .transportAllowance(employee.getTransportAllowance())
                    .otherAllowances(employee.getOtherAllowances())
                    .overtime(0.0)
                    .bonus(0.0)
                    .isPaid(false)
                    .build();

            payrollRun.getDetails().add(detail);
        }

        log.info("Creating payroll run for {}/{} with {} employees", month, year, activeEmployees.size());
        return payrollRunRepo.save(payrollRun);
    }

    @Transactional
    public PayrollRun calculatePayroll(Long payrollRunId) {
        PayrollRun payrollRun = payrollRunRepo.findById(payrollRunId)
                .orElseThrow(() -> new RuntimeException("Payroll run not found: " + payrollRunId));

        if (payrollRun.getStatus() != PayrollRun.PayrollStatus.DRAFT) {
            throw new RuntimeException("Only draft payroll can be calculated");
        }

        // Calculate each employee's salary
        for (PayrollDetail detail : payrollRun.getDetails()) {
            calculateEmployeeSalary(detail);
        }

        payrollRun.calculateTotals();
        payrollRun.setStatus(PayrollRun.PayrollStatus.PROCESSED);

        log.info("Payroll {} calculated. Total: {}", payrollRun.getPayrollNumber(), payrollRun.getTotalNetSalary());
        return payrollRunRepo.save(payrollRun);
    }

    @Transactional
    public PayrollRun approvePayroll(Long payrollRunId, String approvedBy) {
        PayrollRun payrollRun = payrollRunRepo.findById(payrollRunId)
                .orElseThrow(() -> new RuntimeException("Payroll run not found: " + payrollRunId));

        if (payrollRun.getStatus() != PayrollRun.PayrollStatus.PROCESSED) {
            throw new RuntimeException("Only processed payroll can be approved");
        }

        payrollRun.setStatus(PayrollRun.PayrollStatus.APPROVED);
        payrollRun.setApprovedBy(approvedBy);
        payrollRun.setApprovedAt(LocalDateTime.now());

        log.info("Payroll {} approved by {}", payrollRun.getPayrollNumber(), approvedBy);
        return payrollRunRepo.save(payrollRun);
    }

    @Transactional
    public PayrollRun processPayment(Long payrollRunId, String processedBy) {
        PayrollRun payrollRun = payrollRunRepo.findById(payrollRunId)
                .orElseThrow(() -> new RuntimeException("Payroll run not found: " + payrollRunId));

        if (payrollRun.getStatus() != PayrollRun.PayrollStatus.APPROVED) {
            throw new RuntimeException("Only approved payroll can be paid");
        }

        // Create journal entry for total salary payment
        JournalEntry journalEntry = createPayrollJournalEntry(payrollRun, processedBy);
        JournalEntry posted = accountingService.createJournalEntry(journalEntry, processedBy);
        accountingService.postJournalEntry(posted.getId(), processedBy);

        // Mark all details as paid
        for (PayrollDetail detail : payrollRun.getDetails()) {
            detail.setIsPaid(true);
            detail.setPaymentReference(posted.getJournalNumber());
        }

        payrollRun.setStatus(PayrollRun.PayrollStatus.PAID);
        payrollRun.setProcessedBy(processedBy);
        payrollRun.setProcessedAt(LocalDateTime.now());
        payrollRun.setJournalEntryId(posted.getJournalNumber());

        log.info("Payroll {} paid. Journal entry: {}", payrollRun.getPayrollNumber(), posted.getJournalNumber());
        return payrollRunRepo.save(payrollRun);
    }

    public List<PayrollRun> getAllPayrollRuns() {
        return payrollRunRepo.findAllByOrderByPeriodYearDescPeriodMonthDesc();
    }

    // ========== Helper Methods ==========

    private void calculateEmployeeSalary(PayrollDetail detail) {
        // Calculate gross salary
        detail.calculateSalary();

        Double grossSalary = detail.getGrossSalary();

        // Calculate deductions
        detail.setPaye(calculatePAYE(grossSalary));
        detail.setNhif(calculateNHIF(grossSalary));
        detail.setNssf(calculateNSSF(grossSalary));

        // Recalculate after deductions
        detail.calculateSalary();
    }

    /**
     * Calculate PAYE (Kenya tax rates - simplified)
     */
    private Double calculatePAYE(Double grossSalary) {
        if (grossSalary <= 24000) {
            return grossSalary * 0.10; // 10%
        } else if (grossSalary <= 32333) {
            return 2400 + ((grossSalary - 24000) * 0.25); // 25%
        } else if (grossSalary <= 500000) {
            return 4483 + ((grossSalary - 32333) * 0.30); // 30%
        } else if (grossSalary <= 800000) {
            return 144783 + ((grossSalary - 500000) * 0.325); // 32.5%
        } else {
            return 242283 + ((grossSalary - 800000) * 0.35); // 35%
        }
    }

    /**
     * Calculate NHIF (Kenya health insurance - 2024 rates)
     */
    private Double calculateNHIF(Double grossSalary) {
        if (grossSalary < 6000) return 150.0;
        if (grossSalary < 8000) return 300.0;
        if (grossSalary < 12000) return 400.0;
        if (grossSalary < 15000) return 500.0;
        if (grossSalary < 20000) return 600.0;
        if (grossSalary < 25000) return 750.0;
        if (grossSalary < 30000) return 850.0;
        if (grossSalary < 35000) return 900.0;
        if (grossSalary < 40000) return 950.0;
        if (grossSalary < 45000) return 1000.0;
        if (grossSalary < 50000) return 1100.0;
        if (grossSalary < 60000) return 1200.0;
        if (grossSalary < 70000) return 1300.0;
        if (grossSalary < 80000) return 1400.0;
        if (grossSalary < 90000) return 1500.0;
        if (grossSalary < 100000) return 1600.0;
        return 1700.0;
    }

    /**
     * Calculate NSSF (Kenya social security - 2024 rates)
     */
    private Double calculateNSSF(Double grossSalary) {
        // Tier I: 6% of first KES 7,000
        double tierI = Math.min(grossSalary, 7000) * 0.06;
        
        // Tier II: 6% of (salary - 7000), capped at KES 36,000
        double tierII = 0;
        if (grossSalary > 7000) {
            tierII = Math.min(grossSalary - 7000, 36000) * 0.06;
        }
        
        return tierI + tierII;
    }

    private JournalEntry createPayrollJournalEntry(PayrollRun payrollRun, String createdBy) {
        JournalEntry entry = JournalEntry.builder()
                .transactionDate(java.time.LocalDate.now())
                .description("Payroll Payment: " + payrollRun.getPeriodMonth() + "/" + payrollRun.getPeriodYear())
                .reference(payrollRun.getPayrollNumber())
                .journalType(JournalEntry.JournalType.CASH_PAYMENTS)
                .createdBy(createdBy)
                .build();

        List<JournalEntryLine> lines = new ArrayList<>();

        // Debit: Salary Expense (total gross)
        JournalEntryLine debitSalary = JournalEntryLine.builder()
                .accountCode("5030") // Salary expense account
                .type(JournalEntryLine.EntryType.DEBIT)
                .amount(payrollRun.getTotalGrossSalary())
                .description("Staff salaries for " + payrollRun.getPeriodMonth() + "/" + payrollRun.getPeriodYear())
                .lineNumber(1)
                .build();
        lines.add(debitSalary);

        // Credit: Bank Account (net salary)
        JournalEntryLine creditBank = JournalEntryLine.builder()
                .accountCode("1020") // Bank account
                .type(JournalEntryLine.EntryType.CREDIT)
                .amount(payrollRun.getTotalNetSalary())
                .description("Net salary payments")
                .lineNumber(2)
                .build();
        lines.add(creditBank);

        // Credit: Tax Payable (PAYE)
        double totalPaye = payrollRun.getDetails().stream()
                .mapToDouble(d -> d.getPaye() != null ? d.getPaye() : 0.0)
                .sum();
        
        JournalEntryLine creditTax = JournalEntryLine.builder()
                .accountCode("2030") // Tax payable
                .type(JournalEntryLine.EntryType.CREDIT)
                .amount(totalPaye)
                .description("PAYE tax payable")
                .lineNumber(3)
                .build();
        lines.add(creditTax);

        // Credit: NHIF Payable
        double totalNhif = payrollRun.getDetails().stream()
                .mapToDouble(d -> d.getNhif() != null ? d.getNhif() : 0.0)
                .sum();
        
        JournalEntryLine creditNhif = JournalEntryLine.builder()
                .accountCode("2040") // NHIF payable
                .type(JournalEntryLine.EntryType.CREDIT)
                .amount(totalNhif)
                .description("NHIF deductions")
                .lineNumber(4)
                .build();
        lines.add(creditNhif);

        // Credit: NSSF Payable
        double totalNssf = payrollRun.getDetails().stream()
                .mapToDouble(d -> d.getNssf() != null ? d.getNssf() : 0.0)
                .sum();
        
        JournalEntryLine creditNssf = JournalEntryLine.builder()
                .accountCode("2050") // NSSF payable
                .type(JournalEntryLine.EntryType.CREDIT)
                .amount(totalNssf)
                .description("NSSF contributions")
                .lineNumber(5)
                .build();
        lines.add(creditNssf);

        entry.setLines(lines);
        return entry;
    }

    private String generatePayrollNumber(Integer month, Integer year) {
        return String.format("PAY-%04d%02d-%d", year, month, System.currentTimeMillis() % 10000);
    }
}
