-- SQL Script to Clean Up Duplicate Permissions and Roles
-- Run this if you have duplicate entries in your database

-- ========================================
-- 1. IDENTIFY DUPLICATES
-- ========================================

-- Check for duplicate permissions
SELECT name, COUNT(*) as count
FROM role_permissions
GROUP BY name
HAVING COUNT(*) > 1
ORDER BY count DESC;

-- Check for duplicate roles
SELECT role_name, COUNT(*) as count
FROM roles
GROUP BY role_name
HAVING COUNT(*) > 1
ORDER BY count DESC;

-- Check for duplicate users (by email)
SELECT email, COUNT(*) as count
FROM users
GROUP BY email
HAVING COUNT(*) > 1
ORDER BY count DESC;

-- ========================================
-- 2. DELETE DUPLICATE PERMISSIONS
-- ========================================

-- Keep only the permission with the lowest ID for each name
DELETE FROM role_permissions
WHERE id NOT IN (
    SELECT MIN(id)
    FROM role_permissions
    GROUP BY name
);

-- Verify: Should return 0 rows
SELECT name, COUNT(*) as count
FROM role_permissions
GROUP BY name
HAVING COUNT(*) > 1;

-- ========================================
-- 3. DELETE DUPLICATE ROLES
-- ========================================

-- Keep only the role with the lowest ID for each role_name
DELETE FROM roles
WHERE id NOT IN (
    SELECT MIN(id)
    FROM roles
    GROUP BY role_name
);

-- Verify: Should return 0 rows
SELECT role_name, COUNT(*) as count
FROM roles
GROUP BY role_name
HAVING COUNT(*) > 1;

-- ========================================
-- 4. DELETE DUPLICATE USERS (Optional)
-- ========================================

-- Keep only the user with the lowest ID for each email
DELETE FROM users
WHERE id NOT IN (
    SELECT MIN(id)
    FROM users
    GROUP BY email
);

-- Verify: Should return 0 rows
SELECT email, COUNT(*) as count
FROM users
GROUP BY email
HAVING COUNT(*) > 1;

-- ========================================
-- 5. ADD UNIQUE CONSTRAINTS (Recommended)
-- ========================================

-- Add unique constraint to prevent future duplicates

-- For permissions
ALTER TABLE role_permissions 
ADD CONSTRAINT uk_permission_name UNIQUE (name);

-- For roles
ALTER TABLE roles 
ADD CONSTRAINT uk_role_name UNIQUE (role_name);

-- For users (if not already present)
-- ALTER TABLE users 
-- ADD CONSTRAINT uk_user_email UNIQUE (email);

-- For users username
-- ALTER TABLE users 
-- ADD CONSTRAINT uk_user_username UNIQUE (user_name);

-- ========================================
-- 6. VIEW FINAL COUNTS
-- ========================================

SELECT 
    'Permissions' as table_name,
    COUNT(*) as total_records,
    COUNT(DISTINCT name) as unique_names
FROM role_permissions

UNION ALL

SELECT 
    'Roles' as table_name,
    COUNT(*) as total_records,
    COUNT(DISTINCT role_name) as unique_names
FROM roles

UNION ALL

SELECT 
    'Users' as table_name,
    COUNT(*) as total_records,
    COUNT(DISTINCT email) as unique_emails
FROM users;

-- ========================================
-- NOTES
-- ========================================

/*
USAGE INSTRUCTIONS:

1. **BACKUP YOUR DATABASE FIRST!**
   pg_dump -U your_user -d your_database > backup.sql

2. Run the identification queries (Section 1) to see if you have duplicates

3. If duplicates exist, run the DELETE statements (Sections 2-4)
   - These keep the oldest record (lowest ID) for each duplicate set

4. Add unique constraints (Section 5) to prevent future duplicates
   - Comment out any constraints that already exist

5. Run the final counts (Section 6) to verify cleanup

CAUTION:
- This script will permanently delete duplicate records
- Make sure you have a backup before running
- Test on a development database first
- The script keeps the FIRST (lowest ID) record of each duplicate set

TROUBLESHOOTING:
- If DELETE fails due to foreign key constraints, you may need to:
  1. Drop foreign key constraints temporarily
  2. Run the DELETE
  3. Recreate foreign key constraints
  
- Or update foreign keys to point to the kept record before deleting
*/
