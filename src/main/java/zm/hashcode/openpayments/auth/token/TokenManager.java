package zm.hashcode.openpayments.auth.token;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import zm.hashcode.openpayments.auth.grant.AccessTokenResponse;
import zm.hashcode.openpayments.http.core.HttpClient;
import zm.hashcode.openpayments.http.core.HttpMethod;
import zm.hashcode.openpayments.http.core.HttpRequest;

/**
 * Service for managing access token lifecycle operations.
 *
 * <p>
 * This service handles token management operations including:
 * <ul>
 * <li>Token rotation - obtaining a new token with the same rights</li>
 * <li>Token revocation - revoking/deleting an existing token</li>
 * </ul>
 *
 * <p>
 * Token management operations require the access token value for authentication and use the management URL provided in
 * the original token response.
 *
 * <p>
 * Example usage:
 *
 * <pre>{@code
 * TokenManager tokenManager = new TokenManager(httpClient, objectMapper);
 *
 * // Rotate an access token
 * AccessTokenResponse newToken = tokenManager.rotateToken(currentToken).join();
 *
 * // Revoke an access token
 * tokenManager.revokeToken(token).join();
 * }</pre>
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc9635#section-6">RFC 9635 Section 6 - Token Management</a>
 */
public final class TokenManager {

    private static final String GNAP_AUTHORIZATION_PREFIX = "GNAP ";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    /**
     * Constructs a token manager.
     *
     * @param httpClient
     *            the HTTP client for making requests
     * @param objectMapper
     *            the JSON object mapper
     * @throws NullPointerException
     *             if any parameter is null
     */
    public TokenManager(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient must not be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
    }

    /**
     * Rotates an access token to obtain a new token with the same rights.
     *
     * <p>
     * Token rotation is used when a token is expiring or needs to be refreshed. The authorization server validates the
     * rotation request and returns a new access token with the same permissions. The old token may continue to work
     * after rotation if the "durable" flag was set in the original token response.
     *
     * @param currentToken
     *            the current access token to rotate
     * @return a CompletableFuture containing the new access token
     * @throws TokenException
     *             if the rotation fails
     * @throws NullPointerException
     *             if currentToken is null
     */
    public CompletableFuture<AccessTokenResponse> rotateToken(AccessTokenResponse currentToken) {
        Objects.requireNonNull(currentToken, "currentToken must not be null");

        String manageUrl = currentToken.manage();
        String tokenValue = currentToken.value();

        // Create headers with GNAP authorization
        Map<String, String> headers = new java.util.concurrent.ConcurrentHashMap<>();
        headers.put("Authorization", GNAP_AUTHORIZATION_PREFIX + tokenValue);

        // Build HTTP POST request to management URL
        HttpRequest httpRequest = HttpRequest.builder().method(HttpMethod.POST).uri(manageUrl).headers(headers).build();

        // Send request and parse response
        return httpClient.execute(httpRequest).thenApply(response -> {
            if (!response.isSuccessful()) {
                throw new TokenException("Token rotation failed: " + response.statusCode() + " - " + response.body());
            }

            try {
                return objectMapper.readValue(response.body(), AccessTokenResponse.class);
            } catch (JsonProcessingException e) {
                throw new TokenException("Failed to parse token rotation response", e);
            }
        });
    }

    /**
     * Revokes an access token, invalidating it immediately.
     *
     * <p>
     * Token revocation permanently invalidates the access token. After revocation, any attempts to use the token will
     * fail. This operation cannot be undone.
     *
     * @param token
     *            the access token to revoke
     * @return a CompletableFuture that completes when the token is revoked
     * @throws TokenException
     *             if the revocation fails
     * @throws NullPointerException
     *             if token is null
     */
    public CompletableFuture<Void> revokeToken(AccessTokenResponse token) {
        Objects.requireNonNull(token, "token must not be null");

        String manageUrl = token.manage();
        String tokenValue = token.value();

        // Create headers with GNAP authorization
        Map<String, String> headers = new java.util.concurrent.ConcurrentHashMap<>();
        headers.put("Authorization", GNAP_AUTHORIZATION_PREFIX + tokenValue);

        // Build HTTP DELETE request to management URL
        HttpRequest httpRequest = HttpRequest.builder().method(HttpMethod.DELETE).uri(manageUrl).headers(headers)
                .build();

        // Send request
        return httpClient.execute(httpRequest).thenApply(response -> {
            if (!response.isSuccessful()) {
                throw new TokenException("Token revocation failed: " + response.statusCode() + " - " + response.body());
            }
            return null;
        });
    }
}
