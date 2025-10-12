/**
 * HTTP request and response interceptors for Open Payments client.
 *
 * <p>
 * This package provides interceptor implementations for cross-cutting concerns in HTTP communication:
 * <ul>
 * <li><b>Logging</b> - Request and response logging with configurable verbosity</li>
 * <li><b>Authentication</b> - Automatic authentication header injection</li>
 * <li><b>Error Handling</b> - Structured error extraction and logging</li>
 * </ul>
 *
 * <h2>Interceptor Types</h2>
 *
 * <h3>Request Interceptors</h3>
 * <p>
 * Process and modify outgoing HTTP requests:
 *
 * <ul>
 * <li>{@link zm.hashcode.openpayments.http.interceptor.LoggingRequestInterceptor} - Logs requests with sensitive header
 * masking</li>
 * <li>{@link zm.hashcode.openpayments.http.interceptor.AuthenticationInterceptor} - Adds authentication headers</li>
 * </ul>
 *
 * <h3>Response Interceptors</h3>
 * <p>
 * Process incoming HTTP responses:
 *
 * <ul>
 * <li>{@link zm.hashcode.openpayments.http.interceptor.LoggingResponseInterceptor} - Logs responses with different
 * levels for success/error</li>
 * <li>{@link zm.hashcode.openpayments.http.interceptor.ErrorHandlingInterceptor} - Extracts structured error
 * information</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Adding Logging</h3>
 *
 * <pre>{@code
 * HttpClient client = new ApacheHttpClient(config);
 *
 * // Add request logging
 * client.addRequestInterceptor(new LoggingRequestInterceptor());
 *
 * // Add response logging with custom configuration
 * client.addResponseInterceptor(new LoggingResponseInterceptor(Level.FINE, // Log successful responses at FINE level
 *         Level.SEVERE, // Log errors at SEVERE level
 *         true, // Include headers
 *         false // Don't include body
 * ));
 * }</pre>
 *
 * <h3>Adding Authentication</h3>
 *
 * <pre>{@code
 * // Bearer token authentication (OAuth 2.0, JWT)
 * client.addRequestInterceptor(AuthenticationInterceptor.bearer("my-access-token"));
 *
 * // GNAP token authentication (Open Payments)
 * client.addRequestInterceptor(AuthenticationInterceptor.gnap("gnap-token"));
 *
 * // Basic authentication
 * client.addRequestInterceptor(AuthenticationInterceptor.basic("dXNlcjpwYXNz"));
 *
 * // Custom authentication scheme
 * client.addRequestInterceptor(AuthenticationInterceptor.custom("ApiKey", "sk_live_123"));
 * }</pre>
 *
 * <h3>Adding Error Handling</h3>
 *
 * <pre>{@code
 * ObjectMapper objectMapper = new ObjectMapper();
 * client.addResponseInterceptor(new ErrorHandlingInterceptor(objectMapper));
 *
 * // Errors are automatically logged and structured information is extracted
 * // from JSON error responses
 * }</pre>
 *
 * <h3>Combining Multiple Interceptors</h3>
 *
 * <pre>{@code
 * HttpClient client = new ApacheHttpClient(config);
 *
 * // Interceptors execute in the order they are added
 * client.addRequestInterceptor(new LoggingRequestInterceptor());
 * client.addRequestInterceptor(AuthenticationInterceptor.bearer(token));
 *
 * client.addResponseInterceptor(new ErrorHandlingInterceptor(objectMapper));
 * client.addResponseInterceptor(new LoggingResponseInterceptor());
 * }</pre>
 *
 * <h2>Custom Interceptors</h2>
 *
 * <p>
 * You can create custom interceptors by implementing the functional interfaces:
 *
 * <pre>{@code
 * // Custom request interceptor
 * RequestInterceptor customRequest = request -> {
 *     // Modify request (e.g., add headers, modify URL)
 *     Map<String, String> newHeaders = new HashMap<>(request.headers());
 *     newHeaders.put("X-Custom-Header", "value");
 *
 *     return HttpRequest.builder().method(request.method()).uri(request.uri()).headers(newHeaders)
 *             .body(request.getBody().orElse(null)).build();
 * };
 *
 * // Custom response interceptor
 * ResponseInterceptor customResponse = response -> {
 *     // Process response (e.g., extract metrics, cache)
 *     if (response.statusCode() == 429) {
 *         // Handle rate limiting
 *     }
 *     return response;
 * };
 * }</pre>
 *
 * <h2>Security Considerations</h2>
 *
 * <ul>
 * <li>Logging interceptors automatically mask sensitive headers (Authorization, tokens, keys)</li>
 * <li>Be cautious when logging request/response bodies - they may contain sensitive data</li>
 * <li>Consider log levels carefully in production (use FINE/DEBUG for verbose logging)</li>
 * <li>Authentication interceptors handle credentials - ensure secure storage</li>
 * </ul>
 *
 * @see zm.hashcode.openpayments.http.core.HttpClient
 * @see zm.hashcode.openpayments.http.interceptor.RequestInterceptor
 * @see zm.hashcode.openpayments.http.interceptor.ResponseInterceptor
 */
package zm.hashcode.openpayments.http.interceptor;
