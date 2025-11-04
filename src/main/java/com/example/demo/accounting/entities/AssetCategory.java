package com.example.demo.accounting.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Asset Category - Classification for fixed assets
 */
@Entity
@Table(name = "asset_categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;

    private String accountCode; // Chart of Accounts mapping

    @Enumerated(EnumType.STRING)
    private FixedAsset.DepreciationMethod defaultDepreciationMethod;

    private Double defaultDepreciationRate; // Annual %

    private Integer defaultUsefulLife; // Years

    private Boolean isActive = true;
}
