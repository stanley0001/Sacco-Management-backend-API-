package com.example.demo.finance.accounting.services;

import com.example.demo.finance.accounting.entities.*;
import com.example.demo.finance.accounting.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Log4j2
public class AccountingDataSeeder implements CommandLineRunner {

    private final ChartOfAccountsRepo chartOfAccountsRepo;
    private final ExpenseCategoryRepository expenseCategoryRepo;
    private final AssetCategoryRepository assetCategoryRepo;
    private final EmployeeRepository employeeRepo;
    private final ExpenseRepository expenseRepo;
    private final AccountingService accountingService;

    @Override
    @Transactional
    public void run(String... args) {
        try {
            log.info("🚀 Starting Accounting Data Seeder...");
            
            // Initialize Chart of Accounts if empty
            if (chartOfAccountsRepo.count() == 0) {
                log.info("📊 Initializing Chart of Accounts...");
                accountingService.initializeStandardChartOfAccounts("system");
                log.info("✅ Chart of Accounts initialized with {} accounts", chartOfAccountsRepo.count());
            } else {
                log.info("✅ Chart of Accounts already exists ({} accounts)", chartOfAccountsRepo.count());
            }

            // Initialize Expense Categories if empty
            if (expenseCategoryRepo.count() == 0) {
                log.info("💰 Initializing Expense Categories...");
                initializeExpenseCategories();
                log.info("✅ Expense Categories initialized with {} categories", expenseCategoryRepo.count());
            } else {
                log.info("✅ Expense Categories already exists ({} categories)", expenseCategoryRepo.count());
            }

            // Initialize Asset Categories if empty
            if (assetCategoryRepo.count() == 0) {
                log.info("🏢 Initializing Asset Categories...");
                initializeAssetCategories();
                log.info("✅ Asset Categories initialized with {} categories", assetCategoryRepo.count());
            } else {
                log.info("✅ Asset Categories already exists ({} categories)", assetCategoryRepo.count());
            }

            // Create dummy employees if empty
            if (employeeRepo.count() == 0) {
                log.info("👥 Creating dummy employees...");
                createDummyEmployees();
                log.info("✅ Created {} dummy employees", employeeRepo.count());
            } else {
                log.info("✅ Employees already exist ({} employees)", employeeRepo.count());
            }

            // Create dummy expenses if empty
            if (expenseRepo.count() == 0 && expenseCategoryRepo.count() > 0) {
                log.info("💵 Creating dummy expenses...");
                createDummyExpenses();
                log.info("✅ Created {} dummy expenses", expenseRepo.count());
            } else {
                log.info("✅ Expenses already exist or categories not initialized");
            }

            log.info("🎉 Accounting Data Seeder completed successfully!");
            
        } catch (Exception e) {
            log.error("❌ Error in Accounting Data Seeder: {}", e.getMessage(), e);
        }
    }

    private void initializeExpenseCategories() {
        createExpenseCategory("EXP-SAL", "Salaries & Wages", "Staff salaries and wages", "5030", 800000.0);
        createExpenseCategory("EXP-RENT", "Rent", "Office rent and leases", "5040", 150000.0);
        createExpenseCategory("EXP-UTIL", "Utilities", "Electricity, water, internet", "5050", 50000.0);
        createExpenseCategory("EXP-STAT", "Stationery & Supplies", "Office supplies", "5060", 30000.0);
        createExpenseCategory("EXP-COMM", "Communication", "Phone, internet costs", "5070", 25000.0);
        createExpenseCategory("EXP-TRAV", "Travel & Transport", "Travel and transport", "5080", 40000.0);
        createExpenseCategory("EXP-MAINT", "Repairs & Maintenance", "Equipment maintenance", "5090", 35000.0);
        createExpenseCategory("EXP-MARK", "Marketing", "Advertising and marketing", "5100", 60000.0);
        createExpenseCategory("EXP-PROF", "Professional Fees", "Legal, audit fees", "5110", 45000.0);
        createExpenseCategory("EXP-INS", "Insurance", "Insurance premiums", "5120", 40000.0);
        createExpenseCategory("EXP-DEPR", "Depreciation", "Asset depreciation", "5130", 0.0);
        createExpenseCategory("EXP-BANK", "Bank Charges", "Banking fees", "5140", 15000.0);
        createExpenseCategory("EXP-TAX", "Taxes & Licenses", "Business taxes", "5150", 25000.0);
        createExpenseCategory("EXP-OTHER", "Other Expenses", "Miscellaneous expenses", "5999", 20000.0);
    }

    private void createExpenseCategory(String code, String name, String description, String accountCode, Double budget) {
        ExpenseCategory category = ExpenseCategory.builder()
                .code(code)
                .name(name)
                .description(description)
                .accountCode(accountCode)
                .budgetAmount(budget)
                .isActive(true)
                .build();
        expenseCategoryRepo.save(category);
    }

    private void initializeAssetCategories() {
        createAssetCategory("ASSET-FURN", "Furniture & Fixtures", "Office furniture", 
                           "1051", FixedAsset.DepreciationMethod.STRAIGHT_LINE, 12.5, 8);
        createAssetCategory("ASSET-COMP", "Computer Equipment", "Computers and IT equipment", 
                           "1052", FixedAsset.DepreciationMethod.STRAIGHT_LINE, 25.0, 4);
        createAssetCategory("ASSET-OFFC", "Office Equipment", "Printers, scanners, etc", 
                           "1053", FixedAsset.DepreciationMethod.STRAIGHT_LINE, 20.0, 5);
        createAssetCategory("ASSET-VEH", "Vehicles", "Company vehicles", 
                           "1054", FixedAsset.DepreciationMethod.DECLINING_BALANCE, 25.0, 4);
        createAssetCategory("ASSET-BUILD", "Buildings", "Office buildings", 
                           "1055", FixedAsset.DepreciationMethod.STRAIGHT_LINE, 2.0, 50);
    }

