package com.example.demo.loanManagement.parsistence.repositories;

import com.example.demo.loanManagement.parsistence.entities.Disbursements;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisbursementsRepo extends JpaRepository<Disbursements, Long> {
}
