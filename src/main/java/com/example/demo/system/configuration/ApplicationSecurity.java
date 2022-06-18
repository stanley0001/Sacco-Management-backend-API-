package com.example.demo.configuration;

import com.example.demo.services.auth.CustomAuthenticationFailureHandler;
import com.example.demo.services.auth.JWTauthFilter;
import com.example.demo.services.auth.authService;
import com.google.inject.internal.util.ImmutableList;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true, proxyTargetClass = true)
@RequiredArgsConstructor
@Log4j2
@EnableWebSecurity
public class ApplicationSecurity extends WebSecurityConfigurerAdapter {
    @Autowired
    private authService authService;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private JWTauthFilter jwTauthFilter;
    @Bean
    CorsConfigurationSource corsConfigurationSource()
    {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("HEAD","GET","POST","PUT","OPTION"));
        configuration.setAllowedHeaders(ImmutableList.of("Authorization", "Cache-Control", "Content-Type", "App-Key"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        //bypass pre flight request
        httpSecurity.cors();
// We don't need CSRF for this example
        httpSecurity.csrf().disable()
// dont authenticate this particular request
                .authorizeRequests().antMatchers("/authenticate",
                "/users/resetPassword",
                "/customers/magicListener",
                "/v2/api-docs",
                "/configuration/ui",
                "/swagger-resources/**",
                "/configuration/security",
                "/swagger-ui.html",
                "/webjars/**").permitAll()
// all other requests need to be authenticated
        .anyRequest().authenticated().and()
// make sure we use stateless session; session won't be used to
// store user's state.
        .exceptionHandling().authenticationEntryPoint(authService).and().sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

// Add a filter to validate the tokens with every request
        httpSecurity.addFilterBefore(jwTauthFilter, UsernamePasswordAuthenticationFilter.class);
    }



    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder());
    }


    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception{
        return super.authenticationManagerBean();
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return new CustomAuthenticationFailureHandler();
    }

}


