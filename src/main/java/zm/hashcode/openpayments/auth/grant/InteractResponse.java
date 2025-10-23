package zm.hashcode.openpayments.auth.grant;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Interaction response details.
 *
 * <p>
 * Contains the URLs for user interaction with the authorization server, including the redirect URL for starting
 * interaction and the finish token for completing it.
 *
 * @see <a href="https://openpayments.dev/grants/response/">Open Payments - Grant Response</a>
 */
public record InteractResponse(@JsonProperty("redirect") String redirect, @JsonProperty("finish") String finish) {

    public InteractResponse {
        Objects.requireNonNull(redirect, "redirect must not be null");
        Objects.requireNonNull(finish, "finish must not be null");
    }
}
