package zm.hashcode.openpayments.auth.signature;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;

import zm.hashcode.openpayments.auth.exception.SignatureException;

/**
 * Utility class for generating and validating content digests using SHA-256.
 *
 * <p>
 * Content digests are used in HTTP message signatures to ensure request body integrity. The digest is computed using
 * SHA-256 and encoded in the structured field format defined by RFC 9421.
 *
 * <p>
 * The digest format is: {@code sha-256=:base64_encoded_hash:=}
 *
 * <p>
 * Example usage:
 *
 * <pre>{@code
 * String body = "{\"amount\": \"100\"}";
 * String digest = ContentDigest.generate(body);
 * // Result: "sha-256=:ABC123...XYZ:="
 *
 * // Validate digest
 * boolean isValid = ContentDigest.validate(body, digest);
 * }</pre>
 *
 * <p>
 * <b>Thread Safety:</b> This class is thread-safe and all methods are stateless.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc9421.html">RFC 9421 - HTTP Message Signatures</a>
 * @see <a href="https://openpayments.dev/identity/http-signatures/">Open Payments - HTTP Signatures</a>
 */
public final class ContentDigest {

    private static final String ALGORITHM = "SHA-256";
    private static final String DIGEST_PREFIX = "sha-256=:";
    private static final String DIGEST_SUFFIX = ":=";

    // Private constructor to prevent instantiation
    private ContentDigest() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Generates a SHA-256 content digest for the given request body.
     *
     * <p>
     * The digest is computed by:
     * <ol>
     * <li>Converting the body to UTF-8 bytes</li>
     * <li>Computing SHA-256 hash</li>
     * <li>Base64 encoding the hash</li>
     * <li>Wrapping in structured field format</li>
     * </ol>
     *
     * @param body
     *            the request body to digest
     * @return the content digest in format {@code sha-256=:base64_hash:=}
     * @throws NullPointerException
     *             if body is null
     * @throws SignatureException
     *             if digest computation fails
     */
    public static String generate(String body) {
        Objects.requireNonNull(body, "body must not be null");

        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
            byte[] hash = digest.digest(bodyBytes);
            String base64Hash = Base64.getEncoder().encodeToString(hash);

            return DIGEST_PREFIX + base64Hash + DIGEST_SUFFIX;
        } catch (NoSuchAlgorithmException e) {
            // This should never happen as SHA-256 is always available
            throw new SignatureException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Validates a content digest against the given request body.
     *
     * <p>
     * The validation performs a constant-time comparison to prevent timing attacks.
     *
     * @param body
     *            the request body
     * @param digestHeader
     *            the Content-Digest header value to validate
     * @return true if the digest is valid, false otherwise
     * @throws NullPointerException
     *             if body or digestHeader is null
     */
    public static boolean validate(String body, String digestHeader) {
        Objects.requireNonNull(body, "body must not be null");
        Objects.requireNonNull(digestHeader, "digestHeader must not be null");

        try {
            String expectedDigest = generate(body);
            return MessageDigest.isEqual(expectedDigest.getBytes(StandardCharsets.UTF_8),
                    digestHeader.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            // If any error occurs during validation, treat as invalid
            return false;
        }
    }

    /**
     * Extracts the base64-encoded hash from a digest header.
     *
     * <p>
     * This method parses the structured field format and returns just the hash value.
     *
     * @param digestHeader
     *            the digest header (e.g., "sha-256=:ABC123:=")
     * @return the base64-encoded hash, or empty string if format is invalid
     * @throws NullPointerException
     *             if digestHeader is null
     */
    public static String extractHash(String digestHeader) {
        Objects.requireNonNull(digestHeader, "digestHeader must not be null");

        if (!digestHeader.startsWith(DIGEST_PREFIX) || !digestHeader.endsWith(DIGEST_SUFFIX)) {
            return "";
        }

        return digestHeader.substring(DIGEST_PREFIX.length(), digestHeader.length() - DIGEST_SUFFIX.length());
    }

    /**
     * Checks if a digest header has the correct format.
     *
     * @param digestHeader
     *            the digest header to check
     * @return true if the format is valid
     * @throws NullPointerException
     *             if digestHeader is null
     */
    public static boolean isValidFormat(String digestHeader) {
        Objects.requireNonNull(digestHeader, "digestHeader must not be null");

        if (!digestHeader.startsWith(DIGEST_PREFIX) || !digestHeader.endsWith(DIGEST_SUFFIX)) {
            return false;
        }

        String hash = extractHash(digestHeader);
        if (hash.isEmpty()) {
            return false;
        }

        // Validate base64 format
        try {
            Base64.getDecoder().decode(hash);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
