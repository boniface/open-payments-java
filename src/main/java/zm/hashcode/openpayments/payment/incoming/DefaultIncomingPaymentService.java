package zm.hashcode.openpayments.payment.incoming;

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
 * Default implementation of {@link IncomingPaymentService}.
 *
 * <p>
 * This implementation communicates with Open Payments resource servers using authenticated HTTP requests. All methods
 * require GNAP access tokens for authentication.
 *
 * <p>
 * Thread-safe and can be reused across multiple requests.
 */
public final class DefaultIncomingPaymentService implements IncomingPaymentService {

    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String ACCEPT_HEADER = "Accept";
    private static final String INCOMING_PAYMENTS_PATH = "/incoming-payments";
    private static final String COMPLETE_PATH = "/complete";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new DefaultIncomingPaymentService.
     *
     * @param httpClient
     *            the HTTP client for API communication
     * @param objectMapper
     *            the object mapper for JSON serialization/deserialization
     * @throws NullPointerException
     *             if any parameter is null
     */
    public DefaultIncomingPaymentService(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient must not be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
    }

    @Override
    public CompletableFuture<IncomingPayment> create(Consumer<IncomingPaymentRequest.Builder> requestBuilder) {
        Objects.requireNonNull(requestBuilder, "requestBuilder must not be null");

        IncomingPaymentRequest.Builder builder = IncomingPaymentRequest.builder();
        requestBuilder.accept(builder);
        IncomingPaymentRequest request = builder.build();

        // Construct the incoming payments URL for the wallet address
        String url = buildIncomingPaymentsUrl(request.getWalletAddress());

        String requestBody = serializeRequest(request);

        HttpRequest httpRequest = HttpRequest.builder().method(HttpMethod.POST).uri(URI.create(url))
                .header("Content-Type", CONTENT_TYPE_JSON).header(ACCEPT_HEADER, CONTENT_TYPE_JSON).body(requestBody)
                .build();

        return httpClient.execute(httpRequest).thenApply(response -> {
            validateResponse(response, "Failed to create incoming payment");
            return parseIncomingPayment(response.body());
        });
    }

    @Override
    public CompletableFuture<IncomingPayment> get(String url) {
        Objects.requireNonNull(url, "url must not be null");
        return get(URI.create(url));
    }

    @Override
    public CompletableFuture<IncomingPayment> get(URI uri) {
        Objects.requireNonNull(uri, "uri must not be null");

        HttpRequest request = HttpRequest.builder().method(HttpMethod.GET).uri(uri)
                .header(ACCEPT_HEADER, CONTENT_TYPE_JSON).build();

        return httpClient.execute(request).thenApply(response -> {
            validateResponse(response, "Failed to retrieve incoming payment");
            return parseIncomingPayment(response.body());
        });
    }

    @Override
    public CompletableFuture<PaginatedResult<IncomingPayment>> list(String walletAddress) {
        return list(walletAddress, null, 20);
    }

