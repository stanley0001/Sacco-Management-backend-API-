package com.example.demo.system.userManagements.parsitence.repositories;

import com.example.demo.system.userManagements.parsitence.enitities.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface rolesRepo extends JpaRepository<Roles, Long> {
    Optional<Roles> findByRoleName(String roleName);
}
