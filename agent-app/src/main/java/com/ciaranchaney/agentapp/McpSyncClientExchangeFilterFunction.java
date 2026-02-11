package com.ciaranchaney.agentapp;

import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.security.oauth2.client.ClientCredentialsOAuth2AuthorizedClientProvider;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * A wrapper around Spring Security's ServletOAuth2AuthorizedClientExchangeFilterFunction,
 * which adds OAuth2 access_tokens to requests sent to the MCP server.
 *
 * The end goal is to use access_token that represent the end-user's permissions. Those
 * tokens are obtained using the authorization_code OAuth2 flow, but it requires a
 * user to be present and using their browser.
 *
 * By default, the MCP tools are initialized on app startup, so some requests to the MCP
 * server happen, to establish the session (/sse), and to send the initialize and
 * e.g. tools/list requests. For this to work, we need an access_token, but we
 * cannot get one using the authorization_code flow (no user is present). Instead, we rely
 * on the OAuth2 client_credentials flow for machine-to-machine communication.
 */
@Component
@Profile("!dev")
public class McpSyncClientExchangeFilterFunction implements ExchangeFilterFunction {

    private static final Logger log = LoggerFactory.getLogger(McpSyncClientExchangeFilterFunction.class);

    private final ClientCredentialsOAuth2AuthorizedClientProvider clientCredentialTokenProvider =
            new ClientCredentialsOAuth2AuthorizedClientProvider();

    private final ServletOAuth2AuthorizedClientExchangeFilterFunction delegate;
    private final ClientRegistrationRepository clientRegistrationRepository;

    // Must match registration id in application.yml
    // spring.security.oauth2.client.registration.mcp-client.authorization-grant-type=client_credentials
    private static final String CLIENT_CREDENTIALS_CLIENT_REGISTRATION_ID = "mcp-client";

    public McpSyncClientExchangeFilterFunction(
            OAuth2AuthorizedClientManager clientManager,
            ClientRegistrationRepository clientRegistrationRepository) {
        this.delegate = new ServletOAuth2AuthorizedClientExchangeFilterFunction(clientManager);
        this.delegate.setDefaultClientRegistrationId(CLIENT_CREDENTIALS_CLIENT_REGISTRATION_ID);
        this.clientRegistrationRepository = clientRegistrationRepository;

        log.info("========================================");
        log.info("🔐 McpSyncClientExchangeFilterFunction initialized");
        log.info("📡 Will add OAuth2 tokens to ALL MCP requests");
        log.info("========================================");
    }

    /**
     * Add an access_token to the request sent to the MCP server.
     *
     * If we are in the context of a ServletRequest, this means a user is currently
     * involved, and we should add a token on behalf of the user, using the
     * authorization_code grant. This typically happens when doing an MCP tools/call.
     *
     * If we are NOT in the context of a ServletRequest, this means we are in the startup
     * phases of the application, where the MCP client is initialized. We use the
     * client_credentials grant in that case, and add a token on behalf of the
     * application itself.
     */
    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes) {
            log.debug("🔐 Using authorization_code flow (user context) for: {}", request.url());
            return this.delegate.filter(request, next);
        } else {
            log.debug("🔐 Using client_credentials flow (app startup) for: {}", request.url());
            var accessToken = getClientCredentialsAccessToken();

            if (accessToken != null) {
                log.info("✅ Adding OAuth2 Bearer token to MCP request: {}", request.url());
                var requestWithToken = ClientRequest.from(request)
                        .headers(headers -> headers.setBearerAuth(accessToken))
                        .build();
                return next.exchange(requestWithToken);
            } else {
                log.error("❌ Failed to obtain OAuth2 token for MCP request: {}", request.url());
                return next.exchange(request);
            }
        }
    }

    private String getClientCredentialsAccessToken() {
        try {
            ClientRegistration clientRegistration = this.clientRegistrationRepository
                    .findByRegistrationId(CLIENT_CREDENTIALS_CLIENT_REGISTRATION_ID);

            if (clientRegistration == null) {
                log.error("❌ Client registration '{}' not found", CLIENT_CREDENTIALS_CLIENT_REGISTRATION_ID);
                return null;
            }

            var authRequest = OAuth2AuthorizationContext.withClientRegistration(clientRegistration)
                    .principal(new AnonymousAuthenticationToken("client-credentials-client",
                            "client-credentials-client",
                            AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")))
                    .build();

            var authorizedClient = this.clientCredentialTokenProvider.authorize(authRequest);

            if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
                String token = authorizedClient.getAccessToken().getTokenValue();
                log.debug("✅ Client credentials token obtained: {}...",
                        token.substring(0, Math.min(20, token.length())));
                return token;
            }
        } catch (Exception e) {
            log.error("❌ Error obtaining client credentials token: {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * Configure a WebClient to use this exchange filter function.
     */
    public Consumer<org.springframework.web.reactive.function.client.WebClient.Builder> configuration() {
        return builder -> builder.defaultRequest(this.delegate.defaultRequest()).filter(this);
    }
}
