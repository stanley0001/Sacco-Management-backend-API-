-- ================================================
-- WAHUDUMU SACCO PRODUCTS SEED DATA
-- ================================================
-- This script creates all saving and loan products for Wahudumu SACCO
-- Run this after ensuring the products table has all necessary columns

-- ================================================
-- SAVING PRODUCTS
-- ================================================

-- 1. Share Capital
INSERT INTO products (
    name, code, transaction_type, term, time_span, interest, 
    max_limit, min_limit, is_active, daily_interest, interest_upfront,
    top_up, roll_over, interest_strategy, calculation_strategy, interest_type,
    application_fee, processing_fee, insurance_fee, deduct_fees_from_amount,
    allow_fee_auto_stk, mpesa_enabled, auto_payment_enabled, require_application_payment,
    rollover_fee, allow_interest_waiving, waive_on_early_payment, county, branch_code,
    allow_early_repayment, early_repayment_penalty
) VALUES (
    'Share Capital', 'SHARE_CAP', 'SAVINGS', 12, 'MONTHS', 0,
    100000, 50000, true, false, false,
    false, false, 'SIMPLE_INTEREST', 'STANDARD', 'PER_YEAR',
    0, 0, 0, false,
    false, true, false, false,
    0, true, false, '', '',
    true, 0
);

-- 2. Normal Deposit
INSERT INTO products (
    name, code, transaction_type, term, time_span, interest, 
    max_limit, min_limit, is_active, daily_interest, interest_upfront,
    top_up, roll_over, interest_strategy, calculation_strategy, interest_type,
    application_fee, processing_fee, insurance_fee, deduct_fees_from_amount,
    allow_fee_auto_stk, mpesa_enabled, auto_payment_enabled, require_application_payment,
    rollover_fee, allow_interest_waiving, waive_on_early_payment, county, branch_code,
    allow_early_repayment, early_repayment_penalty
) VALUES (
    'Normal Deposit', 'NORM_DEP', 'SAVINGS', 12, 'MONTHS', 0,
    999999999, 500, true, false, false,
    false, false, 'SIMPLE_INTEREST', 'STANDARD', 'PER_YEAR',
    0, 0, 0, false,
    false, true, false, false,
    0, true, false, '', '',
    true, 0
);

-- 3. Junior Saving (Age restriction: below 18 years)
INSERT INTO products (
    name, code, transaction_type, term, time_span, interest, 
    max_limit, min_limit, is_active, daily_interest, interest_upfront,
    top_up, roll_over, interest_strategy, calculation_strategy, interest_type,
    application_fee, processing_fee, insurance_fee, deduct_fees_from_amount,
    allow_fee_auto_stk, mpesa_enabled, auto_payment_enabled, require_application_payment,
    rollover_fee, allow_interest_waiving, waive_on_early_payment, county, branch_code,
    allow_early_repayment, early_repayment_penalty
) VALUES (
    'Junior Saving', 'JUN_SAV', 'SAVINGS', 12, 'MONTHS', 0,
    999999999, 0, true, false, false,
    false, false, 'SIMPLE_INTEREST', 'STANDARD', 'PER_YEAR',
    0, 0, 0, false,
    false, true, false, false,
    0, true, false, '', '',
    true, 0
);

-- ================================================
-- LOAN PRODUCTS
-- ================================================

-- 1. Normal Loans (14.4% reducing balance, 108 months, 3x savings)
INSERT INTO products (
    name, code, transaction_type, term, time_span, interest, 
    max_limit, min_limit, is_active, daily_interest, interest_upfront,
    top_up, roll_over, interest_strategy, calculation_strategy, interest_type,
    application_fee, processing_fee, insurance_fee, deduct_fees_from_amount,
    allow_fee_auto_stk, mpesa_enabled, auto_payment_enabled, require_application_payment,
    rollover_fee, allow_interest_waiving, waive_on_early_payment, county, branch_code,
    allow_early_repayment, early_repayment_penalty
) VALUES (
    'Normal Loan', 'NORM_LOAN', 'LOAN', 108, 'MONTHS', 14.4,
    999999999, 1000, true, false, false,
    false, false, 'REDUCING_BALANCE', 'STANDARD', 'PER_MONTH',
    0, 0, 0, false,
    false, true, false, false,
    0, true, false, '', '',
    true, 0
);

