package com.example.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Database Migration Configuration
 * Handles automatic migration of loan_repayment_schedules table across environments
 */
@Component
@Slf4j
public class DatabaseMigrationConfig {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void migrateDatabase() {
        log.info("Starting database migration for loan_repayment_schedules table...");
        
        try {
            // Check if the table exists
            if (tableExists("loan_repayment_schedules")) {
                log.info("loan_repayment_schedules table exists, checking for required columns...");
                
                // Add missing columns with defaults
                addColumnIfNotExists("loan_repayment_schedules", "loan_account_id", "BIGINT");
                addColumnIfNotExists("loan_repayment_schedules", "installment_number", "INTEGER");
                addColumnIfNotExists("loan_repayment_schedules", "due_date", "DATE");
                addColumnIfNotExists("loan_repayment_schedules", "principal_amount", "NUMERIC(15,2) DEFAULT 0");
                addColumnIfNotExists("loan_repayment_schedules", "interest_amount", "NUMERIC(15,2) DEFAULT 0");
                addColumnIfNotExists("loan_repayment_schedules", "total_amount", "NUMERIC(15,2) DEFAULT 0");
                addColumnIfNotExists("loan_repayment_schedules", "paid_principal", "NUMERIC(15,2) DEFAULT 0");
                addColumnIfNotExists("loan_repayment_schedules", "paid_interest", "NUMERIC(15,2) DEFAULT 0");
                addColumnIfNotExists("loan_repayment_schedules", "total_paid", "NUMERIC(15,2) DEFAULT 0");
                addColumnIfNotExists("loan_repayment_schedules", "outstanding_principal", "NUMERIC(15,2) DEFAULT 0");
                addColumnIfNotExists("loan_repayment_schedules", "outstanding_interest", "NUMERIC(15,2) DEFAULT 0");
                addColumnIfNotExists("loan_repayment_schedules", "total_outstanding", "NUMERIC(15,2) DEFAULT 0");
                addColumnIfNotExists("loan_repayment_schedules", "penalty_amount", "NUMERIC(15,2) DEFAULT 0");
                addColumnIfNotExists("loan_repayment_schedules", "paid_penalty", "NUMERIC(15,2) DEFAULT 0");
                addColumnIfNotExists("loan_repayment_schedules", "outstanding_penalty", "NUMERIC(15,2) DEFAULT 0");
                addColumnIfNotExists("loan_repayment_schedules", "balance_after_payment", "NUMERIC(15,2) DEFAULT 0");
                addColumnIfNotExists("loan_repayment_schedules", "status", "VARCHAR(50) DEFAULT 'PENDING'");
                addColumnIfNotExists("loan_repayment_schedules", "paid_date", "DATE");
                addColumnIfNotExists("loan_repayment_schedules", "payment_reference", "VARCHAR(255)");
                addColumnIfNotExists("loan_repayment_schedules", "created_at", "TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
                addColumnIfNotExists("loan_repayment_schedules", "updated_at", "TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
                
                // Update NULL values with defaults
                updateNullValues();
                
                log.info("Database migration completed successfully!");
            } else {
                log.info("loan_repayment_schedules table does not exist, Hibernate will create it with proper schema");
            }
        } catch (Exception e) {
            log.error("Database migration failed: {}", e.getMessage(), e);
            // Don't fail the application startup, just log the error
        }
    }
    
    private boolean tableExists(String tableName) {
        try {
            String sql = "SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = ?)";
            Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, tableName);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.warn("Could not check if table {} exists: {}", tableName, e.getMessage());
            return false;
        }
    }
    
    private boolean columnExists(String tableName, String columnName) {
        try {
            String sql = "SELECT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = ? AND column_name = ?)";
            Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, tableName, columnName);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.warn("Could not check if column {}.{} exists: {}", tableName, columnName, e.getMessage());
            return false;
        }
    }
    
