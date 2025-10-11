package zm.hashcode.openpayments.http.config;

/**
 * Enumeration of available HTTP client implementations.
 *
 * <p>
 * This enum provides a type-safe way to select which underlying HTTP library to use. Each implementation has different
 * characteristics and may be better suited for different use cases.
 *
 * <h2>Implementation Characteristics</h2>
 *
 * <table border="1">
 * <tr>
 * <th>Implementation</th>
 * <th>Best For</th>
 * <th>Pros</th>
 * <th>Cons</th>
 * </tr>
 * <tr>
 * <td>{@link #APACHE}</td>
 * <td>Enterprise applications, production servers</td>
 * <td>Mature, feature-rich, excellent HTTP/2 support, virtual threads</td>
 * <td>Larger dependency size (~500KB)</td>
 * </tr>
 * <tr>
 * <td>{@link #OKHTTP}</td>
 * <td>Android apps, mobile, lightweight clients</td>
 * <td>Lightweight, fast, excellent for Android, built-in caching</td>
 * <td>Less enterprise features than Apache</td>
 * </tr>
 * <tr>
 * <td>{@link #AUTO}</td>
 * <td>When you don't care about the implementation</td>
 * <td>Automatic selection based on classpath</td>
 * <td>Less predictable</td>
 * </tr>
 * </table>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Explicit Selection</h3>
 *
 * <pre>{@code
 * // Use Apache HttpClient
 * HttpClient client = HttpClientBuilder.create().baseUrl("https://api.example.com")
 *         .implementation(HttpClientImplementation.APACHE).build();
 *
 * // Use OkHttp
 * HttpClient client = HttpClientBuilder.create().baseUrl("https://api.example.com")
 *         .implementation(HttpClientImplementation.OKHTTP).build();
 * }</pre>
 *
 * <h3>Auto Selection</h3>
 *
 * <pre>{@code
 * // Automatically choose based on classpath
 * HttpClient client = HttpClientBuilder.create().baseUrl("https://api.example.com")
 *         .implementation(HttpClientImplementation.AUTO).build();
 * }</pre>
 *
 * @see HttpClientBuilder#implementation(HttpClientImplementation)
 */
public enum HttpClientImplementation {

    /**
     * Apache HttpClient 5 implementation.
     *
     * <p>
     * <b>Best for:</b> Enterprise applications, production servers, applications requiring advanced HTTP features.
     *
     * <p>
     * <b>Features:</b>
     * <ul>
     * <li>Mature and battle-tested (20+ years)</li>
     * <li>Full HTTP/1.1 and HTTP/2 support</li>
     * <li>Virtual threads support (Java 21+)</li>
     * <li>Advanced connection pooling</li>
     * <li>Extensive authentication mechanisms</li>
     * <li>Request/response interceptors</li>
     * </ul>
     *
     * <p>
     * <b>When to use:</b>
     * <ul>
     * <li>Production server applications</li>
     * <li>Applications requiring HTTP/2</li>
     * <li>When you need advanced HTTP features</li>
     * <li>Enterprise environments</li>
     * </ul>
     */
    APACHE("Apache HttpClient 5", "org.apache.hc.client5.http.impl.async.HttpAsyncClients"),

    /**
     * OkHttp implementation.
     *
     * <p>
     * <b>Best for:</b> Android applications, mobile apps, lightweight clients, memory-constrained environments.
     *
     * <p>
     * <b>Features:</b>
     * <ul>
     * <li>Lightweight and fast</li>
     * <li>Default HTTP client for Android</li>
     * <li>Built-in response caching</li>
     * <li>HTTP/2 and WebSocket support</li>
     * <li>Automatic GZIP compression</li>
     * <li>Connection pooling</li>
     * </ul>
     *
     * <p>
     * <b>When to use:</b>
     * <ul>
     * <li>Android applications</li>
     * <li>Mobile applications</li>
     * <li>Applications with strict memory constraints</li>
     * <li>When you need built-in caching</li>
     * <li>WebSocket requirements</li>
     * </ul>
     */
    OKHTTP("OkHttp", "okhttp3.OkHttpClient"),

    /**
     * Automatic selection based on classpath.
     *
     * <p>
     * The implementation is automatically selected in the following order:
     * <ol>
     * <li>Apache HttpClient (if available on classpath)</li>
     * <li>OkHttp (if available on classpath)</li>
     * <li>Throws exception if no implementation is available</li>
     * </ol>
     *
     * <p>
     * <b>When to use:</b>
     * <ul>
     * <li>When you don't care about the specific implementation</li>
     * <li>During prototyping or development</li>
     * <li>When you want to let the library choose the best available option</li>
     * </ul>
     *
     * <p>
     * <b>Note:</b> For production applications, it's recommended to explicitly specify the implementation for
     * predictability and to avoid surprises during deployment.
     */
    AUTO("Auto-detect", null);

    private final String displayName;
    private final String detectionClassName;

    HttpClientImplementation(String displayName, String detectionClassName) {
        this.displayName = displayName;
        this.detectionClassName = detectionClassName;
    }

    /**
     * Returns the display name of this implementation.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Checks if this implementation is available on the classpath.
     *
     * @return true if the implementation is available
     */
    public boolean isAvailable() {
        if (this == AUTO) {
            return APACHE.isAvailable() || OKHTTP.isAvailable();
        }

        if (detectionClassName == null) {
            return false;
        }

        try {
            Class.forName(detectionClassName);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Automatically detects the best available implementation.
     *
     * <p>
     * Selection priority:
     * <ol>
     * <li>Apache HttpClient</li>
     * <li>OkHttp</li>
     * </ol>
     *
     * @return the best available implementation
     * @throws IllegalStateException
     *             if no implementation is available
     */
    public static HttpClientImplementation detectBestAvailable() {
        if (APACHE.isAvailable()) {
            return APACHE;
        }
        if (OKHTTP.isAvailable()) {
            return OKHTTP;
        }

        throw new IllegalStateException("No HTTP client implementation found on classpath. "
                + "Please add either Apache HttpClient 5 " + "(org.apache.httpcomponents.client5:httpclient5) or "
                + "OkHttp " + "(com.squareup.okhttp3:okhttp) to your dependencies.");
    }

    /**
     * Returns a description of this implementation including availability status.
     *
     * @return a description string
     */
    public String describe() {
        if (this == AUTO) {
            return displayName + " (Best available: " + detectBestAvailable().displayName + ")";
        }
        return displayName + " (" + (isAvailable() ? "available" : "not available") + ")";
    }

    @Override
    public String toString() {
        return displayName;
    }
}