-- 2. Emergency Loans (14.4% reducing balance, 18 months, 3x savings)
INSERT INTO products (
    name, code, transaction_type, term, time_span, interest, 
    max_limit, min_limit, is_active, daily_interest, interest_upfront,
    top_up, roll_over, interest_strategy, calculation_strategy, interest_type,
    application_fee, processing_fee, insurance_fee, deduct_fees_from_amount,
    allow_fee_auto_stk, mpesa_enabled, auto_payment_enabled, require_application_payment,
    rollover_fee, allow_interest_waiving, waive_on_early_payment, county, branch_code,
    allow_early_repayment, early_repayment_penalty
) VALUES (
    'Emergency Loan', 'EMER_LOAN', 'LOAN', 18, 'MONTHS', 14.4,
    999999999, 1000, true, false, false,
    false, false, 'REDUCING_BALANCE', 'STANDARD', 'PER_MONTH',
    0, 0, 0, false,
    false, true, false, false,
    0, true, false, '', '',
    true, 0
);

-- 3. Development Loans (14.4% reducing balance, 108 months, 3x savings)
INSERT INTO products (
    name, code, transaction_type, term, time_span, interest, 
    max_limit, min_limit, is_active, daily_interest, interest_upfront,
    top_up, roll_over, interest_strategy, calculation_strategy, interest_type,
    application_fee, processing_fee, insurance_fee, deduct_fees_from_amount,
    allow_fee_auto_stk, mpesa_enabled, auto_payment_enabled, require_application_payment,
    rollover_fee, allow_interest_waiving, waive_on_early_payment, county, branch_code,
    allow_early_repayment, early_repayment_penalty
) VALUES (
    'Development Loan', 'DEV_LOAN', 'LOAN', 108, 'MONTHS', 14.4,
    999999999, 1000, true, false, false,
    false, false, 'REDUCING_BALANCE', 'STANDARD', 'PER_MONTH',
    0, 0, 0, false,
    false, true, false, false,
    0, true, false, '', '',
    true, 0
);

-- 4. Top Up Loans (14.4% reducing balance, 108 months, 3x savings)
INSERT INTO products (
    name, code, transaction_type, term, time_span, interest, 
    max_limit, min_limit, is_active, daily_interest, interest_upfront,
    top_up, roll_over, interest_strategy, calculation_strategy, interest_type,
    application_fee, processing_fee, insurance_fee, deduct_fees_from_amount,
    allow_fee_auto_stk, mpesa_enabled, auto_payment_enabled, require_application_payment,
    rollover_fee, allow_interest_waiving, waive_on_early_payment, county, branch_code,
    allow_early_repayment, early_repayment_penalty
) VALUES (
    'Top Up Loan', 'TOPUP_LOAN', 'LOAN', 108, 'MONTHS', 14.4,
    999999999, 1000, true, false, false,
    true, false, 'REDUCING_BALANCE', 'STANDARD', 'PER_MONTH',
    0, 0, 0, false,
    false, true, false, false,
    0, true, false, '', '',
    true, 0
);

-- 5. Education Loan (14.4% reducing, 18 months, 3x savings)
INSERT INTO products (
    name, code, transaction_type, term, time_span, interest, 
    max_limit, min_limit, is_active, daily_interest, interest_upfront,
    top_up, roll_over, interest_strategy, calculation_strategy, interest_type,
    application_fee, processing_fee, insurance_fee, deduct_fees_from_amount,
    allow_fee_auto_stk, mpesa_enabled, auto_payment_enabled, require_application_payment,
    rollover_fee, allow_interest_waiving, waive_on_early_payment, county, branch_code,
    allow_early_repayment, early_repayment_penalty
) VALUES (
    'Education Loan', 'EDU_LOAN', 'LOAN', 18, 'MONTHS', 14.4,
    999999999, 1000, true, false, false,
    false, false, 'REDUCING_BALANCE', 'STANDARD', 'PER_MONTH',
    0, 0, 0, false,
    false, true, false, false,
    0, true, false, '', '',
    true, 0
);

