package com.example.demo.communication.parsitence.repositories;

import com.example.demo.communication.parsitence.enitities.InfobipToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepo extends JpaRepository<InfobipToken,Long> {
}
