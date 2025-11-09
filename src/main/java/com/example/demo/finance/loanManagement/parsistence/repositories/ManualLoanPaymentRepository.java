package com.example.demo.finance.loanManagement.parsistence.repositories;

import com.example.demo.finance.loanManagement.parsistence.entities.ManualLoanPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ManualLoanPaymentRepository extends JpaRepository<ManualLoanPayment, Long> {
    List<ManualLoanPayment> findByStatusOrderBySubmittedAtDesc(String status);
    List<ManualLoanPayment> findByLoanAccountIdOrderBySubmittedAtDesc(Long loanAccountId);
    List<ManualLoanPayment> findByStatusAndLoanAccountIdOrderBySubmittedAtDesc(String status, Long loanAccountId);
    List<ManualLoanPayment> findAllByOrderBySubmittedAtDesc();
}
