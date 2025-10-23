package zm.hashcode.openpayments.auth.keys;

import java.security.PublicKey;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import zm.hashcode.openpayments.auth.exception.KeyException;

/**
 * JSON Web Key (JWK) representation for Ed25519 public keys.
 *
 * <p>
 * JWKs are used in Open Payments to share public keys for signature verification. The key is published at the client's
 * wallet address JWKS endpoint ({@code /.well-known/jwks.json}) and used by authorization servers to validate HTTP
 * signatures.
 *
 * <p>
 * For Ed25519 keys, the JWK format requires:
 * <ul>
 * <li>{@code kty}: "OKP" (Octet Key Pair)</li>
 * <li>{@code crv}: "Ed25519" (Edwards curve)</li>
 * <li>{@code alg}: "EdDSA" (Edwards-curve Digital Signature Algorithm)</li>
 * <li>{@code kid}: Key identifier (unique)</li>
 * <li>{@code x}: Base64url-encoded public key</li>
 * <li>{@code use}: "sig" (for signature operations, optional)</li>
 * </ul>
 *
 * <p>
 * Example JWK:
 *
 * <pre>{@code
 * {
 *   "kid": "my-key-1",
 *   "alg": "EdDSA",
 *   "kty": "OKP",
 *   "crv": "Ed25519",
 *   "x": "11qYAYKxCrfVS_7TyWQHOg7hcvPapiMlrwIaaPcHURo",
 *   "use": "sig"
 * }
 * }</pre>
 *
 * <p>
 * <b>Immutability:</b> This record is immutable and thread-safe.
 *
 * @param kid
 *            key identifier - must be unique
 * @param alg
 *            algorithm - must be "EdDSA" for Ed25519
 * @param kty
 *            key type - must be "OKP" for Ed25519
 * @param crv
 *            curve - must be "Ed25519"
 * @param x
 *            base64url-encoded public key value
 * @param use
 *            public key use - typically "sig" for signatures (optional)
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc8037">RFC 8037 - CFRG Elliptic Curve JWK</a>
 * @see <a href="https://openpayments.dev/identity/client-keys/">Open Payments - Client Keys</a>
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record JsonWebKey(@JsonProperty("kid") String kid, @JsonProperty("alg") String alg,
        @JsonProperty("kty") String kty, @JsonProperty("crv") String crv, @JsonProperty("x") String x,
        @JsonProperty("use") Optional<String> use) {

    /** Standard algorithm for Ed25519 signatures */
    public static final String ALGORITHM_EDDSA = "EdDSA";

    /** Standard key type for Octet Key Pairs */
    public static final String KEY_TYPE_OKP = "OKP";

    /** Standard curve for Ed25519 */
    public static final String CURVE_ED25519 = "Ed25519";

    /** Standard use for signature operations */
    public static final String USE_SIGNATURE = "sig";

    /**
     * Compact constructor with validation.
     *
     * @throws NullPointerException
     *             if any required field is null
     * @throws IllegalArgumentException
     *             if any field value is invalid
     */
    public JsonWebKey {
        Objects.requireNonNull(kid, "kid must not be null");
        Objects.requireNonNull(alg, "alg must not be null");
        Objects.requireNonNull(kty, "kty must not be null");
        Objects.requireNonNull(crv, "crv must not be null");
        Objects.requireNonNull(x, "x must not be null");
        Objects.requireNonNull(use, "use must not be null (use Optional.empty() if not present)");

        if (kid.isBlank()) {
            throw new IllegalArgumentException("kid must not be blank");
        }
        if (x.isBlank()) {
            throw new IllegalArgumentException("x must not be blank");
        }
    }

    /**
     * Creates a JWK from an Ed25519 public key.
     *
     * <p>
     * This factory method constructs a JWK with standard Ed25519 parameters.
     *
     * @param keyId
     *            unique key identifier
     * @param publicKey
     *            Ed25519 public key
     * @return JWK representation of the public key
     * @throws KeyException
     *             if the public key format is invalid
     */
    public static JsonWebKey from(String keyId, PublicKey publicKey) {
        Objects.requireNonNull(keyId, "keyId must not be null");
        Objects.requireNonNull(publicKey, "publicKey must not be null");

        if (!"Ed25519".equals(publicKey.getAlgorithm()) && !"EdDSA".equals(publicKey.getAlgorithm())) {
            throw new KeyException("Public key must be Ed25519, got: " + publicKey.getAlgorithm());
        }

        // Get raw public key bytes (32 bytes for Ed25519)
        byte[] publicKeyBytes = publicKey.getEncoded();

        // For Ed25519, the encoded key includes ASN.1 wrapping
        // The actual 32-byte public key starts at offset 12
        byte[] rawPublicKey;
        if (publicKeyBytes.length == 44) {
            // Standard Ed25519 public key encoding (12 bytes ASN.1 + 32 bytes key)
            rawPublicKey = new byte[32];
            System.arraycopy(publicKeyBytes, 12, rawPublicKey, 0, 32);
        } else if (publicKeyBytes.length == 32) {
            // Raw public key (already unwrapped)
            rawPublicKey = publicKeyBytes;
        } else {
            throw new KeyException("Invalid Ed25519 public key length: " + publicKeyBytes.length);
        }

        // Base64url encode (no padding)
        String x = Base64.getUrlEncoder().withoutPadding().encodeToString(rawPublicKey);

        return new JsonWebKey(keyId, ALGORITHM_EDDSA, KEY_TYPE_OKP, CURVE_ED25519, x, Optional.of(USE_SIGNATURE));
    }

    /**
     * Validates that this JWK has correct Ed25519 parameters.
     *
     * @return true if this JWK is valid for Ed25519 operations
     */
    public boolean isValid() {
        return ALGORITHM_EDDSA.equals(alg) && KEY_TYPE_OKP.equals(kty) && CURVE_ED25519.equals(crv) && !kid.isBlank()
                && !x.isBlank() && isValidBase64Url(x);
    }

    /**
     * Checks if this is an Ed25519 signature key.
     *
     * @return true if this JWK is for Ed25519 signatures
     */
    public boolean isEd25519SignatureKey() {
        return isValid() && use.map(USE_SIGNATURE::equals).orElse(true);
    }

    /**
     * Gets the raw public key bytes (32 bytes for Ed25519).
     *
     * @return decoded public key bytes
     * @throws KeyException
     *             if the x value is not valid base64url
     */
    public byte[] getPublicKeyBytes() {
        try {
            return Base64.getUrlDecoder().decode(x);
        } catch (IllegalArgumentException e) {
            throw new KeyException("Invalid base64url encoding in x field", e);
        }
    }

    /**
     * Validates base64url encoding.
     *
     * @param value
     *            the value to validate
     * @return true if valid base64url
     */
    private static boolean isValidBase64Url(String value) {
        try {
            Base64.getUrlDecoder().decode(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Builder for constructing JWKs.
     */
    public static final class Builder {
        private String kid;
        private String alg = ALGORITHM_EDDSA;
        private String kty = KEY_TYPE_OKP;
        private String crv = CURVE_ED25519;
        private String x;
        private Optional<String> use = Optional.of(USE_SIGNATURE);

        public Builder kid(String kid) {
            this.kid = kid;
            return this;
        }

        public Builder alg(String alg) {
            this.alg = alg;
            return this;
        }

        public Builder kty(String kty) {
            this.kty = kty;
            return this;
        }

        public Builder crv(String crv) {
            this.crv = crv;
            return this;
        }

        public Builder x(String x) {
            this.x = x;
            return this;
        }

        public Builder use(String use) {
            this.use = Optional.ofNullable(use);
            return this;
        }

        public JsonWebKey build() {
            return new JsonWebKey(kid, alg, kty, crv, x, use);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
