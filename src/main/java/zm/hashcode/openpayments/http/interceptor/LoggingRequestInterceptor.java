package zm.hashcode.openpayments.http.interceptor;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import zm.hashcode.openpayments.http.core.HttpRequest;

/**
 * Request interceptor that logs HTTP requests.
 *
 * <p>
 * This interceptor logs request details including method, URI, and headers for debugging and monitoring purposes.
 *
 * <p>
 * Example usage:
 *
 * <pre>{@code
 * HttpClient client = new ApacheHttpClient(config);
 * client.addRequestInterceptor(new LoggingRequestInterceptor());
 * }</pre>
 */
public final class LoggingRequestInterceptor implements RequestInterceptor {

    private static final Logger LOGGER = Logger.getLogger(LoggingRequestInterceptor.class.getName());

    private final Level logLevel;
    private final boolean logHeaders;
    private final boolean logBody;

    /**
     * Creates a logging interceptor with INFO level and headers logging enabled.
     */
    public LoggingRequestInterceptor() {
        this(Level.INFO, true, false);
    }

    /**
     * Creates a logging interceptor with custom configuration.
     *
     * @param logLevel
     *            the log level to use
     * @param logHeaders
     *            whether to log request headers
     * @param logBody
     *            whether to log request body
     */
    public LoggingRequestInterceptor(Level logLevel, boolean logHeaders, boolean logBody) {
        this.logLevel = Objects.requireNonNull(logLevel, "logLevel must not be null");
        this.logHeaders = logHeaders;
        this.logBody = logBody;
    }

    @Override
    public HttpRequest intercept(HttpRequest request) {
        if (!LOGGER.isLoggable(logLevel)) {
            return request;
        }

        StringBuilder logMessage = new StringBuilder(256);
        logMessage.append("HTTP Request: ").append(request.method().name()).append(' ').append(request.uri());

        if (logHeaders && !request.headers().isEmpty()) {
            logMessage.append("\nHeaders: ");
            request.headers().forEach((key, value) -> {
                // Mask sensitive headers
                String displayValue = isSensitiveHeader(key) ? "***REDACTED***" : value;
                logMessage.append("\n  ").append(key).append(": ").append(displayValue);
            });
        }

        if (logBody && request.getBody().isPresent()) {
            logMessage.append("\nBody: ").append(request.getBody().get());
        }

        LOGGER.log(logLevel, logMessage.toString());

        return request;
    }

    private boolean isSensitiveHeader(String headerName) {
        String lower = headerName.toLowerCase(java.util.Locale.ROOT);
        return lower.contains("authorization") || lower.contains("signature") || lower.contains("token")
                || lower.contains("key") || lower.contains("secret");
    }
}
