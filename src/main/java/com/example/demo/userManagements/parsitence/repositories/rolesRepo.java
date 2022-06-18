package com.example.demo.persistence.repository;

import com.example.demo.model.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

public interface rolesRepo extends JpaRepository<Roles, Long> {
}
