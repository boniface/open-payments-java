package zm.hashcode.openpayments.payment.quote;

import java.net.URI;
import java.util.Objects;

import zm.hashcode.openpayments.model.Amount;

/**
 * Request object for creating a quote.
 */
public final class QuoteRequest {
    private final URI walletAddress;
    private final URI receiver;
    private final Amount sendAmount;
    private final Amount receiveAmount;

    private QuoteRequest(Builder builder) {
        this.walletAddress = Objects.requireNonNull(builder.walletAddress);
        this.receiver = Objects.requireNonNull(builder.receiver);
        this.sendAmount = builder.sendAmount;
        this.receiveAmount = builder.receiveAmount;

        if (sendAmount == null && receiveAmount == null) {
            throw new IllegalArgumentException("Either sendAmount or receiveAmount must be set");
        }
        if (sendAmount != null && receiveAmount != null) {
            throw new IllegalArgumentException("Only one of sendAmount or receiveAmount can be set");
        }
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

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private URI walletAddress;
        private URI receiver;
        private Amount sendAmount;
        private Amount receiveAmount;

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

        public Builder receiver(URI receiver) {
            this.receiver = receiver;
            return this;
        }

        public Builder receiver(String receiver) {
            this.receiver = URI.create(receiver);
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

        public QuoteRequest build() {
            return new QuoteRequest(this);
        }
    }
}
