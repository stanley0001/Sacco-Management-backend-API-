package com.example.demo.persistence.repository;

import com.example.demo.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CustomerRepo extends JpaRepository<Customer, Long> {

    Optional<Customer> findByphoneNumber(String customerPhone);

    @Query("select c from  Customer c where c.createdAt >= :date")
    List<Customer> findAllByCreatedAtBefore(@Param("date") LocalDate localDate1);

    @Query("select c from  Customer c where c.createdAt < :date")
    List<Customer> findAllByCreatedAtAfter(@Param("date")LocalDate localDate);
}
