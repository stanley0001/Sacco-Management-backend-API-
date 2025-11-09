package com.example.demo.finance.accounting.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Fixed Asset - Long-term organizational assets
 */
@Entity
@Table(name = "fixed_assets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FixedAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String assetCode;

    @Column(nullable = false)
    private String assetName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private AssetCategory category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Double purchaseCost;

    @Column(nullable = false)
    private LocalDate purchaseDate;

    private String supplier;

    private Integer usefulLifeYears; // Depreciation period

    @Enumerated(EnumType.STRING)
    private DepreciationMethod depreciationMethod = DepreciationMethod.STRAIGHT_LINE;

    private Double depreciationRate; // Annual rate %

    private Double residualValue = 0.0; // Salvage value

    private Double accumulatedDepreciation = 0.0;

    private Double currentBookValue;

    private String location;

    private String assignedTo; // Employee or department

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetStatus status = AssetStatus.ACTIVE;

    private LocalDate disposalDate;
    private Double disposalValue;

    private String journalEntryId; // Purchase journal entry

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private String createdBy;

    public enum AssetStatus {
        ACTIVE,
        DEPRECIATED,
        DISPOSED,
        UNDER_MAINTENANCE,
        STOLEN_LOST
    }

    public enum DepreciationMethod {
        STRAIGHT_LINE,
        DECLINING_BALANCE,
        UNITS_OF_PRODUCTION
    }

    public void calculateCurrentBookValue() {
        this.currentBookValue = purchaseCost - accumulatedDepreciation;
    }
}
