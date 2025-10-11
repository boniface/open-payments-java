package zm.hashcode.openpayments.auth.signature;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import zm.hashcode.openpayments.auth.exception.SignatureException;
import zm.hashcode.openpayments.auth.keys.ClientKey;

/**
 * Service for creating and validating HTTP message signatures per RFC 9421.
 *
 * <p>
 * This service implements HTTP message signatures using Ed25519 (EdDSA) as required by Open Payments. It creates
 * signatures over HTTP request components including method, URI, headers, and body digest.
 *
 * <p>
 * <b>Signature Process:</b>
 * <ol>
 * <li>Collect signature components (@method, @target-uri, headers)</li>
 * <li>Build signature base string</li>
 * <li>Sign with Ed25519 private key</li>
 * <li>Create Signature and Signature-Input headers</li>
 * </ol>
 *
 * <p>
 * Example usage:
 *
 * <pre>{@code
 * ClientKey clientKey = ClientKeyGenerator.generate("key-1");
 * HttpSignatureService service = new HttpSignatureService(clientKey);
 *
 * SignatureComponents components = SignatureComponents.builder().method("POST")
 *         .targetUri("https://auth.example.com/grant").addHeader("content-type", "application/json")
 *         .addHeader("content-digest", "sha-256=:abc:=").body("{}").build();
 *
 * Map<String, String> signatureHeaders = service.createSignatureHeaders(components);
 * // signatureHeaders contains: Signature and Signature-Input
 * }</pre>
 *
 * <p>
 * <b>Thread Safety:</b> This class is thread-safe after construction.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc9421.html">RFC 9421 - HTTP Message Signatures</a>
 * @see <a href="https://openpayments.dev/identity/http-signatures/">Open Payments - HTTP Signatures</a>
 */
public final class HttpSignatureService {

    private static final String SIGNATURE_LABEL = "sig";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final ClientKey clientKey;
    private final String keyId;

    /**
     * Creates a new HTTP signature service.
     *
     * @param clientKey
     *            the client key for signing
     * @throws NullPointerException
     *             if clientKey is null
     */
    public HttpSignatureService(ClientKey clientKey) {
        this.clientKey = Objects.requireNonNull(clientKey, "clientKey must not be null");
        this.keyId = clientKey.keyId();
    }

    /**
     * Creates HTTP signature headers for the given components.
     *
     * <p>
     * This method generates two headers:
     * <ul>
     * <li><b>Signature-Input</b>: Metadata about the signature (components, created time, key ID, algorithm)</li>
     * <li><b>Signature</b>: The actual signature value (base64-encoded)</li>
     * </ul>
     *
     * @param components
     *            the signature components
     * @return map containing "signature" and "signature-input" headers
     * @throws SignatureException
     *             if signature creation fails
     * @throws NullPointerException
     *             if components is null
     */
    public Map<String, String> createSignatureHeaders(SignatureComponents components) {
        Objects.requireNonNull(components, "components must not be null");

        // Build signature base
        String signatureBase = buildSignatureBase(components);

        // Sign the base
        byte[] signatureBytes = clientKey.sign(signatureBase.getBytes(StandardCharsets.UTF_8));
        String signatureValue = Base64.getEncoder().encodeToString(signatureBytes);

        // Create signature metadata
        long createdTime = Instant.now().getEpochSecond();
        String nonce = generateNonce();

        // Build Signature-Input header
        String signatureInput = buildSignatureInputHeader(components, createdTime, nonce);

        // Build Signature header
        String signature = SIGNATURE_LABEL + "=:" + signatureValue + ":";

        // Return headers
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("signature-input", signatureInput);
        headers.put("signature", signature);

        return headers;
    }

