package zm.hashcode.openpayments.payment.outgoing;

import java.net.URI;
import java.time.Instant;
import java.util.Objects;

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
 */
public final class OutgoingPayment {
    private final URI id;
    private final URI walletAddress;
    private final URI receiver;
    private final Amount sendAmount;
    private final Amount sentAmount;
    private final URI quoteId;
    private final boolean failed;
    private final Instant createdAt;
    private final Instant updatedAt;

    private OutgoingPayment(Builder builder) {
        this.id = Objects.requireNonNull(builder.id);
        this.walletAddress = Objects.requireNonNull(builder.walletAddress);
        this.receiver = Objects.requireNonNull(builder.receiver);
        this.sendAmount = builder.sendAmount;
        this.sentAmount = builder.sentAmount;
        this.quoteId = builder.quoteId;
        this.failed = builder.failed;
        this.createdAt = Objects.requireNonNull(builder.createdAt);
        this.updatedAt = Objects.requireNonNull(builder.updatedAt);
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

    public Amount getSentAmount() {
        return sentAmount;
    }

    public URI getQuoteId() {
        return quoteId;
    }

    public boolean isFailed() {
        return failed;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
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
            return new OutgoingPayment(this);
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
        OutgoingPayment that = (OutgoingPayment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