    private void createAssetCategory(String code, String name, String description, 
                                     String accountCode, FixedAsset.DepreciationMethod method,
                                     Double rate, Integer usefulLife) {
        AssetCategory category = AssetCategory.builder()
                .code(code)
                .name(name)
                .description(description)
                .accountCode(accountCode)
                .defaultDepreciationMethod(method)
                .defaultDepreciationRate(rate)
                .defaultUsefulLife(usefulLife)
                .isActive(true)
                .build();
        assetCategoryRepo.save(category);
    }

    private void createDummyEmployees() {
        // Create 5 dummy employees
        createEmployee("EMP001", "John", "Doe", "Mwangi", "12345678", "0712345678", 
                      "john.doe@company.com", "Manager", "Administration", 80000.0, 
                      20000.0, 10000.0, 5000.0);
        
        createEmployee("EMP002", "Jane", "Smith", "Wanjiku", "23456789", "0723456789", 
                      "jane.smith@company.com", "Accountant", "Finance", 65000.0, 
                      15000.0, 8000.0, 3000.0);
        
        createEmployee("EMP003", "Peter", "Kamau", "Njoroge", "34567890", "0734567890", 
                      "peter.kamau@company.com", "Loan Officer", "Loans", 55000.0, 
                      12000.0, 7000.0, 2000.0);
        
        createEmployee("EMP004", "Mary", "Akinyi", "Otieno", "45678901", "0745678901", 
                      "mary.akinyi@company.com", "Customer Service", "Operations", 45000.0, 
                      10000.0, 5000.0, 2000.0);
        
        createEmployee("EMP005", "David", "Kipchoge", "Korir", "56789012", "0756789012", 
                      "david.kipchoge@company.com", "IT Officer", "Technology", 70000.0, 
                      18000.0, 9000.0, 4000.0);
    }

    private void createEmployee(String code, String firstName, String lastName, String middleName,
                                String nationalId, String phone, String email, String position,
                                String department, Double basicSalary, Double housing,
                                Double transport, Double other) {
        Employee employee = Employee.builder()
                .employeeCode(code)
                .firstName(firstName)
                .lastName(lastName)
                .middleName(middleName)
                .nationalId(nationalId)
                .phoneNumber(phone)
                .email(email)
                .position(position)
                .department(department)
                .basicSalary(basicSalary)
                .housingAllowance(housing)
                .transportAllowance(transport)
                .otherAllowances(other)
                .kraPin("A" + nationalId + "Z")
                .nhifNumber("NHIF" + nationalId)
                .nssfNumber("NSSF" + nationalId)
                .bankName("KCB Bank")
                .bankBranch("Nairobi Branch")
                .bankAccountNumber("1234567" + code.substring(3))
                .dateOfJoining(LocalDate.now().minusMonths(6))
                .status(Employee.EmployeeStatus.ACTIVE)
                .createdBy("system")
                .build();
        employeeRepo.save(employee);
    }

    private void createDummyExpenses() {
        ExpenseCategory rentCategory = expenseCategoryRepo.findByCode("EXP-RENT").orElse(null);
        ExpenseCategory utilCategory = expenseCategoryRepo.findByCode("EXP-UTIL").orElse(null);
        ExpenseCategory statCategory = expenseCategoryRepo.findByCode("EXP-STAT").orElse(null);

        if (rentCategory != null) {
            createExpense(rentCategory, LocalDate.now().minusDays(5), 150000.0, 
                         "ABC Properties", "Office rent for October 2025", Expense.ExpenseStatus.PAID);
            createExpense(rentCategory, LocalDate.now().minusDays(35), 150000.0, 
                         "ABC Properties", "Office rent for September 2025", Expense.ExpenseStatus.PAID);
        }

        if (utilCategory != null) {
            createExpense(utilCategory, LocalDate.now().minusDays(3), 15000.0, 
                         "Kenya Power", "Electricity bill - October", Expense.ExpenseStatus.APPROVED);
            createExpense(utilCategory, LocalDate.now().minusDays(2), 8000.0, 
                         "Safaricom", "Internet and phone bills", Expense.ExpenseStatus.PENDING);
        }

        if (statCategory != null) {
            createExpense(statCategory, LocalDate.now().minusDays(1), 12000.0, 
                         "Office Supplies Ltd", "Stationery and office supplies", Expense.ExpenseStatus.PENDING);
        }
    }

    private void createExpense(ExpenseCategory category, LocalDate date, Double amount,
                               String payee, String description, Expense.ExpenseStatus status) {
        Expense expense = Expense.builder()
                .expenseNumber("EXP-" + System.currentTimeMillis())
                .expenseDate(date)
                .category(category)
                .amount(amount)
                .payee(payee)
                .description(description)
                .status(status)
                .paymentMethod(Expense.PaymentMethod.BANK_TRANSFER)
                .createdBy("system")
                .build();
        
        if (status == Expense.ExpenseStatus.APPROVED || status == Expense.ExpenseStatus.PAID) {
            expense.setApprovedBy("system");
            expense.setApprovedAt(java.time.LocalDateTime.now().minusDays(1));
        }
        
        if (status == Expense.ExpenseStatus.PAID) {
            expense.setPaidBy("system");
            expense.setPaidAt(java.time.LocalDateTime.now());
        }
        
        expenseRepo.save(expense);
    }
}
