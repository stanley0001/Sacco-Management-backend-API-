package com.example.demo.accounting.repositories;

import com.example.demo.accounting.entities.FixedAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FixedAssetRepository extends JpaRepository<FixedAsset, Long> {
    
    Optional<FixedAsset> findByAssetCode(String assetCode);
    
    List<FixedAsset> findByStatusOrderByAssetCodeAsc(FixedAsset.AssetStatus status);
    
    List<FixedAsset> findByCategoryIdOrderByPurchaseDateDesc(Long categoryId);
    
    boolean existsByAssetCode(String assetCode);
    
    @Query("SELECT COALESCE(SUM(a.currentBookValue), 0.0) FROM FixedAsset a WHERE a.status = 'ACTIVE'")
    Double getTotalAssetValue();
    
    @Query("SELECT COALESCE(SUM(a.accumulatedDepreciation), 0.0) FROM FixedAsset a WHERE a.status = 'ACTIVE'")
    Double getTotalDepreciation();
}
