package com.example.demo.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.model.rolePermissions;
public interface permissionsRepo extends JpaRepository<rolePermissions, Long> {
}
