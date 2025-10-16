/**
 * Outgoing payment API operations for the Open Payments Java SDK.
 *
 * <p>
 * This package provides services for managing outgoing payments in the Open Payments protocol. Outgoing payments
 * represent instructions to send funds from a wallet address to a receiver.
 *
 * <p>
 * The main service interface is {@link zm.hashcode.openpayments.payment.outgoing.OutgoingPaymentService}, which
 * provides methods to:
 * <ul>
 * <li>Create outgoing payment instructions</li>
 * <li>Retrieve outgoing payment details</li>
 * <li>List outgoing payments with pagination</li>
 * </ul>
 *
 * <h2>Example Usage</h2>
 *
 * <pre>{@code
 * // First, create a quote
 * Quote quote = client.quotes().create(builder -> builder.walletAddress("https://wallet.example.com/alice")
 *         .receiver("https://wallet.example.com/bob").sendAmount(Amount.of("100.00", "USD", 2))).join();
 *
 * // Then create an outgoing payment using the quote
 * OutgoingPayment payment = client.outgoingPayments().create(builder -> builder
 *         .walletAddress("https://wallet.example.com/alice").quoteId(quote.getId()).metadata("Payment for services"))
 *         .join();
 *
 * // Retrieve an existing payment
 * OutgoingPayment retrieved = client.outgoingPayments().get(payment.getId()).join();
 *
 * // List all outgoing payments
 * PaginatedResult<OutgoingPayment> payments = client.outgoingPayments().list("https://wallet.example.com/alice")
 *         .join();
 * }</pre>
 *
 * @see zm.hashcode.openpayments.payment.outgoing.OutgoingPaymentService
 * @see zm.hashcode.openpayments.payment.outgoing.OutgoingPayment
 * @see zm.hashcode.openpayments.payment.outgoing.OutgoingPaymentRequest
 */
package zm.hashcode.openpayments.payment.outgoing;
