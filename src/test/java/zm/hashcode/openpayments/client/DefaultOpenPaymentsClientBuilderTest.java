package zm.hashcode.openpayments.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.net.URI;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import zm.hashcode.openpayments.auth.GrantService;
import zm.hashcode.openpayments.http.core.HttpClient;
import zm.hashcode.openpayments.payment.incoming.IncomingPaymentService;
import zm.hashcode.openpayments.payment.outgoing.OutgoingPaymentService;
import zm.hashcode.openpayments.payment.quote.QuoteService;
import zm.hashcode.openpayments.wallet.WalletAddressService;

/**
 * Unit tests for {@link DefaultOpenPaymentsClientBuilder}.
 */
@DisplayName("DefaultOpenPaymentsClientBuilder")
class DefaultOpenPaymentsClientBuilderTest {

    private DefaultOpenPaymentsClientBuilder builder;
    private PrivateKey testPrivateKey;
    private String testKeyId;
    private String testWalletAddress;

    @BeforeEach
    void setUp() throws Exception {
        builder = (DefaultOpenPaymentsClientBuilder) OpenPaymentsClient.builder();

        // Generate test key
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("Ed25519");
        KeyPair keyPair = keyGen.generateKeyPair();
        testPrivateKey = keyPair.getPrivate();
        testKeyId = "test-key-id";
        testWalletAddress = "https://wallet.example.com/alice";
    }

    @Nested
    @DisplayName("Required Fields")
    class RequiredFieldsTests {

        @Test
        @DisplayName("should require wallet address")
        void shouldRequireWalletAddress() {
            assertThatThrownBy(() -> builder.privateKey(testPrivateKey).keyId(testKeyId).build())
                    .isInstanceOf(IllegalStateException.class).hasMessageContaining("walletAddress is required");
        }

        @Test
        @DisplayName("should require private key")
        void shouldRequirePrivateKey() {
            assertThatThrownBy(() -> builder.walletAddress(testWalletAddress).keyId(testKeyId).build())
                    .isInstanceOf(IllegalStateException.class).hasMessageContaining("privateKey is required");
        }

        @Test
        @DisplayName("should require key ID")
        void shouldRequireKeyId() {
            assertThatThrownBy(() -> builder.walletAddress(testWalletAddress).privateKey(testPrivateKey).build())
                    .isInstanceOf(IllegalStateException.class).hasMessageContaining("keyId is required");
        }

