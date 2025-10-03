package zm.hashcode.openpayments.wallet;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a wallet address in the Open Payments system.
 *
 * <p>
 * A wallet address is a URL that identifies an account and provides information about the account's authorization and
 * resource servers.
 *
 * @param id
 *            the unique identifier (URL) for this wallet address
 * @param assetCode
 *            the asset code (typically ISO 4217 currency code)
 * @param assetScale
 *            the asset scale (number of decimal places)
 * @param authServer
 *            the URL of the authorization server
 * @param resourceServer
 *            the URL of the resource server
 * @param publicName
 *            the public name of the account holder (optional)
 */
public record WalletAddress(URI id, String assetCode, int assetScale, URI authServer, URI resourceServer,
        String publicName) {

    public WalletAddress {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(assetCode, "assetCode must not be null");
        Objects.requireNonNull(authServer, "authServer must not be null");
        Objects.requireNonNull(resourceServer, "resourceServer must not be null");
    }

    /**
     * Returns the public name of the account holder, if available.
     *
     * @return an Optional containing the public name
     */
    public Optional<String> getPublicName() {
        return Optional.ofNullable(publicName);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private URI id;
        private String assetCode;
        private int assetScale;
        private URI authServer;
        private URI resourceServer;
        private String publicName;

        private Builder() {
        }

        public Builder id(URI id) {
            this.id = id;
            return this;
        }

        public Builder id(String id) {
            this.id = URI.create(id);
            return this;
        }

        public Builder assetCode(String assetCode) {
            this.assetCode = assetCode;
            return this;
        }

        public Builder assetScale(int assetScale) {
            this.assetScale = assetScale;
            return this;
        }

        public Builder authServer(URI authServer) {
            this.authServer = authServer;
            return this;
        }

        public Builder authServer(String authServer) {
            this.authServer = URI.create(authServer);
            return this;
        }

        public Builder resourceServer(URI resourceServer) {
            this.resourceServer = resourceServer;
            return this;
        }

        public Builder resourceServer(String resourceServer) {
            this.resourceServer = URI.create(resourceServer);
            return this;
        }

        public Builder publicName(String publicName) {
            this.publicName = publicName;
            return this;
        }

        public WalletAddress build() {
            return new WalletAddress(id, assetCode, assetScale, authServer, resourceServer, publicName);
        }
    }
}
