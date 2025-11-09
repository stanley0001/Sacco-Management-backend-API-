package com.example.demo.system.userManagements.services.auth;

import com.example.demo.system.userManagements.serviceImplementation.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Log4j2
public class JWTauthFilter extends OncePerRequestFilter {
    @Autowired
    private UserService jwtUserDetailsService;

    @Autowired
    private SecurityConstants jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");
        final String requestURI = request.getRequestURI();
        final String method = request.getMethod();

        // Skip JWT validation for refresh token endpoint
        if (requestURI != null && (requestURI.contains("/api/refresh-token") || requestURI.endsWith("/refresh-token"))) {
            System.out.println("Skipping JWT validation for refresh token request: " + method + " " + requestURI);
            chain.doFilter(request, response);
            return;
        }

        String username = null;
        String jwtToken = null;
// JWT Token is in the form "Bearer token". Remove Bearer word and get
// only the Token
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7).trim(); // Trim any whitespace
            System.out.println("Processing JWT token for URI: " + requestURI);
            try {
                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
            } catch (IllegalArgumentException e) {
                System.out.println("Unable to get JWT Token: " + e.getMessage());
            } catch (ExpiredJwtException e) {
                System.out.println("JWT Token has expired");
            } catch (Exception e) {
                System.out.println("JWT parsing error: " + e.getMessage());
            }
        }

// Once we get the token validate it.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = this.jwtUserDetailsService.loadUserByUsername(username);

// if token is valid configure Spring Security to manually set
// authentication
            if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {

                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
// After setting the Authentication in the context, we specify
// that the current user is authenticated. So it passes the
// Spring Security Configurations successfully.
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        chain.doFilter(request, response);
    }

}