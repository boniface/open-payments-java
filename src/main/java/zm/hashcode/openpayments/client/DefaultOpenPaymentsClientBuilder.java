package zm.hashcode.openpayments.client;

import java.net.URI;
import java.security.PrivateKey;
import java.time.Duration;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import zm.hashcode.openpayments.auth.GrantService;
import zm.hashcode.openpayments.http.core.HttpClient;
import zm.hashcode.openpayments.http.interceptor.ErrorHandlingInterceptor;
import zm.hashcode.openpayments.payment.incoming.IncomingPaymentService;
import zm.hashcode.openpayments.payment.outgoing.OutgoingPaymentService;
import zm.hashcode.openpayments.payment.quote.QuoteService;
import zm.hashcode.openpayments.wallet.WalletAddressService;

/**
 * Default implementation of {@link OpenPaymentsClientBuilder}.
 *
 * <p>
 * This builder constructs fully configured {@link OpenPaymentsClient} instances with all required services and
 * dependencies properly initialized.
 */
final class DefaultOpenPaymentsClientBuilder implements OpenPaymentsClientBuilder {

    private static final String UNUSED_FIELD_SUPPRESSION = "PMD.UnusedPrivateField";

    // Required fields
    private URI walletAddressUri;
    private PrivateKey privateKey;
    private String keyId;

    // Optional fields with defaults (reserved for future enhancements - v0.2.0)
    // These fields are currently unused but reserved for:
    // - Custom HttpClient configuration (requestTimeout, connectionTimeout, userAgent)
    // - Automatic token refresh scheduling (autoRefreshTokens)
    @SuppressWarnings(UNUSED_FIELD_SUPPRESSION)
    private Duration requestTimeout = Duration.ofSeconds(30);
    @SuppressWarnings(UNUSED_FIELD_SUPPRESSION)
    private Duration connectionTimeout = Duration.ofSeconds(10);
    @SuppressWarnings(UNUSED_FIELD_SUPPRESSION)
    private boolean autoRefreshTokens = true;
    @SuppressWarnings(UNUSED_FIELD_SUPPRESSION)
    private String userAgent = "Open-Payments-Java/0.1.0";

    // Optional service overrides (for testing or custom implementations)
    private HttpClient httpClient;
    private ObjectMapper objectMapper;
    private WalletAddressService walletAddressService;
    private IncomingPaymentService incomingPaymentService;
    private OutgoingPaymentService outgoingPaymentService;
    private QuoteService quoteService;
    private GrantService grantService;

    // Package-private constructor - used by OpenPaymentsClient.builder() static factory method
    @SuppressWarnings("PMD.UnnecessaryConstructor") // Explicit for documentation and access control
    DefaultOpenPaymentsClientBuilder() {
        // Intentionally package-private to prevent external instantiation
    }

    @Override
    public DefaultOpenPaymentsClientBuilder walletAddress(String walletAddress) {
        Objects.requireNonNull(walletAddress, "walletAddress must not be null");
        this.walletAddressUri = URI.create(walletAddress);
        return this;
    }

    @Override
    public DefaultOpenPaymentsClientBuilder walletAddress(URI walletAddress) {
        this.walletAddressUri = Objects.requireNonNull(walletAddress, "walletAddress must not be null");
        return this;
    }

    @Override
    public DefaultOpenPaymentsClientBuilder privateKey(PrivateKey privateKey) {
        this.privateKey = Objects.requireNonNull(privateKey, "privateKey must not be null");
        return this;
    }

    @Override
    public DefaultOpenPaymentsClientBuilder keyId(String keyId) {
        this.keyId = Objects.requireNonNull(keyId, "keyId must not be null");
        return this;
    }

    @Override
    public DefaultOpenPaymentsClientBuilder requestTimeout(Duration timeout) {
        this.requestTimeout = Objects.requireNonNull(timeout, "timeout must not be null");
        return this;
    }

    @Override
    public DefaultOpenPaymentsClientBuilder connectionTimeout(Duration timeout) {
        this.connectionTimeout = Objects.requireNonNull(timeout, "timeout must not be null");
        return this;
    }

    @Override
    public DefaultOpenPaymentsClientBuilder autoRefreshTokens(boolean autoRefresh) {
        this.autoRefreshTokens = autoRefresh;
        return this;
    }

    @Override
    public DefaultOpenPaymentsClientBuilder userAgent(String userAgent) {
        this.userAgent = Objects.requireNonNull(userAgent, "userAgent must not be null");
        return this;
    }

