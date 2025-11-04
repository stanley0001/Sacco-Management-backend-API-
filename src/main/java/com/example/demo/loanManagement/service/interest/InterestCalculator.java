package com.example.demo.loanManagement.service.interest;

import java.math.BigDecimal;

/**
 * Strategy interface for calculating loan interest based on different interest types
 */
public interface InterestCalculator {
    
    /**
     * Calculate total interest for a loan
     * 
     * @param principal The loan principal amount
     * @param rate The interest rate (as a percentage, e.g., 10 for 10%)
     * @param termMonths The loan term in months
     * @return Total interest amount
     */
    BigDecimal calculateInterest(BigDecimal principal, BigDecimal rate, int termMonths);
    
    /**
     * Calculate monthly installment amount
     * 
     * @param principal The loan principal amount
     * @param totalInterest The total interest amount
     * @param termMonths The loan term in months
     * @return Monthly installment amount
     */
    default BigDecimal calculateMonthlyInstallment(BigDecimal principal, BigDecimal totalInterest, int termMonths) {
        BigDecimal totalRepayable = principal.add(totalInterest);
        return totalRepayable.divide(BigDecimal.valueOf(termMonths), 2, BigDecimal.ROUND_HALF_UP);
    }
}
