package com.example.demo.controllers.auth;

import com.example.demo.model.ResponseModel;
import com.example.demo.model.login;
import com.example.demo.services.auth.SecurityConstants;
import com.example.demo.services.userService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

    @RestController
    @CrossOrigin
    public class authentication {

        @Autowired
        private AuthenticationManager authenticationManager;

        @Autowired
        private SecurityConstants jwtTokenUtil;

        @Autowired
        private userService userDetailsService;

        @PostMapping(value = "/authenticate")
        public ResponseEntity<ResponseModel> createAuthenticationToken(@RequestBody login authenticationRequest) throws Exception{
           ResponseModel checkUser= userDetailsService.findUser(authenticationRequest.getUserName());
            ResponseModel response = new ResponseModel();
           if (checkUser.getHttpStatusCode()==200) {
                authenticate(authenticationRequest.getUserName(), authenticationRequest.getPassword());

                final UserDetails userDetails = userDetailsService
                        .loadUserByUsername(authenticationRequest.getUserName());

                final String token = jwtTokenUtil.generateToken(userDetails);
                if (token == null) {
                    response.setHttpStatus(HttpStatus.FORBIDDEN);
                    response.setMessage("Unable to authenticate");
                    response.setReason("Invalid Credentials");
                    response.setHttpStatusCode(401);

                } else {
                    response.setHttpStatus(HttpStatus.OK);
                    response.setMessage(token);
                    response.setReason("Authenticated");
                    response.setHttpStatusCode(200);
                    response.setUser(userDetails);
                }
            }
            else {
                response=checkUser;
            }
            return new ResponseEntity<>(response, HttpStatus.OK);

        }

        private void authenticate(String username, String password) throws Exception {

            try {
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            }
             catch (BadCredentialsException e) {
                throw new BadCredentialsException( "INVALID_CREDENTIALS");

            }
        }
    }
