package zm.hashcode.openpayments.http.resilience;

import java.time.Duration;
import java.util.Objects;
import java.util.Set;

/**
 * Configuration for HTTP client resilience features including retries and circuit breaker.
 *
 * <p>
 * This immutable configuration class defines retry behavior, circuit breaker settings, and conditions for when retries
 * should occur.
 *
 * <p>
 * Use the builder pattern to construct instances:
 *
 * <pre>{@code
 * var config = ResilienceConfig.builder().maxRetries(3)
 *         .retryStrategy(RetryStrategy.exponentialBackoff(Duration.ofMillis(100)))
 *         .retryOnStatusCodes(Set.of(408, 429, 500, 502, 503, 504)).build();
 * }</pre>
 *
 * @param maxRetries
 *            maximum number of retry attempts
 * @param retryStrategy
 *            strategy for calculating retry delays
 * @param maxRetryDelay
 *            maximum delay between retries
 * @param retryOnStatusCodes
 *            HTTP status codes that trigger retries
 * @param circuitBreakerEnabled
 *            whether circuit breaker is enabled
 * @param circuitBreakerThreshold
 *            number of failures before opening circuit
 * @param circuitBreakerTimeout
 *            duration circuit stays open before trying again
 * @param circuitBreakerHalfOpenRequests
 *            number of test requests in half-open state
 */
