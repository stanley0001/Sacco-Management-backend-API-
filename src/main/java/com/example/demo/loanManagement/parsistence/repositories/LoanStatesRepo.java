package com.example.demo.persistence.repository;

import com.example.demo.model.LoanStates;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanStatesRepo extends JpaRepository<LoanStates, Long> {
    List<LoanStates> findByAccountNumber(String valueOf);
}
