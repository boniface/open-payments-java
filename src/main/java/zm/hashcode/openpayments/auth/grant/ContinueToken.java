package zm.hashcode.openpayments.auth.grant;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Continue token details.
 *
 * <p>
 * A token used to continue a pending grant request. This token is used as authorization for continuation requests.
 *
 * @see <a href="https://openpayments.dev/grants/response/">Open Payments - Grant Response</a>
 */
public record ContinueToken(@JsonProperty("value") String value) {

    public ContinueToken {
        Objects.requireNonNull(value, "value must not be null");
    }
}
