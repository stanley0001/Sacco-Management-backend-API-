package com.example.demo.loanManagement.parsistence.entities;

import jakarta.persistence.*;

@Entity
public class Products  {
    @Id
    @Column(nullable = false,updatable = false,unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String name;
    @Column(unique = true)
    private String code;
    @Column(name = "is_active")
    private Boolean isActive = true; // Default to active
    private Integer term;
    private Integer interest;
    private Integer maxLimit;
    private Integer minLimit;
    private Boolean topUp;
    private Boolean rollOver;
    private Boolean dailyInterest;
    private Boolean interestUpfront;
    private String transactionType;
    private String timeSpan;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "interest_strategy")
    private InterestStrategy interestStrategy = InterestStrategy.FLAT_RATE;
    
    @Column(name = "allow_early_repayment")
    private Boolean allowEarlyRepayment = true;
    
    @Column(name = "early_repayment_penalty")
    private Double earlyRepaymentPenalty = 0.0;
    
    // Custom Calculation Strategy (for complex products like Virtucore)
    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_strategy")
    private CalculationStrategy calculationStrategy = CalculationStrategy.STANDARD;
    
    // Interest Application Type
    @Enumerated(EnumType.STRING)
    @Column(name = "interest_type")
    private InterestType interestType = InterestType.PER_MONTH; // Default to per month
    
    // Loan Eligibility Configuration
    @Column(name = "eligibility_multiplier")
    private Double eligibilityMultiplier = 3.0; // Default: 3x savings
    
    // Age Restrictions
    @Column(name = "min_age")
    private Integer minAge;
    
    @Column(name = "max_age")
    private Integer maxAge;
    
    // Member Type Restrictions
    @Column(name = "require_check_off")
    private Boolean requireCheckOff = false;
    
    // Share Capital Configuration
    @Column(name = "share_value")
    private Double shareValue;
    
    @Column(name = "min_shares")
    private Integer minShares;
    
    @Column(name = "max_shares")
    private Integer maxShares;
    
    // Fee Configuration
    @Column(name = "application_fee")
    private Double applicationFee = 0.0;
    
    @Column(name = "processing_fee")
    private Double processingFee = 0.0;
    
    @Column(name = "insurance_fee")
    private Double insuranceFee = 0.0;
    
    @Column(name = "deduct_fees_from_amount")
    private Boolean deductFeesFromAmount = false; // If false, require actual payment
    
    @Column(name = "allow_fee_auto_stk")
    private Boolean allowFeeAutoStk = false; // Allow STK push for fees
    
    // MPESA Integration Settings
    @Column(name = "mpesa_enabled")
    private Boolean mpesaEnabled = true;
    
    @Column(name = "auto_payment_enabled")
    private Boolean autoPaymentEnabled = false;
    
    @Column(name = "mpesa_paybill")
    private String mpesaPaybill;
    
    // Loan Process Settings
    @Column(name = "require_application_payment")
    private Boolean requireApplicationPayment = false; // Require payment before approval
    
    @Column(name = "rollover_fee")
    private Double rolloverFee = 0.0;
    
    @Column(name = "allow_interest_waiving")
    private Boolean allowInterestWaiving = true;
    
    @Column(name = "waive_on_early_payment")
    private Boolean waiveOnEarlyPayment = false; // Auto waive future interest on early payment
    
    // Portfolio and Reporting
    @Column(name = "county")
    private String county; // For credit manager view per county
    
    @Column(name = "branch_code")
    private String branchCode;

    public Products() {
    }

    public Products(Long id) {
        this.id = id;
    }

    public Products(String name, String code, Boolean isActive, Integer term, Integer interest, Integer maxLimit, Integer minLimit, Boolean topUp, Boolean rollOver, Boolean dailyInterest, Boolean interestUpfront, String transactionType, String timeSpan) {
        this.name = name;
        this.code = code;
        this.isActive = isActive;
        this.term = term;
        this.interest = interest;
        this.maxLimit = maxLimit;
        this.minLimit = minLimit;
        this.topUp = topUp;
        this.rollOver = rollOver;
        this.dailyInterest = dailyInterest;
        this.interestUpfront = interestUpfront;
        this.transactionType = transactionType;
        this.timeSpan = timeSpan;
    }

    public Products(Long id, String name, String code, Boolean isActive, Integer term, Integer interest, Integer maxLimit, Integer minLimit, Boolean topUp, Boolean rollOver, Boolean dailyInterest, Boolean interestUpfront, String transactionType, String timeSpan) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.isActive = isActive;
        this.term = term;
        this.interest = interest;
        this.maxLimit = maxLimit;
        this.minLimit = minLimit;
        this.topUp = topUp;
        this.rollOver = rollOver;
        this.dailyInterest = dailyInterest;
        this.interestUpfront = interestUpfront;
        this.transactionType = transactionType;
        this.timeSpan = timeSpan;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Integer getTerm() {
        return term;
    }

    public void setTerm(Integer term) {
        this.term = term;
    }

    public Integer getInterest() {
        return interest;
    }

    public void setInterest(Integer interest) {
        this.interest = interest;
    }

    public Integer getMaxLimit() {
        return maxLimit;
    }

    public void setMaxLimit(Integer maxLimit) {
        this.maxLimit = maxLimit;
    }

    public Integer getMinLimit() {
        return minLimit;
    }

    public void setMinLimit(Integer minLimit) {
        this.minLimit = minLimit;
    }

    public Boolean getTopUp() {
        return topUp;
    }

    public void setTopUp(Boolean topUp) {
        this.topUp = topUp;
    }

    public Boolean getRollOver() {
        return rollOver;
    }

    public void setRollOver(Boolean rollOver) {
        this.rollOver = rollOver;
    }

