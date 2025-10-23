/**
 * Incoming payment API operations for the Open Payments Java SDK.
 *
 * <p>
 * This package provides services for managing incoming payments in the Open Payments protocol. Incoming payments
 * represent requests to receive funds into a wallet address.
 *
 * <p>
 * The main service interface is {@link zm.hashcode.openpayments.payment.incoming.IncomingPaymentService}, which
 * provides methods to:
 * <ul>
 * <li>Create incoming payment requests</li>
 * <li>Retrieve incoming payment details</li>
 * <li>List incoming payments with pagination</li>
 * <li>Complete incoming payments</li>
 * </ul>
 *
 * <h2>Example Usage</h2>
 *
 * <pre>{@code
 * // Create an incoming payment
 * IncomingPayment payment = client.incomingPayments()
 *         .create(builder -> builder.walletAddress("https://wallet.example.com/alice")
 *                 .incomingAmount(Amount.of("100.00", "USD", 2)).expiresAt(Instant.now().plus(Duration.ofHours(24)))
 *                 .metadata("Invoice #12345"))
 *         .join();
 *
 * // Retrieve an existing payment
 * IncomingPayment retrieved = client.incomingPayments().get(payment.getId()).join();
 *
 * // List all incoming payments
 * PaginatedResult<IncomingPayment> payments = client.incomingPayments().list("https://wallet.example.com/alice")
 *         .join();
 *
 * // Complete a payment
 * IncomingPayment completed = client.incomingPayments().complete(payment.getId().toString()).join();
 * }</pre>
 *
 * @see zm.hashcode.openpayments.payment.incoming.IncomingPaymentService
 * @see zm.hashcode.openpayments.payment.incoming.IncomingPayment
 * @see zm.hashcode.openpayments.payment.incoming.IncomingPaymentRequest
 */
package zm.hashcode.openpayments.payment.incoming;
