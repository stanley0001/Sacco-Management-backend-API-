package com.example.demo.banking.parsitence.repositories;

import com.example.demo.banking.parsitence.enitities.Payments;
import com.example.demo.customerManagement.parsistence.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepo extends JpaRepository<Payments, Long> {

    @Query("select p from  Payments p where p.paymentTime >= :date")
    List<Payments> findAllByPaymentTimeBefore(@Param("date") LocalDateTime localDate1);

    List<Payments> findAllByCustomer(Customer client);
}
