package zm.hashcode.openpayments.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Represents an access token for authenticating Open Payments API requests.
 */
public final class AccessToken {
    private final String value;
    private final String manageUrl;
    private final Instant expiresAt;
    private final List<AccessRight> access;

    private AccessToken(Builder builder) {
        this.value = Objects.requireNonNull(builder.value);
        this.manageUrl = builder.manageUrl;
        this.expiresAt = builder.expiresAt;
        this.access = builder.access != null ? List.copyOf(builder.access) : List.of();
    }

    public String getValue() {
        return value;
    }

    public String getManageUrl() {
        return manageUrl;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public List<AccessRight> getAccess() {
        return access;
    }

    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    public boolean isExpiringSoon(Duration threshold) {
        return expiresAt != null && Instant.now().plus(threshold).isAfter(expiresAt);
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
            return new AccessToken(this);
        }
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
}
