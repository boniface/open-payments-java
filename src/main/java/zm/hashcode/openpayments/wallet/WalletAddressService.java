package zm.hashcode.openpayments.wallet;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

/**
 * Service for wallet address operations in the Open Payments API.
 *
 * <p>
 * Wallet addresses are publicly accessible URLs that identify accounts and provide information about authorization and
 * resource servers.
 */
public interface WalletAddressService {

    /**
     * Retrieves a wallet address by its URL.
     *
     * <p>
     * This operation does not require authentication as wallet addresses are public resources.
     *
     * @param url
     *            the wallet address URL
     * @return a CompletableFuture containing the wallet address
     */
    CompletableFuture<WalletAddress> get(String url);

    /**
     * Retrieves a wallet address by its URI.
     *
     * <p>
     * This operation does not require authentication as wallet addresses are public resources.
     *
     * @param uri
     *            the wallet address URI
     * @return a CompletableFuture containing the wallet address
     */
    CompletableFuture<WalletAddress> get(URI uri);

    /**
     * Retrieves the public keys associated with a wallet address.
     *
     * @param walletAddressUrl
     *            the wallet address URL
     * @return a CompletableFuture containing the public keys
     */
    CompletableFuture<PublicKeySet> getKeys(String walletAddressUrl);
}
