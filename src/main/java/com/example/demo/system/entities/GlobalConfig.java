package com.example.demo.system.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Global System Configuration
 * Stores system-wide settings and preferences
 */
@Entity
@Table(name = "global_configs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String configKey;

    @Column(nullable = false, length = 100)
    private String configName;

    @Column(length = 500)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String configValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConfigType configType = ConfigType.STRING;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(length = 200)
    private String category; // e.g., "LOAN_SETTINGS", "APPROVAL_WORKFLOW", "STANDING_ORDER"

    @Column(length = 100)
    private String updatedBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum ConfigType {
        STRING,
        BOOLEAN,
        INTEGER,
        DECIMAL,
        JSON
    }

    /**
     * Get value as Boolean
     */
    public Boolean getBooleanValue() {
        if (configValue == null) return false;
        return Boolean.parseBoolean(configValue);
    }

    /**
     * Get value as Integer
     */
    public Integer getIntegerValue() {
        if (configValue == null) return 0;
        try {
            return Integer.parseInt(configValue);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Get value as BigDecimal
     */
    public BigDecimal getDecimalValue() {
        if (configValue == null) return BigDecimal.ZERO;
        try {
            return new BigDecimal(configValue);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Set value from Boolean
     */
    public void setBooleanValue(Boolean value) {
        this.configValue = value != null ? value.toString() : "false";
        this.configType = ConfigType.BOOLEAN;
    }

    /**
     * Set value from Integer
     */
    public void setIntegerValue(Integer value) {
        this.configValue = value != null ? value.toString() : "0";
        this.configType = ConfigType.INTEGER;
    }

    /**
     * Set value from BigDecimal
     */
    public void setDecimalValue(BigDecimal value) {
        this.configValue = value != null ? value.toString() : "0";
        this.configType = ConfigType.DECIMAL;
    }
}
