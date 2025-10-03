package zm.hashcode.openpayments.client;

import java.net.URI;
import java.security.PrivateKey;
import java.time.Duration;

/**
 * Builder for constructing {@link OpenPaymentsClient} instances.
 *
 * <p>
 * This builder provides a fluent API for configuring the Open Payments client with required authentication credentials
 * and optional customization.
 *
 * <p>
 * Example:
 *
 * <pre>{@code
 * var client = OpenPaymentsClient.builder().walletAddress("https://wallet.example.com/alice").privateKey(privateKey)
 *         .keyId(keyId).requestTimeout(Duration.ofSeconds(30)).build();
 * }</pre>
 */
public interface OpenPaymentsClientBuilder {

    /**
     * Sets the wallet address URL for this client.
     *
     * @param walletAddress
     *            the wallet address URL
     * @return this builder
     */
    OpenPaymentsClientBuilder walletAddress(String walletAddress);

    /**
     * Sets the wallet address URI for this client.
     *
     * @param walletAddress
     *            the wallet address URI
     * @return this builder
     */
    OpenPaymentsClientBuilder walletAddress(URI walletAddress);

    /**
     * Sets the private key for signing HTTP requests.
     *
     * @param privateKey
     *            the private key
     * @return this builder
     */
    OpenPaymentsClientBuilder privateKey(PrivateKey privateKey);

    /**
     * Sets the key ID associated with the private key.
     *
     * @param keyId
     *            the key ID
     * @return this builder
     */
    OpenPaymentsClientBuilder keyId(String keyId);

    /**
     * Sets the request timeout duration.
     *
     * @param timeout
     *            the request timeout
     * @return this builder
     */
    OpenPaymentsClientBuilder requestTimeout(Duration timeout);

    /**
     * Sets the connection timeout duration.
     *
     * @param timeout
     *            the connection timeout
     * @return this builder
     */
    OpenPaymentsClientBuilder connectionTimeout(Duration timeout);

    /**
     * Enables or disables automatic token refresh.
     *
     * @param autoRefresh
     *            true to enable automatic token refresh
     * @return this builder
     */
    OpenPaymentsClientBuilder autoRefreshTokens(boolean autoRefresh);

    /**
     * Sets a custom user agent string.
     *
     * @param userAgent
     *            the user agent string
     * @return this builder
     */
    OpenPaymentsClientBuilder userAgent(String userAgent);

    /**
     * Builds and returns a new {@link OpenPaymentsClient} instance.
     *
     * @return a new client instance
     * @throws IllegalStateException
     *             if required configuration is missing
     */
    OpenPaymentsClient build();
}
