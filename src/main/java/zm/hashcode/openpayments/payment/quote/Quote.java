package zm.hashcode.openpayments.payment.quote;

import java.net.URI;
import java.time.Instant;
import java.util.Objects;

import zm.hashcode.openpayments.model.Amount;

/**
 * Represents a quote for a payment in the Open Payments system.
 *
 * <p>
 * A quote provides information about exchange rates and fees for a payment before it is executed.
 */
public final class Quote {
    private final URI id;
    private final URI walletAddress;
    private final URI receiver;
    private final Amount sendAmount;
    private final Amount receiveAmount;
    private final Instant expiresAt;
    private final Instant createdAt;

    private Quote(Builder builder) {
        this.id = Objects.requireNonNull(builder.id);
        this.walletAddress = Objects.requireNonNull(builder.walletAddress);
        this.receiver = Objects.requireNonNull(builder.receiver);
        this.sendAmount = builder.sendAmount;
        this.receiveAmount = builder.receiveAmount;
        this.expiresAt = Objects.requireNonNull(builder.expiresAt);
        this.createdAt = Objects.requireNonNull(builder.createdAt);
    }

    public URI getId() {
        return id;
    }

    public URI getWalletAddress() {
        return walletAddress;
    }

    public URI getReceiver() {
        return receiver;
    }

    public Amount getSendAmount() {
        return sendAmount;
    }

    public Amount getReceiveAmount() {
        return receiveAmount;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private URI id;
        private URI walletAddress;
        private URI receiver;
        private Amount sendAmount;
        private Amount receiveAmount;
        private Instant expiresAt;
        private Instant createdAt;

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

        public Builder walletAddress(URI walletAddress) {
            this.walletAddress = walletAddress;
            return this;
        }

        public Builder receiver(URI receiver) {
            this.receiver = receiver;
            return this;
        }

        public Builder sendAmount(Amount sendAmount) {
            this.sendAmount = sendAmount;
            return this;
        }

        public Builder receiveAmount(Amount receiveAmount) {
            this.receiveAmount = receiveAmount;
            return this;
        }

        public Builder expiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Quote build() {
            return new Quote(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Quote quote = (Quote) o;
        return Objects.equals(id, quote.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Quote{" + "id=" + id + ", receiver=" + receiver + ", expiresAt=" + expiresAt + '}';
    }
}
