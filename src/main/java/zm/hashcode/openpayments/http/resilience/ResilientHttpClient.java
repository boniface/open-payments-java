package zm.hashcode.openpayments.http.resilience;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import zm.hashcode.openpayments.http.core.HttpClient;
import zm.hashcode.openpayments.http.core.HttpRequest;
import zm.hashcode.openpayments.http.core.HttpResponse;
import zm.hashcode.openpayments.http.interceptor.RequestInterceptor;
import zm.hashcode.openpayments.http.interceptor.ResponseInterceptor;

/**
 * Resilient HTTP client decorator that adds retry logic and circuit breaker pattern.
 *
 * <p>
 * This decorator wraps any {@link HttpClient} implementation and provides:
 * <ul>
 * <li><b>Automatic retries</b>: Configurable retry attempts with backoff strategies</li>
 * <li><b>Circuit breaker</b>: Prevents cascading failures by failing fast when service is down</li>
 * <li><b>Configurable retry conditions</b>: Retry based on status codes or exceptions</li>
 * </ul>
 *
 * <p>
 * The circuit breaker has three states:
 * <ul>
 * <li><b>CLOSED</b>: Normal operation, requests pass through</li>
 * <li><b>OPEN</b>: Too many failures detected, requests fail immediately</li>
 * <li><b>HALF_OPEN</b>: Testing if service has recovered with limited requests</li>
 * </ul>
 *
 * <p>
 * This implementation uses virtual threads (Java 21+) for efficient retry delays without blocking platform threads.
 *
 * <p>
 * Example usage:
 *
 * <pre>{@code
 * HttpClient baseClient = new ApacheHttpClient(clientConfig);
 * HttpClient resilientClient = new ResilientHttpClient(baseClient, resilienceConfig);
 * }</pre>
 */
public final class ResilientHttpClient implements HttpClient {

    private static final Logger LOGGER = Logger.getLogger(ResilientHttpClient.class.getName());

    private final HttpClient delegate;
    private final ResilienceConfig config;
    private final CircuitBreaker circuitBreaker;

