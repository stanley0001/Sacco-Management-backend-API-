package com.example.demo.erp.communication.sms.repositories;

import com.example.demo.erp.communication.sms.entities.SmsConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SmsConfigRepository extends JpaRepository<SmsConfig, Long> {

    Optional<SmsConfig> findByConfigNameIgnoreCase(String configName);

    Optional<SmsConfig> findByDefaultConfigTrue();

    Optional<SmsConfig> findByActiveTrueAndDefaultConfigTrue();
}
