package zm.hashcode.openpayments.auth.grant;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Finish parameters for interaction.
 *
 * <p>
 * Specifies how the interaction should finish, including the callback method, URI, and security nonce.
 *
 * @see <a href="https://openpayments.dev/grants/request/">Open Payments - Grant Request</a>
 */
public record Finish(@JsonProperty("method") String method, @JsonProperty("uri") String uri,
        @JsonProperty("nonce") String nonce) {

    public Finish {
        Objects.requireNonNull(method, "method must not be null");
        Objects.requireNonNull(uri, "uri must not be null");
        Objects.requireNonNull(nonce, "nonce must not be null");
    }
}
