package com.example.demo.loanManagement.services;

import com.example.demo.loanManagement.parsistence.entities.InterestStrategy;
import com.example.demo.loanManagement.parsistence.entities.Products;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class LoanCalculatorService {

    /**
     * Calculate loan details based on principal, product, and strategy
     */
    public LoanCalculation calculateLoan(double principal, Products product, InterestStrategy strategy) {
        double ratePerPeriod = product.getInterest() / 100.0;
        int term = product.getTerm();
        
        return switch (strategy) {
            case FLAT_RATE -> calculateFlatRate(principal, ratePerPeriod, term);
            case REDUCING_BALANCE -> calculateReducingBalance(principal, ratePerPeriod, term);
            case DECLINING_BALANCE -> calculateDecliningBalance(principal, ratePerPeriod, term);
            case SIMPLE_INTEREST -> calculateSimpleInterest(principal, ratePerPeriod, term);
            case COMPOUND_INTEREST -> calculateCompoundInterest(principal, ratePerPeriod, term);
            case ADD_ON_INTEREST -> calculateAddOnInterest(principal, ratePerPeriod, term);
        };
    }

    /**
     * FLAT RATE: Interest on original principal only
     * Total Interest = Principal × Rate × Term
     */
    private LoanCalculation calculateFlatRate(double principal, double rate, int term) {
        double totalInterest = principal * rate * term;
        double totalAmount = principal + totalInterest;
        double monthlyPayment = totalAmount / term;
        
        LoanCalculation calc = new LoanCalculation();
        calc.setPrincipal(round(principal));
        calc.setInterestRate(rate * 100);
        calc.setTerm(term);
        calc.setTotalInterest(round(totalInterest));
        calc.setTotalAmount(round(totalAmount));
        calc.setMonthlyPayment(round(monthlyPayment));
        calc.setStrategy(InterestStrategy.FLAT_RATE.name());
        calc.setSchedule(generateFlatRateSchedule(principal, rate, term));
        
        return calc;
    }

    /**
     * REDUCING BALANCE: Interest on outstanding balance (most common)
     * Monthly Interest = Outstanding Balance × Rate
     */
    private LoanCalculation calculateReducingBalance(double principal, double rate, int term) {
        double monthlyRate = rate;
        double monthlyPayment = calculateReducingBalancePayment(principal, monthlyRate, term);
        double totalAmount = monthlyPayment * term;
        double totalInterest = totalAmount - principal;
        
        LoanCalculation calc = new LoanCalculation();
        calc.setPrincipal(round(principal));
        calc.setInterestRate(rate * 100);
        calc.setTerm(term);
        calc.setTotalInterest(round(totalInterest));
        calc.setTotalAmount(round(totalAmount));
        calc.setMonthlyPayment(round(monthlyPayment));
        calc.setStrategy(InterestStrategy.REDUCING_BALANCE.name());
        calc.setSchedule(generateReducingBalanceSchedule(principal, monthlyRate, term, monthlyPayment));
        
        return calc;
    }

    /**
     * Calculate equal monthly payment for reducing balance
     * Formula: P × [r × (1 + r)^n] / [(1 + r)^n - 1]
     */
    private double calculateReducingBalancePayment(double principal, double rate, int term) {
        if (rate == 0) return principal / term;
        
        double factor = Math.pow(1 + rate, term);
        return principal * (rate * factor) / (factor - 1);
    }

    /**
     * DECLINING BALANCE: Fixed principal + declining interest
     */
    private LoanCalculation calculateDecliningBalance(double principal, double rate, int term) {
        double fixedPrincipal = principal / term;
        double totalInterest = 0;
        double balance = principal;
        
        for (int i = 0; i < term; i++) {
            double interest = balance * rate;
            totalInterest += interest;
            balance -= fixedPrincipal;
        }
        
        double totalAmount = principal + totalInterest;
        double averagePayment = totalAmount / term;
        
        LoanCalculation calc = new LoanCalculation();
        calc.setPrincipal(round(principal));
        calc.setInterestRate(rate * 100);
        calc.setTerm(term);
        calc.setTotalInterest(round(totalInterest));
        calc.setTotalAmount(round(totalAmount));
        calc.setMonthlyPayment(round(averagePayment));
        calc.setStrategy(InterestStrategy.DECLINING_BALANCE.name());
        calc.setSchedule(generateDecliningBalanceSchedule(principal, rate, term));
        
        return calc;
    }

    /**
     * SIMPLE INTEREST: P × R × T / 100
     */
    private LoanCalculation calculateSimpleInterest(double principal, double rate, int term) {
        double totalInterest = (principal * (rate * 100) * term) / 100;
        double totalAmount = principal + totalInterest;
        double monthlyPayment = totalAmount / term;
        
        LoanCalculation calc = new LoanCalculation();
        calc.setPrincipal(round(principal));
        calc.setInterestRate(rate * 100);
        calc.setTerm(term);
        calc.setTotalInterest(round(totalInterest));
        calc.setTotalAmount(round(totalAmount));
        calc.setMonthlyPayment(round(monthlyPayment));
        calc.setStrategy(InterestStrategy.SIMPLE_INTEREST.name());
        calc.setSchedule(generateSimpleInterestSchedule(principal, rate, term));
        
        return calc;
    }

    /**
     * COMPOUND INTEREST: A = P(1 + r)^n - P
     */
    private LoanCalculation calculateCompoundInterest(double principal, double rate, int term) {
        double totalAmount = principal * Math.pow(1 + rate, term);
        double totalInterest = totalAmount - principal;
        double monthlyPayment = totalAmount / term;
        
        LoanCalculation calc = new LoanCalculation();
        calc.setPrincipal(round(principal));
        calc.setInterestRate(rate * 100);
        calc.setTerm(term);
        calc.setTotalInterest(round(totalInterest));
        calc.setTotalAmount(round(totalAmount));
        calc.setMonthlyPayment(round(monthlyPayment));
        calc.setStrategy(InterestStrategy.COMPOUND_INTEREST.name());
        calc.setSchedule(generateCompoundInterestSchedule(principal, rate, term));
        
        return calc;
    }

    /**
     * ADD-ON INTEREST: Interest added upfront
     */
    private LoanCalculation calculateAddOnInterest(double principal, double rate, int term) {
        double totalInterest = principal * rate * term;
        double totalAmount = principal + totalInterest;
        double monthlyPayment = totalAmount / term;
        
        LoanCalculation calc = new LoanCalculation();
        calc.setPrincipal(round(principal));
        calc.setInterestRate(rate * 100);
        calc.setTerm(term);
        calc.setTotalInterest(round(totalInterest));
        calc.setTotalAmount(round(totalAmount));
        calc.setMonthlyPayment(round(monthlyPayment));
        calc.setStrategy(InterestStrategy.ADD_ON_INTEREST.name());
        calc.setSchedule(generateAddOnInterestSchedule(principal, rate, term));
        
        return calc;
    }

    // Schedule generators
    
    private List<RepaymentScheduleItem> generateFlatRateSchedule(double principal, double rate, int term) {
        List<RepaymentScheduleItem> schedule = new ArrayList<>();
        double totalInterest = principal * rate * term;
        double totalAmount = principal + totalInterest;
        double monthlyPayment = totalAmount / term;
        double principalPerMonth = principal / term;
        double interestPerMonth = totalInterest / term;
        double balance = principal;
        LocalDate date = LocalDate.now().plusMonths(1);
        
        for (int i = 1; i <= term; i++) {
            RepaymentScheduleItem item = new RepaymentScheduleItem();
            item.setInstallmentNumber(i);
            item.setDueDate(date.toString());
            item.setPrincipalAmount(round(principalPerMonth));
            item.setInterestAmount(round(interestPerMonth));
            item.setTotalPayment(round(monthlyPayment));
            item.setBalanceAfterPayment(round(balance - principalPerMonth));
            
            schedule.add(item);
            balance -= principalPerMonth;
            date = date.plusMonths(1);
        }
        
        return schedule;
    }

    private List<RepaymentScheduleItem> generateReducingBalanceSchedule(double principal, double rate, int term, double monthlyPayment) {
        List<RepaymentScheduleItem> schedule = new ArrayList<>();
        double balance = principal;
        LocalDate date = LocalDate.now().plusMonths(1);
        
        for (int i = 1; i <= term; i++) {
            double interest = balance * rate;
            double principalPaid = monthlyPayment - interest;
            
            RepaymentScheduleItem item = new RepaymentScheduleItem();
            item.setInstallmentNumber(i);
            item.setDueDate(date.toString());
            item.setPrincipalAmount(round(principalPaid));
            item.setInterestAmount(round(interest));
            item.setTotalPayment(round(monthlyPayment));
            item.setBalanceAfterPayment(round(balance - principalPaid));
            
            schedule.add(item);
            balance -= principalPaid;
            date = date.plusMonths(1);
        }
        
        return schedule;
    }

    private List<RepaymentScheduleItem> generateDecliningBalanceSchedule(double principal, double rate, int term) {
        List<RepaymentScheduleItem> schedule = new ArrayList<>();
        double fixedPrincipal = principal / term;
        double balance = principal;
        LocalDate date = LocalDate.now().plusMonths(1);
        
        for (int i = 1; i <= term; i++) {
            double interest = balance * rate;
            double totalPayment = fixedPrincipal + interest;
            
            RepaymentScheduleItem item = new RepaymentScheduleItem();
            item.setInstallmentNumber(i);
            item.setDueDate(date.toString());
            item.setPrincipalAmount(round(fixedPrincipal));
            item.setInterestAmount(round(interest));
            item.setTotalPayment(round(totalPayment));
            item.setBalanceAfterPayment(round(balance - fixedPrincipal));
            
            schedule.add(item);
            balance -= fixedPrincipal;
            date = date.plusMonths(1);
        }
        
        return schedule;
    }

    private List<RepaymentScheduleItem> generateSimpleInterestSchedule(double principal, double rate, int term) {
        return generateFlatRateSchedule(principal, rate, term);
    }

    private List<RepaymentScheduleItem> generateCompoundInterestSchedule(double principal, double rate, int term) {
        List<RepaymentScheduleItem> schedule = new ArrayList<>();
        double balance = principal;
        LocalDate date = LocalDate.now().plusMonths(1);
        double totalAmount = principal * Math.pow(1 + rate, term);
        double monthlyPayment = totalAmount / term;
        
        for (int i = 1; i <= term; i++) {
            double interest = balance * rate;
            double principalPaid = monthlyPayment - interest;
            
            RepaymentScheduleItem item = new RepaymentScheduleItem();
            item.setInstallmentNumber(i);
            item.setDueDate(date.toString());
            item.setPrincipalAmount(round(principalPaid));
            item.setInterestAmount(round(interest));
            item.setTotalPayment(round(monthlyPayment));
            item.setBalanceAfterPayment(round(balance - principalPaid));
            
            schedule.add(item);
            balance -= principalPaid;
            date = date.plusMonths(1);
        }
        
        return schedule;
    }

    private List<RepaymentScheduleItem> generateAddOnInterestSchedule(double principal, double rate, int term) {
        return generateFlatRateSchedule(principal, rate, term);
    }

    private double round(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * DTO for loan calculation results
     */
    @Data
    public static class LoanCalculation {
        private double principal;
        private double interestRate;
        private int term;
        private double totalInterest;
        private double totalAmount;
        private double monthlyPayment;
        private String strategy;
        private List<RepaymentScheduleItem> schedule;
    }

    /**
     * DTO for repayment schedule item
     */
    @Data
    public static class RepaymentScheduleItem {
        private int installmentNumber;
        private String dueDate;
        private double principalAmount;
        private double interestAmount;
        private double totalPayment;
        private double balanceAfterPayment;
    }
}
