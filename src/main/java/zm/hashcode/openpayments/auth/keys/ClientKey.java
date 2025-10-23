package zm.hashcode.openpayments.auth.keys;

import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Objects;

import zm.hashcode.openpayments.auth.exception.SignatureException;

/**
 * Represents a client's Ed25519 key pair for authentication in Open Payments.
 *
 * <p>
 * A client key consists of:
 * <ul>
 * <li><b>Key ID</b>: Unique identifier for the key</li>
 * <li><b>Private Key</b>: Used to sign HTTP requests (kept secret)</li>
 * <li><b>Public Key</b>: Shared via JWKS for signature verification</li>
 * </ul>
 *
 * <p>
 * The private key is used to create HTTP message signatures, while the public key is published at the client's wallet
 * address JWKS endpoint for authorization servers to verify signatures.
 *
 * <p>
 * Example usage:
 *
 * <pre>{@code
 * // Generate new key pair
 * ClientKey key = ClientKeyGenerator.generate("my-key-1");
 *
 * // Sign data
 * byte[] data = "Hello, World!".getBytes();
 * byte[] signature = key.sign(data);
 *
 * // Get JWK for publishing
 * JsonWebKey jwk = key.toJwk();
 * }</pre>
 *
 * <p>
 * <b>Security:</b> The private key should be stored securely and never logged or exposed in error messages.
 *
 * <p>
 * <b>Immutability:</b> This record is immutable and thread-safe.
 *
 * @param keyId
 *            unique identifier for this key pair
 * @param privateKey
 *            Ed25519 private key for signing
 * @param publicKey
 *            Ed25519 public key for verification
 * @see JsonWebKey
 * @see <a href="https://openpayments.dev/identity/client-keys/">Open Payments - Client Keys</a>
 */
public record ClientKey(String keyId, PrivateKey privateKey, PublicKey publicKey) {

    /**
     * Compact constructor with validation.
     *
     * @throws NullPointerException
     *             if any parameter is null
     * @throws IllegalArgumentException
     *             if keyId is blank or keys are not Ed25519
     */
    public ClientKey {
        Objects.requireNonNull(keyId, "keyId must not be null");
        Objects.requireNonNull(privateKey, "privateKey must not be null");
        Objects.requireNonNull(publicKey, "publicKey must not be null");

        if (keyId.isBlank()) {
            throw new IllegalArgumentException("keyId must not be blank");
        }

        // Validate key algorithms
        String privateAlg = privateKey.getAlgorithm();
        String publicAlg = publicKey.getAlgorithm();

        if (!"Ed25519".equals(privateAlg) && !"EdDSA".equals(privateAlg)) {
            throw new IllegalArgumentException("Private key must be Ed25519, got: " + privateAlg);
        }

        if (!"Ed25519".equals(publicAlg) && !"EdDSA".equals(publicAlg)) {
            throw new IllegalArgumentException("Public key must be Ed25519, got: " + publicAlg);
        }
    }

    /**
     * Creates a JWK representation of the public key.
     *
     * <p>
     * The JWK can be published to the client's JWKS endpoint for signature verification by authorization servers.
     *
     * @return JWK representation of the public key
     */
    public JsonWebKey toJwk() {
        return JsonWebKey.from(keyId, publicKey);
    }

    /**
     * Signs data using the private key.
     *
     * <p>
     * This method uses Ed25519 (EdDSA) to create a digital signature over the provided data.
     *
     * @param data
     *            the data to sign
     * @return the signature bytes
     * @throws zm.hashcode.openpayments.auth.exception.SignatureException
     *             if signing fails
     * @throws NullPointerException
     *             if data is null
     */
    public byte[] sign(byte[] data) {
        Objects.requireNonNull(data, "data must not be null");

        try {
            Signature signature = Signature.getInstance("Ed25519");
            signature.initSign(privateKey);
            signature.update(data);
            return signature.sign();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new SignatureException("Ed25519 algorithm not available", e);
        } catch (InvalidKeyException e) {
            throw new SignatureException("Invalid private key", e);
        } catch (java.security.SignatureException e) {
            throw new SignatureException("Signature creation failed", e);
        }
    }

    /**
     * Verifies a signature using the public key.
     *
     * <p>
     * This method verifies that the signature was created by the corresponding private key.
     *
     * @param data
     *            the original data that was signed
     * @param signatureBytes
     *            the signature to verify
     * @return true if the signature is valid
     * @throws SignatureException
     *             if verification fails
     * @throws NullPointerException
     *             if any parameter is null
     */
    public boolean verify(byte[] data, byte[] signatureBytes) {
        Objects.requireNonNull(data, "data must not be null");
        Objects.requireNonNull(signatureBytes, "signatureBytes must not be null");

        try {
            Signature signature = Signature.getInstance("Ed25519");
            signature.initVerify(publicKey);
            signature.update(data);
            return signature.verify(signatureBytes);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new SignatureException("Ed25519 algorithm not available", e);
        } catch (InvalidKeyException e) {
            // Invalid key during init - this is a fatal error
            throw new SignatureException("Invalid public key", e);
        } catch (java.security.SignatureException e) {
            // Signature verification can fail for invalid/tampered signatures
            // This is expected behavior, not an exception case
            return false;
        }
    }

    /**
     * Custom toString that doesn't expose private key.
     *
     * @return string representation without private key
     */
    @Override
    public String toString() {
        return "ClientKey{keyId='" + keyId + "', algorithm=Ed25519}";
    }

    /**
     * Custom equals that compares only keyId and public key.
     *
     * <p>
     * Private keys are not compared to avoid timing attacks.
     *
     * @param obj
     *            the object to compare
     * @return true if keyId and public key match
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ClientKey other = (ClientKey) obj;
        return Objects.equals(keyId, other.keyId) && Objects.equals(publicKey, other.publicKey);
    }

    /**
     * Custom hashCode based on keyId and public key only.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(keyId, publicKey);
    }
}
