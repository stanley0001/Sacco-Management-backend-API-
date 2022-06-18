package com.example.demo.persistence.repository;

import com.example.demo.model.Disbursements;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisbursementsRepo extends JpaRepository<Disbursements, Long> {
}
