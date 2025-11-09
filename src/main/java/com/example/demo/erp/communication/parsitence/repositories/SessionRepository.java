package com.example.demo.erp.communication.parsitence.repositories;

import com.example.demo.erp.communication.parsitence.enitities.WhatsAppSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionRepository extends JpaRepository<WhatsAppSession,Long> {
    Optional<WhatsAppSession> findByPhone(String phone);
}
