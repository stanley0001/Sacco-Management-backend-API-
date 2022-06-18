package com.example.demo.userManagements.parsitence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.userManagements.parsitence.enitities.rolePermissions;
public interface permissionsRepo extends JpaRepository<rolePermissions, Long> {
}
