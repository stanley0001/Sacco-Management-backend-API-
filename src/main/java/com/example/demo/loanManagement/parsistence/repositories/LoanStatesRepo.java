package com.example.demo.loanManagement.parsistence.repositories;

import com.example.demo.loanManagement.parsistence.models.LoanStates;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanStatesRepo extends JpaRepository<LoanStates, Long> {
    List<LoanStates> findByAccountNumber(String valueOf);
}
