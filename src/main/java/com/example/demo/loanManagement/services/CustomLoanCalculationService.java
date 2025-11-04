package com.example.demo.loanManagement.services;

import com.example.demo.loanManagement.parsistence.entities.CalculationStrategy;
import com.example.demo.loanManagement.parsistence.entities.Products;
import com.example.demo.loanManagement.service.interest.InterestCalculator;
import com.example.demo.loanManagement.service.interest.InterestCalculatorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom loan calculation service for specialized products like Virtucore loans
 * Handles complex interest calculations and repayment schedules
 */
@Service
public class CustomLoanCalculationService {
    
    private final InterestCalculatorFactory interestCalculatorFactory;
    
    @Autowired
    public CustomLoanCalculationService(InterestCalculatorFactory interestCalculatorFactory) {
        this.interestCalculatorFactory = interestCalculatorFactory;
    }

    /**
     * Calculate loan details based on custom calculation strategy
     */
    public LoanCalculationResult calculateLoan(Products product, Double principal, Integer term) {
        if (product.getCalculationStrategy() == null || product.getCalculationStrategy() == CalculationStrategy.STANDARD) {
            return calculateStandardLoan(product, principal, term);
        }
        
        switch (product.getCalculationStrategy()) {
            case VIRTUCORE_1_MONTH:
                return calculateVirtucore1Month(principal);
                
            case VIRTUCORE_5_WEEK:
                return calculateVirtucoreWeekly(principal, 5, 25.0);
                
            case VIRTUCORE_6_WEEK:
                return calculateVirtucoreWeekly(principal, 6, 30.0);
                
            case VIRTUCORE_7_WEEK:
                return calculateVirtucoreWeekly(principal, 7, 35.0);
                
            case VIRTUCORE_8_WEEK:
                return calculateVirtucoreWeekly(principal, 8, 40.0);
                
            case LONG_TERM_15_PERCENT:
                return calculateLongTerm(principal, term, 15.0);
                
            case LONG_TERM_5_PERCENT:
                return calculateLongTerm(principal, term, 5.0);
                
            default:
                return calculateStandardLoan(product, principal, term);
        }
    }
    
    /**
     * Virtucore 1 Month: Principal × 20% + Principal
     * Example: 10,000 × 20% = 2,000 + 10,000 = 12,000 monthly
     */
    private LoanCalculationResult calculateVirtucore1Month(Double principal) {
        BigDecimal principalBd = BigDecimal.valueOf(principal);
        BigDecimal interestRate = BigDecimal.valueOf(20.0);
        
        // Interest = Principal × 20%
        BigDecimal interest = principalBd.multiply(interestRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        // Total = Principal + Interest
        BigDecimal totalAmount = principalBd.add(interest);
        
        // Single monthly payment
        BigDecimal installment = totalAmount;
        
        List<RepaymentSchedule> schedule = new ArrayList<>();
        schedule.add(new RepaymentSchedule(1, installment.doubleValue(), interest.doubleValue(), principal));
        
        return new LoanCalculationResult(
            principal,
            interest.doubleValue(),
            totalAmount.doubleValue(),
            installment.doubleValue(),
            1,
            schedule,
            "MONTHLY"
        );
    }
    
    /**
     * Virtucore Weekly: Principal × Rate%, divided by 4 to get weekly interest, 
     * then multiplied by number of weeks
     * Example (5 weeks, 25%): 10,000 × 25% = 2,500 ÷ 4 = 625 × 5 = 3,125 + 10,000 = 13,125 ÷ 5 = 2,625 weekly
     */
    private LoanCalculationResult calculateVirtucoreWeekly(Double principal, Integer weeks, Double rate) {
        BigDecimal principalBd = BigDecimal.valueOf(principal);
        BigDecimal rateBd = BigDecimal.valueOf(rate);
        BigDecimal weeksBd = BigDecimal.valueOf(weeks);
        
        // Total interest = Principal × Rate%
        BigDecimal totalInterest = principalBd.multiply(rateBd).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        // Interest per week = Total Interest ÷ 4 (weeks in a month)
        BigDecimal interestPerWeek = totalInterest.divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP);
        
        // Adjusted total interest = Interest per week × Number of weeks
        BigDecimal adjustedInterest = interestPerWeek.multiply(weeksBd);
        
        // Total amount = Principal + Adjusted Interest
        BigDecimal totalAmount = principalBd.add(adjustedInterest);
        
        // Weekly installment = Total Amount ÷ Weeks
        BigDecimal weeklyInstallment = totalAmount.divide(weeksBd, 2, RoundingMode.HALF_UP);
        
        // Build repayment schedule
        List<RepaymentSchedule> schedule = new ArrayList<>();
        BigDecimal remainingPrincipal = principalBd;
        BigDecimal principalPerWeek = principalBd.divide(weeksBd, 2, RoundingMode.HALF_UP);
        
        for (int i = 1; i <= weeks; i++) {
            BigDecimal principalPayment = (i == weeks) ? remainingPrincipal : principalPerWeek;
            BigDecimal interestPayment = weeklyInstallment.subtract(principalPayment);
            
            schedule.add(new RepaymentSchedule(
                i, 
                weeklyInstallment.doubleValue(), 
                interestPayment.doubleValue(), 
                principalPayment.doubleValue()
            ));
            
            remainingPrincipal = remainingPrincipal.subtract(principalPayment);
        }
        
        return new LoanCalculationResult(
            principal,
            adjustedInterest.doubleValue(),
            totalAmount.doubleValue(),
            weeklyInstallment.doubleValue(),
            weeks,
            schedule,
            "WEEKLY"
        );
    }
    
