package zm.hashcode.openpayments.auth;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Service for managing grants and access tokens in the Open Payments system.
 *
 * <p>
 * This service handles the GNAP (Grant Negotiation and Authorization Protocol) flow for obtaining and managing access
 * tokens.
 */
public interface GrantService {

    /**
     * Requests a new grant.
     *
     * @param requestBuilder
     *            a consumer to build the grant request
     * @return a CompletableFuture containing the grant
     */
    CompletableFuture<Grant> request(Consumer<GrantRequest.Builder> requestBuilder);

    /**
     * Continues an existing grant flow.
     *
     * @param continueUri
     *            the continuation URI from a previous grant response
     * @param continueToken
     *            the continuation token
     * @param interactRef
     *            the interaction reference (if user interaction was required)
     * @return a CompletableFuture containing the updated grant
     */
    CompletableFuture<Grant> continueGrant(String continueUri, String continueToken, String interactRef);

    /**
     * Revokes an access token.
     *
     * @param manageUrl
     *            the token management URL
     * @param accessToken
     *            the token to revoke
     * @return a CompletableFuture that completes when the token is revoked
     */
    CompletableFuture<Void> revokeToken(String manageUrl, String accessToken);

    /**
     * Rotates an access token to obtain a new one.
     *
     * @param manageUrl
     *            the token management URL
     * @param accessToken
     *            the current token
     * @return a CompletableFuture containing the new access token
     */
    CompletableFuture<AccessToken> rotateToken(String manageUrl, String accessToken);
}