    @Override
    public CompletableFuture<PaginatedResult<IncomingPayment>> list(String walletAddress, String cursor, int limit) {
        Objects.requireNonNull(walletAddress, "walletAddress must not be null");

        String url = buildIncomingPaymentsUrl(URI.create(walletAddress));

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
            validateResponse(response, "Failed to list incoming payments");
            return parsePaginatedResult(response.body());
        });
    }

    @Override
    public CompletableFuture<IncomingPayment> complete(String paymentUrl) {
        Objects.requireNonNull(paymentUrl, "paymentUrl must not be null");

        String completeUrl = paymentUrl + COMPLETE_PATH;

        HttpRequest request = HttpRequest.builder().method(HttpMethod.POST).uri(URI.create(completeUrl))
                .header("Content-Type", CONTENT_TYPE_JSON).header(ACCEPT_HEADER, CONTENT_TYPE_JSON).body("{}").build();

        return httpClient.execute(request).thenApply(response -> {
            validateResponse(response, "Failed to complete incoming payment");
            return parseIncomingPayment(response.body());
        });
    }

    private String buildIncomingPaymentsUrl(URI walletAddress) {
        String path = walletAddress.getPath();
        // Ensure path doesn't end with slash before appending /incoming-payments
        if (path != null && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        String scheme = walletAddress.getScheme();
        String authority = walletAddress.getAuthority();
        return scheme + "://" + authority + (path != null ? path : "") + INCOMING_PAYMENTS_PATH;
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
            throw new IncomingPaymentException(
                    String.format("%s: HTTP %d - %s", errorMessage, response.statusCode(), response.body()));
        }
    }

    @SuppressWarnings({"PMD.AvoidCatchingGenericException", "PMD.UseConcurrentHashMap"})
    // Jackson can throw various exceptions, HashMap is local and not shared
    private String serializeRequest(IncomingPaymentRequest request) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("walletAddress", request.getWalletAddress().toString());
            if (request.getIncomingAmount() != null) {
                data.put("incomingAmount", request.getIncomingAmount());
            }
            if (request.getExpiresAt() != null) {
                data.put("expiresAt", request.getExpiresAt().toString());
            }
            if (request.getMetadata() != null) {
                data.put("metadata", request.getMetadata());
            }
            if (request.getExternalRef() != null) {
                data.put("externalRef", request.getExternalRef());
            }
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            throw new IncomingPaymentException("Failed to serialize request: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException") // Jackson can throw various exceptions
    private IncomingPayment parseIncomingPayment(String json) {
        try {
            Map<String, Object> data = objectMapper.readValue(json, new TypeReference<>() {
            });

            IncomingPayment.Builder builder = IncomingPayment.builder().id((String) data.get("id"))
                    .walletAddress((String) data.get("walletAddress"))
                    .completed((Boolean) data.getOrDefault("completed", false))
                    .createdAt(objectMapper.convertValue(data.get("createdAt"), java.time.Instant.class))
                    .updatedAt(objectMapper.convertValue(data.get("updatedAt"), java.time.Instant.class));

            if (data.containsKey("incomingAmount")) {
                builder.incomingAmount(objectMapper.convertValue(data.get("incomingAmount"),
                        zm.hashcode.openpayments.model.Amount.class));
            }
            if (data.containsKey("receivedAmount")) {
                builder.receivedAmount(objectMapper.convertValue(data.get("receivedAmount"),
                        zm.hashcode.openpayments.model.Amount.class));
            }
            if (data.containsKey("expiresAt")) {
                builder.expiresAt(objectMapper.convertValue(data.get("expiresAt"), java.time.Instant.class));
            }
            if (data.containsKey("metadata")) {
                builder.metadata((String) data.get("metadata"));
            }

            return builder.build();
        } catch (Exception e) {
            throw new IncomingPaymentException("Failed to parse incoming payment: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings({"PMD.AvoidCatchingGenericException", "PMD.ExceptionAsFlowControl"})
    // Jackson can throw various exceptions, inner exception is used for control flow
    private PaginatedResult<IncomingPayment> parsePaginatedResult(String json) {
        try {
            Map<String, Object> data = objectMapper.readValue(json, new TypeReference<>() {
            });

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> resultArray = (List<Map<String, Object>>) data.get("result");

            List<IncomingPayment> payments = resultArray.stream().map(item -> {
                try {
                    return parseIncomingPayment(objectMapper.writeValueAsString(item));
                } catch (Exception e) {
                    throw new IncomingPaymentException("Failed to parse payment in list: " + e.getMessage(), e);
                }
            }).toList();

            String cursor = (String) data.get("cursor");
            // If cursor is present, there are more pages
            boolean hasMore = cursor != null;

            return PaginatedResult.of(payments, cursor, hasMore);
        } catch (Exception e) {
            throw new IncomingPaymentException("Failed to parse paginated result: " + e.getMessage(), e);
        }
    }
}