    /**
     * Long Term: Monthly reducing balance
     * Example: 15% per month or 5% per month
     */
    private LoanCalculationResult calculateLongTerm(Double principal, Integer months, Double monthlyRate) {
        BigDecimal principalBd = BigDecimal.valueOf(principal);
        BigDecimal rateBd = BigDecimal.valueOf(monthlyRate).divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        
        // Calculate monthly payment using reducing balance formula
        // M = P * [r(1+r)^n] / [(1+r)^n - 1]
        BigDecimal onePlusRate = BigDecimal.ONE.add(rateBd);
        BigDecimal onePlusRatePowN = onePlusRate.pow(months);
        
        BigDecimal numerator = principalBd.multiply(rateBd).multiply(onePlusRatePowN);
        BigDecimal denominator = onePlusRatePowN.subtract(BigDecimal.ONE);
        
        BigDecimal monthlyPayment = numerator.divide(denominator, 2, RoundingMode.HALF_UP);
        
        // Build repayment schedule
        List<RepaymentSchedule> schedule = new ArrayList<>();
        BigDecimal remainingBalance = principalBd;
        BigDecimal totalInterest = BigDecimal.ZERO;
        
        for (int i = 1; i <= months; i++) {
            BigDecimal interestPayment = remainingBalance.multiply(rateBd).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalPayment = monthlyPayment.subtract(interestPayment);
            
            // Adjust last payment for any rounding differences
            if (i == months) {
                principalPayment = remainingBalance;
                monthlyPayment = principalPayment.add(interestPayment);
            }
            
            schedule.add(new RepaymentSchedule(
                i,
                monthlyPayment.doubleValue(),
                interestPayment.doubleValue(),
                principalPayment.doubleValue()
            ));
            
            totalInterest = totalInterest.add(interestPayment);
            remainingBalance = remainingBalance.subtract(principalPayment);
        }
        
        BigDecimal totalAmount = principalBd.add(totalInterest);
        
        return new LoanCalculationResult(
            principal,
            totalInterest.doubleValue(),
            totalAmount.doubleValue(),
            monthlyPayment.doubleValue(),
            months,
            schedule,
            "MONTHLY"
        );
    }
    
