package com.example.demo.system.userManagements.services.auth;

import com.example.demo.system.parsitence.models.AuthResponse;
import com.example.demo.system.userManagements.parsitence.models.login;
import org.springframework.stereotype.Service;

@Service
public interface Auth {
    AuthResponse auth(login authenticationRequest);
    AuthResponse refreshToken(String refreshToken);
}
