package com.example.demo.loanManagement.parsistence.repositories;

import com.example.demo.loanManagement.parsistence.entities.SuspensePayments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SuspensePaymentRepo extends JpaRepository<SuspensePayments, Long> {
    Optional<List<SuspensePayments>> findByAccountNumberAndStatus(String phone, String status);
    
    Page<SuspensePayments> findByStatus(String status, Pageable pageable);
    
    List<SuspensePayments> findByStatus(String status);
}
