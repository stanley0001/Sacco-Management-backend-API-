package com.example.demo.loanManagement.service.interest;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;

/**
 * Interest calculator for ONCE_TOTAL interest type
 * Formula: Total Interest = Principal × Rate
 * 
 * Example:
 * Principal: 100,000
 * Rate: 10% once
 * Term: 12 months
 * Total Interest = 100,000 × 0.10 = 10,000
 * Total Repayable = 110,000
 * Monthly Installment = 9,166.67
 */
@Component
public class OnceTotalInterestCalculator implements InterestCalculator {
    
    @Override
    public BigDecimal calculateInterest(BigDecimal principal, BigDecimal rate, int termMonths) {
        // Convert rate from percentage to decimal (e.g., 10 -> 0.10)
        BigDecimal rateDecimal = rate.divide(BigDecimal.valueOf(100), 4, BigDecimal.ROUND_HALF_UP);
        
        // Interest = Principal × Rate (term is not used for ONCE_TOTAL)
        BigDecimal interest = principal.multiply(rateDecimal);
        
        return interest.setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
