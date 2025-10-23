package zm.hashcode.openpayments.auth.grant;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Access token response details.
 *
 * <p>
 * Contains the access token value, management URL, optional expiration, and the granted access permissions.
 *
 * @see <a href="https://openpayments.dev/grants/response/">Open Payments - Grant Response</a>
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record AccessTokenResponse(@JsonProperty("value") String value, @JsonProperty("manage") String manage,
        @JsonProperty("expires_in") Optional<Long> expiresIn, @JsonProperty("access") List<Access> access) {

    public AccessTokenResponse {
        Objects.requireNonNull(value, "value must not be null");
        Objects.requireNonNull(manage, "manage must not be null");
        Objects.requireNonNull(access, "access must not be null");
        expiresIn = Optional.ofNullable(expiresIn).orElse(Optional.empty());
        access = List.copyOf(access); // Make immutable
    }
}
