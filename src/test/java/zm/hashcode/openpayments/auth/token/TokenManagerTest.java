package zm.hashcode.openpayments.auth.token;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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

import zm.hashcode.openpayments.auth.grant.Access;
import zm.hashcode.openpayments.auth.grant.AccessTokenResponse;
import zm.hashcode.openpayments.http.core.HttpClient;
import zm.hashcode.openpayments.http.core.HttpRequest;
import zm.hashcode.openpayments.http.core.HttpResponse;

/**
 * Unit tests for {@link TokenManager}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TokenManager")
class TokenManagerTest {

    @Mock
    private HttpClient httpClient;

    private ObjectMapper objectMapper;
    private TokenManager tokenManager;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jdk8.Jdk8Module());
        tokenManager = new TokenManager(httpClient, objectMapper);
    }

    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("should construct with valid parameters")
        void shouldConstructWithValidParameters() {
            assertThat(tokenManager).isNotNull();
        }

        @Test
        @DisplayName("should throw when httpClient is null")
        void shouldThrowWhenHttpClientIsNull() {
            assertThatThrownBy(() -> new TokenManager(null, objectMapper)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("httpClient must not be null");
        }

        @Test
        @DisplayName("should throw when objectMapper is null")
        void shouldThrowWhenObjectMapperIsNull() {
            assertThatThrownBy(() -> new TokenManager(httpClient, null)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("objectMapper must not be null");
        }
    }

    @Nested
    @DisplayName("Token Rotation")
    class TokenRotationTests {

        @Test
        @DisplayName("should rotate token successfully")
        void shouldRotateToken() throws Exception {
            // Setup current token
            AccessTokenResponse currentToken = new AccessTokenResponse("old-token-value",
                    "https://auth.example.com/token/manage", Optional.of(3600L),
                    List.of(Access.incomingPayment(List.of("create", "read"))));

            // Setup mock response with new token
            String responseJson = """
                    {
                        "value": "new-token-value",
                        "manage": "https://auth.example.com/token/manage",
                        "expires_in": 7200,
                        "access": [
                            {
                                "type": "incoming-payment",
                                "actions": ["create", "read"]
                            }
                        ]
                    }
                    """;

            HttpResponse httpResponse = new HttpResponse(200, Map.of(), responseJson);
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));

            // Execute
            AccessTokenResponse newToken = tokenManager.rotateToken(currentToken).join();

            // Verify
            assertThat(newToken).isNotNull();
            assertThat(newToken.value()).isEqualTo("new-token-value");
            assertThat(newToken.manage()).isEqualTo("https://auth.example.com/token/manage");
            assertThat(newToken.expiresIn()).contains(7200L);
            assertThat(newToken.access()).hasSize(1);

            // Verify HTTP request was made with correct headers
            ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
            verify(httpClient).execute(requestCaptor.capture());

            HttpRequest capturedRequest = requestCaptor.getValue();
            assertThat(capturedRequest.method().name()).isEqualTo("POST");
            assertThat(capturedRequest.uri().toString()).isEqualTo("https://auth.example.com/token/manage");
            assertThat(capturedRequest.headers().get("Authorization")).isEqualTo("GNAP old-token-value");
        }

        @Test
        @DisplayName("should include authorization header in rotation request")
        void shouldIncludeAuthorizationHeader() {
            AccessTokenResponse token = new AccessTokenResponse("my-token", "https://example.com/manage",
                    Optional.empty(), List.of());

            HttpResponse httpResponse = new HttpResponse(200, Map.of(),
                    "{\"value\":\"new-token\",\"manage\":\"https://example.com/manage\",\"access\":[]}");
            when(httpClient.execute(any())).thenReturn(CompletableFuture.completedFuture(httpResponse));

            tokenManager.rotateToken(token).join();

            ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
            verify(httpClient).execute(captor.capture());

            HttpRequest capturedRequest = captor.getValue();
            assertThat(capturedRequest.headers().get("Authorization")).isEqualTo("GNAP my-token");
        }

        @Test
        @DisplayName("should throw when rotation fails")
        void shouldThrowWhenRotationFails() {
            AccessTokenResponse token = new AccessTokenResponse("token", "https://example.com/manage", Optional.empty(),
                    List.of());

            HttpResponse httpResponse = new HttpResponse(401, Map.of(), "Unauthorized");
            when(httpClient.execute(any())).thenReturn(CompletableFuture.completedFuture(httpResponse));

            assertThatThrownBy(() -> tokenManager.rotateToken(token).join()).hasCauseInstanceOf(TokenException.class)
                    .hasMessageContaining("Token rotation failed: 401");
        }

        @Test
        @DisplayName("should throw when token is null")
        void shouldThrowWhenTokenIsNull() {
            assertThatThrownBy(() -> tokenManager.rotateToken(null)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("currentToken must not be null");
        }

        @Test
        @DisplayName("should throw when response parsing fails")
        void shouldThrowWhenResponseParsingFails() {
            AccessTokenResponse token = new AccessTokenResponse("token", "https://example.com/manage", Optional.empty(),
                    List.of());

            HttpResponse httpResponse = new HttpResponse(200, Map.of(), "invalid json");
            when(httpClient.execute(any())).thenReturn(CompletableFuture.completedFuture(httpResponse));

            assertThatThrownBy(() -> tokenManager.rotateToken(token).join()).hasCauseInstanceOf(TokenException.class)
                    .hasMessageContaining("Failed to parse token rotation response");
        }
    }

    @Nested
    @DisplayName("Token Revocation")
    class TokenRevocationTests {

        @Test
        @DisplayName("should revoke token successfully")
        void shouldRevokeToken() {
            AccessTokenResponse token = new AccessTokenResponse("token-to-revoke",
                    "https://auth.example.com/token/manage", Optional.empty(), List.of());

            HttpResponse httpResponse = new HttpResponse(204, Map.of(), "");
            when(httpClient.execute(any())).thenReturn(CompletableFuture.completedFuture(httpResponse));

            assertThatCode(() -> tokenManager.revokeToken(token).join()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should send DELETE request")
        void shouldSendDeleteRequest() {
            AccessTokenResponse token = new AccessTokenResponse("token", "https://example.com/manage", Optional.empty(),
                    List.of());

            HttpResponse httpResponse = new HttpResponse(204, Map.of(), "");
            when(httpClient.execute(any())).thenReturn(CompletableFuture.completedFuture(httpResponse));

            tokenManager.revokeToken(token).join();

            ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
            verify(httpClient).execute(captor.capture());

            HttpRequest capturedRequest = captor.getValue();
            assertThat(capturedRequest.method().name()).isEqualTo("DELETE");
            assertThat(capturedRequest.uri().toString()).isEqualTo("https://example.com/manage");
        }

        @Test
        @DisplayName("should include authorization header in revocation request")
        void shouldIncludeAuthorizationHeader() {
            AccessTokenResponse token = new AccessTokenResponse("my-revoke-token", "https://example.com/manage",
                    Optional.empty(), List.of());

            HttpResponse httpResponse = new HttpResponse(204, Map.of(), "");
            when(httpClient.execute(any())).thenReturn(CompletableFuture.completedFuture(httpResponse));

            tokenManager.revokeToken(token).join();

            ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
            verify(httpClient).execute(captor.capture());

            HttpRequest capturedRequest = captor.getValue();
            assertThat(capturedRequest.headers().get("Authorization")).isEqualTo("GNAP my-revoke-token");
        }

        @Test
        @DisplayName("should throw when revocation fails")
        void shouldThrowWhenRevocationFails() {
            AccessTokenResponse token = new AccessTokenResponse("token", "https://example.com/manage", Optional.empty(),
                    List.of());

            HttpResponse httpResponse = new HttpResponse(403, Map.of(), "Forbidden");
            when(httpClient.execute(any())).thenReturn(CompletableFuture.completedFuture(httpResponse));

            assertThatThrownBy(() -> tokenManager.revokeToken(token).join()).hasCauseInstanceOf(TokenException.class)
                    .hasMessageContaining("Token revocation failed: 403");
        }

        @Test
        @DisplayName("should throw when token is null")
        void shouldThrowWhenTokenIsNull() {
            assertThatThrownBy(() -> tokenManager.revokeToken(null)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("token must not be null");
        }

        @Test
        @DisplayName("should handle 200 OK response")
        void shouldHandle200Response() {
            AccessTokenResponse token = new AccessTokenResponse("token", "https://example.com/manage", Optional.empty(),
                    List.of());

            // Some servers might return 200 OK instead of 204 No Content
            HttpResponse httpResponse = new HttpResponse(200, Map.of(), "{}");
            when(httpClient.execute(any())).thenReturn(CompletableFuture.completedFuture(httpResponse));

            assertThatCode(() -> tokenManager.revokeToken(token).join()).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Integration Scenarios")
    class IntegrationScenarios {

        @Test
        @DisplayName("should handle token rotation before expiration")
        void shouldHandleTokenRotationBeforeExpiration() throws Exception {
            // Original token expires in 60 seconds
            AccessTokenResponse currentToken = new AccessTokenResponse("expiring-token",
                    "https://auth.example.com/token/manage", Optional.of(60L),
                    List.of(Access.incomingPayment(List.of("create"))));

            // New token with extended expiration
            String responseJson = """
                    {
                        "value": "refreshed-token",
                        "manage": "https://auth.example.com/token/manage",
                        "expires_in": 3600,
                        "access": [
                            {
                                "type": "incoming-payment",
                                "actions": ["create"]
                            }
                        ]
                    }
                    """;

            HttpResponse httpResponse = new HttpResponse(200, Map.of(), responseJson);
            when(httpClient.execute(any())).thenReturn(CompletableFuture.completedFuture(httpResponse));

            AccessTokenResponse newToken = tokenManager.rotateToken(currentToken).join();

            assertThat(newToken.value()).isEqualTo("refreshed-token");
            assertThat(newToken.expiresIn()).contains(3600L);
        }

        @Test
        @DisplayName("should handle multiple token rotations")
        void shouldHandleMultipleTokenRotations() throws Exception {
            AccessTokenResponse token1 = new AccessTokenResponse("token-1", "https://example.com/manage",
                    Optional.empty(), List.of());

            HttpResponse response1 = new HttpResponse(200, Map.of(),
                    "{\"value\":\"token-2\",\"manage\":\"https://example.com/manage\",\"access\":[]}");
            HttpResponse response2 = new HttpResponse(200, Map.of(),
                    "{\"value\":\"token-3\",\"manage\":\"https://example.com/manage\",\"access\":[]}");

            when(httpClient.execute(any())).thenReturn(CompletableFuture.completedFuture(response1))
                    .thenReturn(CompletableFuture.completedFuture(response2));

            // First rotation
            AccessTokenResponse token2 = tokenManager.rotateToken(token1).join();
            assertThat(token2.value()).isEqualTo("token-2");

            // Second rotation
            AccessTokenResponse token3 = tokenManager.rotateToken(token2).join();
            assertThat(token3.value()).isEqualTo("token-3");

            verify(httpClient, times(2)).execute(any());
        }
    }
}
