package zm.hashcode.openpayments.auth.grant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

import zm.hashcode.openpayments.auth.keys.ClientKey;
import zm.hashcode.openpayments.auth.keys.ClientKeyGenerator;
import zm.hashcode.openpayments.auth.signature.HttpSignatureService;
import zm.hashcode.openpayments.http.core.HttpClient;
import zm.hashcode.openpayments.http.core.HttpRequest;
import zm.hashcode.openpayments.http.core.HttpResponse;

/**
 * Unit tests for {@link GrantService}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GrantService")
class GrantServiceTest {

    @Mock
    private HttpClient httpClient;

    private HttpSignatureService signatureService;
    private ObjectMapper objectMapper;
    private GrantService grantService;

    @BeforeEach
    void setUp() {
        ClientKey clientKey = ClientKeyGenerator.generate("test-key");
        signatureService = new HttpSignatureService(clientKey);
        objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jdk8.Jdk8Module());
        grantService = new GrantService(httpClient, signatureService, objectMapper);
    }

    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("should construct with valid parameters")
        void shouldConstructWithValidParameters() {
            assertThat(grantService).isNotNull();
        }

        @Test
        @DisplayName("should throw when httpClient is null")
        void shouldThrowWhenHttpClientIsNull() {
            assertThatThrownBy(() -> new GrantService(null, signatureService, objectMapper))
                    .isInstanceOf(NullPointerException.class).hasMessageContaining("httpClient must not be null");
        }

        @Test
        @DisplayName("should throw when signatureService is null")
        void shouldThrowWhenSignatureServiceIsNull() {
            assertThatThrownBy(() -> new GrantService(httpClient, null, objectMapper))
                    .isInstanceOf(NullPointerException.class).hasMessageContaining("signatureService must not be null");
        }

        @Test
        @DisplayName("should throw when objectMapper is null")
        void shouldThrowWhenObjectMapperIsNull() {
            assertThatThrownBy(() -> new GrantService(httpClient, signatureService, null))
                    .isInstanceOf(NullPointerException.class).hasMessageContaining("objectMapper must not be null");
        }
    }

    @Nested
    @DisplayName("Request Grant")
    class RequestGrantTests {

        @Test
        @DisplayName("should send grant request successfully")
        void shouldSendGrantRequest() throws Exception {
            // Setup request
            GrantRequest request = GrantRequest.builder()
                    .accessToken(AccessTokenRequest.builder()
                            .addAccess(Access.incomingPayment(List.of("create", "read"))).build())
                    .client(Client.builder().key("https://example.com/jwks.json").build()).build();

            // Setup mock response
            String responseJson = """
                    {
                        "continue": {
                            "access_token": {"value": "continue-token"},
                            "uri": "https://auth.example.com/continue"
                        },
                        "interact": {
                            "redirect": "https://auth.example.com/interact",
                            "finish": "finish-token"
                        }
                    }
                    """;

            HttpResponse httpResponse = new HttpResponse(200, Map.of(), responseJson);
            when(httpClient.execute(any(HttpRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(httpResponse));

            // Execute
            GrantResponse response = grantService.requestGrant("https://auth.example.com/grant", request).join();

            // Verify
            assertThat(response).isNotNull();
            assertThat(response.requiresInteraction()).isTrue();
            assertThat(response.isPending()).isTrue();

            // Verify HTTP request was made with correct headers
            ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
            verify(httpClient).execute(requestCaptor.capture());

            HttpRequest capturedRequest = requestCaptor.getValue();
            assertThat(capturedRequest.headers()).containsKeys("Content-Type", "Content-Digest", "Signature",
                    "Signature-Input");
        }

        @Test
        @DisplayName("should include signature in request")
        void shouldIncludeSignature() throws Exception {
            GrantRequest request = GrantRequest.builder()
                    .accessToken(
                            AccessTokenRequest.builder().addAccess(Access.incomingPayment(List.of("create"))).build())
                    .client(Client.builder().build()).build();

            HttpResponse httpResponse = new HttpResponse(200, Map.of(),
                    "{\"continue\":{\"access_token\":{\"value\":\"token\"},\"uri\":\"uri\"}}");
            when(httpClient.execute(any())).thenReturn(CompletableFuture.completedFuture(httpResponse));

            grantService.requestGrant("https://example.com/grant", request).join();

            ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
            verify(httpClient).execute(captor.capture());

            HttpRequest capturedRequest = captor.getValue();
            assertThat(capturedRequest.headers().get("Signature")).isNotNull();
            assertThat(capturedRequest.headers().get("Signature-Input")).isNotNull();
            assertThat(capturedRequest.headers().get("Signature-Input")).contains("sig=(");
            assertThat(capturedRequest.headers().get("Signature-Input")).contains("@method");
            assertThat(capturedRequest.headers().get("Signature-Input")).contains("@target-uri");
        }

        @Test
        @DisplayName("should throw when grant request fails")
        void shouldThrowWhenGrantRequestFails() {
            GrantRequest request = GrantRequest.builder()
                    .accessToken(
                            AccessTokenRequest.builder().addAccess(Access.incomingPayment(List.of("create"))).build())
                    .client(Client.builder().build()).build();

            HttpResponse httpResponse = new HttpResponse(400, Map.of(), "Bad Request");
            when(httpClient.execute(any())).thenReturn(CompletableFuture.completedFuture(httpResponse));

            assertThatThrownBy(() -> grantService.requestGrant("https://example.com/grant", request).join())
                    .hasCauseInstanceOf(GrantException.class).hasMessageContaining("Grant request failed: 400");
        }

        @Test
        @DisplayName("should throw when grantEndpoint is null")
        void shouldThrowWhenGrantEndpointIsNull() {
            GrantRequest request = GrantRequest.builder()
                    .accessToken(
                            AccessTokenRequest.builder().addAccess(Access.incomingPayment(List.of("create"))).build())
                    .client(Client.builder().build()).build();

            assertThatThrownBy(() -> grantService.requestGrant(null, request)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("grantEndpoint must not be null");
        }

        @Test
        @DisplayName("should throw when request is null")
        void shouldThrowWhenRequestIsNull() {
            assertThatThrownBy(() -> grantService.requestGrant("https://example.com", null))
                    .isInstanceOf(NullPointerException.class).hasMessageContaining("request must not be null");
        }
    }

    @Nested
    @DisplayName("Continue Grant")
    class ContinueGrantTests {

        @Test
        @DisplayName("should continue grant successfully")
        void shouldContinueGrant() throws Exception {
            Continue continueInfo = new Continue(new ContinueToken("continue-token-value"),
                    "https://auth.example.com/continue/xyz", Optional.empty());

            String responseJson = """
                    {
                        "access_token": {
                            "value": "access-token-xyz",
                            "manage": "https://auth.example.com/manage",
                            "access": [
                                {
                                    "type": "incoming-payment",
                                    "actions": ["create", "read"]
                                }
                            ]
                        }
                    }
                    """;

            HttpResponse httpResponse = new HttpResponse(200, Map.of(), responseJson);
            when(httpClient.execute(any())).thenReturn(CompletableFuture.completedFuture(httpResponse));

            GrantResponse response = grantService.continueGrant(continueInfo, "interact-ref-123").join();

            assertThat(response).isNotNull();
            assertThat(response.isApproved()).isTrue();
            assertThat(response.accessToken()).isPresent();
        }

        @Test
        @DisplayName("should include authorization header")
        void shouldIncludeAuthorizationHeader() {
            Continue continueInfo = new Continue(new ContinueToken("my-token"), "https://example.com/continue",
                    Optional.empty());

            HttpResponse httpResponse = new HttpResponse(200, Map.of(),
                    "{\"access_token\":{\"value\":\"token\",\"manage\":\"url\",\"access\":[]}}");
            when(httpClient.execute(any())).thenReturn(CompletableFuture.completedFuture(httpResponse));

            grantService.continueGrant(continueInfo, "interact-ref").join();

            ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
            verify(httpClient).execute(captor.capture());

            HttpRequest capturedRequest = captor.getValue();
            assertThat(capturedRequest.headers().get("Authorization")).isEqualTo("GNAP my-token");
        }

        @Test
        @DisplayName("should throw when continueInfo is null")
        void shouldThrowWhenContinueInfoIsNull() {
            assertThatThrownBy(() -> grantService.continueGrant(null, "interact-ref"))
                    .isInstanceOf(NullPointerException.class).hasMessageContaining("continueInfo must not be null");
        }

        @Test
        @DisplayName("should throw when interactRef is null")
        void shouldThrowWhenInteractRefIsNull() {
            Continue continueInfo = new Continue(new ContinueToken("token"), "https://example.com/continue",
                    Optional.empty());

            assertThatThrownBy(() -> grantService.continueGrant(continueInfo, null))
                    .isInstanceOf(NullPointerException.class).hasMessageContaining("interactRef must not be null");
        }
    }

    @Nested
    @DisplayName("Cancel Grant")
    class CancelGrantTests {

        @Test
        @DisplayName("should cancel grant successfully")
        void shouldCancelGrant() {
            Continue continueInfo = new Continue(new ContinueToken("continue-token"),
                    "https://auth.example.com/continue/xyz", Optional.empty());

            HttpResponse httpResponse = new HttpResponse(204, Map.of(), "");
            when(httpClient.execute(any())).thenReturn(CompletableFuture.completedFuture(httpResponse));

            assertThatCode(() -> grantService.cancelGrant(continueInfo).join()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should send DELETE request")
        void shouldSendDeleteRequest() {
            Continue continueInfo = new Continue(new ContinueToken("token"), "https://example.com/continue",
                    Optional.empty());

            HttpResponse httpResponse = new HttpResponse(204, Map.of(), "");
            when(httpClient.execute(any())).thenReturn(CompletableFuture.completedFuture(httpResponse));

            grantService.cancelGrant(continueInfo).join();

            ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
            verify(httpClient).execute(captor.capture());

            HttpRequest capturedRequest = captor.getValue();
            assertThat(capturedRequest.method().name()).isEqualTo("DELETE");
            assertThat(capturedRequest.uri().toString()).contains("/continue");
        }

        @Test
        @DisplayName("should include authorization header")
        void shouldIncludeAuthorizationHeader() {
            Continue continueInfo = new Continue(new ContinueToken("my-continue-token"), "https://example.com/continue",
                    Optional.empty());

            HttpResponse httpResponse = new HttpResponse(204, Map.of(), "");
            when(httpClient.execute(any())).thenReturn(CompletableFuture.completedFuture(httpResponse));

            grantService.cancelGrant(continueInfo).join();

            ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
            verify(httpClient).execute(captor.capture());

            HttpRequest capturedRequest = captor.getValue();
            assertThat(capturedRequest.headers().get("Authorization")).isEqualTo("GNAP my-continue-token");
        }

        @Test
        @DisplayName("should throw when cancel fails")
        void shouldThrowWhenCancelFails() {
            Continue continueInfo = new Continue(new ContinueToken("token"), "https://example.com/continue",
                    Optional.empty());

            HttpResponse httpResponse = new HttpResponse(403, Map.of(), "Forbidden");
            when(httpClient.execute(any())).thenReturn(CompletableFuture.completedFuture(httpResponse));

            assertThatThrownBy(() -> grantService.cancelGrant(continueInfo).join())
                    .hasCauseInstanceOf(GrantException.class).hasMessageContaining("Cancel grant failed: 403");
        }

        @Test
        @DisplayName("should throw when continueInfo is null")
        void shouldThrowWhenContinueInfoIsNull() {
            assertThatThrownBy(() -> grantService.cancelGrant(null)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("continueInfo must not be null");
        }
    }
}
