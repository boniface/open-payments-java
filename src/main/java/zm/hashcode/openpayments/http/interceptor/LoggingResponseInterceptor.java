package zm.hashcode.openpayments.http.interceptor;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import zm.hashcode.openpayments.http.core.HttpResponse;

/**
 * Response interceptor that logs HTTP responses.
 *
 * <p>
 * This interceptor logs response details including status code, headers, and optionally the response body for debugging
 * and monitoring purposes.
 *
 * <p>
 * Example usage:
 *
 * <pre>{@code
 * HttpClient client = new ApacheHttpClient(config);
 * client.addResponseInterceptor(new LoggingResponseInterceptor());
 * }</pre>
 */
public final class LoggingResponseInterceptor implements ResponseInterceptor {

    private static final Logger LOGGER = Logger.getLogger(LoggingResponseInterceptor.class.getName());
    private static final int MAX_BODY_LOG_LENGTH = 1000;

    private final Level logLevel;
    private final Level errorLogLevel;
    private final boolean logHeaders;
    private final boolean logBody;

    /**
     * Creates a logging interceptor with INFO level for success and WARNING for errors.
     */
    public LoggingResponseInterceptor() {
        this(Level.INFO, Level.WARNING, true, false);
    }

    /**
     * Creates a logging interceptor with custom configuration.
     *
     * @param logLevel
     *            the log level for successful responses
     * @param errorLogLevel
     *            the log level for error responses (4xx, 5xx)
     * @param logHeaders
     *            whether to log response headers
     * @param logBody
     *            whether to log response body
     */
    public LoggingResponseInterceptor(Level logLevel, Level errorLogLevel, boolean logHeaders, boolean logBody) {
        this.logLevel = Objects.requireNonNull(logLevel, "logLevel must not be null");
        this.errorLogLevel = Objects.requireNonNull(errorLogLevel, "errorLogLevel must not be null");
        this.logHeaders = logHeaders;
        this.logBody = logBody;
    }

    @Override
    public HttpResponse intercept(HttpResponse response) {
        Level level = response.isSuccessful() ? logLevel : errorLogLevel;

        if (!LOGGER.isLoggable(level)) {
            return response;
        }

        StringBuilder logMessage = new StringBuilder(512);
        logMessage.append("HTTP Response: ").append(response.statusCode()).append(' ')
                .append(getStatusText(response.statusCode()));

        if (logHeaders && !response.headers().isEmpty()) {
            logMessage.append("\nHeaders: ");
            response.headers()
                    .forEach((key, value) -> logMessage.append("\n  ").append(key).append(": ").append(value));
        }

        if (logBody && !response.body().isEmpty()) {
            String body = response.body();
            if (body.length() > MAX_BODY_LOG_LENGTH) {
                logMessage.append("\nBody (truncated): ").append(body, 0, MAX_BODY_LOG_LENGTH).append("... (")
                        .append(body.length() - MAX_BODY_LOG_LENGTH).append(" more characters)");
            } else {
                logMessage.append("\nBody: ").append(body);
            }
        }

        LOGGER.log(level, logMessage.toString());

        return response;
    }

    private String getStatusText(int statusCode) {
        return switch (statusCode / 100) {
            case 2 -> "OK";
            case 3 -> "Redirect";
            case 4 -> "Client Error";
            case 5 -> "Server Error";
            default -> "Unknown";
        };
    }
}
