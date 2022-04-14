package com.example.demo.persistence.repository;

import com.example.demo.model.SuspensePayments;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SuspensePaymentRepo extends JpaRepository<SuspensePayments, Long> {
    Optional<List<SuspensePayments>> findByAccountNumberAndStatus(String phone, String suspense);
}
