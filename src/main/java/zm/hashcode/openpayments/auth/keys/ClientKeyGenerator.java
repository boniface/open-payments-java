package zm.hashcode.openpayments.auth.keys;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;

import zm.hashcode.openpayments.auth.exception.KeyException;

/**
 * Generates and loads Ed25519 key pairs for client authentication.
 *
 * <p>
 * This class provides factory methods for creating {@link ClientKey} instances in various ways:
 * <ul>
 * <li>Generate new Ed25519 key pairs</li>
 * <li>Load from base64-encoded keys</li>
 * <li>Load from PEM format (future enhancement)</li>
 * </ul>
 *
 * <p>
 * Example usage:
 *
 * <pre>{@code
 * // Generate new key pair
 * ClientKey key = ClientKeyGenerator.generate("my-client-key-1");
 *
 * // Save private key (base64)
 * String privateKeyBase64 = ClientKeyGenerator.encodePrivateKey(key.privateKey());
 *
 * // Load from saved key
 * ClientKey loadedKey = ClientKeyGenerator.fromBase64("my-client-key-1", privateKeyBase64, publicKeyBase64);
 * }</pre>
 *
 * <p>
 * <b>Security:</b> All key generation uses {@link SecureRandom} for cryptographically strong randomness.
 *
 * <p>
 * <b>Thread Safety:</b> This class is thread-safe and all methods are stateless.
 *
 * @see ClientKey
 * @see JsonWebKey
 */
public final class ClientKeyGenerator {

    private static final String ALGORITHM = "Ed25519";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // Private constructor to prevent instantiation
    private ClientKeyGenerator() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Generates a new Ed25519 key pair.
     *
     * <p>
     * This method creates a fresh key pair using cryptographically strong random number generation.
     *
     * @param keyId
     *            unique identifier for the key pair
     * @return new client key with generated Ed25519 key pair
     * @throws KeyException
     *             if key generation fails
     * @throws NullPointerException
     *             if keyId is null
     * @throws IllegalArgumentException
     *             if keyId is blank
     */
    public static ClientKey generate(String keyId) {
        Objects.requireNonNull(keyId, "keyId must not be null");

        if (keyId.isBlank()) {
            throw new IllegalArgumentException("keyId must not be blank");
        }

        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM);
            generator.initialize(255, SECURE_RANDOM); // Ed25519 uses 255-bit keys
            KeyPair keyPair = generator.generateKeyPair();

