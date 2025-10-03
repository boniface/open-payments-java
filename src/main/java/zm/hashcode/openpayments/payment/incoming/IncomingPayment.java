package zm.hashcode.openpayments.payment.incoming;

import java.net.URI;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import zm.hashcode.openpayments.model.Amount;

/**
 * Represents an incoming payment resource from the Open Payments API.
 *
 * <p>
 * This model contains the data returned by the Open Payments resource server when you create, retrieve, or list
 * incoming payments via the API. An incoming payment is an API resource that represents a payment request - it allows
 * an account to receive funds through the Open Payments protocol.
 *
 * <p>
 * Fields like {@code receivedAmount} and {@code completed} are managed by the Account Servicing Entity (ASE) and
 * reflect the server-side payment state. The SDK receives this data from the API but does not process payments itself.
 */
public final class IncomingPayment {
    private final URI id;
    private final URI walletAddress;
    private final Amount incomingAmount;
    private final Amount receivedAmount;
    private final boolean completed;
    private final Instant expiresAt;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final String metadata;

    private IncomingPayment(Builder builder) {
        this.id = Objects.requireNonNull(builder.id);
        this.walletAddress = Objects.requireNonNull(builder.walletAddress);
        this.incomingAmount = builder.incomingAmount;
        this.receivedAmount = builder.receivedAmount;
        this.completed = builder.completed;
        this.expiresAt = builder.expiresAt;
        this.createdAt = Objects.requireNonNull(builder.createdAt);
        this.updatedAt = Objects.requireNonNull(builder.updatedAt);
        this.metadata = builder.metadata;
    }

    public URI getId() {
        return id;
    }

    public URI getWalletAddress() {
        return walletAddress;
    }

    public Optional<Amount> getIncomingAmount() {
        return Optional.ofNullable(incomingAmount);
    }

    public Optional<Amount> getReceivedAmount() {
        return Optional.ofNullable(receivedAmount);
    }

    public boolean isCompleted() {
        return completed;
    }

    public Optional<Instant> getExpiresAt() {
        return Optional.ofNullable(expiresAt);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Optional<String> getMetadata() {
        return Optional.ofNullable(metadata);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private URI id;
        private URI walletAddress;
        private Amount incomingAmount;
        private Amount receivedAmount;
        private boolean completed;
        private Instant expiresAt;
        private Instant createdAt;
        private Instant updatedAt;
        private String metadata;

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

        public Builder walletAddress(String walletAddress) {
            this.walletAddress = URI.create(walletAddress);
            return this;
        }

        public Builder incomingAmount(Amount incomingAmount) {
            this.incomingAmount = incomingAmount;
            return this;
        }

        public Builder receivedAmount(Amount receivedAmount) {
            this.receivedAmount = receivedAmount;
            return this;
        }

        public Builder completed(boolean completed) {
            this.completed = completed;
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

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder metadata(String metadata) {
            this.metadata = metadata;
            return this;
        }

        public IncomingPayment build() {
            return new IncomingPayment(this);
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
        IncomingPayment that = (IncomingPayment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "IncomingPayment{" + "id=" + id + ", walletAddress=" + walletAddress + ", completed=" + completed + '}';
    }
}
