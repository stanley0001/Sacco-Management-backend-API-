-- Fix existing subscriptions with null credit limits
-- Run this ONCE to update existing data

-- Update null credit limits to 0
UPDATE subscriptions 
SET credit_limit = 0 
WHERE credit_limit IS NULL;

-- Update null terms to 0
UPDATE subscriptions 
SET term = 0 
WHERE term IS NULL;

-- Set creditLimitOverridden to false for existing records
UPDATE subscriptions 
SET credit_limit_overridden = false 
WHERE credit_limit_overridden IS NULL;

-- Verify the updates
SELECT 
    COUNT(*) as total_subscriptions,
    COUNT(CASE WHEN credit_limit IS NULL THEN 1 END) as null_credit_limits,
    COUNT(CASE WHEN term IS NULL THEN 1 END) as null_terms,
    COUNT(CASE WHEN credit_limit = 0 THEN 1 END) as zero_credit_limits
FROM subscriptions;

-- Expected: null_credit_limits and null_terms should be 0
