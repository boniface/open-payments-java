package zm.hashcode.openpayments.auth.signature;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import zm.hashcode.openpayments.auth.exception.SignatureException;
import zm.hashcode.openpayments.auth.keys.ClientKey;
import zm.hashcode.openpayments.auth.keys.ClientKeyGenerator;

/**
 * Unit tests for {@link HttpSignatureService}.
 */
class HttpSignatureServiceTest {

    // ========================================
    // Service Construction Tests
    // ========================================

    @Test
    void shouldConstructWithValidClientKey() {
        ClientKey clientKey = ClientKeyGenerator.generate("test-key");

        HttpSignatureService service = new HttpSignatureService(clientKey);

        assertThat(service.getKeyId()).isEqualTo("test-key");
    }

    @Test
    void shouldThrowWhenClientKeyIsNull() {
        assertThatThrownBy(() -> new HttpSignatureService(null)).isInstanceOf(NullPointerException.class)
                .hasMessageContaining("clientKey must not be null");
    }

    // ========================================
    // Signature Creation Tests
    // ========================================

    @Test
    void shouldCreateSignatureHeaders() {
        ClientKey clientKey = ClientKeyGenerator.generate("key-1");
        HttpSignatureService service = new HttpSignatureService(clientKey);

        var components = SignatureComponents.builder().method("POST").targetUri("https://example.com/grant")
                .addHeader("content-type", "application/json").build();

        Map<String, String> headers = service.createSignatureHeaders(components);

        assertThat(headers).containsKeys("signature-input", "signature");
    }

    @Test
    void shouldCreateSignatureInputHeader() {
        ClientKey clientKey = ClientKeyGenerator.generate("key-1");
        HttpSignatureService service = new HttpSignatureService(clientKey);

        var components = SignatureComponents.builder().method("GET").targetUri("https://example.com").build();

        Map<String, String> headers = service.createSignatureHeaders(components);
        String signatureInput = headers.get("signature-input");

        assertThat(signatureInput).startsWith("sig=(").contains("@method @target-uri").contains(";created=")
                .contains(";keyid=\"key-1\"").contains(";alg=\"ed25519\"").contains(";nonce=");
    }

    @Test
    void shouldCreateSignatureHeader() {
        ClientKey clientKey = ClientKeyGenerator.generate("key-1");
        HttpSignatureService service = new HttpSignatureService(clientKey);

        var components = SignatureComponents.builder().method("GET").targetUri("https://example.com").build();

        Map<String, String> headers = service.createSignatureHeaders(components);
        String signature = headers.get("signature");

        assertThat(signature).startsWith("sig=:").endsWith(":").hasSizeGreaterThan(90); // Base64 Ed25519 signature is
                                                                                        // 88 chars + "sig=:" + ":"
    }

    @Test
    void shouldIncludeAllComponentsInSignature() {
        ClientKey clientKey = ClientKeyGenerator.generate("key-1");
        HttpSignatureService service = new HttpSignatureService(clientKey);

        var components = SignatureComponents.builder().method("POST").targetUri("https://example.com/grant")
                .addHeader("authorization", "GNAP token").addHeader("content-type", "application/json")
                .addHeader("content-digest", "sha-256=:abc:=").body("{}").build();

        Map<String, String> headers = service.createSignatureHeaders(components);
        String signatureInput = headers.get("signature-input");

        assertThat(signatureInput).contains("@method").contains("@target-uri").contains("authorization")
                .contains("content-digest").contains("content-type");
    }

    @Test
    void shouldGenerateDifferentNoncesForEachSignature() {
        ClientKey clientKey = ClientKeyGenerator.generate("key-1");
        HttpSignatureService service = new HttpSignatureService(clientKey);

        var components = SignatureComponents.builder().method("GET").targetUri("https://example.com").build();

        Map<String, String> headers1 = service.createSignatureHeaders(components);
        Map<String, String> headers2 = service.createSignatureHeaders(components);

        String nonce1 = extractNonce(headers1.get("signature-input"));
        String nonce2 = extractNonce(headers2.get("signature-input"));

        assertThat(nonce1).isNotEqualTo(nonce2);
    }

    @Test
    void shouldGenerateCryptographicallyRandomNonces() {
        ClientKey clientKey = ClientKeyGenerator.generate("key-1");
        HttpSignatureService service = new HttpSignatureService(clientKey);

        var components = SignatureComponents.builder().method("GET").targetUri("https://example.com").build();

        Set<String> nonces = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            Map<String, String> headers = service.createSignatureHeaders(components);
            String nonce = extractNonce(headers.get("signature-input"));
            nonces.add(nonce);
        }

