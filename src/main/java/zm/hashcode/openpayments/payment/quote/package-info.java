/**
 * Quote API operations for the Open Payments Java SDK.
 *
 * <p>
 * This package provides services for managing payment quotes in the Open Payments protocol. Quotes provide information
 * about exchange rates and fees for payments before they are executed.
 *
 * <p>
 * The main service interface is {@link zm.hashcode.openpayments.payment.quote.QuoteService}, which provides methods to:
 * <ul>
 * <li>Create quotes for payments</li>
 * <li>Retrieve quote details</li>
 * </ul>
 *
 * <p>
 * Quotes are required before creating outgoing payments and help establish the exact amounts that will be sent and
 * received, accounting for exchange rates and fees.
 *
 * <h2>Example Usage</h2>
 *
 * <pre>{@code
 * // Create a quote specifying the amount to send
 * Quote quote = client.quotes().create(builder -> builder.walletAddress("https://wallet.example.com/alice")
 *         .receiver("https://wallet.example.com/bob").sendAmount(Amount.of("100.00", "USD", 2))).join();
 *
 * // Or create a quote specifying the amount to receive
 * Quote quote = client.quotes().create(builder -> builder.walletAddress("https://wallet.example.com/alice")
 *         .receiver("https://wallet.example.com/bob").receiveAmount(Amount.of("95.00", "EUR", 2))).join();
 *
 * // Check if quote is still valid
 * if (!quote.isExpired()) {
 *     // Use the quote to create an outgoing payment
 *     OutgoingPayment payment = client.outgoingPayments()
 *             .create(builder -> builder.walletAddress("https://wallet.example.com/alice").quoteId(quote.getId()))
 *             .join();
 * }
 *
 * // Retrieve an existing quote
 * Quote retrieved = client.quotes().get(quote.getId()).join();
 * }</pre>
 *
 * @see zm.hashcode.openpayments.payment.quote.QuoteService
 * @see zm.hashcode.openpayments.payment.quote.Quote
 * @see zm.hashcode.openpayments.payment.quote.QuoteRequest
 */
package zm.hashcode.openpayments.payment.quote;
