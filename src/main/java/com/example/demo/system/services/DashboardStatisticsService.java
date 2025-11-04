package com.example.demo.system.services;

import com.example.demo.customerManagement.parsistence.repositories.CustomerRepository;
import com.example.demo.loanManagement.parsistence.entities.LoanAccount;
import com.example.demo.loanManagement.parsistence.repositories.ApplicationRepo;
import com.example.demo.loanManagement.parsistence.repositories.LoanAccountRepo;
import com.example.demo.savingsManagement.persistence.repositories.SavingsAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardStatisticsService {

    private final LoanAccountRepo loanAccountRepo;
    private final ApplicationRepo applicationRepo;
    private final CustomerRepository customerRepository;
    private final SavingsAccountRepository savingsAccountRepo;

    public Map<String, Object> getDashboardStatistics() {
        log.info("Generating dashboard statistics");
        
        Map<String, Object> stats = new HashMap<>();
        
        // Combine all statistics
        stats.putAll(getLoanStatistics());
        stats.putAll(getCustomerStatistics());
        stats.putAll(getSavingsStatistics());
        stats.putAll(getFinancialSummary());
        
        return stats;
    }

    public Map<String, Object> getLoanStatistics() {
        List<LoanAccount> allLoans = loanAccountRepo.findAll();
        
        long totalLoans = allLoans.size();
        long activeLoans = allLoans.stream()
                .filter(loan -> "ACTIVE".equalsIgnoreCase(loan.getStatus()))
                .count();
        long completedLoans = allLoans.stream()
                .filter(loan -> "COMPLETED".equalsIgnoreCase(loan.getStatus()))
                .count();
        long defaultedLoans = allLoans.stream()
                .filter(loan -> "DEFAULTED".equalsIgnoreCase(loan.getStatus()))
                .count();

        BigDecimal totalDisbursed = allLoans.stream()
                .map(loan -> BigDecimal.valueOf(loan.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalOutstanding = allLoans.stream()
                .map(loan -> BigDecimal.valueOf(loan.getAccountBalance()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRepayable = allLoans.stream()
                .map(loan -> BigDecimal.valueOf(loan.getPayableAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCollected = totalRepayable.subtract(totalOutstanding);

        BigDecimal collectionRate = totalRepayable.compareTo(BigDecimal.ZERO) > 0
                ? totalCollected.divide(totalRepayable, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        // Get recent applications (last 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long recentApplications = applicationRepo.findTop10ByApplicationTimeAfter(thirtyDaysAgo).size();

        Map<String, Object> loanStats = new HashMap<>();
        loanStats.put("totalLoans", totalLoans);
        loanStats.put("activeLoans", activeLoans);
        loanStats.put("completedLoans", completedLoans);
        loanStats.put("defaultedLoans", defaultedLoans);
        loanStats.put("totalDisbursed", totalDisbursed);
        loanStats.put("totalOutstanding", totalOutstanding);
        loanStats.put("totalCollected", totalCollected);
        loanStats.put("collectionRate", collectionRate);
        loanStats.put("recentApplications", recentApplications);
        
        return loanStats;
    }

    public Map<String, Object> getCustomerStatistics() {
        long totalCustomers = customerRepository.count();
        
        // Active customers are those with at least one active loan or savings account
        long activeCustomers = loanAccountRepo.findAll().stream()
                .filter(loan -> "ACTIVE".equalsIgnoreCase(loan.getStatus()))
                .map(LoanAccount::getCustomerId)
                .distinct()
                .count();

        Map<String, Object> customerStats = new HashMap<>();
        customerStats.put("totalCustomers", totalCustomers);
        customerStats.put("activeCustomers", activeCustomers);
        customerStats.put("inactiveCustomers", totalCustomers - activeCustomers);
        
        return customerStats;
    }

    public Map<String, Object> getSavingsStatistics() {
        BigDecimal totalSavings = savingsAccountRepo.getTotalSavingsBalance();
        long savingsAccounts = savingsAccountRepo.count();

        BigDecimal averageSavings = savingsAccounts > 0
                ? totalSavings.divide(BigDecimal.valueOf(savingsAccounts), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Map<String, Object> savingsStats = new HashMap<>();
        savingsStats.put("totalSavings", totalSavings);
        savingsStats.put("savingsAccounts", savingsAccounts);
        savingsStats.put("averageSavings", averageSavings);
        
        return savingsStats;
    }

    public Map<String, Object> getFinancialSummary() {
        // Get loan statistics
        List<LoanAccount> allLoans = loanAccountRepo.findAll();
        
        BigDecimal totalAssets = BigDecimal.ZERO;
        BigDecimal totalLiabilities = BigDecimal.ZERO;
        BigDecimal totalEquity = BigDecimal.ZERO;
        
        // Assets: Outstanding loans + cash reserves
        BigDecimal outstandingLoans = allLoans.stream()
                .map(loan -> BigDecimal.valueOf(loan.getAccountBalance()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal cashReserves = BigDecimal.valueOf(1000000); // Mock data - should come from accounting module
        totalAssets = outstandingLoans.add(cashReserves);
        
        // Liabilities: Member deposits (with null safety)
        BigDecimal memberDeposits = savingsAccountRepo.getTotalSavingsBalance();
        memberDeposits = memberDeposits != null ? memberDeposits : BigDecimal.ZERO;
        totalLiabilities = memberDeposits;
        
        // Equity = Assets - Liabilities
        totalEquity = totalAssets.subtract(totalLiabilities);
        
        // Income calculations
        BigDecimal interestIncome = allLoans.stream()
                .map(loan -> BigDecimal.valueOf(loan.getPayableAmount() - loan.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal operatingExpenses = BigDecimal.valueOf(500000); // Mock data
        BigDecimal netIncome = interestIncome.subtract(operatingExpenses);
        
        // Financial ratios (with null safety)
        BigDecimal loanToDepositRatio = (memberDeposits != null && memberDeposits.compareTo(BigDecimal.ZERO) > 0)
                ? outstandingLoans.divide(memberDeposits, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
        
        BigDecimal returnOnAssets = totalAssets.compareTo(BigDecimal.ZERO) > 0
                ? netIncome.divide(totalAssets, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
        
        BigDecimal returnOnEquity = totalEquity.compareTo(BigDecimal.ZERO) > 0
                ? netIncome.divide(totalEquity, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        Map<String, Object> financialSummary = new HashMap<>();
        financialSummary.put("totalAssets", totalAssets);
        financialSummary.put("totalLiabilities", totalLiabilities);
        financialSummary.put("totalEquity", totalEquity);
        financialSummary.put("interestIncome", interestIncome);
        financialSummary.put("operatingExpenses", operatingExpenses);
        financialSummary.put("netIncome", netIncome);
        financialSummary.put("loanToDepositRatio", loanToDepositRatio);
        financialSummary.put("returnOnAssets", returnOnAssets);
        financialSummary.put("returnOnEquity", returnOnEquity);
        
        return financialSummary;
    }
}