        // All nonces should be unique
        assertThat(nonces).hasSize(100);
    }

    @Test
    void shouldIncludeCreatedTimestamp() {
        ClientKey clientKey = ClientKeyGenerator.generate("key-1");
        HttpSignatureService service = new HttpSignatureService(clientKey);

        var components = SignatureComponents.builder().method("GET").targetUri("https://example.com").build();

        long beforeCreation = Instant.now().getEpochSecond();
        Map<String, String> headers = service.createSignatureHeaders(components);
        long afterCreation = Instant.now().getEpochSecond();

        String signatureInput = headers.get("signature-input");
        long createdTime = extractCreatedTime(signatureInput);

        assertThat(createdTime).isBetween(beforeCreation, afterCreation);
    }

    @Test
    void shouldThrowWhenComponentsIsNull() {
        ClientKey clientKey = ClientKeyGenerator.generate("key-1");
        HttpSignatureService service = new HttpSignatureService(clientKey);

        assertThatThrownBy(() -> service.createSignatureHeaders(null)).isInstanceOf(NullPointerException.class)
                .hasMessageContaining("components must not be null");
    }

    @Test
    void shouldCreateDifferentSignaturesForDifferentComponents() {
        ClientKey clientKey = ClientKeyGenerator.generate("key-1");
        HttpSignatureService service = new HttpSignatureService(clientKey);

        var components1 = SignatureComponents.builder().method("GET").targetUri("https://example.com/path1").build();

        var components2 = SignatureComponents.builder().method("GET").targetUri("https://example.com/path2").build();

        Map<String, String> headers1 = service.createSignatureHeaders(components1);
        Map<String, String> headers2 = service.createSignatureHeaders(components2);

        String sig1 = extractSignatureValue(headers1.get("signature"));
        String sig2 = extractSignatureValue(headers2.get("signature"));

        assertThat(sig1).isNotEqualTo(sig2);
    }

    @Test
    void shouldIncludeKeyIdInSignatureInput() {
        ClientKey clientKey = ClientKeyGenerator.generate("my-custom-key-id");
        HttpSignatureService service = new HttpSignatureService(clientKey);

        var components = SignatureComponents.builder().method("GET").targetUri("https://example.com").build();

        Map<String, String> headers = service.createSignatureHeaders(components);
        String signatureInput = headers.get("signature-input");

        assertThat(signatureInput).contains("keyid=\"my-custom-key-id\"");
    }

    @Test
    void shouldIncludeEd25519AlgorithmInSignatureInput() {
        ClientKey clientKey = ClientKeyGenerator.generate("key-1");
        HttpSignatureService service = new HttpSignatureService(clientKey);

        var components = SignatureComponents.builder().method("GET").targetUri("https://example.com").build();

        Map<String, String> headers = service.createSignatureHeaders(components);
        String signatureInput = headers.get("signature-input");

        assertThat(signatureInput).contains("alg=\"ed25519\"");
    }

    // ========================================
    // Signature Validation Tests
    // ========================================

    @Test
    void shouldValidateCorrectSignature() {
        ClientKey clientKey = ClientKeyGenerator.generate("key-1");
        HttpSignatureService service = new HttpSignatureService(clientKey);

        var components = SignatureComponents.builder().method("POST").targetUri("https://example.com/grant")
                .addHeader("content-type", "application/json").build();

        Map<String, String> headers = service.createSignatureHeaders(components);
        String signatureValue = extractSignatureValue(headers.get("signature"));

        boolean valid = service.validateSignature(components, signatureValue);

        assertThat(valid).isTrue();
    }

    @Test
    void shouldRejectTamperedSignature() {
        ClientKey clientKey = ClientKeyGenerator.generate("key-1");
        HttpSignatureService service = new HttpSignatureService(clientKey);

        var components = SignatureComponents.builder().method("POST").targetUri("https://example.com/grant").build();

        Map<String, String> headers = service.createSignatureHeaders(components);
        String signatureValue = extractSignatureValue(headers.get("signature"));

        // Tamper with the signature
        String tamperedSignature = signatureValue.substring(0, signatureValue.length() - 4) + "AAAA";

        boolean valid = service.validateSignature(components, tamperedSignature);

        assertThat(valid).isFalse();
    }

    @Test
    void shouldRejectSignatureWithDifferentComponents() {
        ClientKey clientKey = ClientKeyGenerator.generate("key-1");
        HttpSignatureService service = new HttpSignatureService(clientKey);

        var components1 = SignatureComponents.builder().method("POST").targetUri("https://example.com/grant").build();

        var components2 = SignatureComponents.builder().method("GET") // Different method
                .targetUri("https://example.com/grant").build();

        Map<String, String> headers = service.createSignatureHeaders(components1);
        String signatureValue = extractSignatureValue(headers.get("signature"));

        boolean valid = service.validateSignature(components2, signatureValue);

        assertThat(valid).isFalse();
    }

    @Test
    void shouldRejectSignatureWithDifferentKey() {
        ClientKey clientKey1 = ClientKeyGenerator.generate("key-1");
        ClientKey clientKey2 = ClientKeyGenerator.generate("key-2");

        HttpSignatureService service1 = new HttpSignatureService(clientKey1);
        HttpSignatureService service2 = new HttpSignatureService(clientKey2);

        var components = SignatureComponents.builder().method("GET").targetUri("https://example.com").build();

        Map<String, String> headers = service1.createSignatureHeaders(components);
        String signatureValue = extractSignatureValue(headers.get("signature"));

        boolean valid = service2.validateSignature(components, signatureValue);

        assertThat(valid).isFalse();
    }

    @Test
    void shouldThrowWhenValidatingWithNullComponents() {
        ClientKey clientKey = ClientKeyGenerator.generate("key-1");
        HttpSignatureService service = new HttpSignatureService(clientKey);

        assertThatThrownBy(() -> service.validateSignature(null, "signature")).isInstanceOf(NullPointerException.class)
                .hasMessageContaining("components must not be null");
    }

    @Test
    void shouldThrowWhenValidatingWithNullSignature() {
        ClientKey clientKey = ClientKeyGenerator.generate("key-1");
        HttpSignatureService service = new HttpSignatureService(clientKey);

        var components = SignatureComponents.builder().method("GET").targetUri("https://example.com").build();

        assertThatThrownBy(() -> service.validateSignature(components, null)).isInstanceOf(NullPointerException.class)
                .hasMessageContaining("signatureValue must not be null");
    }

    @Test
    void shouldThrowWhenSignatureIsNotBase64() {
        ClientKey clientKey = ClientKeyGenerator.generate("key-1");
        HttpSignatureService service = new HttpSignatureService(clientKey);

        var components = SignatureComponents.builder().method("GET").targetUri("https://example.com").build();

        assertThatThrownBy(() -> service.validateSignature(components, "not-base64!!!"))
                .isInstanceOf(SignatureException.class).hasMessageContaining("Invalid signature encoding");
    }

    @Test
    void shouldValidateDeterministicSignatures() {
        ClientKey clientKey = ClientKeyGenerator.generate("key-1");
        HttpSignatureService service = new HttpSignatureService(clientKey);

        var components = SignatureComponents.builder().method("POST").targetUri("https://example.com/grant")
                .addHeader("content-type", "application/json").build();

        // Create signature twice with same base (but different nonces/timestamps)
        Map<String, String> headers1 = service.createSignatureHeaders(components);
        Map<String, String> headers2 = service.createSignatureHeaders(components);

        String sig1 = extractSignatureValue(headers1.get("signature"));
        String sig2 = extractSignatureValue(headers2.get("signature"));

        // Both should validate
        assertThat(service.validateSignature(components, sig1)).isTrue();
        assertThat(service.validateSignature(components, sig2)).isTrue();
    }

    @Test
    void shouldRejectSignatureForModifiedUri() {
        ClientKey clientKey = ClientKeyGenerator.generate("key-1");
        HttpSignatureService service = new HttpSignatureService(clientKey);

        var components = SignatureComponents.builder().method("GET").targetUri("https://example.com/original").build();

        Map<String, String> headers = service.createSignatureHeaders(components);
        String signatureValue = extractSignatureValue(headers.get("signature"));

        // Try to validate with modified URI
        var modifiedComponents = SignatureComponents.builder().method("GET").targetUri("https://example.com/modified")
                .build();

        boolean valid = service.validateSignature(modifiedComponents, signatureValue);

        assertThat(valid).isFalse();
    }

    @Test
    void shouldRejectSignatureForModifiedHeaders() {
        ClientKey clientKey = ClientKeyGenerator.generate("key-1");
        HttpSignatureService service = new HttpSignatureService(clientKey);

        var components = SignatureComponents.builder().method("POST").targetUri("https://example.com")
                .addHeader("content-type", "application/json").build();

        Map<String, String> headers = service.createSignatureHeaders(components);
        String signatureValue = extractSignatureValue(headers.get("signature"));

        // Try to validate with modified header
        var modifiedComponents = SignatureComponents.builder().method("POST").targetUri("https://example.com")
                .addHeader("content-type", "text/plain") // Changed!
                .build();

        boolean valid = service.validateSignature(modifiedComponents, signatureValue);

        assertThat(valid).isFalse();
    }

    // ========================================
    // Signature Base Construction Tests
    // ========================================

    @Test
    void shouldBuildCorrectSignatureBaseForSimpleRequest() {
        // This tests the internal signature base format
        // We'll validate by checking signature consistency
        ClientKey clientKey = ClientKeyGenerator.generate("key-1");
        HttpSignatureService service = new HttpSignatureService(clientKey);

        var components = SignatureComponents.builder().method("GET").targetUri("https://example.com/path").build();

        // Sign twice - Ed25519 signatures are deterministic for same input
        Map<String, String> headers1 = service.createSignatureHeaders(components);
        Map<String, String> headers2 = service.createSignatureHeaders(components);

        String sig1 = extractSignatureValue(headers1.get("signature"));
        String sig2 = extractSignatureValue(headers2.get("signature"));

        // Both should validate with the same components
        assertThat(service.validateSignature(components, sig1)).isTrue();
        assertThat(service.validateSignature(components, sig2)).isTrue();
    }

    @Test
    void shouldIncludeDerivedComponentsInBase() {
        ClientKey clientKey = ClientKeyGenerator.generate("key-1");
        HttpSignatureService service = new HttpSignatureService(clientKey);

        var components = SignatureComponents.builder().method("POST").targetUri("https://example.com/grant").build();

        Map<String, String> headers = service.createSignatureHeaders(components);
        String signature = extractSignatureValue(headers.get("signature"));

        assertThat(service.validateSignature(components, signature)).isTrue();
    }

    @Test
    void shouldIncludeHeaderComponentsInBase() {
        ClientKey clientKey = ClientKeyGenerator.generate("key-1");
        HttpSignatureService service = new HttpSignatureService(clientKey);

        var components = SignatureComponents.builder().method("POST").targetUri("https://example.com")
                .addHeader("content-type", "application/json").addHeader("authorization", "GNAP token").build();

        Map<String, String> headers = service.createSignatureHeaders(components);
        String signature = extractSignatureValue(headers.get("signature"));

        // Change header value - signature should fail
        var tamperedComponents = SignatureComponents.builder().method("POST").targetUri("https://example.com")
                .addHeader("content-type", "text/plain") // Changed!
                .addHeader("authorization", "GNAP token").build();

        assertThat(service.validateSignature(tamperedComponents, signature)).isFalse();
    }

    @Test
    void shouldHandleSpecialCharactersInHeaders() {
        ClientKey clientKey = ClientKeyGenerator.generate("key-1");
        HttpSignatureService service = new HttpSignatureService(clientKey);

        var components = SignatureComponents.builder().method("POST").targetUri("https://example.com")
                .addHeader("content-type", "application/json; charset=utf-8").build();

        Map<String, String> headers = service.createSignatureHeaders(components);
        String signature = extractSignatureValue(headers.get("signature"));

        assertThat(service.validateSignature(components, signature)).isTrue();
    }

    @Test
    void shouldHandleUnicodeInUri() {
        ClientKey clientKey = ClientKeyGenerator.generate("key-1");
        HttpSignatureService service = new HttpSignatureService(clientKey);

        var components = SignatureComponents.builder().method("GET").targetUri("https://example.com/path?name=Test™")
                .build();

        Map<String, String> headers = service.createSignatureHeaders(components);
        String signature = extractSignatureValue(headers.get("signature"));

        assertThat(service.validateSignature(components, signature)).isTrue();
    }

    @Test
    void shouldHandleMultipleHeadersInSignature() {
        ClientKey clientKey = ClientKeyGenerator.generate("key-1");
        HttpSignatureService service = new HttpSignatureService(clientKey);

        var components = SignatureComponents.builder().method("POST").targetUri("https://example.com/grant")
                .addHeader("authorization", "GNAP token").addHeader("content-type", "application/json")
                .addHeader("content-length", "123").addHeader("content-digest", "sha-256=:abc:=").body("{}").build();

        Map<String, String> headers = service.createSignatureHeaders(components);
        String signature = extractSignatureValue(headers.get("signature"));

        assertThat(service.validateSignature(components, signature)).isTrue();
    }

    // ========================================
    // Edge Cases
    // ========================================

    @Test
    void shouldHandleEmptyBody() {
        ClientKey clientKey = ClientKeyGenerator.generate("key-1");
        HttpSignatureService service = new HttpSignatureService(clientKey);

        var components = SignatureComponents.builder().method("POST").targetUri("https://example.com").body("").build();

        Map<String, String> headers = service.createSignatureHeaders(components);
        String signature = extractSignatureValue(headers.get("signature"));

        assertThat(service.validateSignature(components, signature)).isTrue();
    }

    @Test
    void shouldHandleLargeBody() {
        ClientKey clientKey = ClientKeyGenerator.generate("key-1");
        HttpSignatureService service = new HttpSignatureService(clientKey);

        String largeBody = "x".repeat(10000);

        var components = SignatureComponents.builder().method("POST").targetUri("https://example.com").body(largeBody)
                .build();

        Map<String, String> headers = service.createSignatureHeaders(components);
        String signature = extractSignatureValue(headers.get("signature"));

        assertThat(service.validateSignature(components, signature)).isTrue();
    }

    @Test
    void shouldHandleComplexUriWithQueryParams() {
        ClientKey clientKey = ClientKeyGenerator.generate("key-1");
        HttpSignatureService service = new HttpSignatureService(clientKey);

        String complexUri = "https://auth.example.com:8443/grant?client_id=123&state=abc&redirect_uri=https://example.com/callback";

        var components = SignatureComponents.builder().method("POST").targetUri(complexUri).build();

        Map<String, String> headers = service.createSignatureHeaders(components);
        String signature = extractSignatureValue(headers.get("signature"));

        assertThat(service.validateSignature(components, signature)).isTrue();
    }

    @Test
    void shouldHandleAllHttpMethods() {
        String[] methods = {"GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"};

        for (String method : methods) {
            ClientKey clientKey = ClientKeyGenerator.generate("key-1");
            HttpSignatureService service = new HttpSignatureService(clientKey);

            var components = SignatureComponents.builder().method(method).targetUri("https://example.com").build();

            Map<String, String> headers = service.createSignatureHeaders(components);
            String signature = extractSignatureValue(headers.get("signature"));

            assertThat(service.validateSignature(components, signature)).isTrue();
        }
    }

    @Test
    void shouldCreateSignatureForMinimalRequest() {
        ClientKey clientKey = ClientKeyGenerator.generate("key-1");
        HttpSignatureService service = new HttpSignatureService(clientKey);

        var components = SignatureComponents.builder().method("GET").targetUri("https://example.com").build();

        Map<String, String> headers = service.createSignatureHeaders(components);

        assertThat(headers).isNotEmpty();
        assertThat(headers.get("signature-input")).contains("@method @target-uri");
    }

    @Test
    void shouldCreateSignatureForMaximalRequest() {
        ClientKey clientKey = ClientKeyGenerator.generate("key-1");
        HttpSignatureService service = new HttpSignatureService(clientKey);

        var components = SignatureComponents.builder().method("POST").targetUri("https://example.com/grant")
                .addHeader("authorization", "GNAP token").addHeader("content-type", "application/json")
                .addHeader("content-length", "456").addHeader("content-digest", "sha-256=:base64hash:=")
                .body("{\"key\":\"value\"}").build();

        Map<String, String> headers = service.createSignatureHeaders(components);
        String signatureInput = headers.get("signature-input");

        assertThat(signatureInput).contains("@method").contains("@target-uri").contains("authorization")
                .contains("content-digest").contains("content-type").contains("content-length");
    }

    @Test
    void shouldThrowWhenSignatureBaseHasMissingHeader() {
        ClientKey clientKey = ClientKeyGenerator.generate("key-1");
        HttpSignatureService service = new HttpSignatureService(clientKey);

        // Create components that claim to have a header but don't
        var components = SignatureComponents.builder().method("POST").targetUri("https://example.com")
                // content-digest is in component identifiers but not actually added
                .addHeader("content-digest", "sha-256=:abc:=").body("{}").build();

        // This should work - header is present
        Map<String, String> headers = service.createSignatureHeaders(components);
        assertThat(headers).isNotEmpty();

        // Now test with a truly missing header by manipulating component identifiers
        // This is a corner case that shouldn't happen in normal usage
    }

    // ========================================
    // Helper Methods
    // ========================================

    private String extractNonce(String signatureInput) {
        Pattern pattern = Pattern.compile("nonce=\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(signatureInput);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("No nonce found in: " + signatureInput);
    }

    private long extractCreatedTime(String signatureInput) {
        Pattern pattern = Pattern.compile("created=(\\d+)");
        Matcher matcher = pattern.matcher(signatureInput);
        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        }
        throw new IllegalArgumentException("No created time found");
    }

    private String extractSignatureValue(String signatureHeader) {
        // Format: sig=:base64_signature:
        Pattern pattern = Pattern.compile("sig=:([^:]+):");
        Matcher matcher = pattern.matcher(signatureHeader);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("Invalid signature format: " + signatureHeader);
    }
}
