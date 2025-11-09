package com.example.demo.system.userManagements.parsitence.repositories;

import com.example.demo.system.userManagements.parsitence.enitities.rolePermissions;
import org.springframework.data.jpa.repository.JpaRepository;

public interface permissionsRepo extends JpaRepository<rolePermissions, Long> {
}
