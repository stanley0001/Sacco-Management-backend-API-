package com.example.demo.reports.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanPortfolioReport {
    private BigDecimal totalLoansOutstanding;
    private BigDecimal totalPrincipalDisbursed;
    private BigDecimal totalInterestEarned;
    private BigDecimal totalArrearsAmount;
    private Long totalLoanAccounts;
    private Long activeLoans;
    private Long completedLoans;
    private Long defaultedLoans;
    private List<ProductBreakdown> productBreakdown;
    private List<LoanAging> agingAnalysis;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductBreakdown {
        private String productName;
        private String productCode;
        private Long numberOfLoans;
        private BigDecimal totalOutstanding;
        private BigDecimal totalDisbursed;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoanAging {
        private String agingBucket;
        private Long numberOfLoans;
        private BigDecimal amount;
        private BigDecimal percentage;
    }
}
