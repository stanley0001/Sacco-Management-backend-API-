package com.example.demo.finance.loanManagement.service.interest;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;

/**
 * Interest calculator for PER_MONTH interest type
 * Formula: Total Interest = Principal × Rate × Term (in months)
 * 
 * Example:
 * Principal: 100,000
 * Rate: 10% per month
 * Term: 12 months
 * Total Interest = 100,000 × 0.10 × 12 = 120,000
 * Total Repayable = 220,000
 * Monthly Installment = 18,333.33
 */
@Component
public class PerMonthInterestCalculator implements InterestCalculator {
    
    @Override
    public BigDecimal calculateInterest(BigDecimal principal, BigDecimal rate, int termMonths) {
        // Convert rate from percentage to decimal (e.g., 10 -> 0.10)
        BigDecimal rateDecimal = rate.divide(BigDecimal.valueOf(100), 4, BigDecimal.ROUND_HALF_UP);
        
        // Interest = Principal × Rate × Term
        BigDecimal interest = principal
                .multiply(rateDecimal)
                .multiply(BigDecimal.valueOf(termMonths));
        
        return interest.setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
