package zm.hashcode.openpayments.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents an access token for authenticating Open Payments API requests.
 *
 * @param value
 *            the token value
 * @param manageUrl
 *            the URL for managing this token (optional)
 * @param expiresAt
 *            when the token expires (optional)
 * @param access
 *            the access rights granted by this token
 */
public record AccessToken(String value, String manageUrl, Instant expiresAt, List<AccessRight> access) {

    public AccessToken {
        Objects.requireNonNull(value, "value must not be null");
        access = access != null ? List.copyOf(access) : List.of();
    }

    /**
     * Returns the token value.
     *
     * @return the token value
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the URL for managing this token, if available.
     *
     * @return an Optional containing the manage URL
     */
    public Optional<String> getManageUrl() {
        return Optional.ofNullable(manageUrl);
    }

    /**
     * Returns when this token expires, if available.
     *
     * @return an Optional containing the expiration timestamp
     */
    public Optional<Instant> getExpiresAt() {
        return Optional.ofNullable(expiresAt);
    }

    /**
     * Returns the access rights granted by this token.
     *
     * @return the access rights list
     */
    public List<AccessRight> getAccess() {
        return access;
    }

    /**
     * Returns whether this token has expired.
     *
     * @return true if expired, false otherwise
     */
    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    /**
     * Returns whether this token is expiring soon.
     *
     * @param threshold
     *            the duration threshold
     * @return true if expiring within the threshold, false otherwise
     */
    public boolean isExpiringSoon(Duration threshold) {
        return expiresAt != null && Instant.now().plus(threshold).isAfter(expiresAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccessToken that = (AccessToken) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "AccessToken{" + "expiresAt=" + expiresAt + ", isExpired=" + isExpired() + '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String value;
        private String manageUrl;
        private Instant expiresAt;
        private List<AccessRight> access;

        private Builder() {
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public Builder manageUrl(String manageUrl) {
            this.manageUrl = manageUrl;
            return this;
        }

        public Builder expiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder access(List<AccessRight> access) {
            this.access = access;
            return this;
        }

        public AccessToken build() {
            return new AccessToken(value, manageUrl, expiresAt, access);
        }
    }
}
