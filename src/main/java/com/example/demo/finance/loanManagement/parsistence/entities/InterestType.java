package com.example.demo.finance.loanManagement.parsistence.entities;

/**
 * Defines how frequently interest is applied to a loan product
 * This works WITH InterestStrategy to determine final calculation
 * 
 * Examples:
 * - REDUCING_BALANCE + PER_MONTH = Monthly reducing balance
 * - SIMPLE_INTEREST + PER_YEAR = Annual simple interest (per annum)
 * - FLAT_RATE + ONCE_TOTAL = One-time flat interest
 * - REDUCING_BALANCE + PER_DAY = Daily reducing balance
 */
public enum InterestType {
    /**
     * Interest applied once for entire loan period (flat rate)
     * Total Interest = Principal × Rate (regardless of term)
     * Example: 10% once on 12-month loan = 10% total
     */
    ONCE_TOTAL("Once (Flat)", "Interest charged once for entire period", 0),
    
    /**
     * Interest applied daily
     * Rate is per day, multiplied by number of days
     * Example: 1% per day for 30 days = 30% total
     */
    PER_DAY("Per Day", "Interest charged daily", 1),
    
    /**
     * Interest applied weekly
     * Rate is per week, multiplied by number of weeks
     * Example: 2% per week for 4 weeks = 8% total
     */
    PER_WEEK("Per Week", "Interest charged weekly", 7),
    
    /**
     * Interest applied monthly (most common)
     * Rate is per month, multiplied by number of months
     * Example: 10% per month for 12 months = 120% total
     */
    PER_MONTH("Per Month", "Interest charged monthly", 30),
    
    /**
     * Interest applied quarterly (every 3 months)
     * Rate is per quarter, multiplied by number of quarters
     * Example: 15% per quarter for 4 quarters = 60% total
     */
    PER_QUARTER("Per Quarter", "Interest charged quarterly", 90),
    
    /**
     * Interest applied annually (per annum - p.a.)
     * Rate is per year, multiplied by number of years
     * Example: 12% per annum for 2 years = 24% total
     */
    PER_YEAR("Per Year (p.a.)", "Interest charged annually", 365);
    
    private final String displayName;
    private final String description;
    private final int daysPerPeriod; // For conversion calculations
    
    InterestType(String displayName, String description, int daysPerPeriod) {
        this.displayName = displayName;
        this.description = description;
        this.daysPerPeriod = daysPerPeriod;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getDaysPerPeriod() {
        return daysPerPeriod;
    }
    
    /**
     * Calculate the multiplier based on loan term and time span
     * For PER_MONTH with 12-month term = 12
     * For PER_YEAR with 24-month term = 2
     * For ONCE_TOTAL = 1
     */
    public double getMultiplier(int term, String timeSpan) {
        if (this == ONCE_TOTAL) {
            return 1.0; // Interest charged once, regardless of term
        }
        
        // Convert term to days based on timeSpan
        int termInDays = convertToDays(term, timeSpan);
        
        // Calculate how many periods fit in the term
        return (double) termInDays / this.daysPerPeriod;
    }
    
    private int convertToDays(int term, String timeSpan) {
        return switch (timeSpan.toUpperCase()) {
            case "DAYS" -> term;
            case "WEEKS" -> term * 7;
            case "MONTHS" -> term * 30;
            case "YEARS" -> term * 365;
            default -> term * 30; // Default to months
        };
    }
}
