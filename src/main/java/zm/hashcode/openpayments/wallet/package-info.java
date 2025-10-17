/**
 * Wallet address API client services for the Open Payments Java SDK.
 *
 * <p>
 * This package contains services for interacting with Open Payments wallet addresses. Wallet addresses are the
 * fundamental identifiers in the Open Payments ecosystem, representing accounts that can send and receive payments.
 *
 * <p>
 * The main service interface is {@link zm.hashcode.openpayments.wallet.WalletAddressService}, which provides methods
 * to:
 * <ul>
 * <li>Retrieve wallet address metadata and capabilities</li>
 * <li>Fetch public keys for signature verification</li>
 * <li>Discover authorization server endpoints</li>
 * </ul>
 *
 * <h2>Example Usage</h2>
 *
 * <pre>{@code
 * OpenPaymentsClient client = OpenPaymentsClient.builder().walletAddress("https://wallet.example.com/alice")
 *         .privateKey(privateKey).keyId("key-123").build();
 *
 * // Get wallet address information
 * WalletAddress walletAddress = client.walletAddresses().get("https://wallet.example.com/alice").join();
 *
 * // Get public keys for verification
 * PublicKeySet keys = client.walletAddresses().getKeys("https://wallet.example.com/alice").join();
 * }</pre>
 *
 * @see zm.hashcode.openpayments.wallet.WalletAddressService
 * @see zm.hashcode.openpayments.wallet.WalletAddress
 * @see zm.hashcode.openpayments.wallet.PublicKeySet
 */
package zm.hashcode.openpayments.wallet;
