package zm.hashcode.openpayments.auth;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents an access right within a grant, defining what actions can be performed on which resources.
 *
 * @param type
 *            the type of access (e.g., "incoming-payment", "quote")
 * @param actions
 *            the allowed actions (e.g., "create", "read")
 * @param identifier
 *            the resource identifier (optional)
 * @param limits
 *            the access limits (optional)
 */
public record AccessRight(String type, List<String> actions, String identifier, Limits limits) {

    public AccessRight {
        Objects.requireNonNull(type, "type must not be null");
        actions = actions != null ? List.copyOf(actions) : List.of();
    }

    /**
     * Returns the type of access.
     *
     * @return the access type
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the allowed actions.
     *
     * @return the actions list
     */
    public List<String> getActions() {
        return actions;
    }

    /**
     * Returns the resource identifier, if available.
     *
     * @return an Optional containing the identifier
     */
    public Optional<String> getIdentifier() {
        return Optional.ofNullable(identifier);
    }

    /**
     * Returns the access limits, if available.
     *
     * @return an Optional containing the limits
     */
    public Optional<Limits> getLimits() {
        return Optional.ofNullable(limits);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccessRight that = (AccessRight) o;
        return Objects.equals(type, that.type) && Objects.equals(actions, that.actions)
                && Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, actions, identifier);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String type;
        private List<String> actions;
        private String identifier;
        private Limits limits;

        private Builder() {
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder actions(List<String> actions) {
            this.actions = actions;
            return this;
        }

        public Builder identifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        public Builder limits(Limits limits) {
            this.limits = limits;
            return this;
        }

        public AccessRight build() {
            return new AccessRight(type, actions, identifier, limits);
        }
    }

    /**
     * Represents limits on an access right (e.g., spending limits).
     *
     * @param receiveAmount
     *            the maximum receive amount (optional)
     * @param sendAmount
     *            the maximum send amount (optional)
     * @param receiver
     *            the receiver wallet address (optional)
     */
    public record Limits(String receiveAmount, String sendAmount, String receiver) {

        /**
         * Creates a new Limits instance.
         *
         * @param receiveAmount
         *            the maximum receive amount (optional)
         * @param sendAmount
         *            the maximum send amount (optional)
         * @param receiver
         *            the receiver wallet address (optional)
         * @return a new Limits instance
         */
        public static Limits of(String receiveAmount, String sendAmount, String receiver) {
            return new Limits(receiveAmount, sendAmount, receiver);
        }

        /**
         * Returns the maximum receive amount, if available.
         *
         * @return an Optional containing the receive amount
         */
        public Optional<String> getReceiveAmount() {
            return Optional.ofNullable(receiveAmount);
        }

        /**
         * Returns the maximum send amount, if available.
         *
         * @return an Optional containing the send amount
         */
        public Optional<String> getSendAmount() {
            return Optional.ofNullable(sendAmount);
        }

        /**
         * Returns the receiver wallet address, if available.
         *
         * @return an Optional containing the receiver
         */
        public Optional<String> getReceiver() {
            return Optional.ofNullable(receiver);
        }
    }
}
