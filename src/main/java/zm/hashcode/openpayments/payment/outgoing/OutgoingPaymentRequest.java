package zm.hashcode.openpayments.payment.outgoing;

import java.net.URI;
import java.util.Objects;

/**
 * Request object for creating an outgoing payment.
 */
public final class OutgoingPaymentRequest {
    private final URI walletAddress;
    private final URI quoteId;
    private final String metadata;

    private OutgoingPaymentRequest(Builder builder) {
        this.walletAddress = Objects.requireNonNull(builder.walletAddress);
        this.quoteId = Objects.requireNonNull(builder.quoteId);
        this.metadata = builder.metadata;
    }

    public URI getWalletAddress() {
        return walletAddress;
    }

    public URI getQuoteId() {
        return quoteId;
    }

    public String getMetadata() {
        return metadata;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private URI walletAddress;
        private URI quoteId;
        private String metadata;

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

        public Builder quoteId(URI quoteId) {
            this.quoteId = quoteId;
            return this;
        }

        public Builder quoteId(String quoteId) {
            this.quoteId = URI.create(quoteId);
            return this;
        }

        public Builder metadata(String metadata) {
            this.metadata = metadata;
            return this;
        }

        public OutgoingPaymentRequest build() {
            return new OutgoingPaymentRequest(this);
        }
    }
}
