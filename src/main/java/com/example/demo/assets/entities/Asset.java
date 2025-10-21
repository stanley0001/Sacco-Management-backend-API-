package com.example.demo.assets.entities;

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
 * Asset Management - For asset-based financing and collateral tracking
 */
@Entity
@Table(name = "assets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String assetNumber;

    @Column(nullable = false, length = 200)
    private String assetName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetType assetType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetCategory assetCategory;

    // Ownership
    private Long ownerId; // Customer ID
    private String ownerName;
    private String ownerType; // MEMBER, SACCO, THIRD_PARTY

    // Valuation
    @Column(nullable = false)
    private Double purchasePrice;

    private LocalDate purchaseDate;

    @Column(nullable = false)
    private Double currentValue;

    private Double marketValue;
    private LocalDate valuationDate;
    private String valuedBy; // Appraiser name

    // Depreciation
    @Enumerated(EnumType.STRING)
    private DepreciationMethod depreciationMethod;

    private Integer usefulLifeYears;
    private Double depreciationRate; // Annual percentage
    private Double salvageValue;
    private Double accumulatedDepreciation = 0.0;
    private Double bookValue;

    // Physical details
    private String serialNumber;
    private String registrationNumber; // For vehicles, land titles, etc.
    private String make;
    private String model;
    private String yearOfManufacture;
    private String condition; // EXCELLENT, GOOD, FAIR, POOR

    // Location
    private String location;
    private String address;
    private String gpsCoordinates;

    // Documentation
    private String ownershipDocument; // File path or reference
    private String valuationDocument;
    private String insuranceDocument;
    private String photoPath;

    // Insurance
    private String insuranceCompany;
    private String insurancePolicyNumber;
    private Double insuranceValue;
    private LocalDate insuranceExpiryDate;

    // Collateral tracking
    @Enumerated(EnumType.STRING)
    private CollateralStatus collateralStatus = CollateralStatus.AVAILABLE;

    private Long linkedLoanAccountId;
    private String loanAccountNumber;
    private Double loanToValueRatio; // Percentage

    // Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetStatus status = AssetStatus.ACTIVE;

    private String disposalReason;
    private LocalDate disposalDate;
    private Double disposalValue;

    // Audit fields
    private String createdBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private String lastInspectedBy;
    private LocalDate lastInspectionDate;
    private LocalDate nextInspectionDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public enum AssetType {
        TANGIBLE,    // Physical assets
        INTANGIBLE,  // Non-physical assets
        FINANCIAL    // Financial instruments
    }

    public enum AssetCategory {
        // Tangible Assets
        LAND,
        BUILDING,
        VEHICLE,
        MACHINERY,
        EQUIPMENT,
        FURNITURE,
        ELECTRONICS,
        INVENTORY,
        LIVESTOCK,
        
        // Intangible Assets
        GOODWILL,
        PATENTS,
        TRADEMARKS,
        SOFTWARE,
        
        // Financial Assets
        SHARES,
        BONDS,
        TREASURY_BILLS,
        FIXED_DEPOSITS,
        OTHER
    }

    public enum DepreciationMethod {
        STRAIGHT_LINE,
        DECLINING_BALANCE,
        DOUBLE_DECLINING_BALANCE,
        UNITS_OF_PRODUCTION,
        SUM_OF_YEARS_DIGITS,
        NONE // For land and intangible assets
    }

    public enum CollateralStatus {
        AVAILABLE,          // Can be used as collateral
        PLEDGED,            // Currently securing a loan
        ENCUMBERED,         // Has other liens
        SEIZED,             // Repossessed
        RELEASED,           // Released from pledge
        NOT_ELIGIBLE        // Cannot be used as collateral
    }

    public enum AssetStatus {
        ACTIVE,
        UNDER_MAINTENANCE,
        DISPOSED,
        STOLEN,
        DAMAGED,
        WRITTEN_OFF
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        calculateBookValue();
    }

    @PrePersist
    protected void onCreate() {
        this.bookValue = this.purchasePrice;
    }

    /**
     * Calculate current book value
     */
    public void calculateBookValue() {
        this.bookValue = this.purchasePrice - this.accumulatedDepreciation;
    }

    /**
     * Calculate depreciation for a period
     */
    public void calculateDepreciation(int years) {
        if (depreciationMethod == DepreciationMethod.STRAIGHT_LINE && usefulLifeYears != null) {
            double annualDepreciation = (purchasePrice - (salvageValue != null ? salvageValue : 0.0)) / usefulLifeYears;
            this.accumulatedDepreciation = Math.min(annualDepreciation * years, purchasePrice - (salvageValue != null ? salvageValue : 0.0));
        }
        calculateBookValue();
    }
}
