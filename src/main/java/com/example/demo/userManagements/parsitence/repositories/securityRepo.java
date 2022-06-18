package com.example.demo.userManagements.parsitence.repositories;

import com.example.demo.userManagements.parsitence.models.Security;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface securityRepo extends JpaRepository<Security, Long> {
    Optional<Security> findByuserId(String id);

//    Security existsByuserId(String userId);

 //  Optional<Security> findByuserIdAndisActive(String userId,Boolean isActive);
}
