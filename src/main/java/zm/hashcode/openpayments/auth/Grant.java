package zm.hashcode.openpayments.auth;

import java.util.Objects;

/**
 * Represents an access grant in the Open Payments system.
 *
 * <p>
 * Grants are obtained through the GNAP (Grant Negotiation and Authorization Protocol) flow and provide authorization to
 * access specific resources.
 */
public final class Grant {
    private final String continueUri;
    private final String continueToken;
    private final AccessToken accessToken;
    private final String interactUrl;
    private final String interactRef;

    private Grant(Builder builder) {
        this.continueUri = builder.continueUri;
        this.continueToken = builder.continueToken;
        this.accessToken = builder.accessToken;
        this.interactUrl = builder.interactUrl;
        this.interactRef = builder.interactRef;
    }

    public String getContinueUri() {
        return continueUri;
    }

    public String getContinueToken() {
        return continueToken;
    }

    public AccessToken getAccessToken() {
        return accessToken;
    }

    public String getInteractUrl() {
        return interactUrl;
    }

    public String getInteractRef() {
        return interactRef;
    }

    public boolean requiresInteraction() {
        return interactUrl != null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String continueUri;
        private String continueToken;
        private AccessToken accessToken;
        private String interactUrl;
        private String interactRef;

        private Builder() {
        }

        public Builder continueUri(String continueUri) {
            this.continueUri = continueUri;
            return this;
        }

        public Builder continueToken(String continueToken) {
            this.continueToken = continueToken;
            return this;
        }

        public Builder accessToken(AccessToken accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder interactUrl(String interactUrl) {
            this.interactUrl = interactUrl;
            return this;
        }

        public Builder interactRef(String interactRef) {
            this.interactRef = interactRef;
            return this;
        }

        public Grant build() {
            return new Grant(this);
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
        Grant grant = (Grant) o;
        return Objects.equals(continueUri, grant.continueUri) && Objects.equals(continueToken, grant.continueToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(continueUri, continueToken);
    }

    @Override
    public String toString() {
        return "Grant{" + "requiresInteraction=" + requiresInteraction() + '}';
    }
}
