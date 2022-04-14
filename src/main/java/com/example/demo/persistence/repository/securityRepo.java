package com.example.demo.persistence.repository;

import com.example.demo.model.Security;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface securityRepo extends JpaRepository<Security, Long> {
    Optional<Security> findByuserId(String id);

//    Security existsByuserId(String userId);

 //  Optional<Security> findByuserIdAndisActive(String userId,Boolean isActive);
}
