package com.example.demo.reports.models;

public enum ReportType {
    // Loan Reports
    LOAN_PORTFOLIO,
    LOAN_ARREARS,
    LOAN_DISBURSEMENTS,
    LOAN_REPAYMENTS,
    LOAN_AGING_ANALYSIS,
    
    // Savings Reports
    SAVINGS_BALANCES,
    SAVINGS_TRANSACTIONS,
    DORMANT_ACCOUNTS,
    
    // Customer Reports
    CUSTOMER_LIST,
    CUSTOMER_STATEMENTS,
    
    // Financial Reports
    TRIAL_BALANCE,
    INCOME_STATEMENT,
    BALANCE_SHEET,
    CASH_FLOW,
    
    // SASRA Reports
    SASRA_PRUDENTIAL_RETURNS,
    SASRA_SG3_LOAN_CLASSIFICATION,
    SASRA_SG4_LIQUIDITY,
    SASRA_SG5_CAPITAL_ADEQUACY,
    SASRA_MONTHLY_RETURNS
}
