package zm.hashcode.openpayments.auth.grant;

import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * GNAP grant request per Open Payments specification.
 *
 * <p>
 * Represents a request to an authorization server for access to resources. A grant request initiates the authorization
 * flow and specifies what access is being requested, client information, and optionally how user interaction should be
 * handled.
 *
 * <p>
 * Example usage:
 *
 * <pre>{@code
 * GrantRequest request = GrantRequest.builder()
 *         .accessToken(
 *                 AccessTokenRequest.builder().addAccess(Access.incomingPayment(List.of("create", "read"))).build())
 *         .client(Client.builder().key("https://myapp.example.com/.well-known/jwks.json")
 *                 .display(new Display("My App", Optional.empty())).build())
 *         .interact(Interact.redirect("https://myapp.example.com/callback", "callback-nonce-123")).build();
 * }</pre>
 *
 * @see <a href="https://openpayments.dev/grants/request/">Open Payments - Grant Request</a>
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record GrantRequest(@JsonProperty("access_token") AccessTokenRequest accessToken,
        @JsonProperty("client") Client client, @JsonProperty("interact") Optional<Interact> interact) {

    public GrantRequest {
        Objects.requireNonNull(accessToken, "accessToken must not be null");
        Objects.requireNonNull(client, "client must not be null");
        interact = Optional.ofNullable(interact).orElse(Optional.empty());
    }

    /**
     * Creates a builder for constructing grant requests.
     *
     * @return new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link GrantRequest}.
     */
    public static final class Builder {
        private AccessTokenRequest accessToken;
        private Client client;
        private Optional<Interact> interact = Optional.empty();

        private Builder() {
        }

        /**
         * Sets the access token request.
         *
         * @param accessToken
         *            the access token request
         * @return this builder
         */
        public Builder accessToken(AccessTokenRequest accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        /**
         * Sets the client information.
         *
         * @param client
         *            the client information
         * @return this builder
         */
        public Builder client(Client client) {
            this.client = client;
            return this;
        }

        /**
         * Sets the interaction parameters.
         *
         * @param interact
         *            the interaction parameters
         * @return this builder
         */
        public Builder interact(Interact interact) {
            this.interact = Optional.ofNullable(interact);
            return this;
        }

        /**
         * Builds the grant request.
         *
         * @return the grant request
         * @throws NullPointerException
         *             if required fields are null
         */
        public GrantRequest build() {
            return new GrantRequest(accessToken, client, interact);
        }
    }
}
