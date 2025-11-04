package com.example.demo.accounting.repositories;

import com.example.demo.accounting.entities.AssetCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetCategoryRepository extends JpaRepository<AssetCategory, Long> {
    
    Optional<AssetCategory> findByCode(String code);
    
    List<AssetCategory> findByIsActiveTrueOrderByNameAsc();
    
    boolean existsByCode(String code);
}
