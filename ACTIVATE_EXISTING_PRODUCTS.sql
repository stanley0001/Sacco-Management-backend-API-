-- Script to activate all existing products in the database
-- Run this SQL script in your MySQL database to set all products to active

-- Update all existing products to be active (if they are currently NULL or false)
UPDATE products 
SET is_active = true 
WHERE is_active IS NULL OR is_active = false;

-- Verify the update
SELECT 
    id,
    name,
    code,
    is_active,
    transaction_type,
    interest,
    term
FROM products
ORDER BY id;

-- Count active vs inactive products
SELECT 
    is_active,
    COUNT(*) as count
FROM products
GROUP BY is_active;
