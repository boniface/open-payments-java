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
 *
 * @param id
 *            the unique identifier for this incoming payment
 * @param walletAddress
 *            the wallet address receiving the payment
 * @param incomingAmount
 *            the requested incoming amount (optional)
 * @param receivedAmount
 *            the actual received amount (optional)
 * @param completed
 *            whether the payment is completed
 * @param expiresAt
 *            when the payment request expires (optional)
 * @param createdAt
 *            when the payment was created
 * @param updatedAt
 *            when the payment was last updated
 * @param metadata
 *            optional metadata for the payment
 */
public record IncomingPayment(URI id, URI walletAddress, Amount incomingAmount, Amount receivedAmount,
        boolean completed, Instant expiresAt, Instant createdAt, Instant updatedAt, String metadata) {

    public IncomingPayment {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(walletAddress, "walletAddress must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

    /**
     * Returns the ID of this incoming payment.
     *
     * @return the payment ID
     */
    public URI getId() {
        return id;
    }

    /**
     * Returns the wallet address receiving this payment.
     *
     * @return the wallet address
     */
    public URI getWalletAddress() {
        return walletAddress;
    }

    /**
     * Returns the requested incoming amount, if specified.
     *
     * @return an Optional containing the incoming amount
     */
    public Optional<Amount> getIncomingAmount() {
        return Optional.ofNullable(incomingAmount);
    }

    /**
     * Returns the actual received amount, if any.
     *
     * @return an Optional containing the received amount
     */
    public Optional<Amount> getReceivedAmount() {
        return Optional.ofNullable(receivedAmount);
    }

    /**
     * Returns whether this payment is completed.
     *
     * @return true if completed, false otherwise
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Returns the expiration time of this payment request, if set.
     *
     * @return an Optional containing the expiration time
     */
    public Optional<Instant> getExpiresAt() {
        return Optional.ofNullable(expiresAt);
    }

    /**
     * Returns when this payment was created.
     *
     * @return the creation timestamp
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns when this payment was last updated.
     *
     * @return the last update timestamp
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Returns the metadata for this payment, if any.
     *
     * @return an Optional containing the metadata
     */
    public Optional<String> getMetadata() {
        return Optional.ofNullable(metadata);
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
            return new IncomingPayment(id, walletAddress, incomingAmount, receivedAmount, completed, expiresAt,
                    createdAt, updatedAt, metadata);
        }
    }
}
