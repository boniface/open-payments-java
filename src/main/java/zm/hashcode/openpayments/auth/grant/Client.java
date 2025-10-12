package zm.hashcode.openpayments.auth.grant;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Client information for grant requests.
 *
 * <p>
 * Contains information about the client making the grant request, including the client's public key reference and
 * display information for user interaction.
 *
 * @see <a href="https://openpayments.dev/grants/request/">Open Payments - Grant Request</a>
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record Client(@JsonProperty("key") Optional<String> key, @JsonProperty("display") Optional<Display> display) {

    public Client {
        key = Optional.ofNullable(key).orElse(Optional.empty());
        display = Optional.ofNullable(display).orElse(Optional.empty());
    }

    /**
     * Creates a builder for constructing client information.
     *
     * @return new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link Client}.
     */
    public static final class Builder {
        private Optional<String> key = Optional.empty();
        private Optional<Display> display = Optional.empty();

        private Builder() {
        }

        /**
         * Sets the JWK reference URL.
         *
         * @param key
         *            the JWK reference URL
         * @return this builder
         */
        public Builder key(String key) {
            this.key = Optional.ofNullable(key);
            return this;
        }

        /**
         * Sets the display information.
         *
         * @param display
         *            the display information
         * @return this builder
         */
        public Builder display(Display display) {
            this.display = Optional.ofNullable(display);
            return this;
        }

        /**
         * Builds the client information.
         *
         * @return the client information
         */
        public Client build() {
            return new Client(key, display);
        }
    }
}
