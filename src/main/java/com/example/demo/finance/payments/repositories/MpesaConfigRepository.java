package com.example.demo.finance.payments.repositories;

import com.example.demo.finance.payments.entities.MpesaConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MpesaConfigRepository extends JpaRepository<MpesaConfig, Long> {
    
    Optional<MpesaConfig> findByDefaultConfigTrue();
    
    Optional<MpesaConfig> findByConfigName(String configName);
    
    List<MpesaConfig> findByActiveTrue();
    
    List<MpesaConfig> findByEnvironment(MpesaConfig.EnvironmentType environment);
}