    public Boolean getDailyInterest() {
        return dailyInterest;
    }

    public void setDailyInterest(Boolean dailyInterest) {
        this.dailyInterest = dailyInterest;
    }

    public Boolean getInterestUpfront() {
        return interestUpfront;
    }

    public void setInterestUpfront(Boolean interestUpfront) {
        this.interestUpfront = interestUpfront;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getTimeSpan() {
        return timeSpan;
    }

    public void setTimeSpan(String timeSpan) {
        this.timeSpan = timeSpan;
    }

    public InterestStrategy getInterestStrategy() {
        return interestStrategy;
    }

    public void setInterestStrategy(InterestStrategy interestStrategy) {
        this.interestStrategy = interestStrategy;
    }

    public Boolean getAllowEarlyRepayment() {
        return allowEarlyRepayment;
    }

    public void setAllowEarlyRepayment(Boolean allowEarlyRepayment) {
        this.allowEarlyRepayment = allowEarlyRepayment;
    }

    public Double getEarlyRepaymentPenalty() {
        return earlyRepaymentPenalty;
    }

    public void setEarlyRepaymentPenalty(Double earlyRepaymentPenalty) {
        this.earlyRepaymentPenalty = earlyRepaymentPenalty;
    }

    public CalculationStrategy getCalculationStrategy() {
        return calculationStrategy;
    }

    public void setCalculationStrategy(CalculationStrategy calculationStrategy) {
        this.calculationStrategy = calculationStrategy;
    }

    public Double getApplicationFee() {
        return applicationFee;
    }

    public void setApplicationFee(Double applicationFee) {
        this.applicationFee = applicationFee;
    }

    public Double getProcessingFee() {
        return processingFee;
    }

    public void setProcessingFee(Double processingFee) {
        this.processingFee = processingFee;
    }

    public Double getInsuranceFee() {
        return insuranceFee;
    }

    public void setInsuranceFee(Double insuranceFee) {
        this.insuranceFee = insuranceFee;
    }

    public Boolean getDeductFeesFromAmount() {
        return deductFeesFromAmount;
    }

    public void setDeductFeesFromAmount(Boolean deductFeesFromAmount) {
        this.deductFeesFromAmount = deductFeesFromAmount;
    }

    public Boolean getAllowFeeAutoStk() {
        return allowFeeAutoStk;
    }

    public void setAllowFeeAutoStk(Boolean allowFeeAutoStk) {
        this.allowFeeAutoStk = allowFeeAutoStk;
    }

    public Boolean getMpesaEnabled() {
        return mpesaEnabled;
    }

    public void setMpesaEnabled(Boolean mpesaEnabled) {
        this.mpesaEnabled = mpesaEnabled;
    }

    public Boolean getAutoPaymentEnabled() {
        return autoPaymentEnabled;
    }

    public void setAutoPaymentEnabled(Boolean autoPaymentEnabled) {
        this.autoPaymentEnabled = autoPaymentEnabled;
    }

    public String getMpesaPaybill() {
        return mpesaPaybill;
    }

    public void setMpesaPaybill(String mpesaPaybill) {
        this.mpesaPaybill = mpesaPaybill;
    }

    public Boolean getRequireApplicationPayment() {
        return requireApplicationPayment;
    }

    public void setRequireApplicationPayment(Boolean requireApplicationPayment) {
        this.requireApplicationPayment = requireApplicationPayment;
    }

    public Double getRolloverFee() {
        return rolloverFee;
    }

    public void setRolloverFee(Double rolloverFee) {
        this.rolloverFee = rolloverFee;
    }

    public Boolean getAllowInterestWaiving() {
        return allowInterestWaiving;
    }

    public void setAllowInterestWaiving(Boolean allowInterestWaiving) {
        this.allowInterestWaiving = allowInterestWaiving;
    }

    public Boolean getWaiveOnEarlyPayment() {
        return waiveOnEarlyPayment;
    }

    public void setWaiveOnEarlyPayment(Boolean waiveOnEarlyPayment) {
        this.waiveOnEarlyPayment = waiveOnEarlyPayment;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public InterestType getInterestType() {
        return interestType;
    }

    public void setInterestType(InterestType interestType) {
        this.interestType = interestType;
    }

    public Double getEligibilityMultiplier() {
        return eligibilityMultiplier;
    }

    public void setEligibilityMultiplier(Double eligibilityMultiplier) {
        this.eligibilityMultiplier = eligibilityMultiplier;
    }

    public Integer getMinAge() {
        return minAge;
    }

    public void setMinAge(Integer minAge) {
        this.minAge = minAge;
    }

    public Integer getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }

    public Boolean getRequireCheckOff() {
        return requireCheckOff;
    }

    public void setRequireCheckOff(Boolean requireCheckOff) {
        this.requireCheckOff = requireCheckOff;
    }

    public Double getShareValue() {
        return shareValue;
    }

    public void setShareValue(Double shareValue) {
        this.shareValue = shareValue;
    }

    public Integer getMinShares() {
        return minShares;
    }

    public void setMinShares(Integer minShares) {
        this.minShares = minShares;
    }

    public Integer getMaxShares() {
        return maxShares;
    }

    public void setMaxShares(Integer maxShares) {
        this.maxShares = maxShares;
    }

    @Override
    public String toString() {
        return "Products{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", isActive=" + isActive +
                ", term=" + term +
                ", interest=" + interest +
                ", maxLimit=" + maxLimit +
                ", minLimit=" + minLimit +
                ", topUp=" + topUp +
                ", rollOver=" + rollOver +
                ", dailyInterest=" + dailyInterest +
                ", interestUpfront=" + interestUpfront +
                ", transactionType='" + transactionType + '\'' +
                ", timeSpan='" + timeSpan + '\'' +
                '}';
    }
}
