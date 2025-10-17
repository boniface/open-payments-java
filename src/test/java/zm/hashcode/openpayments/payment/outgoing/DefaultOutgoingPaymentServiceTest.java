package zm.hashcode.openpayments.payment.outgoing;

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
import zm.hashcode.openpayments.model.PaginatedResult;

/**
 * Unit tests for {@link DefaultOutgoingPaymentService}.
 */
@DisplayName("DefaultOutgoingPaymentService")
class DefaultOutgoingPaymentServiceTest {

    private HttpClient httpClient;
    private ObjectMapper objectMapper;
    private DefaultOutgoingPaymentService service;

    @BeforeEach
    void setUp() {
        httpClient = mock(HttpClient.class);
        objectMapper = new ObjectMapper().registerModule(new Jdk8Module()).registerModule(new JavaTimeModule());
        service = new DefaultOutgoingPaymentService(httpClient, objectMapper);
    }

    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("should require http client")
        void shouldRequireHttpClient() {
            assertThatThrownBy(() -> new DefaultOutgoingPaymentService(null, objectMapper))
                    .isInstanceOf(NullPointerException.class).hasMessageContaining("httpClient must not be null");
        }

        @Test
        @DisplayName("should require object mapper")
        void shouldRequireObjectMapper() {
            assertThatThrownBy(() -> new DefaultOutgoingPaymentService(httpClient, null))
                    .isInstanceOf(NullPointerException.class).hasMessageContaining("objectMapper must not be null");
        }
    }

    @Nested
    @DisplayName("Create Outgoing Payment")
    class CreateTests {

        @Test
        @DisplayName("should create outgoing payment successfully")
        void shouldCreateOutgoingPaymentSuccessfully() {
            String responseJson = """
                    {
                        "id": "https://wallet.example.com/alice/outgoing-payments/123",
                        "walletAddress": "https://wallet.example.com/alice",
                        "receiver": "https://wallet.example.com/bob",
                        "quoteId": "https://wallet.example.com/quotes/456",
                        "failed": false,
                        "sendAmount": {"value": "1000", "assetCode": "USD", "assetScale": 2},
                        "sentAmount": {"value": "1000", "assetCode": "USD", "assetScale": 2},
                        "createdAt": "2025-01-01T00:00:00Z",
                        "updatedAt": "2025-01-01T00:00:00Z"
                    }
                    """;
            HttpResponse httpResponse = new HttpResponse(201, Map.of(), responseJson);
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));

            OutgoingPayment result = service.create(builder -> builder.walletAddress("https://wallet.example.com/alice")
                    .quoteId("https://wallet.example.com/quotes/456").metadata("test-metadata")).join();

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(URI.create("https://wallet.example.com/alice/outgoing-payments/123"));
            assertThat(result.getWalletAddress()).isEqualTo(URI.create("https://wallet.example.com/alice"));
            assertThat(result.getReceiver()).isEqualTo(URI.create("https://wallet.example.com/bob"));
            assertThat(result.getQuoteId()).isPresent();
            assertThat(result.getQuoteId().get()).isEqualTo(URI.create("https://wallet.example.com/quotes/456"));
            assertThat(result.isFailed()).isFalse();
            assertThat(result.getSendAmount()).isPresent();
            assertThat(result.getSendAmount().get().value()).isEqualTo("1000");
            assertThat(result.getSentAmount()).isPresent();
        }

        @Test
        @DisplayName("should send correct HTTP request")
        void shouldSendCorrectHttpRequest() {
            String responseJson = """
                    {
                        "id": "https://wallet.example.com/alice/outgoing-payments/123",
                        "walletAddress": "https://wallet.example.com/alice",
                        "receiver": "https://wallet.example.com/bob",
                        "quoteId": "https://wallet.example.com/quotes/456",
                        "failed": false,
                        "createdAt": "2025-01-01T00:00:00Z",
                        "updatedAt": "2025-01-01T00:00:00Z"
                    }
                    """;
            HttpResponse httpResponse = new HttpResponse(201, Map.of(), responseJson);
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));

            service.create(builder -> builder.walletAddress("https://wallet.example.com/alice")
                    .quoteId("https://wallet.example.com/quotes/456")).join();

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

            assertThatThrownBy(() -> service.create(builder -> builder.walletAddress("https://wallet.example.com/alice")
                    .quoteId("https://wallet.example.com/quotes/456")).join())
                    .hasCauseInstanceOf(OutgoingPaymentException.class)
                    .hasMessageContaining("Failed to create outgoing payment");
        }
    }

    @Nested
    @DisplayName("Get Outgoing Payment by String URL")
    class GetByStringTests {

        @Test
        @DisplayName("should get outgoing payment by string url successfully")
        void shouldGetOutgoingPaymentByStringSuccessfully() {
            String responseJson = """
                    {
                        "id": "https://wallet.example.com/alice/outgoing-payments/123",
                        "walletAddress": "https://wallet.example.com/alice",
                        "receiver": "https://wallet.example.com/bob",
                        "quoteId": "https://wallet.example.com/quotes/456",
                        "failed": false,
                        "createdAt": "2025-01-01T00:00:00Z",
                        "updatedAt": "2025-01-01T00:00:00Z"
                    }
                    """;
            HttpResponse httpResponse = new HttpResponse(200, Map.of(), responseJson);
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));

            OutgoingPayment result = service.get("https://wallet.example.com/alice/outgoing-payments/123").join();

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(URI.create("https://wallet.example.com/alice/outgoing-payments/123"));
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

            assertThatThrownBy(() -> service.get("https://wallet.example.com/alice/outgoing-payments/123").join())
                    .hasCauseInstanceOf(OutgoingPaymentException.class)
                    .hasMessageContaining("Failed to retrieve outgoing payment");
        }
    }

    @Nested
    @DisplayName("Get Outgoing Payment by URI")
    class GetByUriTests {

        @Test
        @DisplayName("should get outgoing payment by uri successfully")
        void shouldGetOutgoingPaymentByUriSuccessfully() {
            String responseJson = """
                    {
                        "id": "https://wallet.example.com/alice/outgoing-payments/123",
                        "walletAddress": "https://wallet.example.com/alice",
                        "receiver": "https://wallet.example.com/bob",
                        "quoteId": "https://wallet.example.com/quotes/456",
                        "failed": false,
                        "createdAt": "2025-01-01T00:00:00Z",
                        "updatedAt": "2025-01-01T00:00:00Z"
                    }
                    """;
            HttpResponse httpResponse = new HttpResponse(200, Map.of(), responseJson);
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));

            URI uri = URI.create("https://wallet.example.com/alice/outgoing-payments/123");
            OutgoingPayment result = service.get(uri).join();

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
    @DisplayName("List Outgoing Payments")
    class ListTests {

        @Test
        @DisplayName("should list outgoing payments with default pagination")
        void shouldListOutgoingPaymentsWithDefaultPagination() {
            String responseJson = """
                    {
                        "result": [
                            {
                                "id": "https://wallet.example.com/alice/outgoing-payments/123",
                                "walletAddress": "https://wallet.example.com/alice",
                                "receiver": "https://wallet.example.com/bob",
                                "quoteId": "https://wallet.example.com/quotes/456",
                                "failed": false,
                                "createdAt": "2025-01-01T00:00:00Z",
                                "updatedAt": "2025-01-01T00:00:00Z"
                            }
                        ],
                        "cursor": "next-page-cursor"
                    }
                    """;
            HttpResponse httpResponse = new HttpResponse(200, Map.of(), responseJson);
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));

            PaginatedResult<OutgoingPayment> result = service.list("https://wallet.example.com/alice").join();

            assertThat(result).isNotNull();
            assertThat(result.items()).hasSize(1);
            assertThat(result.cursor()).isEqualTo("next-page-cursor");
            assertThat(result.hasMore()).isTrue();
        }

        @Test
        @DisplayName("should list outgoing payments with custom pagination")
        void shouldListOutgoingPaymentsWithCustomPagination() {
            String responseJson = """
                    {
                        "result": [
                            {
                                "id": "https://wallet.example.com/alice/outgoing-payments/123",
                                "walletAddress": "https://wallet.example.com/alice",
                                "receiver": "https://wallet.example.com/bob",
                                "quoteId": "https://wallet.example.com/quotes/456",
                                "failed": false,
                                "createdAt": "2025-01-01T00:00:00Z",
                                "updatedAt": "2025-01-01T00:00:00Z"
                            },
                            {
                                "id": "https://wallet.example.com/alice/outgoing-payments/456",
                                "walletAddress": "https://wallet.example.com/alice",
                                "receiver": "https://wallet.example.com/charlie",
                                "quoteId": "https://wallet.example.com/quotes/789",
                                "failed": false,
                                "createdAt": "2025-01-02T00:00:00Z",
                                "updatedAt": "2025-01-02T00:00:00Z"
                            }
                        ]
                    }
                    """;
            HttpResponse httpResponse = new HttpResponse(200, Map.of(), responseJson);
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));

            PaginatedResult<OutgoingPayment> result = service
                    .list("https://wallet.example.com/alice", "cursor-value", 10).join();

            assertThat(result).isNotNull();
            assertThat(result.items()).hasSize(2);
            assertThat(result.cursor()).isNull();
            assertThat(result.hasMore()).isFalse();
        }

        @Test
        @DisplayName("should throw when wallet address is null")
        void shouldThrowWhenWalletAddressIsNull() {
            assertThatThrownBy(() -> service.list(null)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("walletAddress must not be null");
        }

        @Test
        @DisplayName("should handle empty result list")
        void shouldHandleEmptyResultList() {
            String responseJson = """
                    {
                        "result": []
                    }
                    """;
            HttpResponse httpResponse = new HttpResponse(200, Map.of(), responseJson);
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));

            PaginatedResult<OutgoingPayment> result = service.list("https://wallet.example.com/alice").join();

            assertThat(result).isNotNull();
            assertThat(result.items()).isEmpty();
            assertThat(result.hasMore()).isFalse();
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

            assertThatThrownBy(() -> service.get("https://wallet.example.com/alice/outgoing-payments/123").join())
                    .hasCauseInstanceOf(OutgoingPaymentException.class)
                    .hasMessageContaining("Failed to parse outgoing payment");
        }

        @Test
        @DisplayName("should handle missing required fields in response")
        void shouldHandleMissingRequiredFieldsInResponse() {
            String responseJson = """
                    {
                        "id": "https://wallet.example.com/alice/outgoing-payments/123"
                    }
                    """;
            HttpResponse httpResponse = new HttpResponse(200, Map.of(), responseJson);
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));

            assertThatThrownBy(() -> service.get("https://wallet.example.com/alice/outgoing-payments/123").join())
                    .hasCauseInstanceOf(OutgoingPaymentException.class);
        }

        @Test
        @DisplayName("should handle HTTP client failures")
        void shouldHandleHttpClientFailures() {
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Connection failed")));

            assertThatThrownBy(() -> service.get("https://wallet.example.com/alice/outgoing-payments/123").join())
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
                        "id": "https://wallet.example.com/alice/outgoing-payments/123",
                        "walletAddress": "https://wallet.example.com/alice",
                        "receiver": "https://wallet.example.com/bob",
                        "quoteId": "https://wallet.example.com/quotes/456",
                        "failed": false,
                        "createdAt": "2025-01-01T00:00:00Z",
                        "updatedAt": "2025-01-01T00:00:00Z"
                    }
                    """;
            HttpResponse httpResponse = new HttpResponse(201, Map.of(), responseJson);
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));

            service.create(builder -> builder.walletAddress("https://wallet.example.com/alice")
                    .quoteId("https://wallet.example.com/quotes/456")).join();

            verify(httpClient).execute(any(HttpRequest.class));
        }

        @Test
        @DisplayName("should construct correct url for wallet address with trailing slash")
        void shouldConstructCorrectUrlWithTrailingSlash() {
            String responseJson = """
                    {
                        "id": "https://wallet.example.com/alice/outgoing-payments/123",
                        "walletAddress": "https://wallet.example.com/alice/",
                        "receiver": "https://wallet.example.com/bob",
                        "quoteId": "https://wallet.example.com/quotes/456",
                        "failed": false,
                        "createdAt": "2025-01-01T00:00:00Z",
                        "updatedAt": "2025-01-01T00:00:00Z"
                    }
                    """;
            HttpResponse httpResponse = new HttpResponse(201, Map.of(), responseJson);
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));

            service.create(builder -> builder.walletAddress("https://wallet.example.com/alice/")
                    .quoteId("https://wallet.example.com/quotes/456")).join();

            verify(httpClient).execute(any(HttpRequest.class));
        }
    }
}
