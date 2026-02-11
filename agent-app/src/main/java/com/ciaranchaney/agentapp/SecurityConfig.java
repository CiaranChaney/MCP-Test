package com.ciaranchaney.agentapp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for Agent App (OAuth2 Client).
 *
 * The Agent App acts as a client that calls the MCP Server.
 * It needs OAuth2 client support to obtain tokens.
 */
@Configuration
@EnableWebSecurity
@Profile("!dev")  // Don't activate in dev profile
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            .oauth2Client(Customizer.withDefaults())  // ← Enable OAuth2 client support
            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
