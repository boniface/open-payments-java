package zm.hashcode.openpayments.payment.quote;

import java.net.URI;
import java.util.HashMap;
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

/**
 * Default implementation of {@link QuoteService}.
 *
 * <p>
 * This implementation communicates with Open Payments resource servers using authenticated HTTP requests. All methods
 * require GNAP access tokens for authentication.
 *
 * <p>
 * Thread-safe and can be reused across multiple requests.
 */
public record DefaultQuoteService(HttpClient httpClient, ObjectMapper objectMapper) implements QuoteService {

    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String ACCEPT_HEADER = "Accept";
    private static final String QUOTES_PATH = "/quotes";

    /**
     * Creates a new DefaultQuoteService.
     *
     * @param httpClient
     *            the HTTP client for API communication
     * @param objectMapper
     *            the object mapper for JSON serialization/deserialization
     * @throws NullPointerException
     *             if any parameter is null
     */
    public DefaultQuoteService {
        Objects.requireNonNull(httpClient, "httpClient must not be null");
        Objects.requireNonNull(objectMapper, "objectMapper must not be null");
    }

    @Override
    public CompletableFuture<Quote> create(Consumer<QuoteRequest.Builder> requestBuilder) {
        Objects.requireNonNull(requestBuilder, "requestBuilder must not be null");

        QuoteRequest.Builder builder = QuoteRequest.builder();
        requestBuilder.accept(builder);
        QuoteRequest request = builder.build();

        // Construct the quotes URL for the wallet address
        String url = buildQuotesUrl(request.getWalletAddress());

        String requestBody = serializeRequest(request);

        HttpRequest httpRequest = HttpRequest.builder().method(HttpMethod.POST).uri(URI.create(url))
                .header("Content-Type", CONTENT_TYPE_JSON).header(ACCEPT_HEADER, CONTENT_TYPE_JSON).body(requestBody)
                .build();

        return httpClient.execute(httpRequest).thenApply(response -> {
            validateResponse(response, "Failed to create quote");
            return parseQuote(response.body());
        });
    }

    @Override
    public CompletableFuture<Quote> get(String url) {
        Objects.requireNonNull(url, "url must not be null");
        return get(URI.create(url));
    }

    @Override
    public CompletableFuture<Quote> get(URI uri) {
        Objects.requireNonNull(uri, "uri must not be null");

        HttpRequest request = HttpRequest.builder().method(HttpMethod.GET).uri(uri)
                .header(ACCEPT_HEADER, CONTENT_TYPE_JSON).build();

        return httpClient.execute(request).thenApply(response -> {
            validateResponse(response, "Failed to retrieve quote");
            return parseQuote(response.body());
        });
    }

    private String buildQuotesUrl(URI walletAddress) {
        String path = walletAddress.getPath();
        // Ensure path doesn't end with slash before appending /quotes
        if (path != null && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        String scheme = walletAddress.getScheme();
        String authority = walletAddress.getAuthority();
        return scheme + "://" + authority + (path != null ? path : "") + QUOTES_PATH;
    }

    private void validateResponse(HttpResponse response, String errorMessage) {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new QuoteException(
                    String.format("%s: HTTP %d - %s", errorMessage, response.statusCode(), response.body()));
        }
    }

    @SuppressWarnings({"PMD.AvoidCatchingGenericException", "PMD.UseConcurrentHashMap"})
    // Jackson can throw various exceptions, HashMap is local and not shared
    private String serializeRequest(QuoteRequest request) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("walletAddress", request.getWalletAddress().toString());
            data.put("receiver", request.getReceiver().toString());
            if (request.getSendAmount() != null) {
                data.put("sendAmount", request.getSendAmount());
            }
            if (request.getReceiveAmount() != null) {
                data.put("receiveAmount", request.getReceiveAmount());
            }
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            throw new QuoteException("Failed to serialize request: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException") // Jackson can throw various exceptions
    private Quote parseQuote(String json) {
        try {
            Map<String, Object> data = objectMapper.readValue(json, new TypeReference<>() {
            });

            Quote.Builder builder = Quote.builder().id((String) data.get("id"))
                    .walletAddress(URI.create((String) data.get("walletAddress")))
                    .receiver(URI.create((String) data.get("receiver")))
                    .expiresAt(objectMapper.convertValue(data.get("expiresAt"), java.time.Instant.class))
                    .createdAt(objectMapper.convertValue(data.get("createdAt"), java.time.Instant.class));

            if (data.containsKey("sendAmount")) {
                builder.sendAmount(
                        objectMapper.convertValue(data.get("sendAmount"), zm.hashcode.openpayments.model.Amount.class));
            }
            if (data.containsKey("receiveAmount")) {
                builder.receiveAmount(objectMapper.convertValue(data.get("receiveAmount"),
                        zm.hashcode.openpayments.model.Amount.class));
            }

            return builder.build();
        } catch (Exception e) {
            throw new QuoteException("Failed to parse quote: " + e.getMessage(), e);
        }
    }
}
