package zm.hashcode.openpayments.payment.quote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import zm.hashcode.openpayments.http.core.HttpClient;
import zm.hashcode.openpayments.http.core.HttpRequest;
import zm.hashcode.openpayments.http.core.HttpResponse;
import zm.hashcode.openpayments.model.Amount;

/**
 * Unit tests for {@link DefaultQuoteService}.
 */
@DisplayName("DefaultQuoteService")
class DefaultQuoteServiceTest {

    private HttpClient httpClient;
    private ObjectMapper objectMapper;
    private DefaultQuoteService service;

    @BeforeEach
    void setUp() {
        httpClient = mock(HttpClient.class);
        objectMapper = new ObjectMapper().registerModule(new Jdk8Module()).registerModule(new JavaTimeModule());
        service = new DefaultQuoteService(httpClient, objectMapper);
    }

    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("should require http client")
        void shouldRequireHttpClient() {
            assertThatThrownBy(() -> new DefaultQuoteService(null, objectMapper))
                    .isInstanceOf(NullPointerException.class).hasMessageContaining("httpClient must not be null");
        }

        @Test
        @DisplayName("should require object mapper")
        void shouldRequireObjectMapper() {
            assertThatThrownBy(() -> new DefaultQuoteService(httpClient, null)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("objectMapper must not be null");
        }
    }

    @Nested
    @DisplayName("Create Quote")
    class CreateTests {

        @Test
        @DisplayName("should create quote with send amount successfully")
        void shouldCreateQuoteWithSendAmountSuccessfully() {
            String responseJson = """
                    {
                        "id": "https://wallet.example.com/alice/quotes/123",
                        "walletAddress": "https://wallet.example.com/alice",
                        "receiver": "https://wallet.example.com/bob",
                        "sendAmount": {"value": "1000", "assetCode": "USD", "assetScale": 2},
                        "receiveAmount": {"value": "950", "assetCode": "EUR", "assetScale": 2},
                        "expiresAt": "2025-01-01T01:00:00Z",
                        "createdAt": "2025-01-01T00:00:00Z"
                    }
                    """;
            HttpResponse httpResponse = new HttpResponse(201, Map.of(), responseJson);
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));

            Quote result = service.create(builder -> builder.walletAddress("https://wallet.example.com/alice")
                    .receiver("https://wallet.example.com/bob").sendAmount(Amount.of("1000", "USD", 2))).join();

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(URI.create("https://wallet.example.com/alice/quotes/123"));
            assertThat(result.getWalletAddress()).isEqualTo(URI.create("https://wallet.example.com/alice"));
            assertThat(result.getReceiver()).isEqualTo(URI.create("https://wallet.example.com/bob"));
            assertThat(result.getSendAmount()).isPresent();
            assertThat(result.getSendAmount().get().value()).isEqualTo("1000");
            assertThat(result.getReceiveAmount()).isPresent();
            assertThat(result.getReceiveAmount().get().value()).isEqualTo("950");
            assertThat(result.getExpiresAt()).isNotNull();
            assertThat(result.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("should create quote with receive amount successfully")
        void shouldCreateQuoteWithReceiveAmountSuccessfully() {
            String responseJson = """
                    {
                        "id": "https://wallet.example.com/alice/quotes/456",
                        "walletAddress": "https://wallet.example.com/alice",
                        "receiver": "https://wallet.example.com/bob",
                        "sendAmount": {"value": "1050", "assetCode": "USD", "assetScale": 2},
                        "receiveAmount": {"value": "1000", "assetCode": "EUR", "assetScale": 2},
                        "expiresAt": "2025-01-01T01:00:00Z",
                        "createdAt": "2025-01-01T00:00:00Z"
                    }
                    """;
            HttpResponse httpResponse = new HttpResponse(201, Map.of(), responseJson);
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));

