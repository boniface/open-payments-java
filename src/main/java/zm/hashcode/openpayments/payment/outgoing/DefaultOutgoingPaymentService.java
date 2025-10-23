package zm.hashcode.openpayments.payment.outgoing;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import zm.hashcode.openpayments.http.core.HttpClient;
import zm.hashcode.openpayments.http.core.HttpMethod;
import zm.hashcode.openpayments.http.core.HttpRequest;
import zm.hashcode.openpayments.http.core.HttpResponse;
import zm.hashcode.openpayments.model.PaginatedResult;

/**
 * Default implementation of {@link OutgoingPaymentService}.
 *
 * <p>
 * This implementation communicates with Open Payments resource servers using authenticated HTTP requests. All methods
 * require GNAP access tokens for authentication.
 *
 * <p>
 * Thread-safe and can be reused across multiple requests.
 */
public final class DefaultOutgoingPaymentService implements OutgoingPaymentService {

    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String ACCEPT_HEADER = "Accept";
    private static final String OUTGOING_PAYMENTS_PATH = "/outgoing-payments";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new DefaultOutgoingPaymentService.
     *
     * @param httpClient
     *            the HTTP client for API communication
     * @param objectMapper
     *            the object mapper for JSON serialization/deserialization
     * @throws NullPointerException
     *             if any parameter is null
     */
    public DefaultOutgoingPaymentService(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient must not be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
    }

    @Override
    public CompletableFuture<OutgoingPayment> create(Consumer<OutgoingPaymentRequest.Builder> requestBuilder) {
        Objects.requireNonNull(requestBuilder, "requestBuilder must not be null");

        OutgoingPaymentRequest.Builder builder = OutgoingPaymentRequest.builder();
        requestBuilder.accept(builder);
        OutgoingPaymentRequest request = builder.build();

        // Construct the outgoing payments URL for the wallet address
        String url = buildOutgoingPaymentsUrl(request.getWalletAddress());

        String requestBody = serializeRequest(request);

        HttpRequest httpRequest = HttpRequest.builder().method(HttpMethod.POST).uri(URI.create(url))
                .header("Content-Type", CONTENT_TYPE_JSON).header(ACCEPT_HEADER, CONTENT_TYPE_JSON).body(requestBody)
                .build();

        return httpClient.execute(httpRequest).thenApply(response -> {
            validateResponse(response, "Failed to create outgoing payment");
            return parseOutgoingPayment(response.body());
        });
    }

    @Override
    public CompletableFuture<OutgoingPayment> get(String url) {
        Objects.requireNonNull(url, "url must not be null");
        return get(URI.create(url));
    }

    @Override
    public CompletableFuture<OutgoingPayment> get(URI uri) {
        Objects.requireNonNull(uri, "uri must not be null");

        HttpRequest request = HttpRequest.builder().method(HttpMethod.GET).uri(uri)
                .header(ACCEPT_HEADER, CONTENT_TYPE_JSON).build();

        return httpClient.execute(request).thenApply(response -> {
            validateResponse(response, "Failed to retrieve outgoing payment");
            return parseOutgoingPayment(response.body());
        });
    }

    @Override
    public CompletableFuture<PaginatedResult<OutgoingPayment>> list(String walletAddress) {
        return list(walletAddress, null, 20);
    }

    @Override
    public CompletableFuture<PaginatedResult<OutgoingPayment>> list(String walletAddress, String cursor, int limit) {
        Objects.requireNonNull(walletAddress, "walletAddress must not be null");

        String url = buildOutgoingPaymentsUrl(URI.create(walletAddress));

        // Add query parameters
        @SuppressWarnings("PMD.UseConcurrentHashMap") // HashMap is local and not shared
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("first", String.valueOf(limit));
        if (cursor != null) {
            queryParams.put("cursor", cursor);
        }

        String fullUrl = appendQueryParams(url, queryParams);

        HttpRequest request = HttpRequest.builder().method(HttpMethod.GET).uri(URI.create(fullUrl))
                .header(ACCEPT_HEADER, CONTENT_TYPE_JSON).build();

        return httpClient.execute(request).thenApply(response -> {
            validateResponse(response, "Failed to list outgoing payments");
            return parsePaginatedResult(response.body());
        });
    }

