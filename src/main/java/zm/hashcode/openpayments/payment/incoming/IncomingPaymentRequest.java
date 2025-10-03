package zm.hashcode.openpayments.payment.incoming;

import java.net.URI;
import java.time.Instant;
import java.util.Objects;

import zm.hashcode.openpayments.model.Amount;

/**
 * Request object for creating an incoming payment.
 */
public final class IncomingPaymentRequest {
    private final URI walletAddress;
    private final Amount incomingAmount;
    private final Instant expiresAt;
    private final String metadata;
    private final String externalRef;

    private IncomingPaymentRequest(Builder builder) {
        this.walletAddress = Objects.requireNonNull(builder.walletAddress);
        this.incomingAmount = builder.incomingAmount;
        this.expiresAt = builder.expiresAt;
        this.metadata = builder.metadata;
        this.externalRef = builder.externalRef;
    }

    public URI getWalletAddress() {
        return walletAddress;
    }

    public Amount getIncomingAmount() {
        return incomingAmount;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public String getMetadata() {
        return metadata;
    }

    public String getExternalRef() {
        return externalRef;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private URI walletAddress;
        private Amount incomingAmount;
        private Instant expiresAt;
        private String metadata;
        private String externalRef;

        private Builder() {
        }

        public Builder walletAddress(URI walletAddress) {
            this.walletAddress = walletAddress;
            return this;
        }

        public Builder walletAddress(String walletAddress) {
            this.walletAddress = URI.create(walletAddress);
            return this;
        }

        public Builder incomingAmount(Amount incomingAmount) {
            this.incomingAmount = incomingAmount;
            return this;
        }

        public Builder expiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder metadata(String metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder externalRef(String externalRef) {
            this.externalRef = externalRef;
            return this;
        }

        public IncomingPaymentRequest build() {
            return new IncomingPaymentRequest(this);
        }
    }
}
