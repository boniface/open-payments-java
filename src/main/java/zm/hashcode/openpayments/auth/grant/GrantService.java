package zm.hashcode.openpayments.auth.grant;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import zm.hashcode.openpayments.auth.signature.ContentDigest;
import zm.hashcode.openpayments.auth.signature.HttpSignatureService;
import zm.hashcode.openpayments.auth.signature.SignatureComponents;
import zm.hashcode.openpayments.http.core.HttpClient;
import zm.hashcode.openpayments.http.core.HttpMethod;
import zm.hashcode.openpayments.http.core.HttpRequest;

/**
 * Service for managing GNAP grant requests and responses.
 *
 * <p>
 * This service handles the complete grant flow:
 * <ol>
 * <li>Send grant request with signature</li>
 * <li>Parse grant response</li>
 * <li>Continue pending grants</li>
 * <li>Cancel grants</li>
 * </ol>
 *
 * <p>
 * Example usage:
 *
 * <pre>{@code
 * GrantService grantService = new GrantService(httpClient, signatureService, objectMapper);
 *
 * GrantRequest request = GrantRequest.builder()
 *         .accessToken(
 *                 AccessTokenRequest.builder().addAccess(Access.incomingPayment(List.of("create", "read"))).build())
 *         .client(Client.builder().key("https://example.com/jwks.json").build()).build();
 *
 * GrantResponse response = grantService.requestGrant("https://auth.example.com/grant", request).join();
 *
 * if (response.requiresInteraction()) {
 *     // Redirect user to interaction endpoint
 *     String interactUrl = response.interact().get().redirect();
 * }
 * }</pre>
 *
 * @see <a href="https://openpayments.dev/grants/">Open Payments - Grants</a>
 */
public final class GrantService {

    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String GNAP_AUTHORIZATION_PREFIX = "GNAP ";

    private final HttpClient httpClient;
    private final HttpSignatureService signatureService;
    private final ObjectMapper objectMapper;

    /**
     * Constructs a grant service.
     *
     * @param httpClient
     *            the HTTP client for making requests
     * @param signatureService
     *            the signature service for signing requests
     * @param objectMapper
     *            the JSON object mapper
     * @throws NullPointerException
     *             if any parameter is null
     */
    public GrantService(HttpClient httpClient, HttpSignatureService signatureService, ObjectMapper objectMapper) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient must not be null");
        this.signatureService = Objects.requireNonNull(signatureService, "signatureService must not be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
    }

    /**
     * Sends a grant request to the authorization server.
     *
     * @param grantEndpoint
     *            the grant endpoint URL
     * @param request
     *            the grant request
     * @return a CompletableFuture containing the grant response
     * @throws GrantException
     *             if the request fails
     */
    public CompletableFuture<GrantResponse> requestGrant(String grantEndpoint, GrantRequest request) {
        Objects.requireNonNull(grantEndpoint, "grantEndpoint must not be null");
        Objects.requireNonNull(request, "request must not be null");

        try {
            // Serialize request body
            String requestBody = objectMapper.writeValueAsString(request);

            // Generate content digest
            String contentDigest = ContentDigest.generate(requestBody);

            // Build signature components
            SignatureComponents components = SignatureComponents.builder().method("POST").targetUri(grantEndpoint)
                    .addHeader("content-type", CONTENT_TYPE_JSON).addHeader("content-digest", contentDigest)
                    .addHeader("content-length", String.valueOf(requestBody.length())).body(requestBody).build();

            // Create signature headers
            Map<String, String> signatureHeaders = signatureService.createSignatureHeaders(components);

            // Build HTTP request with all headers
            Map<String, String> headers = new java.util.concurrent.ConcurrentHashMap<>();
            headers.put("Content-Type", CONTENT_TYPE_JSON);
            headers.put("Content-Digest", contentDigest);
            headers.put("Content-Length", String.valueOf(requestBody.length()));
            headers.put("Signature-Input", signatureHeaders.get("signature-input"));
            headers.put("Signature", signatureHeaders.get("signature"));

            HttpRequest httpRequest = HttpRequest.builder().method(HttpMethod.POST).uri(grantEndpoint).headers(headers)
                    .body(requestBody).build();

            // Send request and parse response
            return httpClient.execute(httpRequest).thenApply(response -> {
                if (!response.isSuccessful()) {
                    throw new GrantException(
                            "Grant request failed: " + response.statusCode() + " - " + response.body());
                }

                try {
                    return objectMapper.readValue(response.body(), GrantResponse.class);
                } catch (JsonProcessingException e) {
                    throw new GrantException("Failed to parse grant response", e);
                }
            });

        } catch (JsonProcessingException e) {
            return CompletableFuture.failedFuture(new GrantException("Failed to serialize grant request", e));
        }
    }

