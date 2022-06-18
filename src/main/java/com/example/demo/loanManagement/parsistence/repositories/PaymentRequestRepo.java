package com.example.demo.persistence.repository;

import com.example.demo.model.PaymentRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRequestRepo extends JpaRepository<PaymentRequest, Long> {
}
