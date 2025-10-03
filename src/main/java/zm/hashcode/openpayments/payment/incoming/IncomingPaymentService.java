package zm.hashcode.openpayments.payment.incoming;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import zm.hashcode.openpayments.model.PaginatedResult;

/**
 * API client service for incoming payment operations in the Open Payments API.
 *
 * <p>
 * This service makes authenticated HTTP requests to Open Payments resource servers to create, retrieve, list, and
 * complete incoming payment resources. Incoming payments are API resources that represent payment requests - they allow
 * accounts to receive funds through the Open Payments protocol.
 *
 * <p>
 * <strong>Important:</strong> This service does not receive or process payments directly. It communicates with the Open
 * Payments API server (operated by an Account Servicing Entity) which manages the actual payment processing and fund
 * receipt.
 *
 * <p>
 * All methods return {@link java.util.concurrent.CompletableFuture} to support asynchronous HTTP operations.
 */
public interface IncomingPaymentService {

    /**
     * Creates a new incoming payment by sending a POST request to the Open Payments API.
     *
     * <p>
     * This method sends an authenticated HTTP POST request to the resource server to create an incoming payment
     * resource. The actual payment processing is handled by the Account Servicing Entity.
     *
     * @param requestBuilder
     *            a consumer to build the request
     * @return a CompletableFuture containing the created incoming payment resource from the API
     */
    CompletableFuture<IncomingPayment> create(Consumer<IncomingPaymentRequest.Builder> requestBuilder);

    /**
     * Retrieves an incoming payment by sending a GET request to the Open Payments API.
     *
     * @param url
     *            the incoming payment URL (API resource identifier)
     * @return a CompletableFuture containing the incoming payment resource from the API
     */
    CompletableFuture<IncomingPayment> get(String url);

    /**
     * Retrieves an incoming payment by its URI.
     *
     * @param uri
     *            the incoming payment URI
     * @return a CompletableFuture containing the incoming payment
     */
    CompletableFuture<IncomingPayment> get(URI uri);

    /**
     * Lists incoming payments for a wallet address.
     *
     * @param walletAddress
     *            the wallet address URL
     * @return a CompletableFuture containing a paginated list of incoming payments
     */
    CompletableFuture<PaginatedResult<IncomingPayment>> list(String walletAddress);

    /**
     * Lists incoming payments for a wallet address with pagination.
     *
     * @param walletAddress
     *            the wallet address URL
     * @param cursor
     *            the pagination cursor
     * @param limit
     *            the maximum number of results
     * @return a CompletableFuture containing a paginated list of incoming payments
     */
    CompletableFuture<PaginatedResult<IncomingPayment>> list(String walletAddress, String cursor, int limit);

    /**
     * Completes an incoming payment, preventing further funds from being received.
     *
     * @param paymentUrl
     *            the incoming payment URL
     * @return a CompletableFuture containing the completed incoming payment
     */
    CompletableFuture<IncomingPayment> complete(String paymentUrl);
}
