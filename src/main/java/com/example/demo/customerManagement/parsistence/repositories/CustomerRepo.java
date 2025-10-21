package com.example.demo.customerManagement.parsistence.repositories;

import com.example.demo.customerManagement.parsistence.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CustomerRepo extends JpaRepository<Customer, Long> {

    Optional<Customer> findByphoneNumber(String customerPhone);
    
    boolean existsByPhoneNumber(String phoneNumber);
    
    boolean existsByEmail(String email);
    
    boolean existsByDocumentNumber(String documentNumber);

    @Query("select c from  Customer c where c.createdAt >= :date")
    List<Customer> findAllByCreatedAtBefore(@Param("date") LocalDate localDate1);

    @Query("select c from  Customer c where c.createdAt < :date")
    List<Customer> findAllByCreatedAtAfter(@Param("date")LocalDate localDate);

    Customer findByEmail(String recipient);
}
