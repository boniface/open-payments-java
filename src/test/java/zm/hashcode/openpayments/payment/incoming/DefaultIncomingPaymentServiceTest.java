package zm.hashcode.openpayments.payment.incoming;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
import zm.hashcode.openpayments.model.PaginatedResult;

@DisplayName("DefaultIncomingPaymentService")
class DefaultIncomingPaymentServiceTest {

    private HttpClient httpClient;
    private ObjectMapper objectMapper;
    private DefaultIncomingPaymentService service;

    @BeforeEach
    void setUp() {
        httpClient = mock(HttpClient.class);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JavaTimeModule());
        service = new DefaultIncomingPaymentService(httpClient, objectMapper);
    }

    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("should construct with valid parameters")
        void shouldConstructWithValidParameters() {
            DefaultIncomingPaymentService testService = new DefaultIncomingPaymentService(httpClient, objectMapper);
            assertThat(testService).isNotNull();
        }

        @Test
        @DisplayName("should throw when httpClient is null")
        void shouldThrowWhenHttpClientIsNull() {
            assertThatThrownBy(() -> new DefaultIncomingPaymentService(null, objectMapper))
                    .isInstanceOf(NullPointerException.class).hasMessageContaining("httpClient must not be null");
        }

        @Test
        @DisplayName("should throw when objectMapper is null")
        void shouldThrowWhenObjectMapperIsNull() {
            assertThatThrownBy(() -> new DefaultIncomingPaymentService(httpClient, null))
                    .isInstanceOf(NullPointerException.class).hasMessageContaining("objectMapper must not be null");
        }
    }

    @Nested
    @DisplayName("Create Incoming Payment")
    class CreateTests {

        @Test
        @DisplayName("should create incoming payment successfully")
        void shouldCreateIncomingPaymentSuccessfully() {
            String responseJson = """
                    {
                        "id": "https://wallet.example.com/alice/incoming-payments/123",
                        "walletAddress": "https://wallet.example.com/alice",
                        "completed": false,
                        "incomingAmount": {
                            "value": "1000",
                            "assetCode": "USD",
                            "assetScale": 2
                        },
                        "createdAt": "2025-01-01T00:00:00Z",
                        "updatedAt": "2025-01-01T00:00:00Z"
                    }
                    """;

            HttpResponse httpResponse = new HttpResponse(201, Map.of(), responseJson);
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));

            IncomingPayment result = service.create(builder -> builder.walletAddress("https://wallet.example.com/alice")
                    .incomingAmount(Amount.of("1000", "USD", 2))).join();

            assertThat(result.getId()).isEqualTo(URI.create("https://wallet.example.com/alice/incoming-payments/123"));
            assertThat(result.getWalletAddress()).isEqualTo(URI.create("https://wallet.example.com/alice"));
            assertThat(result.isCompleted()).isFalse();
            assertThat(result.getIncomingAmount()).isPresent();
            assertThat(result.getIncomingAmount().get().value()).isEqualTo("1000");
        }

        @Test
        @DisplayName("should throw when requestBuilder is null")
        void shouldThrowWhenRequestBuilderIsNull() {
            assertThatThrownBy(() -> service.create(null)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("requestBuilder must not be null");
        }

        @Test
        @DisplayName("should throw when HTTP error occurs")
        void shouldThrowWhenHttpErrorOccurs() {
            HttpResponse httpResponse = new HttpResponse(400, Map.of(), "Bad Request");
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));

            assertThatThrownBy(
                    () -> service.create(builder -> builder.walletAddress("https://wallet.example.com/alice")).join())
                    .hasCauseInstanceOf(IncomingPaymentException.class).hasMessageContaining("Failed to create");
        }

        @Test
        @DisplayName("should handle missing optional fields")
        void shouldHandleMissingOptionalFields() {
            String responseJson = """
                    {
                        "id": "https://wallet.example.com/alice/incoming-payments/123",
                        "walletAddress": "https://wallet.example.com/alice",
                        "completed": false,
                        "createdAt": "2025-01-01T00:00:00Z",
                        "updatedAt": "2025-01-01T00:00:00Z"
                    }
                    """;

            HttpResponse httpResponse = new HttpResponse(201, Map.of(), responseJson);
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));

            IncomingPayment result = service
                    .create(builder -> builder.walletAddress("https://wallet.example.com/alice")).join();

            assertThat(result.getIncomingAmount()).isEmpty();
            assertThat(result.getReceivedAmount()).isEmpty();
            assertThat(result.getExpiresAt()).isEmpty();
            assertThat(result.getMetadata()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Get Incoming Payment by String")
    class GetByStringTests {

        @Test
        @DisplayName("should retrieve incoming payment successfully")
        void shouldRetrieveIncomingPaymentSuccessfully() {
            String url = "https://wallet.example.com/alice/incoming-payments/123";
            String responseJson = """
                    {
                        "id": "https://wallet.example.com/alice/incoming-payments/123",
                        "walletAddress": "https://wallet.example.com/alice",
                        "completed": false,
                        "createdAt": "2025-01-01T00:00:00Z",
                        "updatedAt": "2025-01-01T00:00:00Z"
                    }
                    """;

            HttpResponse httpResponse = new HttpResponse(200, Map.of(), responseJson);
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));

            IncomingPayment result = service.get(url).join();

            assertThat(result.getId()).isEqualTo(URI.create(url));
            assertThat(result.getWalletAddress()).isEqualTo(URI.create("https://wallet.example.com/alice"));
        }

        @Test
        @DisplayName("should throw when url is null")
        void shouldThrowWhenUrlIsNull() {
            assertThatThrownBy(() -> service.get((String) null)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("url must not be null");
        }

        @Test
        @DisplayName("should throw when HTTP error occurs")
        void shouldThrowWhenHttpErrorOccurs() {
            String url = "https://wallet.example.com/alice/incoming-payments/123";
            HttpResponse httpResponse = new HttpResponse(404, Map.of(), "Not Found");
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));

            assertThatThrownBy(() -> service.get(url).join()).hasCauseInstanceOf(IncomingPaymentException.class)
                    .hasMessageContaining("Failed to retrieve");
        }
    }

    @Nested
    @DisplayName("Get Incoming Payment by URI")
    class GetByUriTests {

        @Test
        @DisplayName("should retrieve incoming payment successfully")
        void shouldRetrieveIncomingPaymentSuccessfully() {
            URI uri = URI.create("https://wallet.example.com/alice/incoming-payments/123");
            String responseJson = """
                    {
                        "id": "https://wallet.example.com/alice/incoming-payments/123",
                        "walletAddress": "https://wallet.example.com/alice",
                        "completed": false,
                        "createdAt": "2025-01-01T00:00:00Z",
                        "updatedAt": "2025-01-01T00:00:00Z"
                    }
                    """;

            HttpResponse httpResponse = new HttpResponse(200, Map.of(), responseJson);
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));

            IncomingPayment result = service.get(uri).join();

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
    @DisplayName("List Incoming Payments")
    class ListTests {

        @Test
        @DisplayName("should list incoming payments successfully")
        void shouldListIncomingPaymentsSuccessfully() {
            String walletAddress = "https://wallet.example.com/alice";
            String responseJson = """
                    {
                        "result": [
                            {
                                "id": "https://wallet.example.com/alice/incoming-payments/1",
                                "walletAddress": "https://wallet.example.com/alice",
                                "completed": false,
                                "createdAt": "2025-01-01T00:00:00Z",
                                "updatedAt": "2025-01-01T00:00:00Z"
                            },
                            {
                                "id": "https://wallet.example.com/alice/incoming-payments/2",
                                "walletAddress": "https://wallet.example.com/alice",
                                "completed": true,
                                "createdAt": "2025-01-02T00:00:00Z",
                                "updatedAt": "2025-01-02T00:00:00Z"
                            }
                        ],
                        "cursor": "next-page-cursor"
                    }
                    """;

            HttpResponse httpResponse = new HttpResponse(200, Map.of(), responseJson);
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));

            PaginatedResult<IncomingPayment> result = service.list(walletAddress).join();

            assertThat(result.items()).hasSize(2);
            assertThat(result.hasMore()).isTrue();
            assertThat(result.getCursor()).isPresent();
            assertThat(result.getCursor().get()).isEqualTo("next-page-cursor");
        }

        @Test
        @DisplayName("should list with pagination parameters")
        void shouldListWithPaginationParameters() {
            String walletAddress = "https://wallet.example.com/alice";
            String responseJson = """
                    {
                        "result": [],
                        "cursor": null
                    }
                    """;

            HttpResponse httpResponse = new HttpResponse(200, Map.of(), responseJson);
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));

            PaginatedResult<IncomingPayment> result = service.list(walletAddress, "cursor123", 10).join();

            assertThat(result.items()).isEmpty();
            assertThat(result.hasMore()).isFalse();
        }

        @Test
        @DisplayName("should throw when walletAddress is null")
        void shouldThrowWhenWalletAddressIsNull() {
            assertThatThrownBy(() -> service.list(null)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("walletAddress must not be null");
        }

        @Test
        @DisplayName("should handle wallet address with trailing slash")
        void shouldHandleWalletAddressWithTrailingSlash() {
            String walletAddress = "https://wallet.example.com/alice/";
            String responseJson = """
                    {
                        "result": [],
                        "cursor": null
                    }
                    """;

            HttpResponse httpResponse = new HttpResponse(200, Map.of(), responseJson);
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));

            PaginatedResult<IncomingPayment> result = service.list(walletAddress).join();

            assertThat(result.items()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Complete Incoming Payment")
    class CompleteTests {

        @Test
        @DisplayName("should complete incoming payment successfully")
        void shouldCompleteIncomingPaymentSuccessfully() {
            String paymentUrl = "https://wallet.example.com/alice/incoming-payments/123";
            String responseJson = """
                    {
                        "id": "https://wallet.example.com/alice/incoming-payments/123",
                        "walletAddress": "https://wallet.example.com/alice",
                        "completed": true,
                        "createdAt": "2025-01-01T00:00:00Z",
                        "updatedAt": "2025-01-01T01:00:00Z"
                    }
                    """;

            HttpResponse httpResponse = new HttpResponse(200, Map.of(), responseJson);
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));

            IncomingPayment result = service.complete(paymentUrl).join();

            assertThat(result.getId()).isEqualTo(URI.create(paymentUrl));
            assertThat(result.isCompleted()).isTrue();
        }

        @Test
        @DisplayName("should throw when paymentUrl is null")
        void shouldThrowWhenPaymentUrlIsNull() {
            assertThatThrownBy(() -> service.complete(null)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("paymentUrl must not be null");
        }

        @Test
        @DisplayName("should throw when HTTP error occurs")
        void shouldThrowWhenHttpErrorOccurs() {
            String paymentUrl = "https://wallet.example.com/alice/incoming-payments/123";
            HttpResponse httpResponse = new HttpResponse(409, Map.of(), "Conflict");
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));

            assertThatThrownBy(() -> service.complete(paymentUrl).join())
                    .hasCauseInstanceOf(IncomingPaymentException.class).hasMessageContaining("Failed to complete");
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("should handle invalid JSON in response")
        void shouldHandleInvalidJsonInResponse() {
            String url = "https://wallet.example.com/alice/incoming-payments/123";
            HttpResponse httpResponse = new HttpResponse(200, Map.of(), "invalid json");
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));

            assertThatThrownBy(() -> service.get(url).join()).hasCauseInstanceOf(IncomingPaymentException.class)
                    .hasMessageContaining("Failed to parse");
        }

        @Test
        @DisplayName("should handle various HTTP error codes")
        void shouldHandleVariousHttpErrorCodes() {
            String url = "https://wallet.example.com/alice/incoming-payments/123";

            int[] errorCodes = {400, 401, 403, 404, 500, 503};
            for (int code : errorCodes) {
                HttpResponse httpResponse = new HttpResponse(code, Map.of(), "Error");
                when(httpClient.execute(any(HttpRequest.class)))
                        .thenReturn(CompletableFuture.completedFuture(httpResponse));

                assertThatThrownBy(() -> service.get(url).join()).hasCauseInstanceOf(IncomingPaymentException.class);
            }
        }
    }
}
