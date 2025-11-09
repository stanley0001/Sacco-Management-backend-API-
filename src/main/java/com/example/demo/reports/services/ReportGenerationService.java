package com.example.demo.reports.services;

import com.example.demo.finance.loanManagement.parsistence.entities.LoanAccount;
import com.example.demo.finance.loanManagement.parsistence.repositories.LoanAccountRepo;
import com.example.demo.reports.models.LoanPortfolioReport;
import com.example.demo.reports.models.SASRAReport;
import com.example.demo.finance.savingsManagement.persistence.repositories.SavingsAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportGenerationService {

    private final LoanAccountRepo loanAccountRepo;
    private final SavingsAccountRepository savingsAccountRepo;

    public LoanPortfolioReport generateLoanPortfolioReport(LocalDate startDate, LocalDate endDate) {
        log.info("Generating loan portfolio report for period: {} to {}", startDate, endDate);
        
        List<LoanAccount> allLoans = loanAccountRepo.findAll();
        
        LoanPortfolioReport report = new LoanPortfolioReport();
        
        BigDecimal totalOutstanding = BigDecimal.ZERO;
        BigDecimal totalDisbursed = BigDecimal.ZERO;
        BigDecimal totalInterest = BigDecimal.ZERO;
        BigDecimal totalArrears = BigDecimal.ZERO;
        
        long activeCount = 0;
        long completedCount = 0;
        long defaultedCount = 0;
        
        for (LoanAccount loan : allLoans) {
            totalOutstanding = totalOutstanding.add(BigDecimal.valueOf(loan.getAccountBalance()));
            totalDisbursed = totalDisbursed.add(BigDecimal.valueOf(loan.getAmount()));
            
            BigDecimal interest = BigDecimal.valueOf(loan.getPayableAmount() - loan.getAmount());
            totalInterest = totalInterest.add(interest);
            
            if ("ACTIVE".equalsIgnoreCase(loan.getStatus())) {
                activeCount++;
            } else if ("COMPLETED".equalsIgnoreCase(loan.getStatus())) {
                completedCount++;
            } else if ("DEFAULTED".equalsIgnoreCase(loan.getStatus())) {
                defaultedCount++;
                totalArrears = totalArrears.add(BigDecimal.valueOf(loan.getAccountBalance()));
            }
        }
        
        report.setTotalLoansOutstanding(totalOutstanding);
        report.setTotalPrincipalDisbursed(totalDisbursed);
        report.setTotalInterestEarned(totalInterest);
        report.setTotalArrearsAmount(totalArrears);
        report.setTotalLoanAccounts((long) allLoans.size());
        report.setActiveLoans(activeCount);
        report.setCompletedLoans(completedCount);
        report.setDefaultedLoans(defaultedCount);
        
        // Product breakdown
        report.setProductBreakdown(generateProductBreakdown(allLoans));
        
        // Aging analysis
        report.setAgingAnalysis(generateAgingAnalysis(allLoans));
        
        return report;
    }

    public SASRAReport generateSASRAReport(LocalDate reportDate) {
        log.info("Generating SASRA report for date: {}", reportDate);
        
        SASRAReport report = new SASRAReport();
        report.setReportType("SASRA_COMPREHENSIVE");
        report.setReportDate(reportDate);
        report.setPeriodCovered(reportDate.getMonth() + " " + reportDate.getYear());
        
        // Generate SG3 - Loan Classification
        report.setLoanClassification(generateSG3LoanClassification());
        
        // Generate SG4 - Liquidity
        report.setLiquidity(generateSG4Liquidity());
        
        // Generate SG5 - Capital Adequacy
        report.setCapitalAdequacy(generateSG5CapitalAdequacy());
        
        // Generate Prudential Returns
        report.setPrudentialReturns(generatePrudentialReturns());
        
        return report;
    }

    private SASRAReport.SG3LoanClassification generateSG3LoanClassification() {
        List<LoanAccount> allLoans = loanAccountRepo.findAll();
        
        BigDecimal normal = BigDecimal.ZERO;
        BigDecimal watch = BigDecimal.ZERO;
        BigDecimal subStandard = BigDecimal.ZERO;
        BigDecimal doubtful = BigDecimal.ZERO;
        BigDecimal loss = BigDecimal.ZERO;
        
        for (LoanAccount loan : allLoans) {
            BigDecimal balance = BigDecimal.valueOf(loan.getAccountBalance());
            long daysOverdue = calculateDaysOverdue(loan);
            
            if (daysOverdue <= 0) {
                normal = normal.add(balance);
            } else if (daysOverdue <= 30) {
                watch = watch.add(balance);
            } else if (daysOverdue <= 90) {
                subStandard = subStandard.add(balance);
            } else if (daysOverdue <= 180) {
                doubtful = doubtful.add(balance);
            } else {
                loss = loss.add(balance);
            }
        }
        
        BigDecimal totalGross = normal.add(watch).add(subStandard).add(doubtful).add(loss);
        BigDecimal npl = subStandard.add(doubtful).add(loss);
        BigDecimal nplRatio = totalGross.compareTo(BigDecimal.ZERO) > 0 
                ? npl.divide(totalGross, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
        
        // Calculate provisions (simplified - actual SASRA rates apply)
        BigDecimal provisions = watch.multiply(BigDecimal.valueOf(0.01))
                .add(subStandard.multiply(BigDecimal.valueOf(0.25)))
                .add(doubtful.multiply(BigDecimal.valueOf(0.50)))
                .add(loss.multiply(BigDecimal.valueOf(1.00)));
        
        BigDecimal netLoans = totalGross.subtract(provisions);
        
        SASRAReport.SG3LoanClassification classification = new SASRAReport.SG3LoanClassification();
        classification.setNormalLoans(normal);
        classification.setWatchLoans(watch);
        classification.setSubStandardLoans(subStandard);
        classification.setDoubtfulLoans(doubtful);
        classification.setLossLoans(loss);
        classification.setTotalGrossLoans(totalGross);
        classification.setTotalProvisions(provisions);
        classification.setNetLoans(netLoans);
        classification.setNplRatio(nplRatio);
        
        return classification;
    }

    private SASRAReport.SG4Liquidity generateSG4Liquidity() {
        // Simplified calculation - should be based on actual financial data
        BigDecimal cashAndBank = BigDecimal.valueOf(5000000); // Mock data
        BigDecimal liquidAssets = cashAndBank.add(BigDecimal.valueOf(2000000));
        BigDecimal currentLiabilities = BigDecimal.valueOf(3000000);
        BigDecimal memberDeposits = savingsAccountRepo.getTotalSavingsBalance();
        
        BigDecimal liquidityRatio = currentLiabilities.compareTo(BigDecimal.ZERO) > 0
                ? liquidAssets.divide(currentLiabilities, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
        
        BigDecimal totalLoans = loanAccountRepo.findAll().stream()
                .map(loan -> BigDecimal.valueOf(loan.getAccountBalance()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal loanToDepositRatio = memberDeposits.compareTo(BigDecimal.ZERO) > 0
                ? totalLoans.divide(memberDeposits, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
        
        SASRAReport.SG4Liquidity liquidity = new SASRAReport.SG4Liquidity();
        liquidity.setCashAndBankBalances(cashAndBank);
        liquidity.setLiquidAssets(liquidAssets);
        liquidity.setCurrentLiabilities(currentLiabilities);
        liquidity.setMemberDeposits(memberDeposits);
        liquidity.setLiquidityRatio(liquidityRatio);
        liquidity.setLoanToDepositRatio(loanToDepositRatio);
        
        return liquidity;
    }

    private SASRAReport.SG5CapitalAdequacy generateSG5CapitalAdequacy() {
        // Simplified calculation - should be based on actual balance sheet
        BigDecimal institutionalCapital = BigDecimal.valueOf(10000000); // Mock data
        BigDecimal coreCapital = institutionalCapital;
        BigDecimal totalCapital = coreCapital.add(BigDecimal.valueOf(2000000)); // Including reserves
        
        BigDecimal totalLoans = loanAccountRepo.findAll().stream()
                .map(loan -> BigDecimal.valueOf(loan.getAccountBalance()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Risk-weighted assets (100% for loans as per SASRA)
        BigDecimal riskWeightedAssets = totalLoans;
        
        BigDecimal coreCapitalRatio = riskWeightedAssets.compareTo(BigDecimal.ZERO) > 0
                ? coreCapital.divide(riskWeightedAssets, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
        
        BigDecimal totalCapitalRatio = riskWeightedAssets.compareTo(BigDecimal.ZERO) > 0
                ? totalCapital.divide(riskWeightedAssets, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
        
        SASRAReport.SG5CapitalAdequacy capitalAdequacy = new SASRAReport.SG5CapitalAdequacy();
        capitalAdequacy.setCoreCapital(coreCapital);
        capitalAdequacy.setTotalCapital(totalCapital);
        capitalAdequacy.setRiskWeightedAssets(riskWeightedAssets);
        capitalAdequacy.setCoreCapitalRatio(coreCapitalRatio);
        capitalAdequacy.setTotalCapitalRatio(totalCapitalRatio);
        capitalAdequacy.setInstitutionalCapital(institutionalCapital);
        
        return capitalAdequacy;
    }

    private SASRAReport.PrudentialReturns generatePrudentialReturns() {
        // Simplified - should integrate with accounting module
        SASRAReport.PrudentialReturns returns = new SASRAReport.PrudentialReturns();
        returns.setTotalAssets(BigDecimal.valueOf(50000000));
        returns.setTotalLiabilities(BigDecimal.valueOf(35000000));
        returns.setTotalEquity(BigDecimal.valueOf(15000000));
        returns.setTotalIncome(BigDecimal.valueOf(5000000));
        returns.setTotalExpenses(BigDecimal.valueOf(3000000));
        returns.setNetIncome(BigDecimal.valueOf(2000000));
        returns.setTotalMembers(500);
        returns.setActiveMembers(450);
        returns.setNewMembersThisPeriod(20);
        
        return returns;
    }

    private List<LoanPortfolioReport.ProductBreakdown> generateProductBreakdown(List<LoanAccount> loans) {
        Map<String, List<LoanAccount>> loansByProduct = loans.stream()
                .collect(Collectors.groupingBy(LoanAccount::getLoanref));
        
        List<LoanPortfolioReport.ProductBreakdown> breakdown = new ArrayList<>();
        
        loansByProduct.forEach((productCode, productLoans) -> {
            BigDecimal totalOutstanding = productLoans.stream()
                    .map(loan -> BigDecimal.valueOf(loan.getAccountBalance()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal totalDisbursed = productLoans.stream()
                    .map(loan -> BigDecimal.valueOf(loan.getAmount()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            breakdown.add(new LoanPortfolioReport.ProductBreakdown(
                    productCode, productCode, (long) productLoans.size(), totalOutstanding, totalDisbursed
            ));
        });
        
        return breakdown;
    }

    private List<LoanPortfolioReport.LoanAging> generateAgingAnalysis(List<LoanAccount> loans) {
        List<LoanPortfolioReport.LoanAging> aging = new ArrayList<>();
        
        long current = 0, days30 = 0, days60 = 0, days90 = 0, days180 = 0, daysOver180 = 0;
        BigDecimal amtCurrent = BigDecimal.ZERO, amt30 = BigDecimal.ZERO, amt60 = BigDecimal.ZERO,
                   amt90 = BigDecimal.ZERO, amt180 = BigDecimal.ZERO, amtOver180 = BigDecimal.ZERO;
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (LoanAccount loan : loans) {
            long daysOverdue = calculateDaysOverdue(loan);
            BigDecimal balance = BigDecimal.valueOf(loan.getAccountBalance());
            totalAmount = totalAmount.add(balance);
            
            if (daysOverdue <= 0) {
                current++;
                amtCurrent = amtCurrent.add(balance);
            } else if (daysOverdue <= 30) {
                days30++;
                amt30 = amt30.add(balance);
            } else if (daysOverdue <= 60) {
                days60++;
                amt60 = amt60.add(balance);
            } else if (daysOverdue <= 90) {
                days90++;
                amt90 = amt90.add(balance);
            } else if (daysOverdue <= 180) {
                days180++;
                amt180 = amt180.add(balance);
            } else {
                daysOver180++;
                amtOver180 = amtOver180.add(balance);
            }
        }
        
        aging.add(new LoanPortfolioReport.LoanAging("Current", current, amtCurrent, calculatePercentage(amtCurrent, totalAmount)));
        aging.add(new LoanPortfolioReport.LoanAging("1-30 Days", days30, amt30, calculatePercentage(amt30, totalAmount)));
        aging.add(new LoanPortfolioReport.LoanAging("31-60 Days", days60, amt60, calculatePercentage(amt60, totalAmount)));
        aging.add(new LoanPortfolioReport.LoanAging("61-90 Days", days90, amt90, calculatePercentage(amt90, totalAmount)));
        aging.add(new LoanPortfolioReport.LoanAging("91-180 Days", days180, amt180, calculatePercentage(amt180, totalAmount)));
        aging.add(new LoanPortfolioReport.LoanAging("Over 180 Days", daysOver180, amtOver180, calculatePercentage(amtOver180, totalAmount)));
        
        return aging;
    }

    private long calculateDaysOverdue(LoanAccount loan) {
        if (loan.getDueDate() == null) return 0;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dueDate = loan.getDueDate();
        return ChronoUnit.DAYS.between(dueDate, now);
    }

    private BigDecimal calculatePercentage(BigDecimal amount, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return amount.divide(total, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }
}
