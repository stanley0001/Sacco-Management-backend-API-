package com.example.demo.system.userManagements.controllers;

import com.example.demo.system.parsitence.models.AuthResponse;
import com.example.demo.system.userManagements.parsitence.models.login;
import com.example.demo.system.userManagements.services.auth.Auth;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

    @Log4j2
    @RestController
    @RequestMapping("/")
    public class Authentication {
        public final Auth auth;

        public Authentication(Auth auth) {
            this.auth = auth;
        }

        @PostMapping("api/authenticate")
         public ResponseEntity<AuthResponse> createAuthenticationToken(@RequestBody login authenticationRequest){
              AuthResponse response=auth.auth(authenticationRequest);
            return new ResponseEntity<>(response,response.getHttpStatus());
        }

        @PostMapping("api/refresh-token")
        public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Authorization") String refreshToken){
            AuthResponse response = auth.refreshToken(refreshToken);
            return new ResponseEntity<>(response, response.getHttpStatus());
        }
    }
