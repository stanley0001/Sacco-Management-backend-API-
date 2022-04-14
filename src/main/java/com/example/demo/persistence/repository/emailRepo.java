package com.example.demo.persistence.repository;

import com.example.demo.model.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface emailRepo extends JpaRepository<Email, Long> {
    List<Email> findByRecipient(String email);

    @Query("select e from  Email e order by e.id desc")
    List<Email> findAllOrderByIdDesc();

    @Query("select e from  Email e where e.recipient = :email order by e.id desc")
    List<Email> findByRecipientOrderByIdDesc(@Param("email")String email);
}