-- 6. Asset Based Loan (15% straight line, 24 months, 3x savings, max 150,000)
INSERT INTO products (
    name, code, transaction_type, term, time_span, interest, 
    max_limit, min_limit, is_active, daily_interest, interest_upfront,
    top_up, roll_over, interest_strategy, calculation_strategy, interest_type,
    application_fee, processing_fee, insurance_fee, deduct_fees_from_amount,
    allow_fee_auto_stk, mpesa_enabled, auto_payment_enabled, require_application_payment,
    rollover_fee, allow_interest_waiving, waive_on_early_payment, county, branch_code,
    allow_early_repayment, early_repayment_penalty
) VALUES (
    'Asset Based Loan', 'ASSET_LOAN', 'LOAN', 24, 'MONTHS', 15,
    150000, 1000, true, false, false,
    false, false, 'FLAT_RATE', 'STANDARD', 'ONCE_TOTAL',
    0, 0, 0, false,
    false, true, false, false,
    0, true, false, '', '',
    true, 0
);

-- 7. Boresha Maisha Loan (15% straight line, 18 months, 3x savings, max 100,000)
INSERT INTO products (
    name, code, transaction_type, term, time_span, interest, 
    max_limit, min_limit, is_active, daily_interest, interest_upfront,
    top_up, roll_over, interest_strategy, calculation_strategy, interest_type,
    application_fee, processing_fee, insurance_fee, deduct_fees_from_amount,
    allow_fee_auto_stk, mpesa_enabled, auto_payment_enabled, require_application_payment,
    rollover_fee, allow_interest_waiving, waive_on_early_payment, county, branch_code,
    allow_early_repayment, early_repayment_penalty
) VALUES (
    'Boresha Maisha Loan', 'BORESHA_LOAN', 'LOAN', 18, 'MONTHS', 15,
    100000, 1000, true, false, false,
    false, false, 'FLAT_RATE', 'STANDARD', 'ONCE_TOTAL',
    0, 0, 0, false,
    false, true, false, false,
    0, true, false, '', '',
    true, 0
);

-- 8. Salary Advance Loan (10% straight line, 6 months, max 20,000, check-off only)
INSERT INTO products (
    name, code, transaction_type, term, time_span, interest, 
    max_limit, min_limit, is_active, daily_interest, interest_upfront,
    top_up, roll_over, interest_strategy, calculation_strategy, interest_type,
    application_fee, processing_fee, insurance_fee, deduct_fees_from_amount,
    allow_fee_auto_stk, mpesa_enabled, auto_payment_enabled, require_application_payment,
    rollover_fee, allow_interest_waiving, waive_on_early_payment, county, branch_code,
    allow_early_repayment, early_repayment_penalty
) VALUES (
    'Salary Advance Loan', 'SAL_ADV', 'LOAN', 6, 'MONTHS', 10,
    20000, 1000, true, false, false,
    false, false, 'FLAT_RATE', 'STANDARD', 'ONCE_TOTAL',
    0, 0, 0, false,
    false, true, true, false,
    0, true, false, '', '',
    true, 0
);

-- 9. Quickfix Loan (14%, 2 months, 3x savings, max 5,000)
INSERT INTO products (
    name, code, transaction_type, term, time_span, interest, 
    max_limit, min_limit, is_active, daily_interest, interest_upfront,
    top_up, roll_over, interest_strategy, calculation_strategy, interest_type,
    application_fee, processing_fee, insurance_fee, deduct_fees_from_amount,
    allow_fee_auto_stk, mpesa_enabled, auto_payment_enabled, require_application_payment,
    rollover_fee, allow_interest_waiving, waive_on_early_payment, county, branch_code,
    allow_early_repayment, early_repayment_penalty
) VALUES (
    'Quickfix Loan', 'QUICK_FIX', 'LOAN', 2, 'MONTHS', 14,
    5000, 500, true, false, false,
    false, false, 'FLAT_RATE', 'STANDARD', 'ONCE_TOTAL',
    0, 0, 0, false,
    false, true, false, false,
    0, true, false, '', '',
    true, 0
);

-- ================================================
-- VERIFICATION QUERIES
-- ================================================

-- Verify all products created
SELECT name, code, transaction_type, interest, max_limit, interest_strategy, interest_type, term, time_span
FROM products
WHERE code IN (
    'SHARE_CAP', 'NORM_DEP', 'JUN_SAV',
    'NORM_LOAN', 'EMER_LOAN', 'DEV_LOAN', 'TOPUP_LOAN', 
    'EDU_LOAN', 'ASSET_LOAN', 'BORESHA_LOAN', 'SAL_ADV', 'QUICK_FIX'
)
ORDER BY transaction_type, name;
