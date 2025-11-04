package com.example.demo.payments.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * M-PESA Configuration Entity
 * Stores M-PESA integration settings that can be configured via UI
 */
@Entity
@Table(name = "mpesa_config")
@Data
public class MpesaConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String configName; // e.g., "PRIMARY", "BACKUP"
    
    @Column(nullable = false)
    private String consumerKey;
    
    @Column(nullable = false)
    private String consumerSecret;
    
    @Column(nullable = false)
    private String shortcode;
    
    @Column(nullable = false, length = 1000)
    private String passkey;
    
    @Column(nullable = false)
    private String initiatorName;
    
    @Column(nullable = false, length = 1000)
    private String securityCredential;
    
    @Column(nullable = false)
    private String apiUrl; // Sandbox or Production
    
    @Column(nullable = false)
    private String callbackUrl;
    
    @Column(nullable = false)
    private String timeoutUrl;
    
    @Column(nullable = false)
    private String resultUrl;
    
    // Dynamic Callback URLs for different transaction types
    @Column(name = "stk_callback_url")
    private String stkCallbackUrl;
    
    @Column(name = "paybill_callback_url")
    private String paybillCallbackUrl;
    
    @Column(name = "b2c_callback_url")
    private String b2cCallbackUrl;
    
    @Column(name = "validation_url")
    private String validationUrl;
    
    @Column(name = "confirmation_url")
    private String confirmationUrl;
    
    @Column(name = "status_callback_url")
    private String statusCallbackUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnvironmentType environment; // SANDBOX or PRODUCTION
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @Column(nullable = false)
    private Boolean defaultConfig = false;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    // Test Connection Results
    private Boolean lastTestSuccess;
    private LocalDateTime lastTestDate;
    private String lastTestMessage;
    
    // Statistics
    private Long totalTransactions = 0L;
    private Long successfulTransactions = 0L;
    private Long failedTransactions = 0L;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "updated_by")
    private String updatedBy;
    
    public enum EnvironmentType {
        SANDBOX,
        PRODUCTION
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public Double getSuccessRate() {
        if (totalTransactions == 0) return 0.0;
        return (successfulTransactions.doubleValue() / totalTransactions.doubleValue()) * 100;
    }
}
