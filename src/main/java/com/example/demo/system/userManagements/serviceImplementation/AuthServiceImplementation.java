package com.example.demo.system.userManagements.serviceImplementation;

import com.example.demo.system.parsitence.models.AuthResponse;
import com.example.demo.system.userManagements.parsitence.models.login;
import com.example.demo.system.userManagements.services.auth.Auth;
import com.example.demo.system.userManagements.services.auth.SecurityConstants;
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
                String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails);
                if (token == null || refreshToken == null) {
                    response.setHttpStatus(HttpStatus.UNAUTHORIZED);
                    response.setMessage("Unable to authenticate");
                    response.setReason("Invalid Credentials");
                    response.setHttpStatusCode(401);

                } else {
                    response.setHttpStatus(HttpStatus.OK);
                    response.setMessage(token);
                    response.setRefreshToken(refreshToken);
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

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        AuthResponse response = new AuthResponse();

        try {
            // Extract token from "Bearer token" format
            String token = null;
            if (refreshToken != null && refreshToken.startsWith("Bearer ")) {
                token = refreshToken.substring(7).trim(); // Trim any whitespace
            }

            if (token == null || token.isEmpty()) {
                response.setHttpStatus(HttpStatus.UNAUTHORIZED);
                response.setMessage("Invalid refresh token format");
                response.setReason("Token missing or malformed");
                response.setHttpStatusCode(401);
                return response;
            }

            // Validate the refresh token and get username
            String username = jwtTokenUtil.getUsernameFromToken(token);
            if (username == null) {
                response.setHttpStatus(HttpStatus.UNAUTHORIZED);
                response.setMessage("Invalid refresh token");
                response.setReason("Unable to extract username from token");
                response.setHttpStatusCode(401);
                return response;
            }

            // Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (userDetails == null) {
                response.setHttpStatus(HttpStatus.UNAUTHORIZED);
                response.setMessage("User not found");
                response.setReason("User associated with refresh token not found");
                response.setHttpStatusCode(401);
                return response;
            }

            // Validate refresh token
            if (jwtTokenUtil.validateToken(token, userDetails)) {
                // Generate new access token
                String newToken = jwtTokenUtil.generateToken(userDetails);
                if (newToken != null) {
                    response.setHttpStatus(HttpStatus.OK);
                    response.setMessage(newToken);
                    response.setReason("Token refreshed successfully");
                    response.setHttpStatusCode(200);
                } else {
                    response.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
                    response.setMessage("Failed to generate new token");
                    response.setReason("Token generation failed");
                    response.setHttpStatusCode(500);
                }
            } else {
                response.setHttpStatus(HttpStatus.UNAUTHORIZED);
                response.setMessage("Invalid or expired refresh token");
                response.setReason("Refresh token validation failed");
                response.setHttpStatusCode(401);
            }

        } catch (Exception e) {
            log.error("Error refreshing token: {}", e.getMessage());
            response.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            response.setMessage("Token refresh failed");
            response.setReason("Internal server error during token refresh");
            response.setHttpStatusCode(500);
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

