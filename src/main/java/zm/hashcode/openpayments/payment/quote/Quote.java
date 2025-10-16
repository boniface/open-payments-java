package zm.hashcode.openpayments.payment.quote;

import java.net.URI;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import zm.hashcode.openpayments.model.Amount;

/**
 * Represents a quote for a payment in the Open Payments system.
 *
 * <p>
 * A quote provides information about exchange rates and fees for a payment before it is executed.
 *
 * @param id
 *            the unique identifier for this quote
 * @param walletAddress
 *            the wallet address requesting the quote
 * @param receiver
 *            the receiving wallet address
 * @param sendAmount
 *            the amount to send (optional)
 * @param receiveAmount
 *            the amount to receive (optional)
 * @param expiresAt
 *            when the quote expires
 * @param createdAt
 *            when the quote was created
 */
public record Quote(URI id, URI walletAddress, URI receiver, Amount sendAmount, Amount receiveAmount, Instant expiresAt,
        Instant createdAt) {

    public Quote {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(walletAddress, "walletAddress must not be null");
        Objects.requireNonNull(receiver, "receiver must not be null");
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    /**
     * Returns the ID of this quote.
     *
     * @return the quote ID
     */
    public URI getId() {
        return id;
    }

    /**
     * Returns the wallet address requesting the quote.
     *
     * @return the wallet address
     */
    public URI getWalletAddress() {
        return walletAddress;
    }

    /**
     * Returns the receiving wallet address.
     *
     * @return the receiver
     */
    public URI getReceiver() {
        return receiver;
    }

    /**
     * Returns the amount to send, if specified.
     *
     * @return an Optional containing the send amount
     */
    public Optional<Amount> getSendAmount() {
        return Optional.ofNullable(sendAmount);
    }

    /**
     * Returns the amount to receive, if specified.
     *
     * @return an Optional containing the receive amount
     */
    public Optional<Amount> getReceiveAmount() {
        return Optional.ofNullable(receiveAmount);
    }

    /**
     * Returns when this quote expires.
     *
     * @return the expiration timestamp
     */
    public Instant getExpiresAt() {
        return expiresAt;
    }

    /**
     * Returns when this quote was created.
     *
     * @return the creation timestamp
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns whether this quote has expired.
     *
     * @return true if expired, false otherwise
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
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
            return new Quote(id, walletAddress, receiver, sendAmount, receiveAmount, expiresAt, createdAt);
        }
    }
}
