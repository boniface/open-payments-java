package zm.hashcode.openpayments.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import zm.hashcode.openpayments.auth.GrantService;
import zm.hashcode.openpayments.http.core.HttpClient;
import zm.hashcode.openpayments.payment.incoming.IncomingPaymentService;
import zm.hashcode.openpayments.payment.outgoing.OutgoingPaymentService;
import zm.hashcode.openpayments.payment.quote.QuoteService;
import zm.hashcode.openpayments.wallet.WalletAddress;
import zm.hashcode.openpayments.wallet.WalletAddressService;

/**
 * Unit tests for {@link DefaultOpenPaymentsClient}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DefaultOpenPaymentsClient")
class DefaultOpenPaymentsClientTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private WalletAddressService walletAddressService;

    @Mock
    private IncomingPaymentService incomingPaymentService;

    @Mock
    private OutgoingPaymentService outgoingPaymentService;

    @Mock
    private QuoteService quoteService;

    @Mock
    private GrantService grantService;

    @Mock
    private WalletAddress walletAddress;

    private URI testWalletUri;
    private DefaultOpenPaymentsClient client;

    @BeforeEach
    void setUp() {
        testWalletUri = URI.create("https://wallet.example.com/alice");
        client = new DefaultOpenPaymentsClient(httpClient, walletAddressService, incomingPaymentService,
                outgoingPaymentService, quoteService, grantService, testWalletUri);
    }

    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("should construct with all required parameters")
        void shouldConstructWithAllParameters() {
            assertThat(client).isNotNull();
        }

        @Test
        @DisplayName("should throw when httpClient is null")
        void shouldThrowWhenHttpClientIsNull() {
            assertThatThrownBy(() -> new DefaultOpenPaymentsClient(null, walletAddressService, incomingPaymentService,
                    outgoingPaymentService, quoteService, grantService, testWalletUri))
                    .isInstanceOf(NullPointerException.class).hasMessageContaining("httpClient must not be null");
        }

        @Test
        @DisplayName("should throw when walletAddressService is null")
        void shouldThrowWhenWalletAddressServiceIsNull() {
            assertThatThrownBy(() -> new DefaultOpenPaymentsClient(httpClient, null, incomingPaymentService,
                    outgoingPaymentService, quoteService, grantService, testWalletUri))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("walletAddressService must not be null");
        }

        @Test
        @DisplayName("should throw when incomingPaymentService is null")
        void shouldThrowWhenIncomingPaymentServiceIsNull() {
            assertThatThrownBy(() -> new DefaultOpenPaymentsClient(httpClient, walletAddressService, null,
                    outgoingPaymentService, quoteService, grantService, testWalletUri))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("incomingPaymentService must not be null");
        }

        @Test
        @DisplayName("should throw when outgoingPaymentService is null")
        void shouldThrowWhenOutgoingPaymentServiceIsNull() {
            assertThatThrownBy(() -> new DefaultOpenPaymentsClient(httpClient, walletAddressService,
                    incomingPaymentService, null, quoteService, grantService, testWalletUri))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("outgoingPaymentService must not be null");
        }

        @Test
        @DisplayName("should throw when quoteService is null")
        void shouldThrowWhenQuoteServiceIsNull() {
            assertThatThrownBy(() -> new DefaultOpenPaymentsClient(httpClient, walletAddressService,
                    incomingPaymentService, outgoingPaymentService, null, grantService, testWalletUri))
                    .isInstanceOf(NullPointerException.class).hasMessageContaining("quoteService must not be null");
        }

        @Test
        @DisplayName("should throw when grantService is null")
        void shouldThrowWhenGrantServiceIsNull() {
            assertThatThrownBy(() -> new DefaultOpenPaymentsClient(httpClient, walletAddressService,
                    incomingPaymentService, outgoingPaymentService, quoteService, null, testWalletUri))
                    .isInstanceOf(NullPointerException.class).hasMessageContaining("grantService must not be null");
        }

        @Test
        @DisplayName("should throw when walletAddressUri is null")
        void shouldThrowWhenWalletAddressUriIsNull() {
            assertThatThrownBy(() -> new DefaultOpenPaymentsClient(httpClient, walletAddressService,
                    incomingPaymentService, outgoingPaymentService, quoteService, grantService, null))
                    .isInstanceOf(NullPointerException.class).hasMessageContaining("walletAddressUri must not be null");
        }
    }

    @Nested
    @DisplayName("Service Accessors")
    class ServiceAccessorTests {

        @Test
        @DisplayName("should return wallet address service")
        void shouldReturnWalletAddressService() {
            WalletAddressService service = client.walletAddresses();

            assertThat(service).isNotNull().isSameAs(walletAddressService);
        }

        @Test
        @DisplayName("should return incoming payment service")
        void shouldReturnIncomingPaymentService() {
            IncomingPaymentService service = client.incomingPayments();

            assertThat(service).isNotNull().isSameAs(incomingPaymentService);
        }

        @Test
        @DisplayName("should return outgoing payment service")
        void shouldReturnOutgoingPaymentService() {
            OutgoingPaymentService service = client.outgoingPayments();

            assertThat(service).isNotNull().isSameAs(outgoingPaymentService);
        }

        @Test
        @DisplayName("should return quote service")
        void shouldReturnQuoteService() {
            QuoteService service = client.quotes();

            assertThat(service).isNotNull().isSameAs(quoteService);
        }

        @Test
        @DisplayName("should return grant service")
        void shouldReturnGrantService() {
            GrantService service = client.grants();

            assertThat(service).isNotNull().isSameAs(grantService);
        }

        @Test
        @DisplayName("should return same service instance on multiple calls")
        void shouldReturnSameServiceInstance() {
            WalletAddressService service1 = client.walletAddresses();
            WalletAddressService service2 = client.walletAddresses();

            assertThat(service1).isSameAs(service2);
        }
    }

    @Nested
    @DisplayName("Health Check")
    class HealthCheckTests {

        @Test
        @DisplayName("should return true when wallet address is accessible")
        void shouldReturnTrueWhenHealthy() {
            when(walletAddressService.get(testWalletUri)).thenReturn(CompletableFuture.completedFuture(walletAddress));

            boolean result = client.healthCheck().join();

            assertThat(result).isTrue();
            verify(walletAddressService).get(testWalletUri);
        }

        @Test
        @DisplayName("should return false when wallet address is not accessible")
        void shouldReturnFalseWhenUnhealthy() {
            when(walletAddressService.get(testWalletUri))
                    .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Connection failed")));

            boolean result = client.healthCheck().join();

            assertThat(result).isFalse();
            verify(walletAddressService).get(testWalletUri);
        }

        @Test
        @DisplayName("should return false when wallet address is null")
        void shouldReturnFalseWhenWalletAddressIsNull() {
            when(walletAddressService.get(testWalletUri)).thenReturn(CompletableFuture.completedFuture(null));

            boolean result = client.healthCheck().join();

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should handle multiple health checks")
        void shouldHandleMultipleHealthChecks() {
            when(walletAddressService.get(any(URI.class))).thenReturn(CompletableFuture.completedFuture(walletAddress));

            boolean result1 = client.healthCheck().join();
            boolean result2 = client.healthCheck().join();
            boolean result3 = client.healthCheck().join();

            assertThat(result1).isTrue();
            assertThat(result2).isTrue();
            assertThat(result3).isTrue();
        }
    }

    @Nested
    @DisplayName("Resource Management")
    class ResourceManagementTests {

        @Test
        @DisplayName("should close http client when client is closed")
        void shouldCloseHttpClient() {
            client.close();

            verify(httpClient).close();
        }

        @Test
        @DisplayName("should allow multiple close calls")
        void shouldAllowMultipleCloseCalls() {
            client.close();
            client.close();

            verify(httpClient, times(2)).close();
        }
    }

    @Nested
    @DisplayName("Thread Safety")
    class ThreadSafetyTests {

        @Test
        @DisplayName("should handle concurrent service access")
        void shouldHandleConcurrentServiceAccess() throws InterruptedException {
            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];

            for (int i = 0; i < threadCount; i++) {
                threads[i] = new Thread(() -> {
                    client.walletAddresses();
                    client.incomingPayments();
                    client.outgoingPayments();
                    client.quotes();
                    client.grants();
                });
                threads[i].start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            // If we get here without exceptions, thread safety is maintained
            assertThat(client.walletAddresses()).isNotNull();
        }

        @Test
        @DisplayName("should handle concurrent health checks")
        void shouldHandleConcurrentHealthChecks() throws InterruptedException {
            when(walletAddressService.get(any(URI.class))).thenReturn(CompletableFuture.completedFuture(walletAddress));

            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];
            boolean[] results = new boolean[threadCount];

            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    results[index] = client.healthCheck().join();
                });
                threads[i].start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            for (boolean result : results) {
                assertThat(result).isTrue();
            }
        }
    }
}
