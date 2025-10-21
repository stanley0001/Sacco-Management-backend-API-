package com.example.demo.userManagements.services.auth;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
        public class SecurityConstants implements Serializable {

                private static final long serialVersionUID = -2550185165626007488L;

                public static final long JWT_TOKEN_VALIDITY = 60L * 60;

                @Value("${jwt.secret}")
                private String secret;

                private SecretKey getSigningKey() {
                        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                }

                //retrieve username from jwt token
                public String getUsernameFromToken(String token) {
                        return getClaimFromToken(token, Claims::getSubject);
                }

                //retrieve expiration date from jwt token
                public Date getExpirationDateFromToken(String token) {
                        return getClaimFromToken(token, Claims::getExpiration);
                }

                public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
                        final Claims claims = getAllClaimsFromToken(token);
                        return claimsResolver.apply(claims);
                }

                //for retrieveing any information from token we will need the secret key
                private Claims getAllClaimsFromToken(String token) {
                        return Jwts.parser()
                                .verifyWith(getSigningKey())
                                .build()
                                .parseSignedClaims(token)
                                .getPayload();
                }

                //check if the token has expired
                private Boolean isTokenExpired(String token) {
                        final Date expiration = getExpirationDateFromToken(token);
                        return expiration.before(new Date());
                }

                //generate token for user
                public String generateToken(UserDetails userDetails) {
                        Map<String, Object> claims = new HashMap<>();
                        claims.put("permissions",userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
                        return doGenerateToken(claims, userDetails.getUsername());
                }

                //generate refresh token for user
                public String generateRefreshToken(UserDetails userDetails) {
                        Map<String, Object> claims = new HashMap<>();
                        claims.put("permissions",userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
                        claims.put("token_type", "refresh");
                        return doGenerateRefreshToken(claims, userDetails.getUsername());
                }

                private String doGenerateToken(Map<String, Object> claims, String subject) {

                        return Jwts.builder()
                                .claims(claims)
                                .subject(subject)
                                .issuedAt(new Date(System.currentTimeMillis()))
                                .issuer("stanLey")
                                .expiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                                .signWith(getSigningKey())
                                .compact();
                }

                private String doGenerateRefreshToken(Map<String, Object> claims, String subject) {
                        return Jwts.builder()
                                .claims(claims)
                                .subject(subject)
                                .issuedAt(new Date(System.currentTimeMillis()))
                                .issuer("stanLey")
                                .expiration(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)) // 7 days
                                .signWith(getSigningKey())
                                .compact();
                }

                //validate token
                public Boolean validateToken(String token, UserDetails userDetails) {
                        final String username = getUsernameFromToken(token);
                        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
                }


        }