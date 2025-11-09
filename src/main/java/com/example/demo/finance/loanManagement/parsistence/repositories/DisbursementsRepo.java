package com.example.demo.finance.loanManagement.parsistence.repositories;

import com.example.demo.finance.loanManagement.parsistence.entities.Disbursements;
import com.example.demo.finance.loanManagement.parsistence.entities.Disbursements;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisbursementsRepo extends JpaRepository<Disbursements, Long> {
}
