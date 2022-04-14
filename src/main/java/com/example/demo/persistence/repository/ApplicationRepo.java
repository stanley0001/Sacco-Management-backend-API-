package com.example.demo.persistence.repository;

import com.example.demo.model.loanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ApplicationRepo extends JpaRepository<loanApplication, Long> {


    Optional<loanApplication> findByCustomerMobileNumber(String phone);

    List<loanApplication> findByCustomerIdNumber(String documentNumber);

    @Query("select a from  loanApplication a where a.applicationTime > :date ORDER BY a.applicationId desc")
    List<loanApplication> findByApplicationTime(@Param("date") LocalDateTime localDate);
}
