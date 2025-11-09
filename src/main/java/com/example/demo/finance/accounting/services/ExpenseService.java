package com.example.demo.finance.accounting.services;

import com.example.demo.finance.accounting.entities.*;
import com.example.demo.finance.accounting.entities.Expense;
import com.example.demo.finance.accounting.entities.ExpenseCategory;
import com.example.demo.finance.accounting.entities.JournalEntry;
import com.example.demo.finance.accounting.entities.JournalEntryLine;
import com.example.demo.finance.accounting.repositories.ExpenseCategoryRepository;
import com.example.demo.finance.accounting.repositories.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class ExpenseService {

    private final ExpenseRepository expenseRepo;
    private final ExpenseCategoryRepository categoryRepo;
    private final AccountingService accountingService;

    @Transactional
    public Expense createExpense(Expense expense, String createdBy) {
        expense.setExpenseNumber(generateExpenseNumber());
        expense.setStatus(Expense.ExpenseStatus.PENDING);
        expense.setCreatedBy(createdBy);
        
        log.info("Creating expense: {} for amount: {}", expense.getExpenseNumber(), expense.getAmount());
        return expenseRepo.save(expense);
    }

    @Transactional
    public Expense approveExpense(Long id, String approvedBy) {
        Expense expense = expenseRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found: " + id));

        if (expense.getStatus() != Expense.ExpenseStatus.PENDING) {
            throw new RuntimeException("Only pending expenses can be approved");
        }

        expense.setStatus(Expense.ExpenseStatus.APPROVED);
        expense.setApprovedBy(approvedBy);
        expense.setApprovedAt(LocalDateTime.now());

        log.info("Expense {} approved by {}", expense.getExpenseNumber(), approvedBy);
        return expenseRepo.save(expense);
    }

    @Transactional
    public Expense payExpense(Long id, String paidBy) {
        Expense expense = expenseRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found: " + id));

        if (expense.getStatus() != Expense.ExpenseStatus.APPROVED) {
            throw new RuntimeException("Only approved expenses can be paid");
        }

        // Create journal entry
        JournalEntry journalEntry = createExpenseJournalEntry(expense, paidBy);
        JournalEntry posted = accountingService.createJournalEntry(journalEntry, paidBy);
        accountingService.postJournalEntry(posted.getId(), paidBy);

        expense.setStatus(Expense.ExpenseStatus.PAID);
        expense.setPaidBy(paidBy);
        expense.setPaidAt(LocalDateTime.now());
        expense.setJournalEntryId(posted.getJournalNumber());

        log.info("Expense {} paid by {}. Journal entry: {}", expense.getExpenseNumber(), paidBy, posted.getJournalNumber());
        return expenseRepo.save(expense);
    }

    @Transactional
    public Expense rejectExpense(Long id, String reason) {
        Expense expense = expenseRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found: " + id));

        expense.setStatus(Expense.ExpenseStatus.REJECTED);
        expense.setNotes((expense.getNotes() != null ? expense.getNotes() : "") + "\nRejected: " + reason);

        log.info("Expense {} rejected", expense.getExpenseNumber());
        return expenseRepo.save(expense);
    }

    public List<Expense> getExpensesByDateRange(LocalDate startDate, LocalDate endDate) {
        return expenseRepo.findByExpenseDateBetweenOrderByExpenseDateDesc(startDate, endDate);
    }

    public List<Expense> getExpensesByCategory(Long categoryId) {
        return expenseRepo.findByCategoryIdOrderByExpenseDateDesc(categoryId);
    }

    public List<Expense> getExpensesByStatus(Expense.ExpenseStatus status) {
        return expenseRepo.findByStatus(status);
    }

    public Double getTotalExpenses(LocalDate startDate, LocalDate endDate) {
        return expenseRepo.getTotalExpenses(startDate, endDate);
    }

    // ========== Expense Categories ==========

    @Transactional
    public ExpenseCategory createCategory(ExpenseCategory category) {
        if (categoryRepo.existsByCode(category.getCode())) {
            throw new RuntimeException("Category code already exists: " + category.getCode());
        }
        return categoryRepo.save(category);
    }

    public List<ExpenseCategory> getAllCategories() {
        return categoryRepo.findByIsActiveTrueOrderByNameAsc();
    }

    @Transactional
    public void initializeStandardCategories() {
        if (categoryRepo.count() > 0) {
            log.info("Expense categories already initialized");
            return;
        }

        log.info("Initializing standard expense categories...");

        createStandardCategory("EXP-SAL", "Salaries & Wages", "Staff salaries and wages", "5030");
        createStandardCategory("EXP-RENT", "Rent", "Office rent and leases", "5040");
        createStandardCategory("EXP-UTIL", "Utilities", "Electricity, water, internet", "5050");
        createStandardCategory("EXP-STAT", "Stationery & Supplies", "Office supplies", "5060");
        createStandardCategory("EXP-COMM", "Communication", "Phone, internet costs", "5070");
        createStandardCategory("EXP-TRAV", "Travel & Transport", "Travel and transport", "5080");
        createStandardCategory("EXP-MAINT", "Repairs & Maintenance", "Equipment maintenance", "5090");
        createStandardCategory("EXP-MARK", "Marketing", "Advertising and marketing", "5100");
        createStandardCategory("EXP-PROF", "Professional Fees", "Legal, audit fees", "5110");
        createStandardCategory("EXP-INS", "Insurance", "Insurance premiums", "5120");
        createStandardCategory("EXP-DEPR", "Depreciation", "Asset depreciation", "5130");
        createStandardCategory("EXP-BANK", "Bank Charges", "Banking fees", "5140");
        createStandardCategory("EXP-TAX", "Taxes & Licenses", "Business taxes", "5150");
        createStandardCategory("EXP-OTHER", "Other Expenses", "Miscellaneous expenses", "5999");

        log.info("Standard expense categories initialized");
    }

    private void createStandardCategory(String code, String name, String description, String accountCode) {
        ExpenseCategory category = ExpenseCategory.builder()
                .code(code)
                .name(name)
                .description(description)
                .accountCode(accountCode)
                .isActive(true)
                .build();
        categoryRepo.save(category);
    }

    // ========== Helper Methods ==========

    private JournalEntry createExpenseJournalEntry(Expense expense, String createdBy) {
        JournalEntry entry = JournalEntry.builder()
                .transactionDate(expense.getExpenseDate())
                .description("Expense Payment: " + expense.getDescription())
                .reference(expense.getExpenseNumber())
                .journalType(JournalEntry.JournalType.CASH_PAYMENTS)
                .createdBy(createdBy)
                .build();

        List<JournalEntryLine> lines = new ArrayList<>();

        // Debit: Expense Account
        String expenseAccountCode = expense.getCategory().getAccountCode() != null ? 
                                     expense.getCategory().getAccountCode() : "5999";
        
        JournalEntryLine debitLine = JournalEntryLine.builder()
                .accountCode(expenseAccountCode)
                .type(JournalEntryLine.EntryType.DEBIT)
                .amount(expense.getAmount())
                .description("Expense: " + expense.getPayee())
                .lineNumber(1)
                .build();
        lines.add(debitLine);

        // Credit: Cash/Bank Account
        String cashAccountCode = expense.getPaymentMethod() == Expense.PaymentMethod.CASH ? "1010" : "1020";
        
        JournalEntryLine creditLine = JournalEntryLine.builder()
                .accountCode(cashAccountCode)
                .type(JournalEntryLine.EntryType.CREDIT)
                .amount(expense.getAmount())
                .description("Payment to: " + expense.getPayee())
                .lineNumber(2)
                .build();
        lines.add(creditLine);

        entry.setLines(lines);
        return entry;
    }

    private String generateExpenseNumber() {
        return "EXP-" + System.currentTimeMillis();
    }
}
