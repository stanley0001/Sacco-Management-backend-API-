package com.example.demo.erp.communication.sms.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "sms_config")
@Data
@NoArgsConstructor
public class SmsConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String configName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SmsProviderType providerType;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private Boolean defaultConfig = false;

    @Column(length = 512)
    private String description;

    @Column(length = 512)
    private String apiUrl;

    @Column(length = 512)
    private String apiKey;

    @Column(length = 255)
    private String username;

    @Column(length = 255)
    private String partnerId;

    @Column(length = 255)
    private String shortcode;

    @Column(length = 255)
    private String senderId;

    @Column(length = 255)
    private String authToken;

    @Column(length = 255)
    private String template;

    @Column(name = "last_test_success")
    private Boolean lastTestSuccess;

    @Column(name = "last_test_date")
    private LocalDateTime lastTestDate;

    @Column(name = "last_test_message", length = 512)
    private String lastTestMessage;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum SmsProviderType {
        AFRICAS_TALKING,
        TEXT_SMS,
        CUSTOM_GET
    }
}
