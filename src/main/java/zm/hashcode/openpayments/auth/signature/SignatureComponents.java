package zm.hashcode.openpayments.auth.signature;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Components to include in HTTP message signature.
 *
 * <p>
 * Per RFC 9421 and Open Payments requirements, signatures must include specific HTTP request components:
 * <ul>
 * <li><b>@method</b>: HTTP method (GET, POST, etc.)</li>
 * <li><b>@target-uri</b>: Full request URI</li>
 * <li><b>content-type</b>: Content-Type header (if present)</li>
 * <li><b>content-length</b>: Content-Length header (if present)</li>
 * <li><b>content-digest</b>: Content-Digest header (for requests with body)</li>
 * <li><b>authorization</b>: Authorization header (if present, e.g., GNAP token)</li>
 * </ul>
 *
 * <p>
 * Example usage:
 *
 * <pre>{@code
 * var components = SignatureComponents.builder().method("POST").targetUri("https://auth.example.com/grant")
 *         .addHeader("content-type", "application/json").addHeader("content-digest", "sha-256=:abc123:=")
 *         .body("{\"amount\":\"100\"}").build();
 * }</pre>
 *
 * <p>
 * <b>Immutability:</b> This class is immutable after construction.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc9421.html">RFC 9421 - HTTP Message Signatures</a>
 * @see <a href="https://openpayments.dev/identity/http-signatures/">Open Payments - HTTP Signatures</a>
 */
public final class SignatureComponents {

    private final String method;
    private final String targetUri;
    private final Map<String, String> headers;
    private final Optional<String> body;

    private SignatureComponents(Builder builder) {
        this.method = Objects.requireNonNull(builder.method, "method must not be null");
        this.targetUri = Objects.requireNonNull(builder.targetUri, "targetUri must not be null");
        this.headers = Map.copyOf(builder.headers);
        this.body = builder.body;

        if (method.isBlank()) {
            throw new IllegalArgumentException("method must not be blank");
        }
        if (targetUri.isBlank()) {
            throw new IllegalArgumentException("targetUri must not be blank");
        }
    }

    /**
     * Gets the HTTP method.
     *
     * @return HTTP method (e.g., "GET", "POST")
     */
    public String getMethod() {
        return method;
    }

    /**
     * Gets the target URI.
     *
     * @return full target URI
     */
    public String getTargetUri() {
        return targetUri;
    }

    /**
     * Gets all headers.
     *
     * @return immutable map of headers
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Gets a specific header value.
     *
     * @param name
     *            header name (case-insensitive)
     * @return header value, or empty if not present
     */
    public Optional<String> getHeader(String name) {
        Objects.requireNonNull(name, "name must not be null");
        // RFC 9421: header names are case-insensitive
        return headers.entrySet().stream().filter(entry -> entry.getKey().equalsIgnoreCase(name))
                .map(Map.Entry::getValue).findFirst();
    }

    /**
     * Gets the request body.
     *
     * @return optional request body
     */
    public Optional<String> getBody() {
        return body;
    }

    /**
     * Gets the list of component identifiers to include in signature.
     *
     * <p>
     * This returns the component identifiers in the order they should appear in the signature base, following Open
     * Payments requirements:
     * <ol>
     * <li>@method</li>
     * <li>@target-uri</li>
     * <li>authorization (if present)</li>
     * <li>content-digest (if body present)</li>
     * <li>content-type (if present)</li>
     * <li>content-length (if present)</li>
     * </ol>
     *
     * @return list of component identifiers
     */
    public List<String> getComponentIdentifiers() {
        List<String> identifiers = new ArrayList<>();

        // Always include method and target-uri (derived components)
        identifiers.add("@method");
        identifiers.add("@target-uri");

        // Add authorization if present
        if (getHeader("authorization").isPresent()) {
            identifiers.add("authorization");
        }

        // Add content-digest if body present
        if (body.isPresent() && getHeader("content-digest").isPresent()) {
            identifiers.add("content-digest");
        }

        // Add content-type if present
        if (getHeader("content-type").isPresent()) {
            identifiers.add("content-type");
        }

        // Add content-length if present
        if (getHeader("content-length").isPresent()) {
            identifiers.add("content-length");
        }

        return identifiers;
    }

    /**
     * Checks if this has a request body.
     *
     * @return true if body is present
     */
    public boolean hasBody() {
        return body.isPresent();
    }

    /**
     * Creates a builder for constructing signature components.
     *
     * @return new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link SignatureComponents}.
     */
    public static final class Builder {
        private String method;
        private String targetUri;
        private final Map<String, String> headers = new java.util.HashMap<>();
        private Optional<String> body = Optional.empty();

        private Builder() {
        }

        /**
         * Sets the HTTP method.
         *
         * @param method
         *            HTTP method (e.g., "GET", "POST")
         * @return this builder
         */
        public Builder method(String method) {
            this.method = method;
            return this;
        }

        /**
         * Sets the target URI.
         *
         * @param targetUri
         *            full target URI
         * @return this builder
         */
        public Builder targetUri(String targetUri) {
            this.targetUri = targetUri;
            return this;
        }

        /**
         * Adds a header.
         *
         * @param name
         *            header name
         * @param value
         *            header value
         * @return this builder
         */
        public Builder addHeader(String name, String value) {
            Objects.requireNonNull(name, "name must not be null");
            Objects.requireNonNull(value, "value must not be null");
            this.headers.put(name.toLowerCase(), value); // Normalize to lowercase
            return this;
        }

        /**
         * Adds multiple headers.
         *
         * @param headers
         *            headers to add
         * @return this builder
         */
        public Builder headers(Map<String, String> headers) {
            Objects.requireNonNull(headers, "headers must not be null");
            headers.forEach(this::addHeader);
            return this;
        }

        /**
         * Sets the request body.
         *
         * @param body
         *            request body
         * @return this builder
         */
        public Builder body(String body) {
            this.body = Optional.ofNullable(body);
            return this;
        }

        /**
         * Builds the signature components.
         *
         * @return signature components
         * @throws NullPointerException
         *             if method or targetUri is null
         * @throws IllegalArgumentException
         *             if method or targetUri is blank
         */
        public SignatureComponents build() {
            return new SignatureComponents(this);
        }
    }

    @Override
    public String toString() {
        return "SignatureComponents{" + "method='" + method + '\'' + ", targetUri='" + targetUri + '\'' + ", headers="
                + headers.size() + ", hasBody=" + body.isPresent() + '}';
    }
}
