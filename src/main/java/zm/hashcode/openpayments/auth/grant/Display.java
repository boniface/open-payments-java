package zm.hashcode.openpayments.auth.grant;

import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Display information for the client.
 *
 * <p>
 * Contains human-readable information about the client that can be displayed to the user during interaction, such as
 * the client name and optional URI.
 *
 * @see <a href="https://openpayments.dev/grants/request/">Open Payments - Grant Request</a>
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record Display(@JsonProperty("name") String name, @JsonProperty("uri") Optional<String> uri) {

    public Display {
        Objects.requireNonNull(name, "name must not be null");
        uri = Optional.ofNullable(uri).orElse(Optional.empty());
    }

    /**
     * Creates display information with only a name.
     *
     * @param name
     *            the client name
     * @return display information
     */
    public static Display of(String name) {
        return new Display(name, Optional.empty());
    }

    /**
     * Creates display information with name and URI.
     *
     * @param name
     *            the client name
     * @param uri
     *            the client URI
     * @return display information
     */
    public static Display of(String name, String uri) {
        return new Display(name, Optional.of(uri));
    }
}
