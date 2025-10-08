package com.example.demo.reports.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SASRAReport {
    private String reportType;
    private LocalDate reportDate;
    private String periodCovered;
    private SG3LoanClassification loanClassification;
    private SG4Liquidity liquidity;
    private SG5CapitalAdequacy capitalAdequacy;
    private PrudentialReturns prudentialReturns;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SG3LoanClassification {
        private BigDecimal normalLoans;
        private BigDecimal watchLoans;
        private BigDecimal subStandardLoans;
        private BigDecimal doubtfulLoans;
        private BigDecimal lossLoans;
        private BigDecimal totalGrossLoans;
        private BigDecimal totalProvisions;
        private BigDecimal netLoans;
        private BigDecimal nplRatio; // Non-Performing Loans Ratio
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SG4Liquidity {
        private BigDecimal cashAndBankBalances;
        private BigDecimal liquidAssets;
        private BigDecimal currentLiabilities;
        private BigDecimal memberDeposits;
        private BigDecimal liquidityRatio;
        private BigDecimal loanToDepositRatio;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SG5CapitalAdequacy {
        private BigDecimal coreCapital;
        private BigDecimal totalCapital;
        private BigDecimal riskWeightedAssets;
        private BigDecimal coreCapitalRatio;
        private BigDecimal totalCapitalRatio;
        private BigDecimal institutionalCapital;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrudentialReturns {
        private BigDecimal totalAssets;
        private BigDecimal totalLiabilities;
        private BigDecimal totalEquity;
        private BigDecimal totalIncome;
        private BigDecimal totalExpenses;
        private BigDecimal netIncome;
        private Integer totalMembers;
        private Integer activeMembers;
        private Integer newMembersThisPeriod;
    }
}
