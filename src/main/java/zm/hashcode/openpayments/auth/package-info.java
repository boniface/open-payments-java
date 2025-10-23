/**
 * Authentication and authorization components for Open Payments.
 *
 * <p>
 * This package provides comprehensive support for Open Payments authentication including:
 * <ul>
 * <li><b>GNAP Protocol</b> - Grant Negotiation and Authorization Protocol (RFC 9635)</li>
 * <li><b>HTTP Signatures</b> - Message signature authentication (RFC 9421)</li>
 * <li><b>Token Management</b> - Access token rotation and revocation</li>
 * <li><b>Cryptographic Keys</b> - Ed25519 key generation and management</li>
 * </ul>
 *
 * <h2>Key Components</h2>
 *
 * <h3>Grant Management ({@link zm.hashcode.openpayments.auth.grant})</h3>
 * <p>
 * Implements the GNAP protocol for obtaining access tokens:
 *
 * <pre>{@code
 * GrantService grantService = new GrantService(httpClient, signatureService, objectMapper);
 *
 * // Request a grant
 * GrantRequest request = GrantRequest.builder()
 *         .accessToken(AccessTokenRequest.builder().access(Access.incomingPayment(List.of("create", "read"))).build())
 *         .client(Client.builder().key(clientKey.jwk()).build()).build();
 *
 * GrantResponse response = grantService.requestGrant(authServerUrl, request).join();
 * }</pre>
 *
 * <h3>HTTP Signatures ({@link zm.hashcode.openpayments.auth.signature})</h3>
 * <p>
 * Automatic request signing with Ed25519:
 *
 * <pre>{@code
 * ClientKey clientKey = ClientKeyGenerator.generate("my-key-id");
 * HttpSignatureService signatureService = new HttpSignatureService(clientKey);
 *
 * // Sign a request
 * HttpRequest signedRequest = signatureService.signRequest(request);
 * }</pre>
 *
 * <h3>Token Management ({@link zm.hashcode.openpayments.auth.token})</h3>
 * <p>
 * Manage access token lifecycle:
 *
 * <pre>{@code
 * TokenManager tokenManager = new TokenManager(httpClient, objectMapper);
 *
 * // Rotate token before expiration
 * AccessTokenResponse newToken = tokenManager.rotateToken(currentToken).join();
 *
 * // Revoke token when done
 * tokenManager.revokeToken(token).join();
 * }</pre>
 *
 * <h2>Security Considerations</h2>
 *
 * <ul>
 * <li>Private keys must be stored securely (e.g., hardware security module, encrypted keystore)</li>
 * <li>Access tokens should be rotated regularly before expiration</li>
 * <li>Always revoke tokens when they are no longer needed</li>
 * <li>Use HTTPS for all Open Payments API communication</li>
 * <li>Validate all responses to detect tampering or replay attacks</li>
 * </ul>
 *
 * @see <a href="https://openpayments.dev/grants/">Open Payments - Grants</a>
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc9635">RFC 9635 - GNAP Core Protocol</a>
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc9421">RFC 9421 - HTTP Message Signatures</a>
 */
package zm.hashcode.openpayments.auth;
