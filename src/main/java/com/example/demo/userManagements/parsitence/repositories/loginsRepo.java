package com.example.demo.userManagements.parsitence.repositories;

import com.example.demo.userManagements.parsitence.models.loginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface loginsRepo extends JpaRepository<loginHistory, Long> {
}
