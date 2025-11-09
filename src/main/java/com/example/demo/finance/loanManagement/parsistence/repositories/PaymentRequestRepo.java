package com.example.demo.finance.loanManagement.parsistence.repositories;

import com.example.demo.finance.loanManagement.parsistence.entities.PaymentRequest;
import com.example.demo.finance.loanManagement.parsistence.entities.PaymentRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRequestRepo extends JpaRepository<PaymentRequest, Long> {
}
