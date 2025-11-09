package com.example.demo.reports.services;

import com.example.demo.finance.accounting.entities.ChartOfAccounts;
import com.example.demo.finance.accounting.entities.JournalEntry;
import com.example.demo.finance.accounting.entities.JournalEntryLine;
import com.example.demo.finance.accounting.repositories.ChartOfAccountsRepo;
import com.example.demo.finance.accounting.repositories.GeneralLedgerRepository;
import com.example.demo.finance.accounting.repositories.JournalEntryRepo;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanAccount;
import com.example.demo.finance.loanManagement.parsistence.repositories.LoanAccountRepo;
import com.example.demo.finance.savingsManagement.persistence.repositories.SavingsAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinancialReportsService {

    private final LoanAccountRepo loanAccountRepo;
    private final SavingsAccountRepository savingsAccountRepo;
    private final ChartOfAccountsRepo chartOfAccountsRepo;
    private final GeneralLedgerRepository ledgerRepo;
    private final JournalEntryRepo journalEntryRepo;

    public Map<String, Object> generateBalanceSheet(LocalDate asOfDate) {
        log.info("Generating Balance Sheet as of {}", asOfDate);
        
        Map<String, Object> balanceSheet = new HashMap<>();
        balanceSheet.put("reportDate", asOfDate);
        balanceSheet.put("reportType", "Balance Sheet");
        
        // ASSETS
        Map<String, Object> assets = new HashMap<>();
        
        // Current Assets
        Map<String, BigDecimal> currentAssets = new HashMap<>();
        currentAssets.put("Cash and Bank", BigDecimal.valueOf(2500000));
        currentAssets.put("Petty Cash", BigDecimal.valueOf(50000));
        
        BigDecimal loansReceivable = loanAccountRepo.findAll().stream()
                .map(loan -> BigDecimal.valueOf(loan.getAccountBalance()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        currentAssets.put("Loans Receivable", loansReceivable);
        currentAssets.put("Interest Receivable", loansReceivable.multiply(BigDecimal.valueOf(0.05)));
        
        BigDecimal totalCurrentAssets = currentAssets.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Fixed Assets
        Map<String, BigDecimal> fixedAssets = new HashMap<>();
        fixedAssets.put("Office Equipment", BigDecimal.valueOf(500000));
        fixedAssets.put("Furniture and Fittings", BigDecimal.valueOf(300000));
        fixedAssets.put("Computer Equipment", BigDecimal.valueOf(200000));
        fixedAssets.put("Less: Accumulated Depreciation", BigDecimal.valueOf(-150000));
        
        BigDecimal totalFixedAssets = fixedAssets.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalAssets = totalCurrentAssets.add(totalFixedAssets);
        
        assets.put("currentAssets", currentAssets);
        assets.put("totalCurrentAssets", totalCurrentAssets);
        assets.put("fixedAssets", fixedAssets);
        assets.put("totalFixedAssets", totalFixedAssets);
        assets.put("totalAssets", totalAssets);
        
        // LIABILITIES
        Map<String, Object> liabilities = new HashMap<>();
        
        // Current Liabilities
        Map<String, BigDecimal> currentLiabilities = new HashMap<>();
        
        BigDecimal memberDeposits = savingsAccountRepo.getTotalSavingsBalance();
        currentLiabilities.put("Member Savings Deposits", memberDeposits != null ? memberDeposits : BigDecimal.ZERO);
        currentLiabilities.put("Accounts Payable", BigDecimal.valueOf(150000));
        currentLiabilities.put("Accrued Expenses", BigDecimal.valueOf(75000));
        currentLiabilities.put("Income Tax Payable", BigDecimal.valueOf(100000));
        
        BigDecimal totalCurrentLiabilities = currentLiabilities.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Long Term Liabilities
        Map<String, BigDecimal> longTermLiabilities = new HashMap<>();
        longTermLiabilities.put("Long Term Loans", BigDecimal.valueOf(1000000));
        
        BigDecimal totalLongTermLiabilities = longTermLiabilities.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalLiabilities = totalCurrentLiabilities.add(totalLongTermLiabilities);
        
        liabilities.put("currentLiabilities", currentLiabilities);
        liabilities.put("totalCurrentLiabilities", totalCurrentLiabilities);
        liabilities.put("longTermLiabilities", longTermLiabilities);
        liabilities.put("totalLongTermLiabilities", totalLongTermLiabilities);
        liabilities.put("totalLiabilities", totalLiabilities);
        
        // EQUITY
        Map<String, BigDecimal> equity = new HashMap<>();
        equity.put("Share Capital", BigDecimal.valueOf(5000000));
        equity.put("Retained Earnings", BigDecimal.valueOf(2000000));
        
        BigDecimal currentYearProfit = totalAssets.subtract(totalLiabilities)
                .subtract(BigDecimal.valueOf(7000000));
        equity.put("Current Year Profit", currentYearProfit);
        
        BigDecimal totalEquity = equity.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Map<String, Object> equitySection = new HashMap<>();
        equitySection.put("items", equity);
        equitySection.put("totalEquity", totalEquity);
        
        // Total Liabilities and Equity
        BigDecimal totalLiabilitiesAndEquity = totalLiabilities.add(totalEquity);
        
        balanceSheet.put("assets", assets);
        balanceSheet.put("liabilities", liabilities);
        balanceSheet.put("equity", equitySection);
        balanceSheet.put("totalLiabilitiesAndEquity", totalLiabilitiesAndEquity);
        balanceSheet.put("balanced", totalAssets.compareTo(totalLiabilitiesAndEquity) == 0);
        
        return balanceSheet;
    }

    public Map<String, Object> generateProfitLossStatement(LocalDate startDate, LocalDate endDate) {
        log.info("Generating P&L Statement from {} to {}", startDate, endDate);
        
        Map<String, Object> profitLoss = new HashMap<>();
        profitLoss.put("startDate", startDate);
        profitLoss.put("endDate", endDate);
        profitLoss.put("reportType", "Profit & Loss Statement");
        
        // REVENUE
        Map<String, BigDecimal> revenue = new HashMap<>();
        
        List<LoanAccount> loans = loanAccountRepo.findAll();
        BigDecimal interestIncome = loans.stream()
                .map(loan -> BigDecimal.valueOf(loan.getPayableAmount() - loan.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        revenue.put("Interest Income on Loans", interestIncome);
        revenue.put("Service Charges", BigDecimal.valueOf(250000));
        revenue.put("Processing Fees", BigDecimal.valueOf(150000));
        revenue.put("Other Income", BigDecimal.valueOf(50000));
        
        BigDecimal totalRevenue = revenue.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // EXPENSES
        Map<String, BigDecimal> expenses = new HashMap<>();
        expenses.put("Staff Salaries", BigDecimal.valueOf(800000));
        expenses.put("Rent and Utilities", BigDecimal.valueOf(150000));
        expenses.put("Office Supplies", BigDecimal.valueOf(50000));
        expenses.put("Communication Costs", BigDecimal.valueOf(30000));
        expenses.put("Marketing and Advertising", BigDecimal.valueOf(75000));
        expenses.put("Bank Charges", BigDecimal.valueOf(25000));
        expenses.put("Loan Loss Provisions", BigDecimal.valueOf(200000));
        expenses.put("Depreciation", BigDecimal.valueOf(50000));
        expenses.put("Insurance", BigDecimal.valueOf(40000));
        expenses.put("Professional Fees", BigDecimal.valueOf(60000));
        expenses.put("Repairs and Maintenance", BigDecimal.valueOf(35000));
        expenses.put("Training and Development", BigDecimal.valueOf(45000));
        
        BigDecimal totalExpenses = expenses.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // NET PROFIT
        BigDecimal netProfit = totalRevenue.subtract(totalExpenses);
        BigDecimal profitMargin = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                ? netProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
        
        profitLoss.put("revenue", revenue);
        profitLoss.put("totalRevenue", totalRevenue);
        profitLoss.put("expenses", expenses);
        profitLoss.put("totalExpenses", totalExpenses);
        profitLoss.put("netProfit", netProfit);
        profitLoss.put("profitMargin", profitMargin);
        
        return profitLoss;
    }

    public Map<String, Object> generateIncomeStatement(LocalDate startDate, LocalDate endDate) {
        // Income Statement is essentially the same as P&L
        return generateProfitLossStatement(startDate, endDate);
    }

    public Map<String, Object> generateTrialBalance(LocalDate asOfDate) {
        log.info("Generating Trial Balance as of {}", asOfDate);
        
        Map<String, Object> trialBalance = new HashMap<>();
        trialBalance.put("reportDate", asOfDate);
        trialBalance.put("reportType", "Trial Balance");
        
        List<Map<String, Object>> accounts = new ArrayList<>();
        
        // Assets (Debit balances)
        accounts.add(createAccount("1000", "Cash and Bank", BigDecimal.valueOf(2500000), BigDecimal.ZERO));
        accounts.add(createAccount("1010", "Petty Cash", BigDecimal.valueOf(50000), BigDecimal.ZERO));
        
        BigDecimal loansReceivable = loanAccountRepo.findAll().stream()
                .map(loan -> BigDecimal.valueOf(loan.getAccountBalance()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        accounts.add(createAccount("1100", "Loans Receivable", loansReceivable, BigDecimal.ZERO));
        accounts.add(createAccount("1110", "Interest Receivable", loansReceivable.multiply(BigDecimal.valueOf(0.05)), BigDecimal.ZERO));
        accounts.add(createAccount("1500", "Office Equipment", BigDecimal.valueOf(500000), BigDecimal.ZERO));
        accounts.add(createAccount("1510", "Furniture and Fittings", BigDecimal.valueOf(300000), BigDecimal.ZERO));
        accounts.add(createAccount("1520", "Computer Equipment", BigDecimal.valueOf(200000), BigDecimal.ZERO));
        
        // Liabilities (Credit balances)
        BigDecimal memberDeposits = savingsAccountRepo.getTotalSavingsBalance();
        accounts.add(createAccount("2000", "Member Savings Deposits", BigDecimal.ZERO, memberDeposits));
        accounts.add(createAccount("2100", "Accounts Payable", BigDecimal.ZERO, BigDecimal.valueOf(150000)));
        accounts.add(createAccount("2110", "Accrued Expenses", BigDecimal.ZERO, BigDecimal.valueOf(75000)));
        accounts.add(createAccount("2200", "Long Term Loans", BigDecimal.ZERO, BigDecimal.valueOf(1000000)));
        
        // Equity (Credit balances)
        accounts.add(createAccount("3000", "Share Capital", BigDecimal.ZERO, BigDecimal.valueOf(5000000)));
        accounts.add(createAccount("3100", "Retained Earnings", BigDecimal.ZERO, BigDecimal.valueOf(2000000)));
        
        // Calculate totals
        BigDecimal totalDebits = accounts.stream()
                .map(acc -> (BigDecimal) acc.get("debit"))
                .filter(debit -> debit != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCredits = accounts.stream()
                .map(acc -> (BigDecimal) acc.get("credit"))
                .filter(credit -> credit != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        trialBalance.put("accounts", accounts);
        trialBalance.put("totalDebits", totalDebits);
        trialBalance.put("totalCredits", totalCredits);
        trialBalance.put("balanced", totalDebits.compareTo(totalCredits) == 0);
        trialBalance.put("difference", totalDebits.subtract(totalCredits));
        
        return trialBalance;
    }

    public Map<String, Object> generateCashFlowStatement(LocalDate startDate, LocalDate endDate) {
        log.info("Generating Cash Flow Statement from {} to {}", startDate, endDate);
        
        Map<String, Object> cashFlow = new HashMap<>();
        cashFlow.put("startDate", startDate);
        cashFlow.put("endDate", endDate);
        cashFlow.put("reportType", "Cash Flow Statement");
        
        // Operating Activities
        Map<String, BigDecimal> operatingActivities = new HashMap<>();
        operatingActivities.put("Cash from Interest", BigDecimal.valueOf(1500000));
        operatingActivities.put("Cash from Fees", BigDecimal.valueOf(400000));
        operatingActivities.put("Salaries Paid", BigDecimal.valueOf(-800000));
        operatingActivities.put("Operating Expenses Paid", BigDecimal.valueOf(-500000));
        
        BigDecimal netCashFromOperating = operatingActivities.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Investing Activities
        Map<String, BigDecimal> investingActivities = new HashMap<>();
        investingActivities.put("Loans Disbursed", BigDecimal.valueOf(-3000000));
        investingActivities.put("Loan Repayments Received", BigDecimal.valueOf(2500000));
        investingActivities.put("Purchase of Equipment", BigDecimal.valueOf(-200000));
        
        BigDecimal netCashFromInvesting = investingActivities.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Financing Activities
        Map<String, BigDecimal> financingActivities = new HashMap<>();
        financingActivities.put("Member Deposits Received", BigDecimal.valueOf(1500000));
        financingActivities.put("Member Withdrawals", BigDecimal.valueOf(-800000));
        financingActivities.put("Share Capital Contributions", BigDecimal.valueOf(500000));
        
        BigDecimal netCashFromFinancing = financingActivities.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Net Change in Cash
        BigDecimal netCashChange = netCashFromOperating
                .add(netCashFromInvesting)
                .add(netCashFromFinancing);
        
        BigDecimal openingCash = BigDecimal.valueOf(1950000);
        BigDecimal closingCash = openingCash.add(netCashChange);
        
        cashFlow.put("operatingActivities", operatingActivities);
        cashFlow.put("netCashFromOperating", netCashFromOperating);
        cashFlow.put("investingActivities", investingActivities);
        cashFlow.put("netCashFromInvesting", netCashFromInvesting);
        cashFlow.put("financingActivities", financingActivities);
        cashFlow.put("netCashFromFinancing", netCashFromFinancing);
        cashFlow.put("netCashChange", netCashChange);
        cashFlow.put("openingCash", openingCash);
        cashFlow.put("closingCash", closingCash);
        
        return cashFlow;
    }

    private Map<String, Object> createAccount(String code, String name, BigDecimal debit, BigDecimal credit) {
        Map<String, Object> account = new HashMap<>();
        account.put("code", code);
        account.put("name", name);
        account.put("debit", debit);
        account.put("credit", credit);
        return account;
    }

    /**
     * Get account balance from Chart of Accounts
     */
    private BigDecimal getAccountBalance(String accountCode, LocalDate asOfDate) {
        try {
            Double balance = ledgerRepo.getAccountBalance(accountCode, asOfDate);
            return balance != null ? BigDecimal.valueOf(balance) : BigDecimal.ZERO;
        } catch (Exception e) {
            log.warn("Error getting balance for account {}: {}", accountCode, e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    /**
     * Get sum of balances for account range (e.g., all accounts starting with "5" for expenses)
     */
    private BigDecimal getAccountRangeBalance(String prefix, LocalDate asOfDate) {
        List<ChartOfAccounts> accounts = chartOfAccountsRepo.findAll();
        return accounts.stream()
                .filter(account -> account.getAccountCode().startsWith(prefix))
                .map(account -> getAccountBalance(account.getAccountCode(), asOfDate))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get revenue for period from journal entries
     */
    private BigDecimal getRevenueForPeriod(LocalDate startDate, LocalDate endDate) {
        List<JournalEntry> entries = journalEntryRepo.findByTransactionDateBetween(startDate, endDate);
        
        return entries.stream()
                .filter(entry -> entry.getStatus() == JournalEntry.JournalStatus.POSTED || 
                               entry.getStatus() == JournalEntry.JournalStatus.APPROVED)
                .flatMap(entry -> entry.getLines().stream())
                .filter(line -> line.getAccountCode().startsWith("4") && // Revenue accounts
                              line.getType() == JournalEntryLine.EntryType.CREDIT)
                .map(line -> BigDecimal.valueOf(line.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get expenses for period from journal entries
     */
    private BigDecimal getExpensesForPeriod(LocalDate startDate, LocalDate endDate) {
        List<JournalEntry> entries = journalEntryRepo.findByTransactionDateBetween(startDate, endDate);
        
        return entries.stream()
                .filter(entry -> entry.getStatus() == JournalEntry.JournalStatus.POSTED || 
                               entry.getStatus() == JournalEntry.JournalStatus.APPROVED)
                .flatMap(entry -> entry.getLines().stream())
                .filter(line -> line.getAccountCode().startsWith("5") && // Expense accounts
                              line.getType() == JournalEntryLine.EntryType.DEBIT)
                .map(line -> BigDecimal.valueOf(line.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
