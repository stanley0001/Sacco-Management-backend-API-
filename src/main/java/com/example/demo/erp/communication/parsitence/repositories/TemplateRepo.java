package com.example.demo.erp.communication.parsitence.repositories;

import com.example.demo.erp.communication.parsitence.models.messageTemplates;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplateRepo extends JpaRepository<messageTemplates, Long> {
}