    /**
     * Continues a pending grant request.
     *
     * @param continueInfo
     *            the continue information from the grant response
     * @param interactRef
     *            the interaction reference (from callback)
     * @return a CompletableFuture containing the updated grant response
     * @throws GrantException
     *             if the continue request fails
     */
    public CompletableFuture<GrantResponse> continueGrant(Continue continueInfo, String interactRef) {
        Objects.requireNonNull(continueInfo, "continueInfo must not be null");
        Objects.requireNonNull(interactRef, "interactRef must not be null");

        try {
            String continueUri = continueInfo.uri();
            String continueToken = continueInfo.token();

            // Build request body with interact_ref
            Map<String, String> requestBodyMap = Map.of("interact_ref", interactRef);
            String requestBody = objectMapper.writeValueAsString(requestBodyMap);

            // Generate content digest
            String contentDigest = ContentDigest.generate(requestBody);

            // Build signature components (include continue token as authorization)
            SignatureComponents components = SignatureComponents.builder().method("POST").targetUri(continueUri)
                    .addHeader("authorization", GNAP_AUTHORIZATION_PREFIX + continueToken)
                    .addHeader("content-type", CONTENT_TYPE_JSON).addHeader("content-digest", contentDigest)
                    .addHeader("content-length", String.valueOf(requestBody.length())).body(requestBody).build();

            // Create signature headers
            Map<String, String> signatureHeaders = signatureService.createSignatureHeaders(components);

            // Build HTTP request with all headers
            Map<String, String> headers = new java.util.concurrent.ConcurrentHashMap<>();
            headers.put("Authorization", GNAP_AUTHORIZATION_PREFIX + continueToken);
            headers.put("Content-Type", CONTENT_TYPE_JSON);
            headers.put("Content-Digest", contentDigest);
            headers.put("Content-Length", String.valueOf(requestBody.length()));
            headers.put("Signature-Input", signatureHeaders.get("signature-input"));
            headers.put("Signature", signatureHeaders.get("signature"));

            HttpRequest httpRequest = HttpRequest.builder().method(HttpMethod.POST).uri(continueUri).headers(headers)
                    .body(requestBody).build();

            // Send request and parse response
            return httpClient.execute(httpRequest).thenApply(response -> {
                if (!response.isSuccessful()) {
                    throw new GrantException(
                            "Continue grant failed: " + response.statusCode() + " - " + response.body());
                }

                try {
                    return objectMapper.readValue(response.body(), GrantResponse.class);
                } catch (JsonProcessingException e) {
                    throw new GrantException("Failed to parse grant response", e);
                }
            });

        } catch (JsonProcessingException e) {
            return CompletableFuture.failedFuture(new GrantException("Failed to serialize continue request", e));
        }
    }

    /**
     * Cancels a pending grant.
     *
     * @param continueInfo
     *            the continue information
     * @return a CompletableFuture that completes when the grant is cancelled
     * @throws GrantException
     *             if the cancellation fails
     */
    public CompletableFuture<Void> cancelGrant(Continue continueInfo) {
        Objects.requireNonNull(continueInfo, "continueInfo must not be null");

        String continueUri = continueInfo.uri();
        String continueToken = continueInfo.token();

        // Build signature components
        SignatureComponents components = SignatureComponents.builder().method("DELETE").targetUri(continueUri)
                .addHeader("authorization", GNAP_AUTHORIZATION_PREFIX + continueToken).build();

        // Create signature headers
        Map<String, String> signatureHeaders = signatureService.createSignatureHeaders(components);

        // Build HTTP request
        Map<String, String> headers = new java.util.concurrent.ConcurrentHashMap<>();
        headers.put("Authorization", GNAP_AUTHORIZATION_PREFIX + continueToken);
        headers.put("Signature-Input", signatureHeaders.get("signature-input"));
        headers.put("Signature", signatureHeaders.get("signature"));

        HttpRequest httpRequest = HttpRequest.builder().method(HttpMethod.DELETE).uri(continueUri).headers(headers)
                .build();

        // Send request
        return httpClient.execute(httpRequest).thenApply(response -> {
            if (!response.isSuccessful()) {
                throw new GrantException("Cancel grant failed: " + response.statusCode() + " - " + response.body());
            }
            return null;
        });
    }
}
