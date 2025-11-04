package com.example.demo.accounting.repositories;

import com.example.demo.accounting.entities.PayrollRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRunRepository extends JpaRepository<PayrollRun, Long> {
    
    Optional<PayrollRun> findByPayrollNumber(String payrollNumber);
    
    Optional<PayrollRun> findByPeriodMonthAndPeriodYear(Integer periodMonth, Integer periodYear);
    
    List<PayrollRun> findByStatusOrderByPeriodYearDescPeriodMonthDesc(PayrollRun.PayrollStatus status);
    
    List<PayrollRun> findByPeriodYearOrderByPeriodMonthDesc(Integer periodYear);
    
    List<PayrollRun> findAllByOrderByPeriodYearDescPeriodMonthDesc();
}
