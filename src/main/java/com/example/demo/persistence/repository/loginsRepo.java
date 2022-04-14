package com.example.demo.persistence.repository;

import com.example.demo.model.loginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface loginsRepo extends JpaRepository<loginHistory, Long> {
}
