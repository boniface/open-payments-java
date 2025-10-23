package zm.hashcode.openpayments.payment.outgoing;

import java.net.URI;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import zm.hashcode.openpayments.model.Amount;

/**
 * Represents an outgoing payment resource from the Open Payments API.
 *
 * <p>
 * This model contains the data returned by the Open Payments resource server when you create, retrieve, or list
 * outgoing payments via the API. An outgoing payment is an API resource that represents a payment instruction to send
 * funds from an authorized account.
 *
 * <p>
 * Fields like {@code sentAmount} and {@code failed} are managed by the Account Servicing Entity (ASE) and reflect the
 * server-side payment execution state. The SDK receives this data from the API; the actual payment execution is handled
 * by the ASE.
 *
 * @param id
 *            the unique identifier for this outgoing payment
 * @param walletAddress
 *            the wallet address sending the payment
 * @param receiver
 *            the receiving wallet address
 * @param sendAmount
 *            the amount to send (optional)
 * @param sentAmount
 *            the actual sent amount (optional)
 * @param quoteId
 *            the quote ID used for this payment (optional)
 * @param failed
 *            whether the payment has failed
 * @param createdAt
 *            when the payment was created
 * @param updatedAt
 *            when the payment was last updated
 */
public record OutgoingPayment(URI id, URI walletAddress, URI receiver, Amount sendAmount, Amount sentAmount,
        URI quoteId, boolean failed, Instant createdAt, Instant updatedAt) {

    public OutgoingPayment {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(walletAddress, "walletAddress must not be null");
        Objects.requireNonNull(receiver, "receiver must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

    /**
     * Returns the ID of this outgoing payment.
     *
     * @return the payment ID
     */
    public URI getId() {
        return id;
    }

    /**
     * Returns the wallet address sending this payment.
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
     * Returns the actual sent amount, if any.
     *
     * @return an Optional containing the sent amount
     */
    public Optional<Amount> getSentAmount() {
        return Optional.ofNullable(sentAmount);
    }

    /**
     * Returns the quote ID, if any.
     *
     * @return an Optional containing the quote ID
     */
    public Optional<URI> getQuoteId() {
        return Optional.ofNullable(quoteId);
    }

    /**
     * Returns whether this payment has failed.
     *
     * @return true if failed, false otherwise
     */
    public boolean isFailed() {
        return failed;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OutgoingPayment that = (OutgoingPayment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "OutgoingPayment{" + "id=" + id + ", receiver=" + receiver + ", failed=" + failed + '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private URI id;
        private URI walletAddress;
        private URI receiver;
        private Amount sendAmount;
        private Amount sentAmount;
        private URI quoteId;
        private boolean failed;
        private Instant createdAt;
        private Instant updatedAt;

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

        public Builder sentAmount(Amount sentAmount) {
            this.sentAmount = sentAmount;
            return this;
        }

        public Builder quoteId(URI quoteId) {
            this.quoteId = quoteId;
            return this;
        }

        public Builder failed(boolean failed) {
            this.failed = failed;
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

        public OutgoingPayment build() {
            return new OutgoingPayment(id, walletAddress, receiver, sendAmount, sentAmount, quoteId, failed, createdAt,
                    updatedAt);
        }
    }
}