    private void addColumnIfNotExists(String tableName, String columnName, String columnDefinition) {
        if (!columnExists(tableName, columnName)) {
            try {
                String sql = String.format("ALTER TABLE %s ADD COLUMN %s %s", tableName, columnName, columnDefinition);
                jdbcTemplate.execute(sql);
                log.info("Added column {}.{} with definition: {}", tableName, columnName, columnDefinition);
            } catch (Exception e) {
                log.warn("Could not add column {}.{}: {}", tableName, columnName, e.getMessage());
            }
        }
    }
    
    private void updateNullValues() {
        try {
            // Update core fields
            executeUpdate("UPDATE loan_repayment_schedules SET loan_account_id = COALESCE(loan_account, 0) WHERE loan_account_id IS NULL");
            executeUpdate("UPDATE loan_repayment_schedules SET installment_number = COALESCE(installment_number, 1) WHERE installment_number IS NULL");
            executeUpdate("UPDATE loan_repayment_schedules SET due_date = COALESCE(due_date, CURRENT_DATE) WHERE due_date IS NULL");
            
            // Update amount fields
            executeUpdate("UPDATE loan_repayment_schedules SET principal_amount = COALESCE(amount, 0) WHERE principal_amount IS NULL");
            executeUpdate("UPDATE loan_repayment_schedules SET interest_amount = 0 WHERE interest_amount IS NULL");
            executeUpdate("UPDATE loan_repayment_schedules SET total_amount = COALESCE(amount, 0) WHERE total_amount IS NULL");
            executeUpdate("UPDATE loan_repayment_schedules SET paid_principal = COALESCE(amount_paid, 0) WHERE paid_principal IS NULL");
            executeUpdate("UPDATE loan_repayment_schedules SET paid_interest = 0 WHERE paid_interest IS NULL");
            executeUpdate("UPDATE loan_repayment_schedules SET total_paid = COALESCE(amount_paid, 0) WHERE total_paid IS NULL");
            executeUpdate("UPDATE loan_repayment_schedules SET outstanding_principal = COALESCE(amount, 0) - COALESCE(amount_paid, 0) WHERE outstanding_principal IS NULL");
            executeUpdate("UPDATE loan_repayment_schedules SET outstanding_interest = 0 WHERE outstanding_interest IS NULL");
            executeUpdate("UPDATE loan_repayment_schedules SET total_outstanding = COALESCE(amount, 0) - COALESCE(amount_paid, 0) WHERE total_outstanding IS NULL");
            executeUpdate("UPDATE loan_repayment_schedules SET penalty_amount = 0 WHERE penalty_amount IS NULL");
            executeUpdate("UPDATE loan_repayment_schedules SET paid_penalty = 0 WHERE paid_penalty IS NULL");
            executeUpdate("UPDATE loan_repayment_schedules SET outstanding_penalty = 0 WHERE outstanding_penalty IS NULL");
            executeUpdate("UPDATE loan_repayment_schedules SET balance_after_payment = COALESCE(amount, 0) - COALESCE(amount_paid, 0) WHERE balance_after_payment IS NULL");
            
            // Update status and timestamp fields
            executeUpdate("UPDATE loan_repayment_schedules SET status = 'PENDING' WHERE status IS NULL");
            executeUpdate("UPDATE loan_repayment_schedules SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL");
            executeUpdate("UPDATE loan_repayment_schedules SET updated_at = CURRENT_TIMESTAMP WHERE updated_at IS NULL");
            
            log.info("Updated NULL values with proper defaults");
        } catch (Exception e) {
            log.warn("Could not update NULL values: {}", e.getMessage());
        }
    }
    
    private void executeUpdate(String sql) {
        try {
            int rowsUpdated = jdbcTemplate.update(sql);
            if (rowsUpdated > 0) {
                log.debug("Updated {} rows with: {}", rowsUpdated, sql);
            }
        } catch (Exception e) {
            log.warn("Could not execute update: {} - Error: {}", sql, e.getMessage());
        }
    }
}
