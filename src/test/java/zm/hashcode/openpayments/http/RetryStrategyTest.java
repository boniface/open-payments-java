package zm.hashcode.openpayments.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import zm.hashcode.openpayments.http.resilience.RetryStrategy;

class RetryStrategyTest {

    @Test
    void testFixedDelay() {
        Duration fixedDelay = Duration.ofMillis(500);
        RetryStrategy strategy = RetryStrategy.fixedDelay(fixedDelay);

        assertEquals(fixedDelay, strategy.calculateDelay(1));
        assertEquals(fixedDelay, strategy.calculateDelay(2));
        assertEquals(fixedDelay, strategy.calculateDelay(3));
        assertEquals(fixedDelay, strategy.calculateDelay(10));
    }

    @Test
    void testLinearBackoff() {
        Duration baseDelay = Duration.ofMillis(100);
        RetryStrategy strategy = RetryStrategy.linearBackoff(baseDelay);

        assertEquals(Duration.ofMillis(100), strategy.calculateDelay(1)); // 1 * 100ms
        assertEquals(Duration.ofMillis(200), strategy.calculateDelay(2)); // 2 * 100ms
        assertEquals(Duration.ofMillis(300), strategy.calculateDelay(3)); // 3 * 100ms
        assertEquals(Duration.ofMillis(1000), strategy.calculateDelay(10)); // 10 * 100ms
    }

    @Test
    void testExponentialBackoff() {
        Duration baseDelay = Duration.ofMillis(100);
        RetryStrategy strategy = RetryStrategy.exponentialBackoff(baseDelay);

        assertEquals(Duration.ofMillis(100), strategy.calculateDelay(1)); // 2^0 * 100ms = 100ms
        assertEquals(Duration.ofMillis(200), strategy.calculateDelay(2)); // 2^1 * 100ms = 200ms
        assertEquals(Duration.ofMillis(400), strategy.calculateDelay(3)); // 2^2 * 100ms = 400ms
        assertEquals(Duration.ofMillis(800), strategy.calculateDelay(4)); // 2^3 * 100ms = 800ms
        assertEquals(Duration.ofMillis(51200), strategy.calculateDelay(10)); // 2^9 * 100ms = 51200ms
    }

    @Test
    void testFullJitter() {
        Duration baseDelay = Duration.ofMillis(1000);
        RetryStrategy baseStrategy = RetryStrategy.fixedDelay(baseDelay);
        RetryStrategy jitterStrategy = baseStrategy.withFullJitter();

        // Full jitter should return a value between 0 and baseDelay
        for (int i = 0; i < 100; i++) {
            Duration delay = jitterStrategy.calculateDelay(1);
            assertTrue(delay.toMillis() >= 0);
            assertTrue(delay.toMillis() <= baseDelay.toMillis());
        }
    }

    @Test
    void testEqualJitter() {
        Duration baseDelay = Duration.ofMillis(1000);
        RetryStrategy baseStrategy = RetryStrategy.fixedDelay(baseDelay);
        RetryStrategy jitterStrategy = baseStrategy.withEqualJitter();

        // Equal jitter should return a value between baseDelay/2 and baseDelay
        for (int i = 0; i < 100; i++) {
            Duration delay = jitterStrategy.calculateDelay(1);
            long delayMillis = delay.toMillis();
            assertTrue(delayMillis >= baseDelay.toMillis() / 2);
            assertTrue(delayMillis <= baseDelay.toMillis());
        }
    }

    @Test
    void testDecorrelatedJitter() {
        Duration baseDelay = Duration.ofMillis(100);
        RetryStrategy strategy = RetryStrategy.decorrelatedJitter(baseDelay);

        // Decorrelated jitter should produce varying delays
        Duration delay1 = strategy.calculateDelay(1);
        Duration delay2 = strategy.calculateDelay(2);
        Duration delay3 = strategy.calculateDelay(3);

        // All delays should be at least the base delay
        assertTrue(delay1.toMillis() >= baseDelay.toMillis());
        assertTrue(delay2.toMillis() >= baseDelay.toMillis());
        assertTrue(delay3.toMillis() >= baseDelay.toMillis());
    }

    @Test
    void testExponentialBackoffWithFullJitter() {
        Duration baseDelay = Duration.ofMillis(100);
        RetryStrategy strategy = RetryStrategy.exponentialBackoff(baseDelay).withFullJitter();

        // First attempt: should be between 0 and 100ms (2^0 * 100)
        Duration delay1 = strategy.calculateDelay(1);
        assertTrue(delay1.toMillis() >= 0);
        assertTrue(delay1.toMillis() <= 100);

        // Second attempt: should be between 0 and 200ms (2^1 * 100)
        Duration delay2 = strategy.calculateDelay(2);
        assertTrue(delay2.toMillis() >= 0);
        assertTrue(delay2.toMillis() <= 200);

        // Third attempt: should be between 0 and 400ms (2^2 * 100)
        Duration delay3 = strategy.calculateDelay(3);
        assertTrue(delay3.toMillis() >= 0);
        assertTrue(delay3.toMillis() <= 400);
    }

    @Test
    void testExponentialBackoffWithEqualJitter() {
        Duration baseDelay = Duration.ofMillis(100);
        RetryStrategy strategy = RetryStrategy.exponentialBackoff(baseDelay).withEqualJitter();

        // First attempt: should be between 50ms and 100ms
        Duration delay1 = strategy.calculateDelay(1);
        assertTrue(delay1.toMillis() >= 50);
        assertTrue(delay1.toMillis() <= 100);

        // Second attempt: should be between 100ms and 200ms
        Duration delay2 = strategy.calculateDelay(2);
        assertTrue(delay2.toMillis() >= 100);
        assertTrue(delay2.toMillis() <= 200);
    }
}
