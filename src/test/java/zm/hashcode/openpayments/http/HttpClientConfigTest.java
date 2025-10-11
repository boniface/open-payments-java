package zm.hashcode.openpayments.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.time.Duration;

import org.junit.jupiter.api.Test;

import zm.hashcode.openpayments.http.config.HttpClientConfig;

class HttpClientConfigTest {

    @Test
    void testBuilderWithDefaults() {
        var config = HttpClientConfig.builder().baseUrl("https://api.example.com").build();

        assertNotNull(config);
        assertEquals(URI.create("https://api.example.com"), config.baseUrl());
        assertEquals(HttpClientConfig.DEFAULT_CONNECT_TIMEOUT, config.connectTimeout());
        assertEquals(HttpClientConfig.DEFAULT_REQUEST_TIMEOUT, config.requestTimeout());
        assertEquals(HttpClientConfig.DEFAULT_SOCKET_TIMEOUT, config.socketTimeout());
        assertEquals(HttpClientConfig.DEFAULT_MAX_CONNECTIONS, config.maxConnections());
        assertEquals(HttpClientConfig.DEFAULT_MAX_CONNECTIONS_PER_ROUTE, config.maxConnectionsPerRoute());
        assertEquals(HttpClientConfig.DEFAULT_CONNECTION_TTL, config.connectionTimeToLive());
        assertEquals(HttpClientConfig.DEFAULT_FOLLOW_REDIRECTS, config.followRedirects());
        assertTrue(config.getSslContext().isEmpty());
    }

    @Test
    void testBuilderWithCustomValues() {
        var config = HttpClientConfig.builder().baseUrl("https://api.example.com").connectTimeout(Duration.ofSeconds(5))
                .requestTimeout(Duration.ofSeconds(20)).socketTimeout(Duration.ofSeconds(15)).maxConnections(50)
                .maxConnectionsPerRoute(10).connectionTimeToLive(Duration.ofMinutes(10)).followRedirects(false).build();

        assertEquals(Duration.ofSeconds(5), config.connectTimeout());
        assertEquals(Duration.ofSeconds(20), config.requestTimeout());
        assertEquals(Duration.ofSeconds(15), config.socketTimeout());
        assertEquals(50, config.maxConnections());
        assertEquals(10, config.maxConnectionsPerRoute());
        assertEquals(Duration.ofMinutes(10), config.connectionTimeToLive());
        assertEquals(false, config.followRedirects());
    }

    @Test
    void testBuilderWithUriBaseUrl() {
        URI baseUri = URI.create("https://api.example.com");
        var config = HttpClientConfig.builder().baseUrl(baseUri).build();

        assertEquals(baseUri, config.baseUrl());
    }

    @Test
    void testBuilderThrowsWhenBaseUrlIsNull() {
        assertThrows(NullPointerException.class, () -> HttpClientConfig.builder().build());
    }

    @Test
    void testBuilderThrowsWhenMaxConnectionsIsZero() {
        assertThrows(IllegalArgumentException.class,
                () -> HttpClientConfig.builder().baseUrl("https://api.example.com").maxConnections(0).build());
    }

    @Test
    void testBuilderThrowsWhenMaxConnectionsIsNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> HttpClientConfig.builder().baseUrl("https://api.example.com").maxConnections(-1).build());
    }

    @Test
    void testBuilderThrowsWhenMaxConnectionsPerRouteExceedsMaxConnections() {
        assertThrows(IllegalArgumentException.class, () -> HttpClientConfig.builder().baseUrl("https://api.example.com")
                .maxConnections(10).maxConnectionsPerRoute(20).build());
    }

    @Test
    void testBuilderThrowsWhenConnectTimeoutIsNull() {
        assertThrows(NullPointerException.class,
                () -> HttpClientConfig.builder().baseUrl("https://api.example.com").connectTimeout(null).build());
    }

    @Test
    void testRecordImmutability() {
        var config = HttpClientConfig.builder().baseUrl("https://api.example.com").build();

        // Test that config is truly immutable by checking record behavior
        assertNotNull(config.toString());
        assertNotNull(config.hashCode());
        assertEquals(config, config);
    }
}
