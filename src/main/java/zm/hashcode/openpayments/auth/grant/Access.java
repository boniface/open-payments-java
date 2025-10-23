package zm.hashcode.openpayments.auth.grant;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Individual access request for a specific resource type.
 *
 * <p>
 * Describes the type of access being requested, what actions are permitted, and optionally limits on that access. Open
 * Payments supports several resource types including incoming payments, outgoing payments, and quotes.
 *
 * <p>
 * Example usage:
 *
 * <pre>{@code
 * // Request read/create access to incoming payments
 * Access incomingPayment = Access.incomingPayment(List.of("create", "read"));
 *
 * // Request limited access to outgoing payments
 * Access outgoingPayment = Access.outgoingPayment("https://wallet.example/payments/123", List.of("create", "read"),
 *         Limits.builder().debitAmount(new Amount("100.00", "USD", 2)).build());
 * }</pre>
 *
 * @see <a href="https://openpayments.dev/grants/request/">Open Payments - Grant Request</a>
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record Access(@JsonProperty("type") String type, @JsonProperty("actions") Optional<List<String>> actions,
        @JsonProperty("identifier") Optional<String> identifier, @JsonProperty("limits") Optional<Limits> limits) {

    public Access {
        Objects.requireNonNull(type, "type must not be null");
        actions = Optional.ofNullable(actions).orElse(Optional.empty());
        identifier = Optional.ofNullable(identifier).orElse(Optional.empty());
        limits = Optional.ofNullable(limits).orElse(Optional.empty());
    }

    /**
     * Creates an access request for incoming payments.
     *
     * <p>
     * Incoming payments allow a client to receive payments on behalf of a wallet.
     *
     * @param actions
     *            list of permitted actions (e.g., "create", "read", "complete")
     * @return access request for incoming payments
     */
    public static Access incomingPayment(List<String> actions) {
        return new Access("incoming-payment", Optional.of(actions), Optional.empty(), Optional.empty());
    }

    /**
     * Creates an access request for outgoing payments.
     *
     * <p>
     * Outgoing payments allow a client to send payments from a wallet, typically with limits.
     *
     * @param identifier
     *            the wallet payment pointer or resource URL
     * @param actions
     *            list of permitted actions (e.g., "create", "read")
     * @param limits
     *            payment limits
     * @return access request for outgoing payments
     */
    public static Access outgoingPayment(String identifier, List<String> actions, Limits limits) {
        return new Access("outgoing-payment", Optional.of(actions), Optional.of(identifier), Optional.of(limits));
    }

    /**
     * Creates an access request for quotes.
     *
     * <p>
     * Quotes allow a client to request payment quotes from a wallet.
     *
     * @param actions
     *            list of permitted actions (e.g., "create", "read")
     * @return access request for quotes
     */
    public static Access quote(List<String> actions) {
        return new Access("quote", Optional.of(actions), Optional.empty(), Optional.empty());
    }

    /**
     * Creates a custom access request.
     *
     * @param type
     *            the resource type
     * @param actions
     *            list of permitted actions
     * @return access request
     */
    public static Access custom(String type, List<String> actions) {
        return new Access(type, Optional.of(actions), Optional.empty(), Optional.empty());
    }
}
