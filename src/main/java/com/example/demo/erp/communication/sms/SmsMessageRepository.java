package com.example.demo.erp.communication.sms;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SmsMessageRepository extends JpaRepository<SmsMessage, Long> {

    List<SmsMessage> findByStatus(SmsMessage.SmsStatus status);

    List<SmsMessage> findByRecipient(String recipient);

    List<SmsMessage> findBySentDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COUNT(s) FROM SmsMessage s WHERE s.status = ?1")
    long countByStatus(SmsMessage.SmsStatus status);

    @Query("SELECT COALESCE(SUM(s.cost), 0) FROM SmsMessage s WHERE s.status = 'SENT'")
    Double getTotalCost();

    @Query("SELECT s FROM SmsMessage s ORDER BY s.createdAt DESC")
    List<SmsMessage> findAllOrderByCreatedAtDesc();
}
