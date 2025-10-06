package zm.hashcode.openpayments.http;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import zm.hashcode.openpayments.http.config.HttpClientConfig;
import zm.hashcode.openpayments.http.factory.HttpClientBuilder;
import zm.hashcode.openpayments.http.resilience.ResilienceConfig;
import zm.hashcode.openpayments.http.resilience.RetryStrategy;

class HttpClientBuilderTest {

    @Test
    void testCreateReturnsBuilder() {
        var builder = HttpClientBuilder.create();
        assertNotNull(builder);
    }

    @Test
    void testBuildWithBaseUrl() {
        var client = HttpClientBuilder.create().baseUrl("https://api.example.com").build();
        assertNotNull(client);
    }

    @Test
    void testBuildWithCustomTimeouts() {
        var client = HttpClientBuilder.create().baseUrl("https://api.example.com").connectTimeout(Duration.ofSeconds(5))
                .requestTimeout(Duration.ofSeconds(20)).socketTimeout(Duration.ofSeconds(15)).build();
        assertNotNull(client);
    }

    @Test
    void testBuildWithConnectionPooling() {
        var client = HttpClientBuilder.create().baseUrl("https://api.example.com").maxConnections(50)
                .maxConnectionsPerRoute(10).connectionTimeToLive(Duration.ofMinutes(10)).build();
        assertNotNull(client);
    }

    @Test
    void testBuildWithResilience() {
        var client = HttpClientBuilder.create().baseUrl("https://api.example.com").maxRetries(5)
                .retryStrategy(RetryStrategy.exponentialBackoff(Duration.ofMillis(100))).circuitBreakerEnabled(true)
                .circuitBreakerThreshold(10).build();
        assertNotNull(client);
    }

    @Test
    void testBuildWithResilienceDisabled() {
        var client = HttpClientBuilder.create().baseUrl("https://api.example.com").resilienceEnabled(false).build();
        assertNotNull(client);
    }

    @Test
    void testBuildWithCustomConfig() {
        var config = HttpClientConfig.builder().baseUrl("https://api.example.com").connectTimeout(Duration.ofSeconds(5))
                .build();

        var client = HttpClientBuilder.create().withConfig(config).build();
        assertNotNull(client);
    }

    @Test
    void testBuildWithCustomResilienceConfig() {
        var resilienceConfig = ResilienceConfig.builder().maxRetries(5)
                .retryStrategy(RetryStrategy.linearBackoff(Duration.ofMillis(200))).build();

        var client = HttpClientBuilder.create().baseUrl("https://api.example.com").withResilience(resilienceConfig)
                .build();
        assertNotNull(client);
    }

    @Test
    void testSimpleClientFactory() {
        var client = HttpClientBuilder.simple("https://api.example.com");
        assertNotNull(client);
    }

    @Test
    void testWithoutResilienceFactory() {
        var client = HttpClientBuilder.withoutResilience("https://api.example.com");
        assertNotNull(client);
    }

    @Test
    void testBuildThrowsWhenBaseUrlIsMissing() {
        assertThrows(NullPointerException.class, () -> HttpClientBuilder.create().build());
    }

    @Test
    void testBuildWithFollowRedirects() {
        var client = HttpClientBuilder.create().baseUrl("https://api.example.com").followRedirects(false).build();
        assertNotNull(client);
    }

    @Test
    void testBuildWithAllConfigOptions() {
        var client = HttpClientBuilder.create().baseUrl("https://api.example.com").connectTimeout(Duration.ofSeconds(5))
                .requestTimeout(Duration.ofSeconds(20)).socketTimeout(Duration.ofSeconds(15)).maxConnections(50)
                .maxConnectionsPerRoute(10).connectionTimeToLive(Duration.ofMinutes(10)).followRedirects(false)
                .maxRetries(3).retryStrategy(RetryStrategy.exponentialBackoff(Duration.ofMillis(100)))
                .maxRetryDelay(Duration.ofSeconds(30)).circuitBreakerEnabled(true).circuitBreakerThreshold(5)
                .circuitBreakerTimeout(Duration.ofMinutes(1)).build();
        assertNotNull(client);
    }
}
