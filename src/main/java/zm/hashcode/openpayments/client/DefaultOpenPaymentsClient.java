package zm.hashcode.openpayments.client;

import java.net.URI;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import zm.hashcode.openpayments.auth.GrantService;
import zm.hashcode.openpayments.http.core.HttpClient;
import zm.hashcode.openpayments.payment.incoming.IncomingPaymentService;
import zm.hashcode.openpayments.payment.outgoing.OutgoingPaymentService;
import zm.hashcode.openpayments.payment.quote.QuoteService;
import zm.hashcode.openpayments.wallet.WalletAddressService;

/**
 * Default implementation of {@link OpenPaymentsClient}.
 *
 * <p>
 * This class provides the main entry point for interacting with Open Payments APIs. It orchestrates access to various
 * service implementations and manages the underlying HTTP client lifecycle.
 *
 * <p>
 * Instances of this class are thread-safe and should be reused across multiple requests. When no longer needed, clients
 * should be closed using {@link #close()} to release HTTP connection resources.
 */
final class DefaultOpenPaymentsClient implements OpenPaymentsClient {

    private final HttpClient httpClient;
    private final WalletAddressService walletAddressService;
    private final IncomingPaymentService incomingPaymentService;
    private final OutgoingPaymentService outgoingPaymentService;
    private final QuoteService quoteService;
    private final GrantService grantService;
    private final URI walletAddressUri;

    /**
     * Constructs a new DefaultOpenPaymentsClient with the specified services.
     *
     * @param httpClient
     *            the HTTP client for API communication
     * @param walletAddressService
     *            the wallet address service
     * @param incomingPaymentService
     *            the incoming payment service
     * @param outgoingPaymentService
     *            the outgoing payment service
     * @param quoteService
     *            the quote service
     * @param grantService
     *            the grant service
     * @param walletAddressUri
     *            the wallet address URI for this client
     * @throws NullPointerException
     *             if any parameter is null
     */
    DefaultOpenPaymentsClient(HttpClient httpClient, WalletAddressService walletAddressService,
            IncomingPaymentService incomingPaymentService, OutgoingPaymentService outgoingPaymentService,
            QuoteService quoteService, GrantService grantService, URI walletAddressUri) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient must not be null");
        this.walletAddressService = Objects.requireNonNull(walletAddressService,
                "walletAddressService must not be null");
        this.incomingPaymentService = Objects.requireNonNull(incomingPaymentService,
                "incomingPaymentService must not be null");
        this.outgoingPaymentService = Objects.requireNonNull(outgoingPaymentService,
                "outgoingPaymentService must not be null");
        this.quoteService = Objects.requireNonNull(quoteService, "quoteService must not be null");
        this.grantService = Objects.requireNonNull(grantService, "grantService must not be null");
        this.walletAddressUri = Objects.requireNonNull(walletAddressUri, "walletAddressUri must not be null");
    }

    @Override
    public WalletAddressService walletAddresses() {
        return walletAddressService;
    }

    @Override
    public IncomingPaymentService incomingPayments() {
        return incomingPaymentService;
    }

    @Override
    public OutgoingPaymentService outgoingPayments() {
        return outgoingPaymentService;
    }

    @Override
    public QuoteService quotes() {
        return quoteService;
    }

    @Override
    public GrantService grants() {
        return grantService;
    }

    @Override
    public CompletableFuture<Boolean> healthCheck() {
        // Verify connectivity by retrieving the configured wallet address
        // This is a public endpoint that doesn't require authentication
        return walletAddressService.get(walletAddressUri).thenApply(walletAddress -> {
            // If we successfully retrieved the wallet address, the client is healthy
            return walletAddress != null;
        }).exceptionally(throwable -> {
            // Any exception means the health check failed
            return false;
        });
    }

    @Override
    public void close() {
        httpClient.close();
    }
}
