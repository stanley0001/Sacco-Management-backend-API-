package com.example.demo.persistence.repository;

import com.example.demo.persistence.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface userRepo extends JpaRepository<Users, Long> {

    Optional<Users> findByuserName(String userName);

    Optional<Users> findByemail(String email);
}
