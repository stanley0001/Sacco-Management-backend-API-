-- Dummy Data for SACCO Management System
-- Run this after initial schema creation

-- Insert Loan Products
INSERT INTO products (id, name, code, is_active, term, interest, max_limit, min_limit, top_up, roll_over, daily_interest, interest_upfront, transaction_type, time_span, interest_strategy, allow_early_repayment, early_repayment_penalty) VALUES
(1, 'Quick Loan', 'QL001', true, 6, 10, 50000, 5000, true, false, false, false, 'LOAN', 'MONTHS', 'REDUCING_BALANCE', true, 0.0),
(2, 'Emergency Loan', 'EL001', true, 12, 12, 100000, 10000, true, false, false, false, 'LOAN', 'MONTHS', 'REDUCING_BALANCE', true, 2.0),
(3, 'Development Loan', 'DL001', true, 24, 15, 500000, 50000, false, false, false, false, 'LOAN', 'MONTHS', 'FLAT_RATE', true, 3.0),
(4, 'Education Loan', 'ED001', true, 36, 10, 300000, 20000, false, false, false, false, 'LOAN', 'MONTHS', 'REDUCING_BALANCE', true, 0.0),
(5, 'Business Loan', 'BL001', true, 48, 18, 1000000, 100000, true, false, false, false, 'LOAN', 'MONTHS', 'DECLINING_BALANCE', true, 5.0),
(6, 'Asset Finance', 'AF001', true, 60, 16, 2000000, 50000, false, false, false, false, 'LOAN', 'MONTHS', 'REDUCING_BALANCE', true, 4.0),
(7, 'Salary Advance', 'SA001', true, 3, 8, 50000, 5000, true, false, false, true, 'LOAN', 'MONTHS', 'FLAT_RATE', false, 0.0),
(8, 'Refinance Loan', 'RF001', true, 36, 14, 500000, 100000, false, false, false, false, 'LOAN', 'MONTHS', 'REDUCING_BALANCE', true, 2.5),
(9, 'Agricultural Loan', 'AG001', true, 24, 12, 1000000, 50000, false, false, false, false, 'LOAN', 'MONTHS', 'REDUCING_BALANCE', true, 3.0),
(10, 'Housing Loan', 'HL001', true, 240, 15, 5000000, 500000, false, false, false, false, 'LOAN', 'MONTHS', 'REDUCING_BALANCE', true, 5.0);

