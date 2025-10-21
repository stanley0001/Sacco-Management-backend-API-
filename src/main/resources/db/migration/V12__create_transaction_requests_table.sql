-- Transaction Requests Table
CREATE TABLE IF NOT EXISTS transaction_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    customer_id BIGINT NOT NULL,
    customer_name VARCHAR(255),
    phone_number VARCHAR(20),
    amount DECIMAL(15, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    payment_method VARCHAR(50),
    reference_number VARCHAR(255),
    mpesa_transaction_id BIGINT,
    loan_id BIGINT,
    savings_account_id BIGINT,
    description TEXT,
    failure_reason VARCHAR(500),
    service_provider_response TEXT,
    posted_to_account BOOLEAN DEFAULT FALSE,
    utilized_for_loan BOOLEAN DEFAULT FALSE,
    utilized_loan_id BIGINT,
    initiated_by VARCHAR(255),
    processed_by VARCHAR(255),
    initiated_at TIMESTAMP NOT NULL,
    processed_at TIMESTAMP NULL,
    posted_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_type (type),
    INDEX idx_customer (customer_id),
    INDEX idx_status (status),
    INDEX idx_payment_method (payment_method),
    INDEX idx_reference (reference_number),
    INDEX idx_mpesa_transaction (mpesa_transaction_id),
    INDEX idx_loan (loan_id),
    INDEX idx_savings_account (savings_account_id),
    INDEX idx_created_at (created_at),
    INDEX idx_type_status (type, status),
    INDEX idx_customer_type (customer_id, type),
    INDEX idx_customer_created (customer_id, created_at),
    
    FOREIGN KEY (mpesa_transaction_id) REFERENCES mpesa_transactions(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add comment for documentation
ALTER TABLE transaction_requests COMMENT = 'Tracks all deposit, withdrawal, and disbursement requests with their processing status';
