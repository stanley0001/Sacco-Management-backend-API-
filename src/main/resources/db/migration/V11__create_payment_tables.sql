-- M-PESA Transactions Table
CREATE TABLE IF NOT EXISTS mpesa_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_request_id VARCHAR(255) NOT NULL,
    checkout_request_id VARCHAR(255) NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    account_reference VARCHAR(255),
    transaction_desc VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    mpesa_receipt_number VARCHAR(255),
    transaction_date TIMESTAMP NULL,
    callback_received BOOLEAN DEFAULT FALSE,
    callback_response TEXT,
    result_code VARCHAR(10),
    result_desc VARCHAR(500),
    customer_id BIGINT,
    loan_id BIGINT,
    savings_account_id BIGINT,
    initiated_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_merchant_request (merchant_request_id),
    INDEX idx_checkout_request (checkout_request_id),
    INDEX idx_customer (customer_id),
    INDEX idx_loan (loan_id),
    INDEX idx_savings_account (savings_account_id),
    INDEX idx_status (status),
    INDEX idx_phone (phone_number),
    INDEX idx_created_at (created_at),
    INDEX idx_transaction_type (transaction_type),
    INDEX idx_mpesa_receipt (mpesa_receipt_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Payment Methods Table
CREATE TABLE IF NOT EXISTS payment_methods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20),
    bank_account_number VARCHAR(50),
    bank_name VARCHAR(100),
    bank_branch VARCHAR(100),
    is_primary BOOLEAN DEFAULT FALSE,
    is_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_customer (customer_id),
    INDEX idx_type (type),
    INDEX idx_phone (phone_number),
    INDEX idx_bank_account (bank_account_number),
    INDEX idx_primary (customer_id, is_primary),
    INDEX idx_active (customer_id, is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add unique constraint for merchant_request_id
ALTER TABLE mpesa_transactions 
ADD CONSTRAINT uk_merchant_request_id UNIQUE (merchant_request_id);

-- Add comment for documentation
ALTER TABLE mpesa_transactions COMMENT = 'Tracks all M-PESA transactions including STK Push, C2B, and B2C payments';
ALTER TABLE payment_methods COMMENT = 'Stores customer payment methods for M-PESA, bank transfers, and other payment types';
