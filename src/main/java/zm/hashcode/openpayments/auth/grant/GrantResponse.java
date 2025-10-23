package zm.hashcode.openpayments.auth.grant;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * GNAP grant response from authorization server.
 *
 * <p>
 * Represents the response from an authorization server after a grant request. The response can indicate that the grant
 * requires user interaction, is pending continuation, or has been approved with an access token.
 *
 * <p>
 * Response states:
 * <ul>
 * <li><b>Requires Interaction</b>: User must interact with authorization server</li>
 * <li><b>Pending</b>: Grant is pending, client should continue the request</li>
 * <li><b>Approved</b>: Grant approved, access token issued</li>
 * </ul>
 *
 * @see <a href="https://openpayments.dev/grants/response/">Open Payments - Grant Response</a>
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record GrantResponse(@JsonProperty("continue") Optional<Continue> continueInfo,
        @JsonProperty("access_token") Optional<AccessTokenResponse> accessToken,
        @JsonProperty("interact") Optional<InteractResponse> interact) {

    public GrantResponse {
        continueInfo = Optional.ofNullable(continueInfo).orElse(Optional.empty());
        accessToken = Optional.ofNullable(accessToken).orElse(Optional.empty());
        interact = Optional.ofNullable(interact).orElse(Optional.empty());
    }

    /**
     * Checks if the grant requires user interaction.
     *
     * @return true if user interaction is required
     */
    public boolean requiresInteraction() {
        return interact.isPresent();
    }

    /**
     * Checks if the grant is pending continuation.
     *
     * <p>
     * A grant is pending if it has continue information but no access token yet.
     *
     * @return true if the grant is pending
     */
    public boolean isPending() {
        return continueInfo.isPresent() && accessToken.isEmpty();
    }

    /**
     * Checks if the grant has been approved with an access token.
     *
     * @return true if an access token was issued
     */
    public boolean isApproved() {
        return accessToken.isPresent();
    }
}
