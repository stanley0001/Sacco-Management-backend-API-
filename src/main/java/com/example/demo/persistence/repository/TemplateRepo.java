package com.example.demo.persistence.repository;

import com.example.demo.model.messageTemplates;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplateRepo extends JpaRepository<messageTemplates, Long> {
}
