package zm.hashcode.openpayments.auth.grant;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Access token request details for grant requests.
 *
 * <p>
 * Specifies what access is being requested from the authorization server. Each request contains one or more access
 * items describing the type of resource access needed.
 *
 * @see <a href="https://openpayments.dev/grants/request/">Open Payments - Grant Request</a>
 */
public record AccessTokenRequest(@JsonProperty("access") List<Access> access) {

    public AccessTokenRequest {
        Objects.requireNonNull(access, "access must not be null");
        if (access.isEmpty()) {
            throw new IllegalArgumentException("access must not be empty");
        }
        access = List.copyOf(access); // Make immutable
    }

    /**
     * Creates a builder for constructing access token requests.
     *
     * @return new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link AccessTokenRequest}.
     */
    public static final class Builder {
        private final List<Access> access = new ArrayList<>();

        private Builder() {
        }

        /**
         * Adds an access item.
         *
         * @param accessItem
         *            the access item
         * @return this builder
         */
        public Builder addAccess(Access accessItem) {
            Objects.requireNonNull(accessItem, "accessItem must not be null");
            this.access.add(accessItem);
            return this;
        }

        /**
         * Adds multiple access items.
         *
         * @param accessItems
         *            the access items
         * @return this builder
         */
        public Builder access(List<Access> accessItems) {
            Objects.requireNonNull(accessItems, "accessItems must not be null");
            this.access.addAll(accessItems);
            return this;
        }

        /**
         * Builds the access token request.
         *
         * @return the access token request
         * @throws IllegalArgumentException
         *             if no access items added
         */
        public AccessTokenRequest build() {
            return new AccessTokenRequest(access);
        }
    }
}
