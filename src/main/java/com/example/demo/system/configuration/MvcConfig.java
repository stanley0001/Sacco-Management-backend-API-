package com.example.demo.system.configuration;

import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Configuration for serving the Angular SPA
 * All non-API routes will be forwarded to index.html for Angular routing
 */
@Configuration
public class MvcConfig {
    
    /**
     * Handle 404 errors by forwarding to index.html
     * This ensures deep linking works for Angular routes
     */
    @Bean
    public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> containerCustomizer() {
        return container -> {
            container.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND, "/index.html"));
        };
    }
    
    /**
     * Controller to forward all non-API routes to index.html for Angular SPA routing
     * This allows Angular to handle client-side routing
     */
    @Controller
    public static class SpaRoutingController {
        
        @RequestMapping(value = {
            "/",
            "/admin/**",
            "/login",
            "/dashboard/**",
            "/clients/**",
            "/products/**",
            "/reports/**",
            "/users/**",
            "/communication/**",
            "/bps/**"
        })
        public String forward() {
            return "forward:/index.html";
        }
    }
}