    /**
     * Sets a custom HTTP client (for testing or advanced use cases).
     *
     * @param httpClient
     *            the HTTP client
     * @return this builder
     */
    public DefaultOpenPaymentsClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /**
     * Sets a custom ObjectMapper (for testing or advanced use cases).
     *
     * @param objectMapper
     *            the object mapper
     * @return this builder
     */
    public DefaultOpenPaymentsClientBuilder objectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        return this;
    }

    /**
     * Sets a custom WalletAddressService (for testing or advanced use cases).
     *
     * @param service
     *            the service
     * @return this builder
     */
    public DefaultOpenPaymentsClientBuilder walletAddressService(WalletAddressService service) {
        this.walletAddressService = service;
        return this;
    }

    /**
     * Sets a custom IncomingPaymentService (for testing or advanced use cases).
     *
     * @param service
     *            the service
     * @return this builder
     */
    public DefaultOpenPaymentsClientBuilder incomingPaymentService(IncomingPaymentService service) {
        this.incomingPaymentService = service;
        return this;
    }

    /**
     * Sets a custom OutgoingPaymentService (for testing or advanced use cases).
     *
     * @param service
     *            the service
     * @return this builder
     */
    public DefaultOpenPaymentsClientBuilder outgoingPaymentService(OutgoingPaymentService service) {
        this.outgoingPaymentService = service;
        return this;
    }

    /**
     * Sets a custom QuoteService (for testing or advanced use cases).
     *
     * @param service
     *            the service
     * @return this builder
     */
    public DefaultOpenPaymentsClientBuilder quoteService(QuoteService service) {
        this.quoteService = service;
        return this;
    }

    /**
     * Sets a custom GrantService (for testing or advanced use cases).
     *
     * @param service
     *            the service
     * @return this builder
     */
    public DefaultOpenPaymentsClientBuilder grantService(GrantService service) {
        this.grantService = service;
        return this;
    }

    @Override
    public OpenPaymentsClient build() {
        validateRequiredFields();
        ObjectMapper mapper = getOrCreateObjectMapper();
        HttpClient client = getOrCreateHttpClient(mapper);

        WalletAddressService walletService = getOrCreateWalletAddressService(client, mapper);
        IncomingPaymentService incomingService = getOrCreateIncomingPaymentService(client, mapper);
        OutgoingPaymentService outgoingService = getOrCreateOutgoingPaymentService(client, mapper);
        QuoteService quoteServiceImpl = getOrCreateQuoteService(client, mapper);
        GrantService grantServiceImpl = getOrCreateGrantService();

        return new DefaultOpenPaymentsClient(client, walletService, incomingService, outgoingService, quoteServiceImpl,
                grantServiceImpl, walletAddressUri);
    }

    private void validateRequiredFields() {
        if (walletAddressUri == null) {
            throw new IllegalStateException("walletAddress is required");
        }
        if (privateKey == null) {
            throw new IllegalStateException("privateKey is required");
        }
        if (keyId == null) {
            throw new IllegalStateException("keyId is required");
        }
    }

    private ObjectMapper getOrCreateObjectMapper() {
        if (this.objectMapper != null) {
            return this.objectMapper;
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return mapper;
    }

    private HttpClient getOrCreateHttpClient(ObjectMapper mapper) {
        if (this.httpClient == null) {
            throw new UnsupportedOperationException(
                    "Default HttpClient not yet implemented. Provide custom HttpClient via builder.");
        }
        // Add interceptors to HTTP client
        httpClient.addResponseInterceptor(new ErrorHandlingInterceptor(mapper));
        // Note: Authentication interceptor will be added per-request basis when token is available
        // HTTP Signature authentication happens at the service level
        return httpClient;
    }

    private WalletAddressService getOrCreateWalletAddressService(HttpClient client, ObjectMapper mapper) {
        if (this.walletAddressService == null) {
            return new zm.hashcode.openpayments.wallet.DefaultWalletAddressService(client, mapper);
        }
        return walletAddressService;
    }

    private IncomingPaymentService getOrCreateIncomingPaymentService(HttpClient client, ObjectMapper mapper) {
        if (this.incomingPaymentService == null) {
            return new zm.hashcode.openpayments.payment.incoming.DefaultIncomingPaymentService(client, mapper);
        }
        return incomingPaymentService;
    }

    private OutgoingPaymentService getOrCreateOutgoingPaymentService(HttpClient client, ObjectMapper mapper) {
        if (this.outgoingPaymentService == null) {
            return new zm.hashcode.openpayments.payment.outgoing.DefaultOutgoingPaymentService(client, mapper);
        }
        return outgoingPaymentService;
    }

    private QuoteService getOrCreateQuoteService(HttpClient client, ObjectMapper mapper) {
        if (this.quoteService == null) {
            return new zm.hashcode.openpayments.payment.quote.DefaultQuoteService(client, mapper);
        }
        return quoteService;
    }

    private GrantService getOrCreateGrantService() {
        if (this.grantService == null) {
            throw new UnsupportedOperationException(
                    "Default GrantService adapter not yet implemented. Will be added in Phase 7.6");
        }
        return grantService;
    }
}