public record ResilienceConfig(int maxRetries, RetryStrategy retryStrategy, Duration maxRetryDelay,
        Set<Integer> retryOnStatusCodes, boolean circuitBreakerEnabled, int circuitBreakerThreshold,
        Duration circuitBreakerTimeout, int circuitBreakerHalfOpenRequests) {

    /**
     * Default maximum retries (3).
     */
    public static final int DEFAULT_MAX_RETRIES = 3;

    /**
     * Default maximum retry delay (30 seconds).
     */
    public static final Duration DEFAULT_MAX_RETRY_DELAY = Duration.ofSeconds(30);

    /**
     * Default retryable status codes (408, 429, 500, 502, 503, 504).
     */
    public static final Set<Integer> DEFAULT_RETRY_STATUS_CODES = Set.of(408, 429, 500, 502, 503, 504);

    /**
     * Default circuit breaker enabled (true).
     */
    public static final boolean DEFAULT_CIRCUIT_BREAKER_ENABLED = true;

    /**
     * Default circuit breaker failure threshold (5).
     */
    public static final int DEFAULT_CIRCUIT_BREAKER_THRESHOLD = 5;

    /**
     * Default circuit breaker timeout (1 minute).
     */
    public static final Duration DEFAULT_CIRCUIT_BREAKER_TIMEOUT = Duration.ofMinutes(1);

    /**
     * Default circuit breaker half-open requests (3).
     */
    public static final int DEFAULT_CIRCUIT_BREAKER_HALF_OPEN_REQUESTS = 3;

    public ResilienceConfig {
        Objects.requireNonNull(retryStrategy, "retryStrategy must not be null");
        Objects.requireNonNull(maxRetryDelay, "maxRetryDelay must not be null");
        Objects.requireNonNull(retryOnStatusCodes, "retryOnStatusCodes must not be null");
        Objects.requireNonNull(circuitBreakerTimeout, "circuitBreakerTimeout must not be null");

        if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries must be non-negative");
        }
        if (circuitBreakerThreshold <= 0) {
            throw new IllegalArgumentException("circuitBreakerThreshold must be positive");
        }
        if (circuitBreakerHalfOpenRequests <= 0) {
            throw new IllegalArgumentException("circuitBreakerHalfOpenRequests must be positive");
        }

        retryOnStatusCodes = Set.copyOf(retryOnStatusCodes);
    }

    /**
     * Returns whether retries are enabled (maxRetries > 0).
     *
     * @return true if retries are enabled
     */
    public boolean isRetryEnabled() {
        return maxRetries > 0;
    }

    /**
     * Returns whether the given status code should trigger a retry.
     *
     * @param statusCode
     *            the HTTP status code
     * @return true if the status code is retryable
     */
    public boolean isRetryableStatusCode(int statusCode) {
        return retryOnStatusCodes.contains(statusCode);
    }

    /**
     * Creates a new builder for constructing ResilienceConfig instances.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a default ResilienceConfig with exponential backoff retry strategy.
     *
     * @return a default ResilienceConfig instance
     */
    public static ResilienceConfig defaultConfig() {
        return builder().build();
    }

    /**
     * Creates a ResilienceConfig with retries disabled.
     *
     * @return a ResilienceConfig with no retries
     */
    public static ResilienceConfig noRetry() {
        return builder().maxRetries(0).build();
    }

    /**
     * Builder for constructing ResilienceConfig instances.
     */
    public static final class Builder {
        private int maxRetries = DEFAULT_MAX_RETRIES;
        private RetryStrategy retryStrategy = RetryStrategy.exponentialBackoff(Duration.ofMillis(100));
        private Duration maxRetryDelay = DEFAULT_MAX_RETRY_DELAY;
        private Set<Integer> retryOnStatusCodes = DEFAULT_RETRY_STATUS_CODES;
        private boolean circuitBreakerEnabled = DEFAULT_CIRCUIT_BREAKER_ENABLED;
        private int circuitBreakerThreshold = DEFAULT_CIRCUIT_BREAKER_THRESHOLD;
        private Duration circuitBreakerTimeout = DEFAULT_CIRCUIT_BREAKER_TIMEOUT;
        private int circuitBreakerHalfOpenRequests = DEFAULT_CIRCUIT_BREAKER_HALF_OPEN_REQUESTS;

        private Builder() {
        }

        /**
         * Sets the maximum number of retry attempts.
         *
         * @param maxRetries
         *            the maximum retries (0 to disable)
         * @return this builder
         */
        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        /**
         * Sets the retry strategy for calculating delays.
         *
         * @param retryStrategy
         *            the retry strategy
         * @return this builder
         */
        public Builder retryStrategy(RetryStrategy retryStrategy) {
            this.retryStrategy = retryStrategy;
            return this;
        }

        /**
         * Sets the maximum delay between retries.
         *
         * @param maxRetryDelay
         *            the maximum retry delay
         * @return this builder
         */
        public Builder maxRetryDelay(Duration maxRetryDelay) {
            this.maxRetryDelay = maxRetryDelay;
            return this;
        }

        /**
         * Sets the HTTP status codes that trigger retries.
         *
         * @param statusCodes
         *            the retryable status codes
         * @return this builder
         */
        public Builder retryOnStatusCodes(Set<Integer> statusCodes) {
            this.retryOnStatusCodes = statusCodes;
            return this;
        }

        /**
         * Adds HTTP status codes that trigger retries.
         *
         * @param statusCodes
         *            the status codes to add
         * @return this builder
         */
        public Builder addRetryStatusCodes(Integer... statusCodes) {
            var mutableCodes = new java.util.HashSet<>(this.retryOnStatusCodes);
            mutableCodes.addAll(java.util.Arrays.asList(statusCodes));
            this.retryOnStatusCodes = mutableCodes;
            return this;
        }

        /**
         * Sets whether circuit breaker is enabled.
         *
         * @param enabled
         *            true to enable circuit breaker
         * @return this builder
         */
        public Builder circuitBreakerEnabled(boolean enabled) {
            this.circuitBreakerEnabled = enabled;
            return this;
        }

        /**
         * Sets the circuit breaker failure threshold.
         *
         * @param threshold
         *            number of failures before opening circuit
         * @return this builder
         */
        public Builder circuitBreakerThreshold(int threshold) {
            this.circuitBreakerThreshold = threshold;
            return this;
        }

        /**
         * Sets the circuit breaker timeout.
         *
         * @param timeout
         *            duration circuit stays open
         * @return this builder
         */
        public Builder circuitBreakerTimeout(Duration timeout) {
            this.circuitBreakerTimeout = timeout;
            return this;
        }

        /**
         * Sets the number of test requests in half-open state.
         *
         * @param requests
         *            number of half-open requests
         * @return this builder
         */
        public Builder circuitBreakerHalfOpenRequests(int requests) {
            this.circuitBreakerHalfOpenRequests = requests;
            return this;
        }

        /**
         * Builds the ResilienceConfig instance.
         *
         * @return a new ResilienceConfig instance
         * @throws IllegalArgumentException
         *             if configuration is invalid
         */
        public ResilienceConfig build() {
            return new ResilienceConfig(maxRetries, retryStrategy, maxRetryDelay, retryOnStatusCodes,
                    circuitBreakerEnabled, circuitBreakerThreshold, circuitBreakerTimeout,
                    circuitBreakerHalfOpenRequests);
        }
    }
}
