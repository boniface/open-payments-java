package zm.hashcode.openpayments.auth.grant;

import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Continue information for pending grants.
 *
 * <p>
 * Contains information needed to continue a pending grant request, including the continue token, URI, and optional wait
 * time before the next request.
 *
 * @see <a href="https://openpayments.dev/grants/response/">Open Payments - Grant Response</a>
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record Continue(@JsonProperty("access_token") ContinueToken accessToken, @JsonProperty("uri") String uri,
        @JsonProperty("wait") Optional<Integer> waitSeconds) {

    public Continue {
        Objects.requireNonNull(accessToken, "accessToken must not be null");
        Objects.requireNonNull(uri, "uri must not be null");
        waitSeconds = Optional.ofNullable(waitSeconds).orElse(Optional.empty());
    }

    /**
     * Gets the continue token value.
     *
     * @return the continue token value
     */
    public String token() {
        return accessToken.value();
    }
}
