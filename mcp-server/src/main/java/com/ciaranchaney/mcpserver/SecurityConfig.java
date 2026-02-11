package com.ciaranchaney.mcpserver;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Profile("!dev")  // Don't activate in dev profile
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}")
    private String issuerUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:}")
    private String jwkSetUri;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Allow H2 console for development
                .requestMatchers("/h2-console/**").permitAll()
                // Allow health check endpoints
                .requestMatchers("/actuator/health").permitAll()
                // ⚠️ LIMITATION: Spring AI 1.1.0 MCP client doesn't support custom headers
                // SSE and MCP endpoints must be permitAll for now
                // Authentication is handled at the tool level via custom headers in tool calls
                .requestMatchers("/sse/**").permitAll()
                .requestMatchers("/mcp/**").permitAll()
                // Require authentication for all other endpoints
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> {})
            )
            // Disable CSRF for stateless API
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**", "/sse/**", "/mcp/**")
                .disable()
            )
            // Allow frames for H2 console
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        // If using a specific JWK Set URI (recommended for production)
        if (jwkSetUri != null && !jwkSetUri.isEmpty()) {
            return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        }
        // Fallback to issuer URI
        if (issuerUri != null && !issuerUri.isEmpty()) {
            return NimbusJwtDecoder.withIssuerLocation(issuerUri).build();
        }
        throw new IllegalStateException("Either jwk-set-uri or issuer-uri must be configured");
    }
}
