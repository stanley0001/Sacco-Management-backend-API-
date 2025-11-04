-- Fix for loan_repayment_schedules status constraint error
-- This script drops the old constraint and recreates it with correct enum values

-- 1. Drop the existing constraint
ALTER TABLE loan_repayment_schedules 
DROP CONSTRAINT IF EXISTS loan_repayment_schedules_status_check;

-- 2. Ensure the status column is VARCHAR type (not INTEGER)
ALTER TABLE loan_repayment_schedules 
ALTER COLUMN status TYPE VARCHAR(20) USING status::VARCHAR;

-- 3. Add the correct check constraint with enum values
ALTER TABLE loan_repayment_schedules 
ADD CONSTRAINT loan_repayment_schedules_status_check 
CHECK (status IN ('CURRENT', 'PAID', 'DEFAULT', 'REVERSED', 'OVERDUE', 'PENDING'));

-- 4. Verify the constraint
SELECT conname, pg_get_constraintdef(oid) 
FROM pg_constraint 
WHERE conrelid = 'loan_repayment_schedules'::regclass 
AND conname = 'loan_repayment_schedules_status_check';

-- Expected result:
-- loan_repayment_schedules_status_check | CHECK ((status)::text = ANY (ARRAY[('CURRENT'::character varying)::text, ('PAID'::character varying)::text, ('DEFAULT'::character varying)::text, ('REVERSED'::character varying)::text, ('OVERDUE'::character varying)::text, ('PENDING'::character varying)::text]))
