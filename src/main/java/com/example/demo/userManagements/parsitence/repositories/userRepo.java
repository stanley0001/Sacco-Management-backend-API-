package com.example.demo.userManagements.parsitence.repositories;

import com.example.demo.userManagements.parsitence.enitities.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface userRepo extends JpaRepository<Users, Long> {

    Optional<Users> findByuserName(String userName);

    Optional<Users> findByemail(String email);
}
