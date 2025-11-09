package com.example.demo.finance.assets.repositories;

import com.example.demo.finance.assets.entities.Asset;
import com.example.demo.finance.assets.entities.Asset.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepo extends JpaRepository<Asset, Long> {
    
    Optional<Asset> findByAssetNumber(String assetNumber);
    
    List<Asset> findByOwnerId(Long ownerId);
    
    List<Asset> findByAssetType(AssetType assetType);
    
    List<Asset> findByAssetCategory(AssetCategory assetCategory);
    
    List<Asset> findByStatus(AssetStatus status);
    
    List<Asset> findByCollateralStatus(CollateralStatus collateralStatus);
    
    List<Asset> findByLinkedLoanAccountId(Long loanAccountId);
    
    @Query("SELECT a FROM Asset a WHERE a.collateralStatus = 'AVAILABLE' AND a.status = 'ACTIVE'")
    List<Asset> findAvailableForCollateral();
    
    @Query("SELECT a FROM Asset a WHERE a.insuranceExpiryDate <= :expiryDate")
    List<Asset> findByInsuranceExpiringBefore(@Param("expiryDate") LocalDate expiryDate);
    
    @Query("SELECT a FROM Asset a WHERE a.nextInspectionDate <= :inspectionDate")
    List<Asset> findByInspectionDueBefore(@Param("inspectionDate") LocalDate inspectionDate);
    
    @Query("SELECT SUM(a.currentValue) FROM Asset a WHERE a.ownerId = :ownerId AND a.status = 'ACTIVE'")
    Double getTotalAssetValueByOwner(@Param("ownerId") Long ownerId);
    
    @Query("SELECT a FROM Asset a WHERE a.assetName LIKE %:searchTerm% OR a.serialNumber LIKE %:searchTerm%")
    List<Asset> searchAssets(@Param("searchTerm") String searchTerm);
    
    boolean existsByAssetNumber(String assetNumber);
    
    boolean existsBySerialNumber(String serialNumber);
}
