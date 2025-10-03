package zm.hashcode.openpayments.util;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for building URLs with query parameters.
 *
 * <p>
 * This class provides a fluent API for constructing URLs with properly encoded query parameters.
 */
public final class UrlBuilder {

    private final String baseUrl;
    private final Map<String, String> queryParams;

    private UrlBuilder(String baseUrl) {
        this.baseUrl = baseUrl;
        this.queryParams = new LinkedHashMap<>();
    }

    /**
     * Creates a new UrlBuilder for the specified base URL.
     *
     * @param baseUrl
     *            the base URL
     * @return a new UrlBuilder instance
     */
    public static UrlBuilder of(String baseUrl) {
        return new UrlBuilder(baseUrl);
    }

    /**
     * Creates a new UrlBuilder from a URI.
     *
     * @param uri
     *            the base URI
     * @return a new UrlBuilder instance
     */
    public static UrlBuilder of(URI uri) {
        return new UrlBuilder(uri.toString());
    }

    /**
     * Adds a query parameter.
     *
     * @param name
     *            the parameter name
     * @param value
     *            the parameter value
     * @return this builder
     */
    public UrlBuilder queryParam(String name, String value) {
        if (value != null) {
            queryParams.put(name, value);
        }
        return this;
    }

    /**
     * Adds a query parameter with an integer value.
     *
     * @param name
     *            the parameter name
     * @param value
     *            the parameter value
     * @return this builder
     */
    public UrlBuilder queryParam(String name, int value) {
        queryParams.put(name, String.valueOf(value));
        return this;
    }

    /**
     * Adds a query parameter with a long value.
     *
     * @param name
     *            the parameter name
     * @param value
     *            the parameter value
     * @return this builder
     */
    public UrlBuilder queryParam(String name, long value) {
        queryParams.put(name, String.valueOf(value));
        return this;
    }

    /**
     * Adds multiple query parameters.
     *
     * @param params
     *            the parameters to add
     * @return this builder
     */
    public UrlBuilder queryParams(Map<String, String> params) {
        params.forEach(this::queryParam);
        return this;
    }

    /**
     * Builds the URL as a string.
     *
     * @return the complete URL with encoded query parameters
     */
    public String build() {
        if (queryParams.isEmpty()) {
            return baseUrl;
        }

        var queryString = queryParams.entrySet().stream()
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue())).collect(Collectors.joining("&"));

        return baseUrl + "?" + queryString;
    }

    /**
     * Builds the URL as a URI.
     *
     * @return the complete URI with encoded query parameters
     */
    public URI toUri() {
        return URI.create(build());
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
