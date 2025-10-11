package zm.hashcode.openpayments.http.resilience;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Strategy for calculating retry delays.
 *
 * <p>
 * Retry strategies determine how long to wait between retry attempts. Common strategies include:
 * <ul>
 * <li><b>Fixed delay</b>: Same delay between all retries</li>
 * <li><b>Linear backoff</b>: Linearly increasing delay</li>
 * <li><b>Exponential backoff</b>: Exponentially increasing delay (recommended)</li>
 * </ul>
 *
 * <p>
 * All strategies support optional jitter to prevent thundering herd effects when multiple clients retry simultaneously.
 */
@FunctionalInterface
public interface RetryStrategy {

    /**
     * Calculates the delay before the next retry attempt.
     *
     * @param attempt
     *            the retry attempt number (1 for first retry, 2 for second, etc.)
     * @return the duration to wait before retrying
     */
    Duration calculateDelay(int attempt);

    /**
     * Creates a fixed delay retry strategy.
     *
     * <p>
     * Returns the same delay for all retry attempts.
     *
     * @param delay
     *            the fixed delay between retries
     * @return a fixed delay retry strategy
     */
    static RetryStrategy fixedDelay(Duration delay) {
        Objects.requireNonNull(delay, "delay must not be null");
        return attempt -> delay;
    }

    /**
     * Creates a linear backoff retry strategy.
     *
     * <p>
     * The delay increases linearly: baseDelay, 2*baseDelay, 3*baseDelay, etc.
     *
     * @param baseDelay
     *            the base delay multiplied by attempt number
     * @return a linear backoff retry strategy
     */
    static RetryStrategy linearBackoff(Duration baseDelay) {
        Objects.requireNonNull(baseDelay, "baseDelay must not be null");
        return attempt -> baseDelay.multipliedBy(attempt);
    }

    /**
     * Creates an exponential backoff retry strategy.
     *
     * <p>
     * The delay increases exponentially: baseDelay, 2*baseDelay, 4*baseDelay, 8*baseDelay, etc.
     *
     * <p>
     * This is the recommended strategy for most use cases as it provides a good balance between aggressive retries
     * (early attempts) and backing off to avoid overwhelming the server.
     *
     * @param baseDelay
     *            the base delay doubled for each attempt
     * @return an exponential backoff retry strategy
     */
    static RetryStrategy exponentialBackoff(Duration baseDelay) {
        Objects.requireNonNull(baseDelay, "baseDelay must not be null");
        return attempt -> {
            long multiplier = 1L << (attempt - 1); // 2^(attempt-1)
            return baseDelay.multipliedBy(multiplier);
        };
    }

    /**
     * Creates a retry strategy with full jitter.
     *
     * <p>
     * Jitter adds randomness to retry delays to prevent multiple clients from retrying simultaneously (thundering
     * herd). Full jitter randomizes the delay between 0 and the calculated delay.
     *
     * <p>
     * Example with exponential backoff and jitter:
     *
     * <pre>{@code
     * var strategy = RetryStrategy.exponentialBackoff(Duration.ofMillis(100)).withFullJitter();
     * }</pre>
     *
     * @return a new retry strategy with full jitter applied
     */
    default RetryStrategy withFullJitter() {
        return attempt -> {
            Duration baseDelay = calculateDelay(attempt);
            long delayMillis = baseDelay.toMillis();
            if (delayMillis <= 0) {
                return baseDelay;
            }
            long randomMillis = ThreadLocalRandom.current().nextLong(0, delayMillis + 1);
            return Duration.ofMillis(randomMillis);
        };
    }

    /**
     * Creates a retry strategy with equal jitter.
     *
     * <p>
     * Equal jitter splits the delay in half and adds randomness to the second half. This ensures a minimum delay while
     * still preventing thundering herd effects.
     *
     * <p>
     * Formula: delay/2 + random(0, delay/2)
     *
     * @return a new retry strategy with equal jitter applied
     */
    default RetryStrategy withEqualJitter() {
        return attempt -> {
            Duration baseDelay = calculateDelay(attempt);
            long delayMillis = baseDelay.toMillis();
            if (delayMillis <= 0) {
                return baseDelay;
            }
            long halfDelay = delayMillis / 2;
            long randomMillis = ThreadLocalRandom.current().nextLong(0, halfDelay + 1);
            return Duration.ofMillis(halfDelay + randomMillis);
        };
    }

    /**
     * Creates a retry strategy with decorrelated jitter.
     *
     * <p>
     * Decorrelated jitter uses the previous delay as input for calculating the next delay, creating a smooth
     * distribution of retry times. This is AWS's recommended jitter strategy.
     *
     * <p>
     * Formula: random(baseDelay, previousDelay * 3)
     *
     * @param baseDelay
     *            the minimum delay between retries
     * @return a new retry strategy with decorrelated jitter
     */
    static RetryStrategy decorrelatedJitter(Duration baseDelay) {
        Objects.requireNonNull(baseDelay, "baseDelay must not be null");
        return new DecorrelatedJitterStrategy(baseDelay);
    }

    /**
     * Internal implementation of decorrelated jitter that maintains state.
     */
    final class DecorrelatedJitterStrategy implements RetryStrategy {
        private final Duration baseDelay;
        private volatile Duration previousDelay;

        private DecorrelatedJitterStrategy(Duration baseDelay) {
            this.baseDelay = baseDelay;
            this.previousDelay = baseDelay;
        }

        @Override
        public Duration calculateDelay(int attempt) {
            long baseMillis = baseDelay.toMillis();
            long previousMillis = previousDelay.toMillis();
            long maxMillis = previousMillis * 3;
            long randomMillis = ThreadLocalRandom.current().nextLong(baseMillis, maxMillis + 1);
            previousDelay = Duration.ofMillis(randomMillis);
            return previousDelay;
        }
    }
}
