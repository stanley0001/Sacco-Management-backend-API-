package com.example.demo.loanManagement.parsistence.repositories;

import com.example.demo.loanManagement.parsistence.entities.LoanRepaymentSchedules;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepaymentScheduleRepo extends JpaRepository<LoanRepaymentSchedules,Long> {
}
