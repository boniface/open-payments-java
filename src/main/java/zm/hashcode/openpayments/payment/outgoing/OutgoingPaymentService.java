package zm.hashcode.openpayments.payment.outgoing;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import zm.hashcode.openpayments.model.PaginatedResult;

/**
 * API client service for outgoing payment operations in the Open Payments API.
 *
 * <p>
 * This service makes authenticated HTTP requests to Open Payments resource servers to create, retrieve, and list
 * outgoing payment resources. Outgoing payments are API resources that represent payment instructions to send funds
 * from an authorized account.
 *
 * <p>
 * <strong>Important:</strong> This service does not execute payments or transfer funds directly. It sends authenticated
 * HTTP requests to the Open Payments API server (operated by an Account Servicing Entity), which handles the actual
 * payment execution and fund movement.
 *
 * <p>
 * All methods return {@link java.util.concurrent.CompletableFuture} to support asynchronous HTTP operations.
 */
public interface OutgoingPaymentService {

    /**
     * Creates a new outgoing payment by sending a POST request to the Open Payments API.
     *
     * <p>
     * This method sends an authenticated HTTP POST request to the resource server to create an outgoing payment
     * resource. The actual payment execution is handled by the Account Servicing Entity.
     *
     * @param requestBuilder
     *            a consumer to build the request
     * @return a CompletableFuture containing the created outgoing payment resource from the API
     */
    CompletableFuture<OutgoingPayment> create(Consumer<OutgoingPaymentRequest.Builder> requestBuilder);

    /**
     * Retrieves an outgoing payment by its URL.
     *
     * @param url
     *            the outgoing payment URL
     * @return a CompletableFuture containing the outgoing payment
     */
    CompletableFuture<OutgoingPayment> get(String url);

    /**
     * Retrieves an outgoing payment by its URI.
     *
     * @param uri
     *            the outgoing payment URI
     * @return a CompletableFuture containing the outgoing payment
     */
    CompletableFuture<OutgoingPayment> get(URI uri);

    /**
     * Lists outgoing payments for a wallet address.
     *
     * @param walletAddress
     *            the wallet address URL
     * @return a CompletableFuture containing a paginated list of outgoing payments
     */
    CompletableFuture<PaginatedResult<OutgoingPayment>> list(String walletAddress);

    /**
     * Lists outgoing payments for a wallet address with pagination.
     *
     * @param walletAddress
     *            the wallet address URL
     * @param cursor
     *            the pagination cursor
     * @param limit
     *            the maximum number of results
     * @return a CompletableFuture containing a paginated list of outgoing payments
     */
    CompletableFuture<PaginatedResult<OutgoingPayment>> list(String walletAddress, String cursor, int limit);
}
