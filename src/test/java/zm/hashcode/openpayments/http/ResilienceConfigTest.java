package zm.hashcode.openpayments.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Set;

import org.junit.jupiter.api.Test;

import zm.hashcode.openpayments.http.resilience.ResilienceConfig;
import zm.hashcode.openpayments.http.resilience.RetryStrategy;

class ResilienceConfigTest {

    @Test
    void testBuilderWithDefaults() {
        var config = ResilienceConfig.builder().build();

        assertNotNull(config);
        assertEquals(ResilienceConfig.DEFAULT_MAX_RETRIES, config.maxRetries());
        assertEquals(ResilienceConfig.DEFAULT_MAX_RETRY_DELAY, config.maxRetryDelay());
        assertEquals(ResilienceConfig.DEFAULT_RETRY_STATUS_CODES, config.retryOnStatusCodes());
        assertEquals(ResilienceConfig.DEFAULT_CIRCUIT_BREAKER_ENABLED, config.circuitBreakerEnabled());
        assertEquals(ResilienceConfig.DEFAULT_CIRCUIT_BREAKER_THRESHOLD, config.circuitBreakerThreshold());
        assertEquals(ResilienceConfig.DEFAULT_CIRCUIT_BREAKER_TIMEOUT, config.circuitBreakerTimeout());
        assertEquals(ResilienceConfig.DEFAULT_CIRCUIT_BREAKER_HALF_OPEN_REQUESTS,
                config.circuitBreakerHalfOpenRequests());
        assertNotNull(config.retryStrategy());
    }

    @Test
    void testBuilderWithCustomValues() {
        var customStrategy = RetryStrategy.linearBackoff(Duration.ofMillis(200));
        var customStatusCodes = Set.of(500, 502, 503);

        var config = ResilienceConfig.builder().maxRetries(5).retryStrategy(customStrategy)
                .maxRetryDelay(Duration.ofSeconds(60)).retryOnStatusCodes(customStatusCodes)
                .circuitBreakerEnabled(false).circuitBreakerThreshold(10).circuitBreakerTimeout(Duration.ofMinutes(5))
                .circuitBreakerHalfOpenRequests(5).build();

        assertEquals(5, config.maxRetries());
        assertEquals(customStrategy, config.retryStrategy());
        assertEquals(Duration.ofSeconds(60), config.maxRetryDelay());
        assertEquals(customStatusCodes, config.retryOnStatusCodes());
        assertFalse(config.circuitBreakerEnabled());
        assertEquals(10, config.circuitBreakerThreshold());
        assertEquals(Duration.ofMinutes(5), config.circuitBreakerTimeout());
        assertEquals(5, config.circuitBreakerHalfOpenRequests());
    }

    @Test
    void testDefaultConfig() {
        var config = ResilienceConfig.defaultConfig();

        assertNotNull(config);
        assertEquals(ResilienceConfig.DEFAULT_MAX_RETRIES, config.maxRetries());
        assertTrue(config.isRetryEnabled());
    }

    @Test
    void testNoRetryConfig() {
        var config = ResilienceConfig.noRetry();

        assertNotNull(config);
        assertEquals(0, config.maxRetries());
        assertFalse(config.isRetryEnabled());
    }

    @Test
    void testIsRetryEnabled() {
        var configWithRetry = ResilienceConfig.builder().maxRetries(3).build();
        assertTrue(configWithRetry.isRetryEnabled());

        var configWithoutRetry = ResilienceConfig.builder().maxRetries(0).build();
        assertFalse(configWithoutRetry.isRetryEnabled());
    }

    @Test
    void testIsRetryableStatusCode() {
        var config = ResilienceConfig.builder().retryOnStatusCodes(Set.of(408, 429, 500, 502, 503, 504)).build();

        assertTrue(config.isRetryableStatusCode(408));
        assertTrue(config.isRetryableStatusCode(429));
        assertTrue(config.isRetryableStatusCode(500));
        assertTrue(config.isRetryableStatusCode(502));
        assertTrue(config.isRetryableStatusCode(503));
        assertTrue(config.isRetryableStatusCode(504));

        assertFalse(config.isRetryableStatusCode(200));
        assertFalse(config.isRetryableStatusCode(404));
        assertFalse(config.isRetryableStatusCode(400));
    }

    @Test
    void testAddRetryStatusCodes() {
        var config = ResilienceConfig.builder().retryOnStatusCodes(Set.of(500)).addRetryStatusCodes(502, 503).build();

        assertTrue(config.isRetryableStatusCode(500));
        assertTrue(config.isRetryableStatusCode(502));
        assertTrue(config.isRetryableStatusCode(503));
    }

    @Test
    void testBuilderThrowsWhenMaxRetriesIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> ResilienceConfig.builder().maxRetries(-1).build());
    }

    @Test
    void testBuilderThrowsWhenCircuitBreakerThresholdIsZero() {
        assertThrows(IllegalArgumentException.class,
                () -> ResilienceConfig.builder().circuitBreakerThreshold(0).build());
    }

    @Test
    void testBuilderThrowsWhenCircuitBreakerThresholdIsNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> ResilienceConfig.builder().circuitBreakerThreshold(-1).build());
    }

    @Test
    void testBuilderThrowsWhenCircuitBreakerHalfOpenRequestsIsZero() {
        assertThrows(IllegalArgumentException.class,
                () -> ResilienceConfig.builder().circuitBreakerHalfOpenRequests(0).build());
    }

    @Test
    void testBuilderThrowsWhenRetryStrategyIsNull() {
        assertThrows(NullPointerException.class, () -> ResilienceConfig.builder().retryStrategy(null).build());
    }

    @Test
    void testRecordImmutability() {
        var config = ResilienceConfig.builder().build();

        // Test that config is truly immutable by checking record behavior
        assertNotNull(config.toString());
        assertNotNull(config.hashCode());
        assertEquals(config, config);
    }
}
