package zm.hashcode.openpayments.client;

import java.util.concurrent.CompletableFuture;

import zm.hashcode.openpayments.auth.GrantService;
import zm.hashcode.openpayments.payment.incoming.IncomingPaymentService;
import zm.hashcode.openpayments.payment.outgoing.OutgoingPaymentService;
import zm.hashcode.openpayments.payment.quote.QuoteService;
import zm.hashcode.openpayments.wallet.WalletAddressService;

/**
 * Main entry point for the Open Payments Java SDK - a RESTful API client.
 *
 * <p>
 * This client is a <strong>HTTP client library</strong> that communicates with Open Payments-compliant servers (Account
 * Servicing Entities). It handles authentication, request signing, and JSON serialization, allowing your Java
 * application to interact with Open Payments APIs without manual HTTP operations.
 *
 * <p>
 * <strong>Important:</strong> This SDK does not process payments itself. It sends authenticated HTTP requests to Open
 * Payments servers operated by banks, wallets, or payment providers (ASEs), which handle the actual payment processing
 * and fund movement.
 *
 * <p>
 * The client provides access to all Open Payments API operations:
 * <ul>
 * <li><strong>Wallet Addresses:</strong> Discover account metadata and public keys</li>
 * <li><strong>Incoming Payments:</strong> Create and manage payment requests</li>
 * <li><strong>Outgoing Payments:</strong> Initiate payments from authorized accounts</li>
 * <li><strong>Quotes:</strong> Get exchange rates and fee information</li>
 * <li><strong>Grants:</strong> Request and manage GNAP authorization tokens</li>
 * </ul>
 *
 * <p>
 * Example usage:
 *
 * <pre>{@code
 * // Create client (configured with your credentials)
 * var client = OpenPaymentsClient.builder().walletAddress("https://wallet.example.com/alice").privateKey(privateKey)
 *         .keyId(keyId).build();
 *
 * // Make API call to retrieve wallet address metadata
 * var walletAddress = client.walletAddresses().get("https://wallet.example.com/alice").join();
 *
 * System.out.println("Asset: " + walletAddress.assetCode());
 * }</pre>
 *
 * <p>
 * All client instances are thread-safe and should be reused across multiple requests. Clients implement
 * {@link AutoCloseable} and should be closed when no longer needed to release HTTP connection resources.
 *
 * @see <a href="https://openpayments.dev">Open Payments Specification</a>
 */
public interface OpenPaymentsClient extends AutoCloseable {

    /**
     * Returns a service for wallet address operations.
     *
     * @return the wallet address service
     */
    WalletAddressService walletAddresses();

    /**
     * Returns a service for incoming payment operations.
     *
     * @return the incoming payment service
     */
    IncomingPaymentService incomingPayments();

    /**
     * Returns a service for outgoing payment operations.
     *
     * @return the outgoing payment service
     */
    OutgoingPaymentService outgoingPayments();

    /**
     * Returns a service for quote operations.
     *
     * @return the quote service
     */
    QuoteService quotes();

    /**
     * Returns a service for grant operations.
     *
     * @return the grant service
     */
    GrantService grants();

    /**
     * Performs a health check on the client configuration and connectivity to Open Payments servers.
     *
     * <p>
     * This method verifies that the client can successfully communicate with the configured Open Payments API
     * endpoints. It does not perform any payment operations.
     *
     * @return a CompletableFuture that completes with true if the client is properly configured and can connect to the
     *         Open Payments servers
     */
    CompletableFuture<Boolean> healthCheck();

    /**
     * Creates a new builder for constructing an {@link OpenPaymentsClient}.
     *
     * @return a new builder instance
     */
    static OpenPaymentsClientBuilder builder() {
        return new DefaultOpenPaymentsClientBuilder();
    }

    /**
     * Closes this client and releases any underlying resources.
     *
     * <p>
     * After calling this method, the client should not be used for further operations.
     */
    @Override
    void close();
}