            Quote result = service
                    .create(builder -> builder.walletAddress("https://wallet.example.com/alice")
                            .receiver("https://wallet.example.com/bob").receiveAmount(Amount.of("1000", "EUR", 2)))
                    .join();

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(URI.create("https://wallet.example.com/alice/quotes/456"));
            assertThat(result.getSendAmount().get().value()).isEqualTo("1050");
            assertThat(result.getReceiveAmount().get().value()).isEqualTo("1000");
        }

        @Test
        @DisplayName("should send correct HTTP request")
        void shouldSendCorrectHttpRequest() {
            String responseJson = """
                    {
                        "id": "https://wallet.example.com/alice/quotes/123",
                        "walletAddress": "https://wallet.example.com/alice",
                        "receiver": "https://wallet.example.com/bob",
                        "sendAmount": {"value": "1000", "assetCode": "USD", "assetScale": 2},
                        "receiveAmount": {"value": "950", "assetCode": "EUR", "assetScale": 2},
                        "expiresAt": "2025-01-01T01:00:00Z",
                        "createdAt": "2025-01-01T00:00:00Z"
                    }
                    """;
            HttpResponse httpResponse = new HttpResponse(201, Map.of(), responseJson);
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));

            service.create(builder -> builder.walletAddress("https://wallet.example.com/alice")
                    .receiver("https://wallet.example.com/bob").sendAmount(Amount.of("1000", "USD", 2))).join();

            verify(httpClient).execute(any(HttpRequest.class));
        }

        @Test
        @DisplayName("should throw when request builder is null")
        void shouldThrowWhenRequestBuilderIsNull() {
            assertThatThrownBy(() -> service.create(null)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("requestBuilder must not be null");
        }

        @Test
        @DisplayName("should handle HTTP error responses")
        void shouldHandleHttpErrorResponses() {
            HttpResponse httpResponse = new HttpResponse(400, Map.of(), "Bad Request");
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));

            assertThatThrownBy(
                    () -> service
                            .create(builder -> builder.walletAddress("https://wallet.example.com/alice")
                                    .receiver("https://wallet.example.com/bob").sendAmount(Amount.of("1000", "USD", 2)))
                            .join())
                    .hasCauseInstanceOf(QuoteException.class).hasMessageContaining("Failed to create quote");
        }
    }

    @Nested
    @DisplayName("Get Quote by String URL")
    class GetByStringTests {

        @Test
        @DisplayName("should get quote by string url successfully")
        void shouldGetQuoteByStringSuccessfully() {
            String responseJson = """
                    {
                        "id": "https://wallet.example.com/alice/quotes/123",
                        "walletAddress": "https://wallet.example.com/alice",
                        "receiver": "https://wallet.example.com/bob",
                        "sendAmount": {"value": "1000", "assetCode": "USD", "assetScale": 2},
                        "receiveAmount": {"value": "950", "assetCode": "EUR", "assetScale": 2},
                        "expiresAt": "2025-01-01T01:00:00Z",
                        "createdAt": "2025-01-01T00:00:00Z"
                    }
                    """;
            HttpResponse httpResponse = new HttpResponse(200, Map.of(), responseJson);
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));

            Quote result = service.get("https://wallet.example.com/alice/quotes/123").join();

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(URI.create("https://wallet.example.com/alice/quotes/123"));
        }

        @Test
        @DisplayName("should throw when url string is null")
        void shouldThrowWhenUrlStringIsNull() {
            assertThatThrownBy(() -> service.get((String) null)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("url must not be null");
        }

        @Test
        @DisplayName("should handle HTTP error responses")
        void shouldHandleHttpErrorResponses() {
            HttpResponse httpResponse = new HttpResponse(404, Map.of(), "Not Found");
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));

            assertThatThrownBy(() -> service.get("https://wallet.example.com/alice/quotes/123").join())
                    .hasCauseInstanceOf(QuoteException.class).hasMessageContaining("Failed to retrieve quote");
        }
    }

    @Nested
    @DisplayName("Get Quote by URI")
    class GetByUriTests {

        @Test
        @DisplayName("should get quote by uri successfully")
        void shouldGetQuoteByUriSuccessfully() {
            String responseJson = """
                    {
                        "id": "https://wallet.example.com/alice/quotes/123",
                        "walletAddress": "https://wallet.example.com/alice",
                        "receiver": "https://wallet.example.com/bob",
                        "sendAmount": {"value": "1000", "assetCode": "USD", "assetScale": 2},
                        "receiveAmount": {"value": "950", "assetCode": "EUR", "assetScale": 2},
                        "expiresAt": "2025-01-01T01:00:00Z",
                        "createdAt": "2025-01-01T00:00:00Z"
                    }
                    """;
            HttpResponse httpResponse = new HttpResponse(200, Map.of(), responseJson);
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));

            URI uri = URI.create("https://wallet.example.com/alice/quotes/123");
            Quote result = service.get(uri).join();

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(uri);
        }

        @Test
        @DisplayName("should throw when uri is null")
        void shouldThrowWhenUriIsNull() {
            assertThatThrownBy(() -> service.get((URI) null)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("uri must not be null");
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("should handle invalid JSON response")
        void shouldHandleInvalidJsonResponse() {
            HttpResponse httpResponse = new HttpResponse(200, Map.of(), "not valid json");
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));

            assertThatThrownBy(() -> service.get("https://wallet.example.com/alice/quotes/123").join())
                    .hasCauseInstanceOf(QuoteException.class).hasMessageContaining("Failed to parse quote");
        }

        @Test
        @DisplayName("should handle missing required fields in response")
        void shouldHandleMissingRequiredFieldsInResponse() {
            String responseJson = """
                    {
                        "id": "https://wallet.example.com/alice/quotes/123"
                    }
                    """;
            HttpResponse httpResponse = new HttpResponse(200, Map.of(), responseJson);
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));

            assertThatThrownBy(() -> service.get("https://wallet.example.com/alice/quotes/123").join())
                    .hasCauseInstanceOf(QuoteException.class);
        }

        @Test
        @DisplayName("should handle HTTP client failures")
        void shouldHandleHttpClientFailures() {
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Connection failed")));

            assertThatThrownBy(() -> service.get("https://wallet.example.com/alice/quotes/123").join())
                    .hasRootCauseMessage("Connection failed");
        }
    }

    @Nested
    @DisplayName("URL Construction")
    class UrlConstructionTests {

        @Test
        @DisplayName("should construct correct url for wallet address without trailing slash")
        void shouldConstructCorrectUrlWithoutTrailingSlash() {
            String responseJson = """
                    {
                        "id": "https://wallet.example.com/alice/quotes/123",
                        "walletAddress": "https://wallet.example.com/alice",
                        "receiver": "https://wallet.example.com/bob",
                        "sendAmount": {"value": "1000", "assetCode": "USD", "assetScale": 2},
                        "receiveAmount": {"value": "950", "assetCode": "EUR", "assetScale": 2},
                        "expiresAt": "2025-01-01T01:00:00Z",
                        "createdAt": "2025-01-01T00:00:00Z"
                    }
                    """;
            HttpResponse httpResponse = new HttpResponse(201, Map.of(), responseJson);
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));

            service.create(builder -> builder.walletAddress("https://wallet.example.com/alice")
                    .receiver("https://wallet.example.com/bob").sendAmount(Amount.of("1000", "USD", 2))).join();

            verify(httpClient).execute(any(HttpRequest.class));
        }

        @Test
        @DisplayName("should construct correct url for wallet address with trailing slash")
        void shouldConstructCorrectUrlWithTrailingSlash() {
            String responseJson = """
                    {
                        "id": "https://wallet.example.com/alice/quotes/123",
                        "walletAddress": "https://wallet.example.com/alice/",
                        "receiver": "https://wallet.example.com/bob",
                        "sendAmount": {"value": "1000", "assetCode": "USD", "assetScale": 2},
                        "receiveAmount": {"value": "950", "assetCode": "EUR", "assetScale": 2},
                        "expiresAt": "2025-01-01T01:00:00Z",
                        "createdAt": "2025-01-01T00:00:00Z"
                    }
                    """;
            HttpResponse httpResponse = new HttpResponse(201, Map.of(), responseJson);
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));

            service.create(builder -> builder.walletAddress("https://wallet.example.com/alice/")
                    .receiver("https://wallet.example.com/bob").sendAmount(Amount.of("1000", "USD", 2))).join();

            verify(httpClient).execute(any(HttpRequest.class));
        }
    }

    @Nested
    @DisplayName("Quote Validation")
    class QuoteValidationTests {

        @Test
        @DisplayName("should check if quote is expired")
        void shouldCheckIfQuoteIsExpired() {
            String responseJson = """
                    {
                        "id": "https://wallet.example.com/alice/quotes/123",
                        "walletAddress": "https://wallet.example.com/alice",
                        "receiver": "https://wallet.example.com/bob",
                        "sendAmount": {"value": "1000", "assetCode": "USD", "assetScale": 2},
                        "receiveAmount": {"value": "950", "assetCode": "EUR", "assetScale": 2},
                        "expiresAt": "2020-01-01T00:00:00Z",
                        "createdAt": "2020-01-01T00:00:00Z"
                    }
                    """;
            HttpResponse httpResponse = new HttpResponse(200, Map.of(), responseJson);
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));

            Quote result = service.get("https://wallet.example.com/alice/quotes/123").join();

            assertThat(result.isExpired()).isTrue();
        }

        @Test
        @DisplayName("should check if quote is not expired")
        void shouldCheckIfQuoteIsNotExpired() {
            String responseJson = """
                    {
                        "id": "https://wallet.example.com/alice/quotes/123",
                        "walletAddress": "https://wallet.example.com/alice",
                        "receiver": "https://wallet.example.com/bob",
                        "sendAmount": {"value": "1000", "assetCode": "USD", "assetScale": 2},
                        "receiveAmount": {"value": "950", "assetCode": "EUR", "assetScale": 2},
                        "expiresAt": "2099-01-01T00:00:00Z",
                        "createdAt": "2025-01-01T00:00:00Z"
                    }
                    """;
            HttpResponse httpResponse = new HttpResponse(200, Map.of(), responseJson);
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));

            Quote result = service.get("https://wallet.example.com/alice/quotes/123").join();

            assertThat(result.isExpired()).isFalse();
        }
    }
}