    private String buildOutgoingPaymentsUrl(URI walletAddress) {
        String path = walletAddress.getPath();
        // Ensure path doesn't end with slash before appending /outgoing-payments
        if (path != null && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        String scheme = walletAddress.getScheme();
        String authority = walletAddress.getAuthority();
        return scheme + "://" + authority + (path != null ? path : "") + OUTGOING_PAYMENTS_PATH;
    }

    private String appendQueryParams(String url, Map<String, String> params) {
        if (params.isEmpty()) {
            return url;
        }

        StringBuilder result = new StringBuilder(url);
        result.append('?');

        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                result.append('&');
            }
            result.append(entry.getKey()).append('=').append(entry.getValue());
            first = false;
        }

        return result.toString();
    }

    private void validateResponse(HttpResponse response, String errorMessage) {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new OutgoingPaymentException(
                    String.format("%s: HTTP %d - %s", errorMessage, response.statusCode(), response.body()));
        }
    }

    @SuppressWarnings({"PMD.AvoidCatchingGenericException", "PMD.UseConcurrentHashMap"})
    // Jackson can throw various exceptions, HashMap is local and not shared
    private String serializeRequest(OutgoingPaymentRequest request) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("walletAddress", request.getWalletAddress().toString());
            data.put("quoteId", request.getQuoteId().toString());
            if (request.getMetadata() != null) {
                data.put("metadata", request.getMetadata());
            }
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            throw new OutgoingPaymentException("Failed to serialize request: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException") // Jackson can throw various exceptions
    private OutgoingPayment parseOutgoingPayment(String json) {
        try {
            Map<String, Object> data = objectMapper.readValue(json, new TypeReference<>() {
            });

            OutgoingPayment.Builder builder = OutgoingPayment.builder().id((String) data.get("id"))
                    .walletAddress(URI.create((String) data.get("walletAddress")))
                    .receiver(URI.create((String) data.get("receiver")))
                    .quoteId(URI.create((String) data.get("quoteId")))
                    .failed((Boolean) data.getOrDefault("failed", false))
                    .createdAt(objectMapper.convertValue(data.get("createdAt"), java.time.Instant.class))
                    .updatedAt(objectMapper.convertValue(data.get("updatedAt"), java.time.Instant.class));

            if (data.containsKey("sendAmount")) {
                builder.sendAmount(
                        objectMapper.convertValue(data.get("sendAmount"), zm.hashcode.openpayments.model.Amount.class));
            }
            if (data.containsKey("sentAmount")) {
                builder.sentAmount(
                        objectMapper.convertValue(data.get("sentAmount"), zm.hashcode.openpayments.model.Amount.class));
            }

            return builder.build();
        } catch (Exception e) {
            throw new OutgoingPaymentException("Failed to parse outgoing payment: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings({"PMD.AvoidCatchingGenericException", "PMD.ExceptionAsFlowControl"})
    // Jackson can throw various exceptions, inner exception is used for control flow
    private PaginatedResult<OutgoingPayment> parsePaginatedResult(String json) {
        try {
            Map<String, Object> data = objectMapper.readValue(json, new TypeReference<>() {
            });

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> resultArray = (List<Map<String, Object>>) data.get("result");

            List<OutgoingPayment> payments = resultArray.stream().map(item -> {
                try {
                    return parseOutgoingPayment(objectMapper.writeValueAsString(item));
                } catch (Exception e) {
                    throw new OutgoingPaymentException("Failed to parse payment in list: " + e.getMessage(), e);
                }
            }).toList();

            String cursor = (String) data.get("cursor");
            // If cursor is present, there are more pages
            boolean hasMore = cursor != null;

            return PaginatedResult.of(payments, cursor, hasMore);
        } catch (Exception e) {
            throw new OutgoingPaymentException("Failed to parse paginated result: " + e.getMessage(), e);
        }
    }
}
