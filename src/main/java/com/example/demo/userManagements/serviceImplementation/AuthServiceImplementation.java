package com.example.demo.userManagements.serviceImplementation;

import com.example.demo.system.parsitence.models.AuthResponse;
import com.example.demo.userManagements.parsitence.models.login;
import com.example.demo.userManagements.services.auth.Auth;
import com.example.demo.userManagements.services.auth.SecurityConstants;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class AuthServiceImplementation implements Auth {
    public final AuthenticationManager authenticationManager;
    public final SecurityConstants jwtTokenUtil;
    public final UserService userDetailsService;

    public AuthServiceImplementation(AuthenticationManager authenticationManager, SecurityConstants jwtTokenUtil, UserService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
    }

    public AuthResponse auth(login authenticationRequest){
        AuthResponse checkUser= userDetailsService.findUser(authenticationRequest.getUserName());
        AuthResponse response = new AuthResponse();
        if (checkUser.getHttpStatusCode()==200) {
            log.info("Auth user found");
            Boolean auth= this.authenticate(authenticationRequest.getUserName(), authenticationRequest.getPassword());
            if (auth==Boolean.TRUE){
                UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUserName());
                log.info("Generating auth token");
                String token = jwtTokenUtil.generateToken(userDetails);
                if (token == null) {
                    response.setHttpStatus(HttpStatus.UNAUTHORIZED);
                    response.setMessage("Unable to authenticate");
                    response.setReason("Invalid Credentials");
                    response.setHttpStatusCode(401);

                } else {
                    response.setHttpStatus(HttpStatus.OK);
                    response.setMessage(token);
                    response.setReason("Authenticated");
                    response.setHttpStatusCode(200);
                }
            }else {
                response.setHttpStatus(HttpStatus.UNAUTHORIZED);
                response.setHttpStatusCode(401);
                response.setReason("Invalid Password");
            }
        }
        else {
            response=checkUser;
        }
        return response;

    }
    private Boolean authenticate(String username, String password){
        Boolean authenticated = null;
        try {
            Authentication auth=authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            log.info(auth);
            authenticated=true;
        }
        catch (BadCredentialsException e) {
            authenticated=false;
            log.warn( "INVALID_CREDENTIALS {}",e.getMessage());

        }

        return authenticated;
    }
    }