-- Insert Sample Customers (50 members)
INSERT INTO customer (id, first_name, last_name, phone_number, email, document_number, member_number, status, pin_hash, failed_pin_attempts, created_at) VALUES
(1, 'John', 'Kamau', '254712345678', 'john.kamau@email.com', '12345678', 'MEM001', 'ACTIVE', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyg0uO.JqI4m', 0, NOW()),
(2, 'Mary', 'Wanjiku', '254723456789', 'mary.wanjiku@email.com', '23456789', 'MEM002', 'ACTIVE', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyg0uO.JqI4m', 0, NOW()),
(3, 'Peter', 'Omondi', '254734567890', 'peter.omondi@email.com', '34567890', 'MEM003', 'ACTIVE', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyg0uO.JqI4m', 0, NOW()),
(4, 'Grace', 'Njeri', '254745678901', 'grace.njeri@email.com', '45678901', 'MEM004', 'ACTIVE', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyg0uO.JqI4m', 0, NOW()),
(5, 'David', 'Kiplagat', '254756789012', 'david.kiplagat@email.com', '56789012', 'MEM005', 'ACTIVE', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyg0uO.JqI4m', 0, NOW()),
(6, 'Sarah', 'Akinyi', '254767890123', 'sarah.akinyi@email.com', '67890123', 'MEM006', 'ACTIVE', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyg0uO.JqI4m', 0, NOW()),
(7, 'James', 'Mwangi', '254778901234', 'james.mwangi@email.com', '78901234', 'MEM007', 'ACTIVE', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyg0uO.JqI4m', 0, NOW()),
(8, 'Elizabeth', 'Chebet', '254789012345', 'elizabeth.chebet@email.com', '89012345', 'MEM008', 'ACTIVE', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyg0uO.JqI4m', 0, NOW()),
(9, 'Michael', 'Otieno', '254790123456', 'michael.otieno@email.com', '90123456', 'MEM009', 'ACTIVE', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyg0uO.JqI4m', 0, NOW()),
(10, 'Jane', 'Wambui', '254701234567', 'jane.wambui@email.com', '01234567', 'MEM010', 'ACTIVE', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyg0uO.JqI4m', 0, NOW()),
(11, 'Robert', 'Mutua', '254712456789', 'robert.mutua@email.com', '12456789', 'MEM011', 'ACTIVE', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyg0uO.JqI4m', 0, NOW()),
(12, 'Lucy', 'Nyambura', '254723567890', 'lucy.nyambura@email.com', '23567890', 'MEM012', 'ACTIVE', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyg0uO.JqI4m', 0, NOW()),
(13, 'Patrick', 'Kiptoo', '254734678901', 'patrick.kiptoo@email.com', '34678901', 'MEM013', 'ACTIVE', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyg0uO.JqI4m', 0, NOW()),
(14, 'Ann', 'Muthoni', '254745789012', 'ann.muthoni@email.com', '45789012', 'MEM014', 'ACTIVE', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyg0uO.JqI4m', 0, NOW()),
(15, 'Samuel', 'Wekesa', '254756890123', 'samuel.wekesa@email.com', '56890123', 'MEM015', 'ACTIVE', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyg0uO.JqI4m', 0, NOW()),
(16, 'Faith', 'Wairimu', '254767901234', 'faith.wairimu@email.com', '67901234', 'MEM016', 'ACTIVE', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyg0uO.JqI4m', 0, NOW()),
(17, 'Daniel', 'Barasa', '254778012345', 'daniel.barasa@email.com', '78012345', 'MEM017', 'ACTIVE', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyg0uO.JqI4m', 0, NOW()),
(18, 'Monica', 'Jepkoech', '254789123456', 'monica.jepkoech@email.com', '89123456', 'MEM018', 'ACTIVE', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyg0uO.JqI4m', 0, NOW()),
(19, 'George', 'Ngugi', '254790234567', 'george.ngugi@email.com', '90234567', 'MEM019', 'ACTIVE', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyg0uO.JqI4m', 0, NOW()),
(20, 'Catherine', 'Mumbi', '254701345678', 'catherine.mumbi@email.com', '01345678', 'MEM020', 'ACTIVE', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyg0uO.JqI4m', 0, NOW());

-- Note: PIN hash above is BCrypt hash for "1234" - for testing only!

-- Insert Loan Applications
INSERT INTO loan_application (application_id, loan_number, customer_id, customer_id_number, customer_mobile_number, loan_amount, credit_limit, disbursement_type, destination_account, application_status, product_code, loan_term, loan_interest, installments, application_time) VALUES
(1, 2024100100001, '1', '12345678', '254712345678', '25000', '50000', 'MPESA', '254712345678', 'APPROVED', 'QL001', '6', '10', '6', NOW() - INTERVAL '10 days'),
(2, 2024100100002, '2', '23456789', '254723456789', '50000', '100000', 'MPESA', '254723456789', 'APPROVED', 'EL001', '12', '12', '12', NOW() - INTERVAL '8 days'),
(3, 2024100100003, '3', '34567890', '254734567890', '100000', '500000', 'BANK', 'ACC123456', 'APPROVED', 'DL001', '24', '15', '24', NOW() - INTERVAL '5 days'),
(4, 2024100100004, '4', '45678901', '254745678901', '75000', '300000', 'MPESA', '254745678901', 'NEW', 'ED001', '36', '10', '36', NOW() - INTERVAL '2 days'),
(5, 2024100100005, '5', '56789012', '254756789012', '200000', '1000000', 'BANK', 'ACC234567', 'NEW', 'BL001', '48', '18', '48', NOW() - INTERVAL '1 day'),
(6, 2024100100006, '6', '67890123', '254767890123', '150000', '2000000', 'BANK', 'ACC345678', 'APPROVED', 'AF001', '60', '16', '60', NOW() - INTERVAL '15 days'),
(7, 2024100100007, '7', '78901234', '254778901234', '15000', '50000', 'MPESA', '254778901234', 'APPROVED', 'SA001', '3', '8', '3', NOW() - INTERVAL '3 days'),
(8, 2024100100008, '8', '89012345', '254789012345', '250000', '500000', 'BANK', 'ACC456789', 'REJECTED', 'RF001', '36', '14', '36', NOW() - INTERVAL '7 days'),
(9, 2024100100009, '9', '90123456', '254790123456', '300000', '1000000', 'BANK', 'ACC567890', 'APPROVED', 'AG001', '24', '12', '24', NOW() - INTERVAL '20 days'),
(10, 2024100100010, '10', '01234567', '254701234567', '1500000', '5000000', 'BANK', 'ACC678901', 'APPROVED', 'HL001', '240', '15', '240', NOW() - INTERVAL '30 days');

-- Insert Loan Accounts
INSERT INTO loan_account (account_id, application_id, amount, payable_amount, account_balance, customer_id, status, loanref, disbursement_date, maturity_date, due_date) VALUES
(1, 1, 25000.00, 26250.00, 21875.00, '1', 'ACTIVE', 'LN2024001', NOW() - INTERVAL '9 days', NOW() + INTERVAL '6 months', NOW() + INTERVAL '1 month'),
(2, 2, 50000.00, 56000.00, 46666.67, '2', 'ACTIVE', 'LN2024002', NOW() - INTERVAL '7 days', NOW() + INTERVAL '12 months', NOW() + INTERVAL '1 month'),
(3, 3, 100000.00, 115000.00, 95833.33, '3', 'ACTIVE', 'LN2024003', NOW() - INTERVAL '4 days', NOW() + INTERVAL '24 months', NOW() + INTERVAL '1 month'),
(4, 6, 150000.00, 174000.00, 145000.00, '6', 'ACTIVE', 'LN2024004', NOW() - INTERVAL '14 days', NOW() + INTERVAL '60 months', NOW() + INTERVAL '1 month'),
(5, 7, 15000.00, 15300.00, 10200.00, '7', 'ACTIVE', 'LN2024005', NOW() - INTERVAL '2 days', NOW() + INTERVAL '3 months', NOW() + INTERVAL '1 month'),
(6, 9, 300000.00, 336000.00, 280000.00, '9', 'ACTIVE', 'LN2024006', NOW() - INTERVAL '19 days', NOW() + INTERVAL '24 months', NOW() + INTERVAL '1 month'),
(7, 10, 1500000.00, 2025000.00, 1687500.00, '10', 'ACTIVE', 'LN2024007', NOW() - INTERVAL '29 days', NOW() + INTERVAL '240 months', NOW() + INTERVAL '1 month');

-- Insert Savings Accounts
INSERT INTO savings_account (account_id, customer_id, account_number, account_type, balance, interest_rate, status, opening_date) VALUES
(1, 1, 'SAV001', 'SAVINGS', 45230.50, 5.0, 'ACTIVE', NOW() - INTERVAL '1 year'),
(2, 2, 'SAV002', 'SAVINGS', 78450.00, 5.0, 'ACTIVE', NOW() - INTERVAL '2 years'),
(3, 3, 'SAV003', 'SAVINGS', 123500.75, 5.0, 'ACTIVE', NOW() - INTERVAL '6 months'),
(4, 4, 'SAV004', 'SAVINGS', 34200.00, 5.0, 'ACTIVE', NOW() - INTERVAL '8 months'),
(5, 5, 'SAV005', 'SAVINGS', 156780.25, 5.0, 'ACTIVE', NOW() - INTERVAL '1 year'),
(6, 6, 'SAV006', 'SAVINGS', 92100.50, 5.0, 'ACTIVE', NOW() - INTERVAL '9 months'),
(7, 7, 'SAV007', 'SAVINGS', 28900.00, 5.0, 'ACTIVE', NOW() - INTERVAL '3 months'),
(8, 8, 'SAV008', 'SAVINGS', 67340.80, 5.0, 'ACTIVE', NOW() - INTERVAL '1 year'),
(9, 9, 'SAV009', 'SAVINGS', 145600.00, 5.0, 'ACTIVE', NOW() - INTERVAL '2 years'),
(10, 10, 'SAV010', 'SAVINGS', 234500.50, 5.0, 'ACTIVE', NOW() - INTERVAL '1 year'),
(11, 11, 'SAV011', 'SAVINGS', 56700.00, 5.0, 'ACTIVE', NOW() - INTERVAL '5 months'),
(12, 12, 'SAV012', 'SAVINGS', 89200.25, 5.0, 'ACTIVE', NOW() - INTERVAL '7 months'),
(13, 13, 'SAV013', 'SAVINGS', 112400.00, 5.0, 'ACTIVE', NOW() - INTERVAL '10 months'),
(14, 14, 'SAV014', 'SAVINGS', 43500.75, 5.0, 'ACTIVE', NOW() - INTERVAL '4 months'),
(15, 15, 'SAV015', 'SAVINGS', 178900.50, 5.0, 'ACTIVE', NOW() - INTERVAL '1 year'),
(16, 16, 'SAV016', 'SAVINGS', 98700.00, 5.0, 'ACTIVE', NOW() - INTERVAL '8 months'),
(17, 17, 'SAV017', 'SAVINGS', 67200.25, 5.0, 'ACTIVE', NOW() - INTERVAL '6 months'),
(18, 18, 'SAV018', 'SAVINGS', 134500.50, 5.0, 'ACTIVE', NOW() - INTERVAL '1 year'),
(19, 19, 'SAV019', 'SAVINGS', 87300.00, 5.0, 'ACTIVE', NOW() - INTERVAL '9 months'),
(20, 20, 'SAV020', 'SAVINGS', 201400.75, 5.0, 'ACTIVE', NOW() - INTERVAL '2 years');

-- Success message
SELECT 'Dummy data inserted successfully!' AS message;
SELECT COUNT(*) AS total_products FROM products;
SELECT COUNT(*) AS total_customers FROM customer;
SELECT COUNT(*) AS total_loan_applications FROM loan_application;
SELECT COUNT(*) AS total_loan_accounts FROM loan_account;
SELECT COUNT(*) AS total_savings_accounts FROM savings_account;