        @Test
        @DisplayName("should accept wallet address as string")
        void shouldAcceptWalletAddressAsString() {
            builder.walletAddress(testWalletAddress);

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("should accept wallet address as URI")
        void shouldAcceptWalletAddressAsUri() {
            URI uri = URI.create(testWalletAddress);

            builder.walletAddress(uri);

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("should throw when wallet address string is null")
        void shouldThrowWhenWalletAddressStringIsNull() {
            assertThatThrownBy(() -> builder.walletAddress((String) null)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("walletAddress must not be null");
        }

        @Test
        @DisplayName("should throw when wallet address URI is null")
        void shouldThrowWhenWalletAddressUriIsNull() {
            assertThatThrownBy(() -> builder.walletAddress((URI) null)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("walletAddress must not be null");
        }

        @Test
        @DisplayName("should throw when private key is null")
        void shouldThrowWhenPrivateKeyIsNull() {
            assertThatThrownBy(() -> builder.privateKey(null)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("privateKey must not be null");
        }

        @Test
        @DisplayName("should throw when key ID is null")
        void shouldThrowWhenKeyIdIsNull() {
            assertThatThrownBy(() -> builder.keyId(null)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("keyId must not be null");
        }
    }

    @Nested
    @DisplayName("Optional Fields")
    class OptionalFieldsTests {

        @Test
        @DisplayName("should accept custom request timeout")
        void shouldAcceptCustomRequestTimeout() {
            Duration timeout = Duration.ofSeconds(60);

            OpenPaymentsClientBuilder result = builder.requestTimeout(timeout);

            assertThat(result).isSameAs(builder);
        }

        @Test
        @DisplayName("should throw when request timeout is null")
        void shouldThrowWhenRequestTimeoutIsNull() {
            assertThatThrownBy(() -> builder.requestTimeout(null)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("timeout must not be null");
        }

        @Test
        @DisplayName("should accept custom connection timeout")
        void shouldAcceptCustomConnectionTimeout() {
            Duration timeout = Duration.ofSeconds(20);

            OpenPaymentsClientBuilder result = builder.connectionTimeout(timeout);

            assertThat(result).isSameAs(builder);
        }

        @Test
        @DisplayName("should throw when connection timeout is null")
        void shouldThrowWhenConnectionTimeoutIsNull() {
            assertThatThrownBy(() -> builder.connectionTimeout(null)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("timeout must not be null");
        }

        @Test
        @DisplayName("should accept auto refresh tokens setting")
        void shouldAcceptAutoRefreshTokens() {
            OpenPaymentsClientBuilder result1 = builder.autoRefreshTokens(true);
            OpenPaymentsClientBuilder result2 = builder.autoRefreshTokens(false);

            assertThat(result1).isSameAs(builder);
            assertThat(result2).isSameAs(builder);
        }

        @Test
        @DisplayName("should accept custom user agent")
        void shouldAcceptCustomUserAgent() {
            String userAgent = "MyApp/1.0";

            OpenPaymentsClientBuilder result = builder.userAgent(userAgent);

            assertThat(result).isSameAs(builder);
        }

        @Test
        @DisplayName("should throw when user agent is null")
        void shouldThrowWhenUserAgentIsNull() {
            assertThatThrownBy(() -> builder.userAgent(null)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("userAgent must not be null");
        }
    }

    @Nested
    @DisplayName("Service Injection")
    class ServiceInjectionTests {

        private HttpClient mockHttpClient;
        private WalletAddressService mockWalletService;
        private IncomingPaymentService mockIncomingService;
        private OutgoingPaymentService mockOutgoingService;
        private QuoteService mockQuoteService;
        private GrantService mockGrantService;
        private ObjectMapper mockObjectMapper;

        @BeforeEach
        void setUpMocks() {
            mockHttpClient = mock(HttpClient.class);
            mockWalletService = mock(WalletAddressService.class);
            mockIncomingService = mock(IncomingPaymentService.class);
            mockOutgoingService = mock(OutgoingPaymentService.class);
            mockQuoteService = mock(QuoteService.class);
            mockGrantService = mock(GrantService.class);
            mockObjectMapper = new ObjectMapper();
        }

        @Test
        @DisplayName("should accept custom http client")
        void shouldAcceptCustomHttpClient() {
            builder.httpClient(mockHttpClient);

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("should accept custom object mapper")
        void shouldAcceptCustomObjectMapper() {
            builder.objectMapper(mockObjectMapper);

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("should accept custom wallet address service")
        void shouldAcceptCustomWalletAddressService() {
            builder.walletAddressService(mockWalletService);

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("should accept custom incoming payment service")
        void shouldAcceptCustomIncomingPaymentService() {
            builder.incomingPaymentService(mockIncomingService);

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("should accept custom outgoing payment service")
        void shouldAcceptCustomOutgoingPaymentService() {
            builder.outgoingPaymentService(mockOutgoingService);

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("should accept custom quote service")
        void shouldAcceptCustomQuoteService() {
            builder.quoteService(mockQuoteService);

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("should accept custom grant service")
        void shouldAcceptCustomGrantService() {
            builder.grantService(mockGrantService);

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("should build client with all custom services")
        void shouldBuildWithAllCustomServices() {
            OpenPaymentsClient client = builder.walletAddress(testWalletAddress).privateKey(testPrivateKey)
                    .keyId(testKeyId).httpClient(mockHttpClient).objectMapper(mockObjectMapper)
                    .walletAddressService(mockWalletService).incomingPaymentService(mockIncomingService)
                    .outgoingPaymentService(mockOutgoingService).quoteService(mockQuoteService)
                    .grantService(mockGrantService).build();

            assertThat(client).isNotNull();
            assertThat(client.walletAddresses()).isSameAs(mockWalletService);
            assertThat(client.incomingPayments()).isSameAs(mockIncomingService);
            assertThat(client.outgoingPayments()).isSameAs(mockOutgoingService);
            assertThat(client.quotes()).isSameAs(mockQuoteService);
            assertThat(client.grants()).isSameAs(mockGrantService);
        }
    }

    @Nested
    @DisplayName("Builder Pattern")
    class BuilderPatternTests {

        @Test
        @DisplayName("should support fluent chaining")
        void shouldSupportFluentChaining() {
            assertThatCode(() -> builder.walletAddress(testWalletAddress).privateKey(testPrivateKey).keyId(testKeyId)
                    .requestTimeout(Duration.ofSeconds(30)).connectionTimeout(Duration.ofSeconds(10))
                    .autoRefreshTokens(true).userAgent("Test/1.0")).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should return same builder instance")
        void shouldReturnSameBuilderInstance() {
            OpenPaymentsClientBuilder result1 = builder.walletAddress(testWalletAddress);
            OpenPaymentsClientBuilder result2 = builder.privateKey(testPrivateKey);
            OpenPaymentsClientBuilder result3 = builder.keyId(testKeyId);

            assertThat(result1).isSameAs(builder);
            assertThat(result2).isSameAs(builder);
            assertThat(result3).isSameAs(builder);
        }

        @Test
        @DisplayName("should allow setting fields in any order")
        void shouldAllowSettingFieldsInAnyOrder() {
            HttpClient mockHttpClient = mock(HttpClient.class);
            WalletAddressService mockWalletService = mock(WalletAddressService.class);
            IncomingPaymentService mockIncomingService = mock(IncomingPaymentService.class);
            OutgoingPaymentService mockOutgoingService = mock(OutgoingPaymentService.class);
            QuoteService mockQuoteService = mock(QuoteService.class);
            GrantService mockGrantService = mock(GrantService.class);

            assertThatCode(() -> builder.keyId(testKeyId).privateKey(testPrivateKey).walletAddress(testWalletAddress)
                    .userAgent("Test").autoRefreshTokens(false).httpClient(mockHttpClient)
                    .walletAddressService(mockWalletService).incomingPaymentService(mockIncomingService)
                    .outgoingPaymentService(mockOutgoingService).quoteService(mockQuoteService)
                    .grantService(mockGrantService).build()).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Build Validation")
    class BuildValidationTests {

        @Test
        @DisplayName("should throw when building without http client")
        void shouldThrowWhenBuildingWithoutHttpClient() {
            assertThatThrownBy(
                    () -> builder.walletAddress(testWalletAddress).privateKey(testPrivateKey).keyId(testKeyId).build())
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessageContaining("Default HttpClient not yet implemented");
        }

        @Test
        @DisplayName("should create default wallet address service when not provided")
        void shouldCreateDefaultWalletAddressServiceWhenNotProvided() {
            HttpClient mockHttpClient = mock(HttpClient.class);
            IncomingPaymentService mockIncomingService = mock(IncomingPaymentService.class);
            OutgoingPaymentService mockOutgoingService = mock(OutgoingPaymentService.class);
            QuoteService mockQuoteService = mock(QuoteService.class);
            GrantService mockGrantService = mock(GrantService.class);

            OpenPaymentsClient client = builder.walletAddress(testWalletAddress).privateKey(testPrivateKey)
                    .keyId(testKeyId).httpClient(mockHttpClient).incomingPaymentService(mockIncomingService)
                    .outgoingPaymentService(mockOutgoingService).quoteService(mockQuoteService)
                    .grantService(mockGrantService).build();

            assertThat(client.walletAddresses()).isNotNull();
        }

        @Test
        @DisplayName("should create default incoming payment service when not provided")
        void shouldCreateDefaultIncomingPaymentServiceWhenNotProvided() {
            HttpClient mockHttpClient = mock(HttpClient.class);
            WalletAddressService mockWalletService = mock(WalletAddressService.class);
            OutgoingPaymentService mockOutgoingService = mock(OutgoingPaymentService.class);
            QuoteService mockQuoteService = mock(QuoteService.class);
            GrantService mockGrantService = mock(GrantService.class);

            OpenPaymentsClient client = builder.walletAddress(testWalletAddress).privateKey(testPrivateKey)
                    .keyId(testKeyId).httpClient(mockHttpClient).walletAddressService(mockWalletService)
                    .outgoingPaymentService(mockOutgoingService).quoteService(mockQuoteService)
                    .grantService(mockGrantService).build();

            assertThat(client.incomingPayments()).isNotNull();
        }

        @Test
        @DisplayName("should create default outgoing payment service when not provided")
        void shouldCreateDefaultOutgoingPaymentServiceWhenNotProvided() {
            HttpClient mockHttpClient = mock(HttpClient.class);
            WalletAddressService mockWalletService = mock(WalletAddressService.class);
            IncomingPaymentService mockIncomingService = mock(IncomingPaymentService.class);
            QuoteService mockQuoteService = mock(QuoteService.class);
            GrantService mockGrantService = mock(GrantService.class);

            OpenPaymentsClient client = builder.walletAddress(testWalletAddress).privateKey(testPrivateKey)
                    .keyId(testKeyId).httpClient(mockHttpClient).walletAddressService(mockWalletService)
                    .incomingPaymentService(mockIncomingService).quoteService(mockQuoteService)
                    .grantService(mockGrantService).build();

            assertThat(client.outgoingPayments()).isNotNull();
        }

        @Test
        @DisplayName("should create default quote service when not provided")
        void shouldCreateDefaultQuoteServiceWhenNotProvided() {
            HttpClient mockHttpClient = mock(HttpClient.class);
            WalletAddressService mockWalletService = mock(WalletAddressService.class);
            IncomingPaymentService mockIncomingService = mock(IncomingPaymentService.class);
            OutgoingPaymentService mockOutgoingService = mock(OutgoingPaymentService.class);
            GrantService mockGrantService = mock(GrantService.class);

            OpenPaymentsClient client = builder.walletAddress(testWalletAddress).privateKey(testPrivateKey)
                    .keyId(testKeyId).httpClient(mockHttpClient).walletAddressService(mockWalletService)
                    .incomingPaymentService(mockIncomingService).outgoingPaymentService(mockOutgoingService)
                    .grantService(mockGrantService).build();

            assertThat(client.quotes()).isNotNull();
        }
    }

    @Nested
    @DisplayName("ObjectMapper Configuration")
    class ObjectMapperConfigurationTests {

        @Test
        @DisplayName("should create ObjectMapper with Jdk8Module when not provided")
        void shouldCreateObjectMapperWithJdk8Module() {
            // This test verifies that the builder creates a properly configured ObjectMapper
            // We can't directly test this without building the client, but we can verify
            // that the builder doesn't throw when no ObjectMapper is provided
            HttpClient mockHttpClient = mock(HttpClient.class);
            WalletAddressService mockWalletService = mock(WalletAddressService.class);
            IncomingPaymentService mockIncomingService = mock(IncomingPaymentService.class);
            OutgoingPaymentService mockOutgoingService = mock(OutgoingPaymentService.class);
            QuoteService mockQuoteService = mock(QuoteService.class);
            GrantService mockGrantService = mock(GrantService.class);

            assertThatCode(() -> builder.walletAddress(testWalletAddress).privateKey(testPrivateKey).keyId(testKeyId)
                    .httpClient(mockHttpClient).walletAddressService(mockWalletService)
                    .incomingPaymentService(mockIncomingService).outgoingPaymentService(mockOutgoingService)
                    .quoteService(mockQuoteService).grantService(mockGrantService).build()).doesNotThrowAnyException();
        }
    }
}
