package zm.hashcode.openpayments.wallet;

import java.util.Objects;

/**
 * Represents a public key in the Open Payments system.
 *
 * @param kid
 *            key ID
 * @param kty
 *            key type
 * @param use
 *            key use
 * @param alg
 *            algorithm
 * @param x
 *            x coordinate (for elliptic curve keys)
 */
public record PublicKey(String kid, String kty, String use, String alg, String x) {

    public PublicKey {
        Objects.requireNonNull(kid, "kid must not be null");
        Objects.requireNonNull(kty, "kty must not be null");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String kid;
        private String kty;
        private String use;
        private String alg;
        private String x;

        private Builder() {
        }

        public Builder kid(String kid) {
            this.kid = kid;
            return this;
        }

        public Builder kty(String kty) {
            this.kty = kty;
            return this;
        }

        public Builder use(String use) {
            this.use = use;
            return this;
        }

        public Builder alg(String alg) {
            this.alg = alg;
            return this;
        }

        public Builder x(String x) {
            this.x = x;
            return this;
        }

        public PublicKey build() {
            return new PublicKey(kid, kty, use, alg, x);
        }
    }
}
