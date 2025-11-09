package com.example.demo.finance.loanManagement.parsistence.entities;

/**
 * Custom calculation strategies for specialized loan products
 * Supports complex interest calculations like Virtucore loans
 */
public enum CalculationStrategy {
    /**
     * Standard calculation using the selected InterestStrategy
     */
    STANDARD("Standard", "Uses the standard interest strategy"),
    
    /**
     * Virtucore 1 Month: Principal × 20% + Principal, Monthly payment
     * Example: 10,000 × 20% = 2,000 + 10,000 = 12,000 monthly
     */
    VIRTUCORE_1_MONTH("Virtucore 1 Month", "Principal × 20% + Principal (Monthly)"),
    
    /**
     * Virtucore 5 Week: Principal × 25%, divided by 4 to get weekly interest, 
     * then multiplied by 5 weeks
     * Example: 10,000 × 25% = 2,500 ÷ 4 = 625 per week × 5 = 3,125 + 10,000 = 13,125 ÷ 5 = 2,625 weekly
     */
    VIRTUCORE_5_WEEK("Virtucore 5 Week", "Weekly installments with 25% total interest"),
    
    /**
     * Virtucore 6 Week: Principal × 30%, same formula as 5 weeks
     * Example: 10,000 × 30% = 3,000 ÷ 4 = 750 per week × 6 = 4,500 + 10,000 = 14,500 ÷ 6 = 2,417 weekly
     */
    VIRTUCORE_6_WEEK("Virtucore 6 Week", "Weekly installments with 30% total interest"),
    
    /**
     * Virtucore 7 Week: Principal × 35%
     * Example: 10,000 × 35% = 3,500 ÷ 4 = 875 per week × 7 = 6,125 + 10,000 = 16,125 ÷ 7 = 2,304 weekly
     */
    VIRTUCORE_7_WEEK("Virtucore 7 Week", "Weekly installments with 35% total interest"),
    
    /**
     * Virtucore 8 Week: Principal × 40%
     * Example: 10,000 × 40% = 4,000 ÷ 4 = 1,000 per week × 8 = 8,000 + 10,000 = 18,000 ÷ 8 = 2,250 weekly
     */
    VIRTUCORE_8_WEEK("Virtucore 8 Week", "Weekly installments with 40% total interest"),
    
    /**
     * Long Term 15%: 15% per month reducing balance
     */
    LONG_TERM_15_PERCENT("Long Term 15%", "15% per month interest"),
    
    /**
     * Long Term 5%: 5% per month reducing balance
     */
    LONG_TERM_5_PERCENT("Long Term 5%", "5% per month interest");
    
    private final String displayName;
    private final String description;
    
    CalculationStrategy(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}
