package zm.hashcode.openpayments.http.core;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents an HTTP request in the Open Payments SDK.
 *
 * <p>
 * This is an immutable representation of an outgoing HTTP request with method, URL, headers, and optional body.
 *
 * @param method
 *            the HTTP method
 * @param uri
 *            the request URI
 * @param headers
 *            the request headers
 * @param body
 *            the request body (optional)
 */
public record HttpRequest(HttpMethod method, URI uri, Map<String, String> headers, String body) {

    public HttpRequest {
        Objects.requireNonNull(method, "method must not be null");
        Objects.requireNonNull(uri, "uri must not be null");
        headers = Map.copyOf(headers);
    }

    /**
     * Returns the request body, if present.
     *
     * @return an Optional containing the body
     */
    public Optional<String> getBody() {
        return Optional.ofNullable(body);
    }

    /**
     * Returns the value of a header, if present.
     *
     * @param name
     *            the header name
     * @return an Optional containing the header value
     */
    public Optional<String> getHeader(String name) {
        return Optional.ofNullable(headers.get(name));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private HttpMethod method;
        private URI uri;
        private Map<String, String> headers = Map.of();
        private String body;

        private Builder() {
        }

        public Builder method(HttpMethod method) {
            this.method = method;
            return this;
        }

        public Builder uri(URI uri) {
            this.uri = uri;
            return this;
        }

        public Builder uri(String uri) {
            this.uri = URI.create(uri);
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder header(String name, String value) {
            var mutableHeaders = new java.util.HashMap<>(this.headers);
            mutableHeaders.put(name, value);
            this.headers = mutableHeaders;
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public HttpRequest build() {
            return new HttpRequest(method, uri, headers, body);
        }
    }
}
