package com.example.demo.system.repositories;

import com.example.demo.system.entities.GlobalConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GlobalConfigRepository extends JpaRepository<GlobalConfig, Long> {
    
    Optional<GlobalConfig> findByConfigKey(String configKey);
    
    List<GlobalConfig> findByCategory(String category);
    
    List<GlobalConfig> findByIsActiveTrue();
    
    List<GlobalConfig> findByCategoryAndIsActiveTrue(String category);
}
