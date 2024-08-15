package com.example.demo.loanManagement.parsistence.repositories;

import com.example.demo.loanManagement.parsistence.entities.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ApplicationRepo extends JpaRepository<LoanApplication, Long> {


    Optional<LoanApplication> findByCustomerMobileNumber(String phone);

    List<LoanApplication> findByCustomerIdNumber(String documentNumber);

//    @Query(nativeQuery = true,value = "select a from  loan_application a where a.application_time > :date ORDER BY a.application_id desc")
//    List<LoanApplication> findByApplicationTimeAfter(@Param("date") LocalDateTime localDate);
    List<LoanApplication> findTop10ByApplicationTimeAfter(LocalDateTime localDate);
}
