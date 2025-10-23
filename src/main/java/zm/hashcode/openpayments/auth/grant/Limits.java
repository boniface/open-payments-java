package zm.hashcode.openpayments.auth.grant;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Payment limits for access requests.
 *
 * <p>
 * Specifies constraints on payment amounts and frequency for outgoing payment access. These limits ensure that the
 * client can only make payments within the specified bounds.
 *
 * @see <a href="https://openpayments.dev/grants/request/">Open Payments - Grant Request</a>
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record Limits(@JsonProperty("debitAmount") Optional<Amount> debitAmount,
        @JsonProperty("receiveAmount") Optional<Amount> receiveAmount,
        @JsonProperty("interval") Optional<String> interval) {

    public Limits {
        debitAmount = Optional.ofNullable(debitAmount).orElse(Optional.empty());
        receiveAmount = Optional.ofNullable(receiveAmount).orElse(Optional.empty());
        interval = Optional.ofNullable(interval).orElse(Optional.empty());
    }

    /**
     * Creates a builder for constructing payment limits.
     *
     * @return new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link Limits}.
     */
    public static final class Builder {
        private Optional<Amount> debitAmount = Optional.empty();
        private Optional<Amount> receiveAmount = Optional.empty();
        private Optional<String> interval = Optional.empty();

        private Builder() {
        }

        /**
         * Sets the maximum debit amount.
         *
         * @param amount
         *            the maximum debit amount
         * @return this builder
         */
        public Builder debitAmount(Amount amount) {
            this.debitAmount = Optional.ofNullable(amount);
            return this;
        }

        /**
         * Sets the maximum receive amount.
         *
         * @param amount
         *            the maximum receive amount
         * @return this builder
         */
        public Builder receiveAmount(Amount amount) {
            this.receiveAmount = Optional.ofNullable(amount);
            return this;
        }

        /**
         * Sets the time interval for the limits.
         *
         * @param interval
         *            the interval string (ISO 8601 repeating interval)
         * @return this builder
         */
        public Builder interval(String interval) {
            this.interval = Optional.ofNullable(interval);
            return this;
        }

        /**
         * Builds the payment limits.
         *
         * @return the payment limits
         */
        public Limits build() {
            return new Limits(debitAmount, receiveAmount, interval);
        }
    }
}
