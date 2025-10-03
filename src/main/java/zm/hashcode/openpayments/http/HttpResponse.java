package zm.hashcode.openpayments.http;

import java.util.Map;
import java.util.Optional;

/**
 * Represents an HTTP response from the Open Payments API.
 *
 * <p>
 * This is an immutable representation of an HTTP response with status code, headers, and optional body.
 *
 * @param statusCode
 *            the HTTP status code
 * @param headers
 *            the response headers
 * @param body
 *            the response body (optional)
 */
public record HttpResponse(int statusCode, Map<String, String> headers, String body) {

    public HttpResponse {
        headers = Map.copyOf(headers);
    }

    /**
     * Returns whether the response indicates success (2xx status code).
     *
     * @return true if the status code is in the 2xx range
     */
    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }

    /**
     * Returns whether the response indicates a client error (4xx status code).
     *
     * @return true if the status code is in the 4xx range
     */
    public boolean isClientError() {
        return statusCode >= 400 && statusCode < 500;
    }

    /**
     * Returns whether the response indicates a server error (5xx status code).
     *
     * @return true if the status code is in the 5xx range
     */
    public boolean isServerError() {
        return statusCode >= 500 && statusCode < 600;
    }

    /**
     * Returns the response body, if present.
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

    public static HttpResponse of(int statusCode, Map<String, String> headers, String body) {
        return new HttpResponse(statusCode, headers, body);
    }
}
