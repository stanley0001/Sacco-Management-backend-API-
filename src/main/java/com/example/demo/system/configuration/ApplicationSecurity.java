package com.example.demo.system.configuration;

import com.example.demo.userManagements.services.auth.CustomAuthenticationFailureHandler;
import com.example.demo.userManagements.services.auth.JWTauthFilter;
import com.example.demo.userManagements.services.auth.authService;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity(securedEnabled = true, proxyTargetClass = true)
@RequiredArgsConstructor
@Log4j2
@EnableWebSecurity
public class ApplicationSecurity {
    @Autowired
    private authService authService;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private JWTauthFilter jwTauthFilter;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Use allowedOriginPatterns instead of allowedOrigins when allowCredentials is true
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    /* setAllowedHeaders is important! Without it, OPTIONS preflight request
    will fail with 403 Invalid CORS request */
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type", "App-Key"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        //bypass pre flight request and disable CSRF
        httpSecurity
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                // dont authenticate this particular request
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/authenticate",
                                "/auth",
                                "/api/authenticate",
                                "/",
                                "/index.html",
                                "/assets/**",
                                "/styles.*.css",
                                "/*.woff",
                                "/*.ttf",
                                "/*.woff2",
                                "/main.*.js",
                                "/runtime.*.js",
                                "/polyfills.*.js",
                                "/favicon.ico",
                                "/users/resetPassword",
                                "/brains/**",
                                "/customers/customPayment",
                                "/customers/whatsappComm",
                                "/welcome/**",
                                "/customers/magicListener",
                                // Swagger/OpenAPI endpoints
                                "/v2/api-docs/**",
                                "api-docs/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/configuration/ui",
                                "/configuration/security",
                                "/webjars/**").permitAll()
                        // all other requests need to be authenticated
                        .anyRequest().authenticated()
                )
                // make sure we use stateless session; session won't be used to store user's state.
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authService))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        // Add a filter to validate the tokens with every request
        httpSecurity.addFilterBefore(jwTauthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return httpSecurity.build();
    }



    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(bCryptPasswordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return new CustomAuthenticationFailureHandler();
    }

}


