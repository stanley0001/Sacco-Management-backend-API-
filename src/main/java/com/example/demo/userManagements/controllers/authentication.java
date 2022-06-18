package com.example.demo.userManagements.controllers;

import com.example.demo.system.parsitence.models.AuthResponse;
import com.example.demo.userManagements.parsitence.models.login;
import com.example.demo.userManagements.services.auth.Auth;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

    @Log4j2
    @RestController
    @RequestMapping("/")
    public class authentication {
        public final Auth auth;

        public authentication(Auth auth) {
            this.auth = auth;
        }

        @PostMapping("authenticate")
         public ResponseEntity<AuthResponse> createAuthenticationToken(@RequestBody login authenticationRequest){
              AuthResponse response=auth.auth(authenticationRequest);
            return new ResponseEntity<>(response,response.getHttpStatus());
        }
    }
