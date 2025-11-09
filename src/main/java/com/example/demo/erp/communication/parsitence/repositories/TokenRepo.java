package com.example.demo.erp.communication.parsitence.repositories;

import com.example.demo.erp.communication.parsitence.enitities.InfobipToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepo extends JpaRepository<InfobipToken,Long> {
}
