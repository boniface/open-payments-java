package zm.hashcode.openpayments.wallet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import zm.hashcode.openpayments.http.core.HttpClient;
import zm.hashcode.openpayments.http.core.HttpRequest;
import zm.hashcode.openpayments.http.core.HttpResponse;

/**
 * Unit tests for {@link DefaultWalletAddressService}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DefaultWalletAddressService")
class DefaultWalletAddressServiceTest {

    @Mock
    private HttpClient httpClient;

    private ObjectMapper objectMapper;
    private DefaultWalletAddressService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new Jdk8Module());
        service = new DefaultWalletAddressService(httpClient, objectMapper);
    }

    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("should construct with valid parameters")
        void shouldConstructWithValidParameters() {
            assertThat(service).isNotNull();
        }

        @Test
        @DisplayName("should throw when httpClient is null")
        void shouldThrowWhenHttpClientIsNull() {
            assertThatThrownBy(() -> new DefaultWalletAddressService(null, objectMapper))
                    .isInstanceOf(NullPointerException.class).hasMessageContaining("httpClient must not be null");
        }

        @Test
        @DisplayName("should throw when objectMapper is null")
        void shouldThrowWhenObjectMapperIsNull() {
            assertThatThrownBy(() -> new DefaultWalletAddressService(httpClient, null))
                    .isInstanceOf(NullPointerException.class).hasMessageContaining("objectMapper must not be null");
        }
    }

    @Nested
    @DisplayName("Get Wallet Address by String")
    class GetWalletAddressByStringTests {

        @Test
        @DisplayName("should retrieve wallet address successfully")
        void shouldRetrieveWalletAddressSuccessfully() {
            String url = "https://wallet.example.com/alice";
            String responseJson = """
                    {
                        "id": "https://wallet.example.com/alice",
                        "assetCode": "USD",
                        "assetScale": 2,
                        "authServer": "https://auth.example.com",
                        "resourceServer": "https://resource.example.com",
                        "publicName": "Alice"
                    }
                    """;

            HttpResponse httpResponse = new HttpResponse(200, Map.of(), responseJson);
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));

            WalletAddress result = service.get(url).join();

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(URI.create("https://wallet.example.com/alice"));
            assertThat(result.assetCode()).isEqualTo("USD");
            assertThat(result.assetScale()).isEqualTo(2);
            assertThat(result.authServer()).isEqualTo(URI.create("https://auth.example.com"));
            assertThat(result.resourceServer()).isEqualTo(URI.create("https://resource.example.com"));
            assertThat(result.publicName()).isEqualTo("Alice");
        }

        @Test
        @DisplayName("should handle wallet address without publicName")
        void shouldHandleWalletAddressWithoutPublicName() {
            String url = "https://wallet.example.com/bob";
            String responseJson = """
                    {
                        "id": "https://wallet.example.com/bob",
                        "assetCode": "EUR",
                        "assetScale": 2,
                        "authServer": "https://auth.example.com",
                        "resourceServer": "https://resource.example.com"
                    }
                    """;

            HttpResponse httpResponse = new HttpResponse(200, Map.of(), responseJson);
            when(httpClient.execute(any())).thenReturn(CompletableFuture.completedFuture(httpResponse));

            WalletAddress result = service.get(url).join();

            assertThat(result.publicName()).isNull();
            assertThat(result.getPublicName()).isEmpty();
        }

        @Test
        @DisplayName("should throw when url is null")
        void shouldThrowWhenUrlIsNull() {
            assertThatThrownBy(() -> service.get((String) null)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("url must not be null");
        }

        @Test
        @DisplayName("should send GET request with correct headers")
        void shouldSendGetRequestWithCorrectHeaders() {
            String url = "https://wallet.example.com/test";
            String responseJson = """
                    {
                        "id": "https://wallet.example.com/test",
                        "assetCode": "USD",
                        "assetScale": 2,
                        "authServer": "https://auth.example.com",
                        "resourceServer": "https://resource.example.com"
                    }
                    """;

            HttpResponse httpResponse = new HttpResponse(200, Map.of(), responseJson);
            when(httpClient.execute(any())).thenReturn(CompletableFuture.completedFuture(httpResponse));

            service.get(url).join();

            ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
            verify(httpClient).execute(captor.capture());

            HttpRequest request = captor.getValue();
            assertThat(request.method().name()).isEqualTo("GET");
            assertThat(request.uri().toString()).isEqualTo(url);
            assertThat(request.headers().get("Accept")).isEqualTo("application/json");
        }

        @Test
        @DisplayName("should throw WalletAddressException when HTTP error occurs")
        void shouldThrowWhenHttpErrorOccurs() {
            String url = "https://wallet.example.com/notfound";
            HttpResponse httpResponse = new HttpResponse(404, Map.of(), "Not Found");
            when(httpClient.execute(any())).thenReturn(CompletableFuture.completedFuture(httpResponse));

            assertThatThrownBy(() -> service.get(url).join()).hasCauseInstanceOf(WalletAddressException.class)
                    .hasMessageContaining("Failed to retrieve wallet address").hasMessageContaining("404");
        }

        @Test
        @DisplayName("should throw WalletAddressException when JSON is invalid")
        void shouldThrowWhenJsonIsInvalid() {
            String url = "https://wallet.example.com/alice";
            HttpResponse httpResponse = new HttpResponse(200, Map.of(), "invalid json");
            when(httpClient.execute(any())).thenReturn(CompletableFuture.completedFuture(httpResponse));

            assertThatThrownBy(() -> service.get(url).join()).hasCauseInstanceOf(WalletAddressException.class)
                    .hasMessageContaining("Failed to parse wallet address");
        }
    }

    @Nested
    @DisplayName("Get Wallet Address by URI")
    class GetWalletAddressByUriTests {

        @Test
        @DisplayName("should retrieve wallet address successfully")
        void shouldRetrieveWalletAddressSuccessfully() {
            URI uri = URI.create("https://wallet.example.com/alice");
            String responseJson = """
                    {
                        "id": "https://wallet.example.com/alice",
                        "assetCode": "USD",
                        "assetScale": 2,
                        "authServer": "https://auth.example.com",
                        "resourceServer": "https://resource.example.com"
                    }
                    """;

            HttpResponse httpResponse = new HttpResponse(200, Map.of(), responseJson);
            when(httpClient.execute(any())).thenReturn(CompletableFuture.completedFuture(httpResponse));

            WalletAddress result = service.get(uri).join();

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(uri);
        }

        @Test
        @DisplayName("should throw when uri is null")
        void shouldThrowWhenUriIsNull() {
            assertThatThrownBy(() -> service.get((URI) null)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("uri must not be null");
        }

        @Test
        @DisplayName("should handle different asset codes")
        void shouldHandleDifferentAssetCodes() {
            URI uri = URI.create("https://wallet.example.com/test");
            String responseJson = """
                    {
                        "id": "https://wallet.example.com/test",
                        "assetCode": "BTC",
                        "assetScale": 8,
                        "authServer": "https://auth.example.com",
                        "resourceServer": "https://resource.example.com"
                    }
                    """;

            HttpResponse httpResponse = new HttpResponse(200, Map.of(), responseJson);
            when(httpClient.execute(any())).thenReturn(CompletableFuture.completedFuture(httpResponse));

            WalletAddress result = service.get(uri).join();

            assertThat(result.assetCode()).isEqualTo("BTC");
            assertThat(result.assetScale()).isEqualTo(8);
        }
    }

    @Nested
    @DisplayName("Get Public Keys")
    class GetPublicKeysTests {

        @Test
        @DisplayName("should retrieve public keys successfully")
        void shouldRetrievePublicKeysSuccessfully() {
            String walletUrl = "https://wallet.example.com/alice";
            String responseJson = """
                    {
                        "keys": [
                            {
                                "kid": "key-1",
                                "kty": "OKP",
                                "use": "sig",
                                "alg": "EdDSA",
                                "x": "11qYAYKxCrfVS_7TyWQHOg7hcvPapiMlrwIaaPcHURo"
                            }
                        ]
                    }
                    """;

            HttpResponse httpResponse = new HttpResponse(200, Map.of(), responseJson);
            when(httpClient.execute(any())).thenReturn(CompletableFuture.completedFuture(httpResponse));

            PublicKeySet result = service.getKeys(walletUrl).join();

            assertThat(result).isNotNull();
            assertThat(result.keys()).hasSize(1);

            PublicKey key = result.keys().get(0);
            assertThat(key.kid()).isEqualTo("key-1");
            assertThat(key.kty()).isEqualTo("OKP");
            assertThat(key.use()).isEqualTo("sig");
            assertThat(key.alg()).isEqualTo("EdDSA");
            assertThat(key.x()).isEqualTo("11qYAYKxCrfVS_7TyWQHOg7hcvPapiMlrwIaaPcHURo");
        }

        @Test
        @DisplayName("should retrieve multiple public keys")
        void shouldRetrieveMultiplePublicKeys() {
            String walletUrl = "https://wallet.example.com/alice";
            String responseJson = """
                    {
                        "keys": [
                            {
                                "kid": "key-1",
                                "kty": "OKP",
                                "use": "sig",
                                "alg": "EdDSA",
                                "x": "11qYAYKxCrfVS_7TyWQHOg7hcvPapiMlrwIaaPcHURo"
                            },
                            {
                                "kid": "key-2",
                                "kty": "OKP",
                                "use": "sig",
                                "alg": "EdDSA",
                                "x": "22rYAYKxCrfVS_7TyWQHOg7hcvPapiMlrwIaaPcHURo"
                            }
                        ]
                    }
                    """;

            HttpResponse httpResponse = new HttpResponse(200, Map.of(), responseJson);
            when(httpClient.execute(any())).thenReturn(CompletableFuture.completedFuture(httpResponse));

            PublicKeySet result = service.getKeys(walletUrl).join();

            assertThat(result.keys()).hasSize(2);
            assertThat(result.keys().get(0).kid()).isEqualTo("key-1");
            assertThat(result.keys().get(1).kid()).isEqualTo("key-2");
        }

        @Test
        @DisplayName("should construct correct JWKS URL")
        void shouldConstructCorrectJwksUrl() {
            String walletUrl = "https://wallet.example.com/alice";
            String responseJson = """
                    {
                        "keys": []
                    }
                    """;

            HttpResponse httpResponse = new HttpResponse(200, Map.of(), responseJson);
            when(httpClient.execute(any())).thenReturn(CompletableFuture.completedFuture(httpResponse));

            service.getKeys(walletUrl).join();

            ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
            verify(httpClient).execute(captor.capture());

            HttpRequest request = captor.getValue();
            assertThat(request.uri().toString()).isEqualTo("https://wallet.example.com/alice/jwks.json");
        }

        @Test
        @DisplayName("should handle wallet URL with trailing slash")
        void shouldHandleWalletUrlWithTrailingSlash() {
            String walletUrl = "https://wallet.example.com/alice/";
            String responseJson = """
                    {
                        "keys": []
                    }
                    """;

            HttpResponse httpResponse = new HttpResponse(200, Map.of(), responseJson);
            when(httpClient.execute(any())).thenReturn(CompletableFuture.completedFuture(httpResponse));

            service.getKeys(walletUrl).join();

            ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
            verify(httpClient).execute(captor.capture());

            HttpRequest request = captor.getValue();
            // Should normalize to remove double slash
            assertThat(request.uri().toString()).isEqualTo("https://wallet.example.com/alice/jwks.json");
        }

        @Test
        @DisplayName("should throw when walletAddressUrl is null")
        void shouldThrowWhenWalletAddressUrlIsNull() {
            assertThatThrownBy(() -> service.getKeys(null)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("walletAddressUrl must not be null");
        }

        @Test
        @DisplayName("should throw WalletAddressException when HTTP error occurs")
        void shouldThrowWhenHttpErrorOccurs() {
            String walletUrl = "https://wallet.example.com/notfound";
            HttpResponse httpResponse = new HttpResponse(404, Map.of(), "Not Found");
            when(httpClient.execute(any())).thenReturn(CompletableFuture.completedFuture(httpResponse));

            assertThatThrownBy(() -> service.getKeys(walletUrl).join()).hasCauseInstanceOf(WalletAddressException.class)
                    .hasMessageContaining("Failed to retrieve public keys").hasMessageContaining("404");
        }

        @Test
        @DisplayName("should throw WalletAddressException when JSON is invalid")
        void shouldThrowWhenJsonIsInvalid() {
            String walletUrl = "https://wallet.example.com/alice";
            HttpResponse httpResponse = new HttpResponse(200, Map.of(), "invalid json");
            when(httpClient.execute(any())).thenReturn(CompletableFuture.completedFuture(httpResponse));

            assertThatThrownBy(() -> service.getKeys(walletUrl).join()).hasCauseInstanceOf(WalletAddressException.class)
                    .hasMessageContaining("Failed to parse public key set");
        }

        @Test
        @DisplayName("should throw WalletAddressException when keys array is missing")
        void shouldThrowWhenKeysArrayIsMissing() {
            String walletUrl = "https://wallet.example.com/alice";
            String responseJson = """
                    {
                        "notKeys": []
                    }
                    """;

            HttpResponse httpResponse = new HttpResponse(200, Map.of(), responseJson);
            when(httpClient.execute(any())).thenReturn(CompletableFuture.completedFuture(httpResponse));

            assertThatThrownBy(() -> service.getKeys(walletUrl).join()).hasCauseInstanceOf(WalletAddressException.class)
                    .hasMessageContaining("Invalid JWKS structure");
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("should handle various HTTP error codes")
        void shouldHandleVariousHttpErrorCodes() {
            String url = "https://wallet.example.com/test";

            for (int statusCode : new int[]{400, 401, 403, 500, 502, 503}) {
                HttpResponse httpResponse = new HttpResponse(statusCode, Map.of(), "Error");
                when(httpClient.execute(any())).thenReturn(CompletableFuture.completedFuture(httpResponse));

                assertThatThrownBy(() -> service.get(url).join()).hasCauseInstanceOf(WalletAddressException.class)
                        .hasMessageContaining(String.valueOf(statusCode));
            }
        }

        @Test
        @DisplayName("should handle empty response body")
        void shouldHandleEmptyResponseBody() {
            String url = "https://wallet.example.com/test";
            HttpResponse httpResponse = new HttpResponse(200, Map.of(), "");
            when(httpClient.execute(any())).thenReturn(CompletableFuture.completedFuture(httpResponse));

            assertThatThrownBy(() -> service.get(url).join()).hasCauseInstanceOf(WalletAddressException.class)
                    .hasMessageContaining("Failed to parse wallet address");
        }
    }
}