            return new ClientKey(keyId, keyPair.getPrivate(), keyPair.getPublic());
        } catch (NoSuchAlgorithmException e) {
            throw new KeyException("Ed25519 algorithm not available", e);
        }
    }

    /**
     * Loads a client key from base64-encoded private and public keys.
     *
     * <p>
     * The keys should be in PKCS#8 (private) and X.509 (public) formats, base64-encoded.
     *
     * @param keyId
     *            unique identifier for the key pair
     * @param privateKeyBase64
     *            base64-encoded private key (PKCS#8)
     * @param publicKeyBase64
     *            base64-encoded public key (X.509)
     * @return client key with loaded key pair
     * @throws KeyException
     *             if key loading fails
     * @throws NullPointerException
     *             if any parameter is null
     * @throws IllegalArgumentException
     *             if keyId is blank or keys are invalid
     */
    public static ClientKey fromBase64(String keyId, String privateKeyBase64, String publicKeyBase64) {
        Objects.requireNonNull(keyId, "keyId must not be null");
        Objects.requireNonNull(privateKeyBase64, "privateKeyBase64 must not be null");
        Objects.requireNonNull(publicKeyBase64, "publicKeyBase64 must not be null");

        if (keyId.isBlank()) {
            throw new IllegalArgumentException("keyId must not be blank");
        }

        try {
            // Decode base64
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64);
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);

            // Load keys
            PrivateKey privateKey = loadPrivateKey(privateKeyBytes);
            PublicKey publicKey = loadPublicKey(publicKeyBytes);

            return new ClientKey(keyId, privateKey, publicKey);
        } catch (IllegalArgumentException e) {
            throw new KeyException("Invalid base64 encoding", e);
        }
    }

    /**
     * Loads a client key from base64-encoded private key only.
     *
     * <p>
     * The public key is derived from the private key.
     *
     * @param keyId
     *            unique identifier for the key pair
     * @param privateKeyBase64
     *            base64-encoded private key (PKCS#8)
     * @return client key with loaded key pair
     * @throws KeyException
     *             if key loading fails
     * @throws NullPointerException
     *             if any parameter is null
     * @throws IllegalArgumentException
     *             if keyId is blank or key is invalid
     */
    public static ClientKey fromPrivateKeyBase64(String keyId, String privateKeyBase64) {
        Objects.requireNonNull(keyId, "keyId must not be null");
        Objects.requireNonNull(privateKeyBase64, "privateKeyBase64 must not be null");

        if (keyId.isBlank()) {
            throw new IllegalArgumentException("keyId must not be blank");
        }

        try {
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64);
            PrivateKey privateKey = loadPrivateKey(privateKeyBytes);

            // Derive public key from private key
            PublicKey publicKey = derivePublicKey(privateKey);

            return new ClientKey(keyId, privateKey, publicKey);
        } catch (IllegalArgumentException e) {
            throw new KeyException("Invalid base64 encoding", e);
        }
    }

    /**
     * Encodes a private key to base64 string (PKCS#8 format).
     *
     * @param privateKey
     *            the private key to encode
     * @return base64-encoded private key
     * @throws NullPointerException
     *             if privateKey is null
     */
    public static String encodePrivateKey(PrivateKey privateKey) {
        Objects.requireNonNull(privateKey, "privateKey must not be null");
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    /**
     * Encodes a public key to base64 string (X.509 format).
     *
     * @param publicKey
     *            the public key to encode
     * @return base64-encoded public key
     * @throws NullPointerException
     *             if publicKey is null
     */
    public static String encodePublicKey(PublicKey publicKey) {
        Objects.requireNonNull(publicKey, "publicKey must not be null");
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * Loads a private key from PKCS#8 encoded bytes.
     *
     * @param keyBytes
     *            PKCS#8 encoded private key
     * @return the private key
     * @throws KeyException
     *             if loading fails
     */
    private static PrivateKey loadPrivateKey(byte[] keyBytes) {
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance(ALGORITHM);
            return keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new KeyException("Ed25519 algorithm not available", e);
        } catch (InvalidKeySpecException e) {
            throw new KeyException("Invalid private key format", e);
        }
    }

    /**
     * Loads a public key from X.509 encoded bytes.
     *
     * @param keyBytes
     *            X.509 encoded public key
     * @return the public key
     * @throws KeyException
     *             if loading fails
     */
    private static PublicKey loadPublicKey(byte[] keyBytes) {
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance(ALGORITHM);
            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new KeyException("Ed25519 algorithm not available", e);
        } catch (InvalidKeySpecException e) {
            throw new KeyException("Invalid public key format", e);
        }
    }

    /**
     * Derives the public key from a private key.
     *
     * <p>
     * For Ed25519, the public key can be computed from the private key.
     *
     * @param privateKey
     *            the private key
     * @return the corresponding public key
     * @throws KeyException
     *             if derivation fails
     */
    private static PublicKey derivePublicKey(PrivateKey privateKey) {
        // For Ed25519, the standard encoding includes both private and public key data
        // The public key is embedded in the private key encoding
        byte[] privateKeyBytes = privateKey.getEncoded();

        // PKCS#8 format for Ed25519 private key is:
        // - Algorithm identifier (varies, ~14-16 bytes)
        // - Private key (32 bytes)
        // - Public key (32 bytes) - this is what we need

        if (privateKeyBytes.length == 48 || privateKeyBytes.length == 46) {
            // Standard Ed25519 private key encoding includes public key at the end
            byte[] publicKeyBytes = new byte[32];
            System.arraycopy(privateKeyBytes, privateKeyBytes.length - 32, publicKeyBytes, 0, 32);

            // Wrap in X.509 format
            byte[] x509Encoded = wrapPublicKeyX509(publicKeyBytes);
            return loadPublicKey(x509Encoded);
        }

        throw new KeyException("Unable to derive public key from private key encoding");
    }

    /**
     * Wraps raw Ed25519 public key bytes in X.509 format.
     *
     * @param rawPublicKey
     *            32-byte Ed25519 public key
     * @return X.509 encoded public key
     */
    private static byte[] wrapPublicKeyX509(byte[] rawPublicKey) {
        // X.509 header for Ed25519 public key
        byte[] header = new byte[]{0x30, 0x2a, // SEQUENCE (42 bytes)
                0x30, 0x05, // SEQUENCE (5 bytes) - algorithm
                0x06, 0x03, 0x2b, 0x65, 0x70, // OID 1.3.101.112 (Ed25519)
                0x03, 0x21, // BIT STRING (33 bytes)
                0x00 // No unused bits
        };

        byte[] result = new byte[header.length + rawPublicKey.length];
        System.arraycopy(header, 0, result, 0, header.length);
        System.arraycopy(rawPublicKey, 0, result, header.length, rawPublicKey.length);

        return result;
    }
}
