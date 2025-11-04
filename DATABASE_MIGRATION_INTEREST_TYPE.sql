-- ================================================
-- DATABASE MIGRATION: Add Interest Type Support
-- ================================================
-- Date: October 30, 2025
-- Purpose: Add interest_type column to products table
-- Related Feature: Configurable Interest Frequency System

-- Step 1: Add interest_type column with default value
ALTER TABLE products 
ADD COLUMN interest_type VARCHAR(20) DEFAULT 'PER_MONTH' NOT NULL;

-- Step 2: Add comment for documentation
COMMENT ON COLUMN products.interest_type IS 'Interest calculation type: PER_MONTH (recurring) or ONCE_TOTAL (flat)';

-- Step 3: Update existing products (IMPORTANT: Review before running)
-- Option A: Set all existing products to PER_MONTH (safest - maintains current behavior)
UPDATE products 
SET interest_type = 'PER_MONTH' 
WHERE interest_type IS NULL OR interest_type = '';

-- Option B: Set based on interest rate (uncomment if needed)
-- Low interest rates might indicate flat-rate products
-- UPDATE products 
-- SET interest_type = 'ONCE_TOTAL' 
-- WHERE interest <= 5 AND (interest_type IS NULL OR interest_type = '');

-- Step 4: Add index for performance
CREATE INDEX idx_products_interest_type ON products(interest_type);

-- Step 5: Add check constraint to ensure valid values
ALTER TABLE products
ADD CONSTRAINT chk_interest_type 
CHECK (interest_type IN ('PER_MONTH', 'ONCE_TOTAL'));

-- ================================================
-- VERIFICATION QUERIES
-- ================================================

-- Verify column was added
SELECT column_name, data_type, column_default, is_nullable
FROM information_schema.columns
WHERE table_name = 'products' AND column_name = 'interest_type';

-- Check distribution of interest types
SELECT interest_type, COUNT(*) as product_count
FROM products
GROUP BY interest_type;

-- View sample products with new field
SELECT id, name, code, interest, term, interest_type
FROM products
LIMIT 10;

-- ================================================
-- ROLLBACK SCRIPT (Use only if needed to revert)
-- ================================================

-- DROP INDEX idx_products_interest_type;
-- ALTER TABLE products DROP CONSTRAINT chk_interest_type;
-- ALTER TABLE products DROP COLUMN interest_type;

-- ================================================
-- NOTES
-- ================================================

/*
INTEREST TYPE DEFINITIONS:

1. PER_MONTH (Recurring Interest)
   - Interest is charged every month
   - Formula: Total Interest = Principal × Rate × Term (months)
   - Example: 100,000 @ 10% for 12 months = 120,000 interest
   - Use Case: Traditional loans, credit facilities
   
2. ONCE_TOTAL (Flat Interest)
   - Interest is charged once for entire loan period
   - Formula: Total Interest = Principal × Rate
   - Example: 100,000 @ 10% for 12 months = 10,000 interest
   - Use Case: Simple loans, payday loans, short-term facilities

MIGRATION STRATEGY:
- All existing products default to PER_MONTH to maintain current behavior
- Review each product type to determine if ONCE_TOTAL is more appropriate
- Update specific products manually or via targeted UPDATE statements
- Test calculations after migration

TESTING STEPS:
1. Run migration on TEST database first
2. Verify all existing products have interest_type set
3. Create test loan calculations for both types
4. Compare results with manual calculations
5. Test in staging environment
6. Deploy to production during low-traffic period
7. Monitor for calculation errors
8. Keep rollback script ready for 24 hours

DEPENDENCIES:
- Backend: InterestType.java enum must be deployed
- Backend: Interest calculator classes must be deployed
- Backend: CustomLoanCalculationService must be updated
- Frontend: Product interface must include interestType field
- Frontend: Product creation form must have dropdown
*/
