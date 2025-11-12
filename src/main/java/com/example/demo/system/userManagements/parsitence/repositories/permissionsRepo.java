package com.example.demo.system.userManagements.parsitence.repositories;

import com.example.demo.system.userManagements.parsitence.enitities.rolePermissions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface permissionsRepo extends JpaRepository<rolePermissions, Long> {
    Optional<rolePermissions> findByName(String permName);
}
