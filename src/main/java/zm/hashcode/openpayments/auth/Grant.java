package zm.hashcode.openpayments.auth;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents an access grant in the Open Payments system.
 *
 * <p>
 * Grants are obtained through the GNAP (Grant Negotiation and Authorization Protocol) flow and provide authorization to
 * access specific resources.
 *
 * @param continueUri
 *            the URI for continuing the grant flow (optional)
 * @param continueToken
 *            the token for continuing the grant flow (optional)
 * @param accessToken
 *            the access token (optional)
 * @param interactUrl
 *            the URL for user interaction (optional)
 * @param interactRef
 *            the interaction reference (optional)
 */
public record Grant(String continueUri, String continueToken, AccessToken accessToken, String interactUrl,
        String interactRef) {

    /**
     * Returns the continue URI, if available.
     *
     * @return an Optional containing the continue URI
     */
    public Optional<String> getContinueUri() {
        return Optional.ofNullable(continueUri);
    }

    /**
     * Returns the continue token, if available.
     *
     * @return an Optional containing the continue token
     */
    public Optional<String> getContinueToken() {
        return Optional.ofNullable(continueToken);
    }

    /**
     * Returns the access token, if available.
     *
     * @return an Optional containing the access token
     */
    public Optional<AccessToken> getAccessToken() {
        return Optional.ofNullable(accessToken);
    }

    /**
     * Returns the interaction URL, if available.
     *
     * @return an Optional containing the interaction URL
     */
    public Optional<String> getInteractUrl() {
        return Optional.ofNullable(interactUrl);
    }

    /**
     * Returns the interaction reference, if available.
     *
     * @return an Optional containing the interaction reference
     */
    public Optional<String> getInteractRef() {
        return Optional.ofNullable(interactRef);
    }

    /**
     * Returns whether this grant requires user interaction.
     *
     * @return true if interaction is required, false otherwise
     */
    public boolean requiresInteraction() {
        return interactUrl != null;
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
            return new Grant(continueUri, continueToken, accessToken, interactUrl, interactRef);
        }
    }
}
