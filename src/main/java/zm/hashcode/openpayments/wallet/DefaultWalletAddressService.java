package zm.hashcode.openpayments.wallet;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import zm.hashcode.openpayments.http.core.HttpClient;
import zm.hashcode.openpayments.http.core.HttpMethod;
import zm.hashcode.openpayments.http.core.HttpRequest;
import zm.hashcode.openpayments.http.core.HttpResponse;

/**
 * Default implementation of {@link WalletAddressService}.
 *
 * <p>
 * This implementation fetches wallet addresses and public keys from Open Payments servers using HTTP GET requests.
 * Wallet addresses are public resources and do not require authentication.
 *
 * <p>
 * Thread-safe and can be reused across multiple requests.
 */
public record DefaultWalletAddressService(HttpClient httpClient,
        ObjectMapper objectMapper) implements WalletAddressService {

    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String JWKS_PATH = "/jwks.json";

    /**
     * Creates a new DefaultWalletAddressService.
     *
     * @param httpClient
     *            the HTTP client for API communication
     * @param objectMapper
     *            the object mapper for JSON serialization/deserialization
     * @throws NullPointerException
     *             if any parameter is null
     */
    public DefaultWalletAddressService {
        Objects.requireNonNull(httpClient, "httpClient must not be null");
        Objects.requireNonNull(objectMapper, "objectMapper must not be null");
    }

    @Override
    public CompletableFuture<WalletAddress> get(String url) {
        Objects.requireNonNull(url, "url must not be null");
        return get(URI.create(url));
    }

    @Override
    public CompletableFuture<WalletAddress> get(URI uri) {
        Objects.requireNonNull(uri, "uri must not be null");

        HttpRequest request = HttpRequest.builder().method(HttpMethod.GET).uri(uri).header("Accept", CONTENT_TYPE_JSON)
                .build();

        return httpClient.execute(request).thenApply(response -> {
            validateResponse(response, "Failed to retrieve wallet address");
            return parseWalletAddress(response.body());
        });
    }

    @Override
    public CompletableFuture<PublicKeySet> getKeys(String walletAddressUrl) {
        Objects.requireNonNull(walletAddressUrl, "walletAddressUrl must not be null");

        // Construct JWKS URL by appending /jwks.json to the wallet address
        URI walletUri = URI.create(walletAddressUrl);
        String jwksUrl = buildJwksUrl(walletUri);

        HttpRequest request = HttpRequest.builder().method(HttpMethod.GET).uri(URI.create(jwksUrl))
                .header("Accept", CONTENT_TYPE_JSON).build();

        return httpClient.execute(request).thenApply(response -> {
            validateResponse(response, "Failed to retrieve public keys");
            return parsePublicKeySet(response.body());
        });
    }

    private String buildJwksUrl(URI walletUri) {
        String path = walletUri.getPath();
        // Ensure path doesn't end with slash before appending /jwks.json
        if (path != null && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        String scheme = walletUri.getScheme();
        String authority = walletUri.getAuthority();
        return scheme + "://" + authority + (path != null ? path : "") + JWKS_PATH;
    }

    private void validateResponse(HttpResponse response, String errorMessage) {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new WalletAddressException(
                    String.format("%s: HTTP %d - %s", errorMessage, response.statusCode(), response.body()));
        }
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException") // Jackson can throw various exceptions
    private WalletAddress parseWalletAddress(String json) {
        try {
            // Parse JSON into a map first to handle the structure
            Map<String, Object> data = objectMapper.readValue(json, new TypeReference<>() {
            });

            return WalletAddress.builder().id((String) data.get("id")).assetCode((String) data.get("assetCode"))
                    .assetScale(((Number) data.get("assetScale")).intValue())
                    .authServer((String) data.get("authServer")).resourceServer((String) data.get("resourceServer"))
                    .publicName((String) data.get("publicName")).build();
        } catch (Exception e) {
            throw new WalletAddressException("Failed to parse wallet address: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings({"PMD.AvoidCatchingGenericException", "PMD.ExceptionAsFlowControl"})
    // Jackson can throw various exceptions, and invalid JWKS throws for control flow
    private PublicKeySet parsePublicKeySet(String json) {
        try {
            // Parse the JWKS structure: {"keys": [...]}
            Map<String, Object> jwks = objectMapper.readValue(json, new TypeReference<>() {
            });

            @SuppressWarnings("unchecked")
            java.util.List<Map<String, String>> keysList = (java.util.List<Map<String, String>>) jwks.get("keys");

            if (keysList == null) {
                throw new WalletAddressException("Invalid JWKS structure: missing 'keys' array");
            }

            java.util.List<PublicKey> keys = keysList.stream()
                    .map(keyData -> PublicKey.builder().kid(keyData.get("kid")).kty(keyData.get("kty"))
                            .use(keyData.get("use")).alg(keyData.get("alg")).x(keyData.get("x")).build())
                    .toList();

            return PublicKeySet.of(keys);
        } catch (Exception e) {
            throw new WalletAddressException("Failed to parse public key set: " + e.getMessage(), e);
        }
    }
}
