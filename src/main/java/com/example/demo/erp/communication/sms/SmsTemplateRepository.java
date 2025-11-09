package com.example.demo.erp.communication.sms;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SmsTemplateRepository extends JpaRepository<SmsTemplate, Long> {

    Optional<SmsTemplate> findByCode(String code);

    List<SmsTemplate> findByCategory(String category);

    List<SmsTemplate> findByActive(boolean active);

    List<SmsTemplate> findByCategoryAndActive(String category, boolean active);

    boolean existsByCode(String code);
}
