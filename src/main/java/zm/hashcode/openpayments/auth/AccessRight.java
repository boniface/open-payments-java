package zm.hashcode.openpayments.auth;

import java.util.List;
import java.util.Objects;

/**
 * Represents an access right within a grant, defining what actions can be performed on which resources.
 */
public final class AccessRight {
    private final String type;
    private final List<String> actions;
    private final String identifier;
    private final Limits limits;

    private AccessRight(Builder builder) {
        this.type = Objects.requireNonNull(builder.type);
        this.actions = builder.actions != null ? List.copyOf(builder.actions) : List.of();
        this.identifier = builder.identifier;
        this.limits = builder.limits;
    }

    public String getType() {
        return type;
    }

    public List<String> getActions() {
        return actions;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Limits getLimits() {
        return limits;
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
            return new AccessRight(this);
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
        AccessRight that = (AccessRight) o;
        return Objects.equals(type, that.type) && Objects.equals(actions, that.actions)
                && Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, actions, identifier);
    }

    /**
     * Represents limits on an access right (e.g., spending limits).
     */
    public static final class Limits {
        private final String receiveAmount;
        private final String sendAmount;
        private final String receiver;

        private Limits(String receiveAmount, String sendAmount, String receiver) {
            this.receiveAmount = receiveAmount;
            this.sendAmount = sendAmount;
            this.receiver = receiver;
        }

        public static Limits of(String receiveAmount, String sendAmount, String receiver) {
            return new Limits(receiveAmount, sendAmount, receiver);
        }

        public String getReceiveAmount() {
            return receiveAmount;
        }

        public String getSendAmount() {
            return sendAmount;
        }

        public String getReceiver() {
            return receiver;
        }
    }
}
