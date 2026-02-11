package com.ciaranchaney.agentapp;

import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.web.reactive.function.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;

/**
 * Configuration for WebClient with OAuth2 support for MCP client.
 * This uses a custom ExchangeFilterFunction to add OAuth2 tokens to MCP requests.
 */
@Configuration
@Profile("!dev")
public class OAuth2McpClientConfig {

    private static final Logger log = LoggerFactory.getLogger(OAuth2McpClientConfig.class);

    @PostConstruct
    public void init() {
        log.info("========================================");
        log.info("🔐 OAuth2McpClientConfig is ACTIVE");
        log.info("📡 MCP Client will use OAuth2 authentication");
        log.info("========================================");
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientRepository authorizedClientRepository) {

        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials()
                        .refreshToken()
                        .build();

        DefaultOAuth2AuthorizedClientManager authorizedClientManager =
                new DefaultOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientRepository);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        log.info("✓ OAuth2AuthorizedClientManager configured with client_credentials flow");
        return authorizedClientManager;
    }

    /**
     * Primary WebClient.Builder bean that includes OAuth2 support.
     * This will be used by ALL WebClient instances in the application, including Spring AI MCP.
     */
    @Bean
    @Primary
    public WebClient.Builder webClientBuilder(McpSyncClientExchangeFilterFunction filterFunction) {
        log.info("✓ PRIMARY WebClient.Builder created with OAuth2 support");
        return WebClient.builder().apply(filterFunction.configuration());
    }

    /**
     * WebClientCustomizer that will be automatically applied to all WebClient.Builder instances.
     * This is a backup approach to ensure OAuth2 is applied even if Spring AI creates its own builder.
     */
    @Bean
    public WebClientCustomizer oauth2WebClientCustomizer(McpSyncClientExchangeFilterFunction filterFunction) {
        log.info("✓ WebClientCustomizer registered to add OAuth2 to ALL WebClients");
        return builder -> builder.apply(filterFunction.configuration());
    }
}