    /**
     * Creates a new resilient HTTP client.
     *
     * @param delegate
     *            the underlying HTTP client to wrap
     * @param config
     *            resilience configuration
     */
    public ResilientHttpClient(HttpClient delegate, ResilienceConfig config) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.circuitBreaker = config.circuitBreakerEnabled() ? new CircuitBreaker(config) : null;
    }

    @Override
    public CompletableFuture<HttpResponse> execute(HttpRequest request) {
        // Check circuit breaker before attempting request
        if (circuitBreaker != null && !circuitBreaker.allowRequest()) {
            LOGGER.log(Level.WARNING, "Circuit breaker is OPEN, rejecting request to {0}", request.uri());
            return CompletableFuture
                    .failedFuture(new CircuitBreakerOpenException("Circuit breaker is OPEN for " + request.uri()));
        }

        // Execute with retries
        return executeWithRetry(request, 0);
    }

    private CompletableFuture<HttpResponse> executeWithRetry(HttpRequest request, int attemptNumber) {
        return delegate.execute(request).handle((response, throwable) -> {
            if (throwable != null) {
                // Request failed with exception
                recordFailure();
                return handleFailure(request, attemptNumber, throwable, null);
            }

            // Check if response should trigger retry
            if (shouldRetry(response, attemptNumber)) {
                LOGGER.log(Level.INFO, "Retrying request to {0} due to status code {1}, attempt {2}/{3}",
                        new Object[]{request.uri(), response.statusCode(), attemptNumber + 1, config.maxRetries()});
                recordFailure();
                return handleFailure(request, attemptNumber, null, response);
            }

            // Success
            recordSuccess();
            return CompletableFuture.completedFuture(response);
        }).thenCompose(future -> future);
    }

    private CompletableFuture<HttpResponse> handleFailure(HttpRequest request, int attemptNumber, Throwable throwable,
            HttpResponse response) {
        if (attemptNumber >= config.maxRetries()) {
            // Exhausted retries
            if (throwable != null) {
                return CompletableFuture.failedFuture(throwable);
            }
            return CompletableFuture.completedFuture(response);
        }

        // Calculate delay and retry
        Duration delay = calculateRetryDelay(attemptNumber + 1);
        LOGGER.log(Level.FINE, "Waiting {0}ms before retry attempt {1}",
                new Object[]{delay.toMillis(), attemptNumber + 2});

        return delayAsync(delay).thenCompose(v -> executeWithRetry(request, attemptNumber + 1));
    }

    private boolean shouldRetry(HttpResponse response, int attemptNumber) {
        return attemptNumber < config.maxRetries() && config.isRetryableStatusCode(response.statusCode());
    }

    private Duration calculateRetryDelay(int attempt) {
        Duration delay = config.retryStrategy().calculateDelay(attempt);
        // Cap delay at max configured delay
        if (delay.compareTo(config.maxRetryDelay()) > 0) {
            return config.maxRetryDelay();
        }
        return delay;
    }

    /**
     * Creates a CompletableFuture that completes after the specified delay.
     *
     * <p>
     * Uses virtual threads for efficient non-blocking delays.
     *
     * @param delay
     *            the delay duration
     * @return a CompletableFuture that completes after the delay
     */
    private CompletableFuture<Void> delayAsync(Duration delay) {
        return CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(delay.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Retry delay interrupted", e);
            }
        }, task -> Thread.ofVirtual().start(task));
    }

    private void recordSuccess() {
        if (circuitBreaker != null) {
            circuitBreaker.recordSuccess();
        }
    }

    private void recordFailure() {
        if (circuitBreaker != null) {
            circuitBreaker.recordFailure();
        }
    }

    @Override
    public void addRequestInterceptor(RequestInterceptor interceptor) {
        delegate.addRequestInterceptor(interceptor);
    }

    @Override
    public void addResponseInterceptor(ResponseInterceptor interceptor) {
        delegate.addResponseInterceptor(interceptor);
    }

    @Override
    public void close() {
        delegate.close();
    }

    /**
     * Circuit breaker implementation using atomic operations for thread safety.
     *
     * <p>
     * Thread-safe without explicit locking, suitable for high-concurrency scenarios.
     */
    private static final class CircuitBreaker {

        private enum State {
            CLOSED, OPEN, HALF_OPEN
        }

        private final ResilienceConfig config;
        private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
        private final AtomicInteger failureCount = new AtomicInteger(0);
        private final AtomicInteger successCount = new AtomicInteger(0);
        private final AtomicInteger halfOpenRequests = new AtomicInteger(0);
        private final AtomicLong lastFailureTime = new AtomicLong(0);

        private CircuitBreaker(ResilienceConfig config) {
            this.config = config;
        }

        boolean allowRequest() {
            State currentState = state.get();

            return switch (currentState) {
                case CLOSED -> true;
                case OPEN -> {
                    // Check if timeout has elapsed
                    long lastFailure = lastFailureTime.get();
                    long timeoutMillis = config.circuitBreakerTimeout().toMillis();
                    if (System.currentTimeMillis() - lastFailure >= timeoutMillis) {
                        // Transition to half-open
                        if (state.compareAndSet(State.OPEN, State.HALF_OPEN)) {
                            LOGGER.log(Level.INFO, "Circuit breaker transitioning to HALF_OPEN");
                            halfOpenRequests.set(0);
                            successCount.set(0);
                        }
                        yield true;
                    }
                    yield false;
                }
                case HALF_OPEN -> {
                    // Allow limited requests in half-open state
                    int current = halfOpenRequests.get();
                    if (current < config.circuitBreakerHalfOpenRequests()) {
                        halfOpenRequests.incrementAndGet();
                        yield true;
                    }
                    yield false;
                }
            };
        }

        void recordSuccess() {
            State currentState = state.get();

            if (currentState == State.HALF_OPEN) {
                int successes = successCount.incrementAndGet();
                // If all half-open requests succeeded, close the circuit
                if (successes >= config.circuitBreakerHalfOpenRequests()) {
                    if (state.compareAndSet(State.HALF_OPEN, State.CLOSED)) {
                        LOGGER.log(Level.INFO, "Circuit breaker transitioning to CLOSED");
                        failureCount.set(0);
                        successCount.set(0);
                        halfOpenRequests.set(0);
                    }
                }
            } else if (currentState == State.CLOSED) {
                // Reset failure count on success
                failureCount.set(0);
            }
        }

        void recordFailure() {
            lastFailureTime.set(System.currentTimeMillis());
            State currentState = state.get();

            if (currentState == State.HALF_OPEN) {
                // Any failure in half-open state reopens the circuit
                if (state.compareAndSet(State.HALF_OPEN, State.OPEN)) {
                    LOGGER.log(Level.WARNING, "Circuit breaker transitioning to OPEN (failure in half-open state)");
                    halfOpenRequests.set(0);
                    successCount.set(0);
                }
            } else if (currentState == State.CLOSED) {
                int failures = failureCount.incrementAndGet();
                if (failures >= config.circuitBreakerThreshold()) {
                    if (state.compareAndSet(State.CLOSED, State.OPEN)) {
                        LOGGER.log(Level.WARNING, "Circuit breaker transitioning to OPEN (threshold reached: {0})",
                                failures);
                    }
                }
            }
        }
    }

    /**
     * Exception thrown when circuit breaker is open and rejects requests.
     */
    public static final class CircuitBreakerOpenException extends RuntimeException {
        public CircuitBreakerOpenException(String message) {
            super(message);
        }
    }
}