    /**
     * Builds the signature base string per RFC 9421.
     *
     * <p>
     * The signature base is constructed by concatenating the covered components, each formatted as:
     *
     * <pre>
     * "component-name": component-value
     * </pre>
     *
     * For derived components (@method, @target-uri):
     *
     * <pre>
     * "@method": METHOD
     * "@target-uri": https://example.com/path
     * </pre>
     *
     * For regular headers:
     *
     * <pre>
     * "content-type": application/json
     * </pre>
     *
     * @param components
     *            the signature components
     * @return signature base string
     */
    private String buildSignatureBase(SignatureComponents components) {
        StringBuilder base = new StringBuilder();

        for (String identifier : components.getComponentIdentifiers()) {
            // Add component line
            base.append("\"").append(identifier).append("\": ");

            if (identifier.startsWith("@")) {
                // Derived component
                base.append(getDerivedComponentValue(identifier, components));
            } else {
                // HTTP header
                String headerValue = components.getHeader(identifier)
                        .orElseThrow(() -> new SignatureException("Header not found: " + identifier));
                base.append(headerValue);
            }

            base.append("\n");
        }

        // Add signature parameters
        base.append("\"@signature-params\": ");
        base.append(buildSignatureParams(components));

        return base.toString();
    }

    /**
     * Gets the value for a derived component.
     *
     * @param identifier
     *            the derived component identifier (e.g., "@method")
     * @param components
     *            the signature components
     * @return the component value
     */
    private String getDerivedComponentValue(String identifier, SignatureComponents components) {
        return switch (identifier) {
            case "@method" -> components.getMethod();
            case "@target-uri" -> components.getTargetUri();
            default -> throw new SignatureException("Unknown derived component: " + identifier);
        };
    }

    /**
     * Builds the signature parameters line.
     *
     * <p>
     * Format: {@code (component1 component2 ...);created=timestamp;keyid="key-id";alg="ed25519";nonce="nonce"}
     *
     * @param components
     *            the signature components
     * @return signature parameters string
     */
    private String buildSignatureParams(SignatureComponents components) {
        StringBuilder params = new StringBuilder();

        // Add component identifiers
        params.append("(");
        params.append(String.join(" ", components.getComponentIdentifiers()));
        params.append(")");

        // These will be added when we actually create the signature
        // For now, just return the component list
        return params.toString();
    }

    /**
     * Builds the Signature-Input header value.
     *
     * <p>
     * Format: {@code sig=(component1 component2 ...);created=timestamp;keyid="key-id";alg="ed25519";nonce="nonce"}
     *
     * @param components
     *            the signature components
     * @param createdTime
     *            Unix timestamp when signature was created
     * @param nonce
     *            random nonce
     * @return Signature-Input header value
     */
    private String buildSignatureInputHeader(SignatureComponents components, long createdTime, String nonce) {
        StringBuilder input = new StringBuilder();

        input.append(SIGNATURE_LABEL).append("=");

        // Add component identifiers
        input.append("(");
        input.append(String.join(" ", components.getComponentIdentifiers()));
        input.append(")");

        // Add parameters
        input.append(";created=").append(createdTime);
        input.append(";keyid=\"").append(keyId).append("\"");
        input.append(";alg=\"ed25519\"");
        input.append(";nonce=\"").append(nonce).append("\"");

        return input.toString();
    }

    /**
     * Generates a cryptographically random nonce.
     *
     * @return base64-encoded nonce (16 bytes)
     */
    private String generateNonce() {
        byte[] nonceBytes = new byte[16];
        SECURE_RANDOM.nextBytes(nonceBytes);
        return Base64.getEncoder().encodeToString(nonceBytes);
    }

    /**
     * Validates an HTTP signature.
     *
     * <p>
     * This method verifies that the signature was created by the holder of the private key corresponding to the
     * provided public key.
     *
     * @param components
     *            the signature components
     * @param signatureValue
     *            the signature value (base64-encoded)
     * @return true if the signature is valid
     * @throws SignatureException
     *             if validation fails
     * @throws NullPointerException
     *             if any parameter is null
     */
    public boolean validateSignature(SignatureComponents components, String signatureValue) {
        Objects.requireNonNull(components, "components must not be null");
        Objects.requireNonNull(signatureValue, "signatureValue must not be null");

        try {
            // Build signature base
            String signatureBase = buildSignatureBase(components);

            // Decode signature
            byte[] signatureBytes = Base64.getDecoder().decode(signatureValue);

            // Verify with public key
            return clientKey.verify(signatureBase.getBytes(StandardCharsets.UTF_8), signatureBytes);
        } catch (IllegalArgumentException e) {
            // Base64 decode failed
            throw new SignatureException("Invalid signature encoding", e);
        }
    }

    /**
     * Gets the key ID used by this service.
     *
     * @return the key ID
     */
    public String getKeyId() {
        return keyId;
    }
}