    /**
     * Standard loan calculation using product's interest type (PER_MONTH or ONCE_TOTAL)
     */
    private LoanCalculationResult calculateStandardLoan(Products product, Double principal, Integer term) {
        BigDecimal principalBd = BigDecimal.valueOf(principal);
        BigDecimal interestRateBd = BigDecimal.valueOf(product.getInterest());
        
        // Get the appropriate interest calculator based on the product's interest type
        InterestCalculator calculator = interestCalculatorFactory.getCalculator(product.getInterestType());
        
        // Calculate interest using the selected strategy
        BigDecimal interest = calculator.calculateInterest(principalBd, interestRateBd, term);
        BigDecimal totalAmount = principalBd.add(interest);
        BigDecimal installment = calculator.calculateMonthlyInstallment(principalBd, interest, term);
        
        List<RepaymentSchedule> schedule = new ArrayList<>();
        for (int i = 1; i <= term; i++) {
            schedule.add(new RepaymentSchedule(
                i,
                installment.doubleValue(),
                interest.divide(BigDecimal.valueOf(term), 2, RoundingMode.HALF_UP).doubleValue(),
                principal / term
            ));
        }
        
        return new LoanCalculationResult(
            principal,
            interest.doubleValue(),
            totalAmount.doubleValue(),
            installment.doubleValue(),
            term,
            schedule,
            product.getTimeSpan()
        );
    }
    
    /**
     * Calculate total fees for a loan application
     */
    public Double calculateTotalFees(Products product) {
        double total = 0.0;
        if (product.getApplicationFee() != null) total += product.getApplicationFee();
        if (product.getProcessingFee() != null) total += product.getProcessingFee();
        if (product.getInsuranceFee() != null) total += product.getInsuranceFee();
        return total;
    }
    
    /**
     * Calculate disbursement amount after deducting fees
     */
    public Double calculateDisbursementAmount(Products product, Double loanAmount) {
        if (product.getDeductFeesFromAmount()) {
            return loanAmount - calculateTotalFees(product);
        }
        return loanAmount;
    }
    
    // DTOs
    public static class LoanCalculationResult {
        private Double principal;
        private Double totalInterest;
        private Double totalAmount;
        private Double installmentAmount;
        private Integer numberOfInstallments;
        private List<RepaymentSchedule> schedule;
        private String repaymentFrequency;
        
        public LoanCalculationResult(Double principal, Double totalInterest, Double totalAmount, 
                                    Double installmentAmount, Integer numberOfInstallments,
                                    List<RepaymentSchedule> schedule, String repaymentFrequency) {
            this.principal = principal;
            this.totalInterest = totalInterest;
            this.totalAmount = totalAmount;
            this.installmentAmount = installmentAmount;
            this.numberOfInstallments = numberOfInstallments;
            this.schedule = schedule;
            this.repaymentFrequency = repaymentFrequency;
        }
        
        // Getters
        public Double getPrincipal() { return principal; }
        public Double getTotalInterest() { return totalInterest; }
        public Double getTotalAmount() { return totalAmount; }
        public Double getInstallmentAmount() { return installmentAmount; }
        public Integer getNumberOfInstallments() { return numberOfInstallments; }
        public List<RepaymentSchedule> getSchedule() { return schedule; }
        public String getRepaymentFrequency() { return repaymentFrequency; }
    }
    
    public static class RepaymentSchedule {
        private Integer installmentNumber;
        private Double totalPayment;
        private Double interestPayment;
        private Double principalPayment;
        
        public RepaymentSchedule(Integer installmentNumber, Double totalPayment, 
                                Double interestPayment, Double principalPayment) {
            this.installmentNumber = installmentNumber;
            this.totalPayment = totalPayment;
            this.interestPayment = interestPayment;
            this.principalPayment = principalPayment;
        }
        
        // Getters
        public Integer getInstallmentNumber() { return installmentNumber; }
        public Double getTotalPayment() { return totalPayment; }
        public Double getInterestPayment() { return interestPayment; }
        public Double getPrincipalPayment() { return principalPayment; }
    }
}
