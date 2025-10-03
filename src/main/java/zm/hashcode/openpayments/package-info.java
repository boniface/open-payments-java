/**
 * Open Payments Java SDK - A client library for the Open Payments API.
 *
 * <p>
 * This SDK provides Java classes and interfaces for interacting with Open Payments-compliant servers (Account Servicing
 * Entities). It is a <strong>HTTP client library</strong> that handles authentication, HTTP signatures, GNAP
 * authorization, and JSON serialization, allowing Java applications to communicate with Open Payments APIs.
 *
 * <p>
 * <strong>Important:</strong> This SDK is a client library, not a payment processor. It sends HTTP requests to Open
 * Payments servers operated by banks, wallets, or payment providers (ASEs), which handle the actual payment processing
 * and fund movement.
 *
 * <h2>Main Components</h2>
 * <ul>
 * <li>{@link zm.hashcode.openpayments.client.OpenPaymentsClient} - Main API client entry point</li>
 * <li>{@link zm.hashcode.openpayments.wallet.WalletAddressService} - Wallet address discovery</li>
 * <li>{@link zm.hashcode.openpayments.payment.incoming.IncomingPaymentService} - Incoming payment operations</li>
 * <li>{@link zm.hashcode.openpayments.payment.outgoing.OutgoingPaymentService} - Outgoing payment operations</li>
 * <li>{@link zm.hashcode.openpayments.payment.quote.QuoteService} - Quote operations</li>
 * <li>{@link zm.hashcode.openpayments.auth.GrantService} - GNAP authorization</li>
 * </ul>
 *
 * <h2>Example Usage</h2>
 *
 * <pre>{@code
 * // Create API client
 * var client = OpenPaymentsClient.builder().walletAddress("https://wallet.example.com/alice").privateKey(privateKey)
 *         .keyId(keyId).build();
 *
 * // Make API call to retrieve wallet address
 * var wallet = client.walletAddresses().get("https://wallet.example.com/alice").join();
 *
 * // Make API call to create incoming payment
 * var payment = client.incomingPayments()
 *         .create(req -> req.walletAddress(wallet.id()).incomingAmount(Amount.of("1000", "USD", 2))).join();
 * }</pre>
 *
 * @see <a href="https://openpayments.dev">Open Payments Specification</a>
 */
package zm.hashcode.openpayments;
