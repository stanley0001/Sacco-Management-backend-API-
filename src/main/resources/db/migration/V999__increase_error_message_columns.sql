-- Increase column sizes for error messages to handle longer error descriptions
-- This fixes: "ERROR: value too long for type character varying(255)"

-- Fix transaction_requests table
ALTER TABLE transaction_requests 
    ALTER COLUMN failure_reason TYPE VARCHAR(1000);

ALTER TABLE transaction_requests 
    ALTER COLUMN service_provider_response TYPE VARCHAR(1000);

-- Fix email/SMS log table
ALTER TABLE email 
    ALTER COLUMN message TYPE VARCHAR(1000);

-- Fix mpesa_transactions table if it has short columns
ALTER TABLE mpesa_transactions 
    ALTER COLUMN result_desc TYPE VARCHAR(1000);

ALTER TABLE mpesa_transactions 
    ALTER COLUMN response_description TYPE VARCHAR(1000);

-- Add index for faster queries on failed transactions
CREATE INDEX IF NOT EXISTS idx_transaction_requests_failure 
    ON transaction_requests(status, failure_reason) 
    WHERE status = 'FAILED';

-- Add index for failed M-PESA transactions
CREATE INDEX IF NOT EXISTS idx_mpesa_transactions_failed 
    ON mpesa_transactions(result_code, result_desc) 
    WHERE result_code != '0';

COMMENT ON COLUMN transaction_requests.failure_reason IS 'Detailed failure reason - increased to 1000 chars to capture full error messages';
COMMENT ON COLUMN email.message IS 'SMS/Email message content - increased to 1000 chars for detailed messages';
