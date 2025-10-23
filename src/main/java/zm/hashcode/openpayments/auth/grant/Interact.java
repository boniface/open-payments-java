package zm.hashcode.openpayments.auth.grant;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Interaction modes for grant requests.
 *
 * <p>
 * Specifies how the user should interact with the authorization server to approve the grant. Supports redirect-based
 * interaction where the user is redirected to approve the grant, then redirected back to the client.
 *
 * @see <a href="https://openpayments.dev/grants/request/">Open Payments - Grant Request</a>
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record Interact(@JsonProperty("start") List<String> start, @JsonProperty("finish") Optional<Finish> finish) {

    public Interact {
        Objects.requireNonNull(start, "start must not be null");
        if (start.isEmpty()) {
            throw new IllegalArgumentException("start must not be empty");
        }
        start = List.copyOf(start); // Make immutable
        finish = Optional.ofNullable(finish).orElse(Optional.empty());
    }

    /**
     * Creates redirect interaction.
     *
     * <p>
     * Redirect interaction involves redirecting the user to the authorization server, then redirecting back to the
     * client's callback URI with an interaction reference.
     *
     * @param callbackUri
     *            the client's callback URI
     * @param nonce
     *            a random nonce for security
     * @return redirect interaction parameters
     */
    public static Interact redirect(String callbackUri, String nonce) {
        return new Interact(List.of("redirect"), Optional.of(new Finish("redirect", callbackUri, nonce)));
    }
}
