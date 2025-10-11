package zm.hashcode.openpayments.auth.signature;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import zm.hashcode.openpayments.auth.exception.SignatureException;
import zm.hashcode.openpayments.auth.keys.ClientKey;
import zm.hashcode.openpayments.auth.keys.ClientKeyGenerator;

/**
 * Unit tests for {@link HttpSignatureService}.
 */
@DisplayName("HttpSignatureService")
class HttpSignatureServiceTest {

    private static final String TEST_KEY_ID = "key-1";
    private static final String BASE_URI = "https://example.com";

    // Test helper class to reduce boilerplate
    private static class SignatureTestContext {
        final ClientKey clientKey;
        final HttpSignatureService service;
        final SignatureComponents components;

        SignatureTestContext(String keyId, SignatureComponents components) {
            this.clientKey = ClientKeyGenerator.generate(keyId);
            this.service = new HttpSignatureService(clientKey);
            this.components = components;
        }

        Map<String, String> createSignature() {
            return service.createSignatureHeaders(components);
        }

        boolean validateSignature(SignatureComponents comps, String signatureValue) {
            return service.validateSignature(comps, signatureValue);
        }
    }

    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("should construct with valid client key")
        void shouldConstructWithValidClientKey() {
            ClientKey clientKey = ClientKeyGenerator.generate("test-key");
            HttpSignatureService service = new HttpSignatureService(clientKey);

            assertThat(service.getKeyId()).isEqualTo("test-key");
        }

        @Test
        @DisplayName("should throw when client key is null")
        void shouldThrowWhenClientKeyIsNull() {
            assertThatThrownBy(() -> new HttpSignatureService(null)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("clientKey must not be null");
        }
    }

    @Nested
    @DisplayName("Signature Creation")
    class SignatureCreationTests {

        private SignatureTestContext context;

        @BeforeEach
        void setUp() {
            var components = SignatureComponents.builder().method("POST").targetUri(BASE_URI + "/grant")
                    .addHeader("content-type", "application/json").build();
            context = new SignatureTestContext(TEST_KEY_ID, components);
        }

        @Test
        @DisplayName("should create signature headers")
        void shouldCreateSignatureHeaders() {
            Map<String, String> headers = context.createSignature();

            assertThat(headers).containsKeys("signature-input", "signature");
        }

        @Test
        @DisplayName("should create signature input header with required fields")
        void shouldCreateSignatureInputHeader() {
            Map<String, String> headers = context.createSignature();
            String signatureInput = headers.get("signature-input");

            assertThat(signatureInput).startsWith("sig=(").contains("@method", "@target-uri", ";created=",
                    ";keyid=\"" + TEST_KEY_ID + "\"", ";alg=\"ed25519\"", ";nonce=");
        }

        @Test
        @DisplayName("should create signature header with correct format")
        void shouldCreateSignatureHeader() {
            Map<String, String> headers = context.createSignature();
            String signature = headers.get("signature");

            assertThat(signature).startsWith("sig=:").endsWith(":").hasSizeGreaterThan(90); // Base64 Ed25519 signature
        }

        @Test
        @DisplayName("should include all components in signature input")
        void shouldIncludeAllComponentsInSignature() {
            var components = SignatureComponents.builder().method("POST").targetUri(BASE_URI + "/grant")
                    .addHeader("authorization", "GNAP token").addHeader("content-type", "application/json")
                    .addHeader("content-digest", "sha-256=:abc:=").body("{}").build();

            var ctx = new SignatureTestContext(TEST_KEY_ID, components);
            Map<String, String> headers = ctx.createSignature();
            String signatureInput = headers.get("signature-input");

            assertThat(signatureInput).contains("@method", "@target-uri", "authorization", "content-digest",
                    "content-type");
        }

        @Test
        @DisplayName("should generate different nonces for each signature")
        void shouldGenerateDifferentNonces() {
            Map<String, String> headers1 = context.createSignature();
            Map<String, String> headers2 = context.createSignature();

            String nonce1 = extractNonce(headers1.get("signature-input"));
            String nonce2 = extractNonce(headers2.get("signature-input"));

            assertThat(nonce1).isNotEqualTo(nonce2);
        }

        @Test
        @DisplayName("should generate cryptographically random nonces")
        void shouldGenerateCryptographicallyRandomNonces() {
            Set<String> nonces = new HashSet<>();
            for (int i = 0; i < 100; i++) {
                Map<String, String> headers = context.createSignature();
                nonces.add(extractNonce(headers.get("signature-input")));
            }

            assertThat(nonces).hasSize(100);
        }

        @Test
        @DisplayName("should include created timestamp")
        void shouldIncludeCreatedTimestamp() {
            long beforeCreation = Instant.now().getEpochSecond();
            Map<String, String> headers = context.createSignature();
            long afterCreation = Instant.now().getEpochSecond();

            long createdTime = extractCreatedTime(headers.get("signature-input"));

            assertThat(createdTime).isBetween(beforeCreation, afterCreation);
        }

        @Test
        @DisplayName("should create different signatures for different components")
        void shouldCreateDifferentSignaturesForDifferentComponents() {
            var components1 = SignatureComponents.builder().method("GET").targetUri(BASE_URI + "/path1").build();
            var components2 = SignatureComponents.builder().method("GET").targetUri(BASE_URI + "/path2").build();

            var ctx1 = new SignatureTestContext(TEST_KEY_ID, components1);
            var ctx2 = new SignatureTestContext(TEST_KEY_ID, components2);

            String sig1 = extractSignatureValue(ctx1.createSignature().get("signature"));
            String sig2 = extractSignatureValue(ctx2.createSignature().get("signature"));

            assertThat(sig1).isNotEqualTo(sig2);
        }

        @ParameterizedTest
        @ValueSource(strings = {"my-key", "custom-key-id", "key-123"})
        @DisplayName("should include key ID in signature input")
        void shouldIncludeKeyIdInSignatureInput(String keyId) {
            var ctx = new SignatureTestContext(keyId, context.components);
            Map<String, String> headers = ctx.createSignature();

            assertThat(headers.get("signature-input")).contains("keyid=\"" + keyId + "\"");
        }

        @Test
        @DisplayName("should throw when components is null")
        void shouldThrowWhenComponentsIsNull() {
            assertThatThrownBy(() -> context.service.createSignatureHeaders(null))
                    .isInstanceOf(NullPointerException.class).hasMessageContaining("components must not be null");
        }
    }

    @Nested
    @DisplayName("Signature Validation")
    class SignatureValidationTests {

        private SignatureTestContext context;

        @BeforeEach
        void setUp() {
            var components = SignatureComponents.builder().method("POST").targetUri(BASE_URI + "/grant")
                    .addHeader("content-type", "application/json").build();
            context = new SignatureTestContext(TEST_KEY_ID, components);
        }

        @Test
        @DisplayName("should validate correct signature")
        void shouldValidateCorrectSignature() {
            Map<String, String> headers = context.createSignature();
            String signatureValue = extractSignatureValue(headers.get("signature"));

            assertThat(context.validateSignature(context.components, signatureValue)).isTrue();
        }

        @Test
        @DisplayName("should reject tampered signature")
        void shouldRejectTamperedSignature() {
            Map<String, String> headers = context.createSignature();
            String signatureValue = extractSignatureValue(headers.get("signature"));
            String tamperedSignature = signatureValue.substring(0, signatureValue.length() - 4) + "AAAA";

            assertThat(context.validateSignature(context.components, tamperedSignature)).isFalse();
        }

        @Test
        @DisplayName("should reject signature with different components")
        void shouldRejectSignatureWithDifferentComponents() {
            Map<String, String> headers = context.createSignature();
            String signatureValue = extractSignatureValue(headers.get("signature"));

            var differentComponents = SignatureComponents.builder().method("GET") // Different method
                    .targetUri(BASE_URI + "/grant").build();

            assertThat(context.validateSignature(differentComponents, signatureValue)).isFalse();
        }

        @Test
        @DisplayName("should reject signature with different key")
        void shouldRejectSignatureWithDifferentKey() {
            Map<String, String> headers = context.createSignature();
            String signatureValue = extractSignatureValue(headers.get("signature"));

            var otherContext = new SignatureTestContext("different-key", context.components);

            assertThat(otherContext.validateSignature(context.components, signatureValue)).isFalse();
        }

        @Test
        @DisplayName("should validate deterministic signatures")
        void shouldValidateDeterministicSignatures() {
            Map<String, String> headers1 = context.createSignature();
            Map<String, String> headers2 = context.createSignature();

            String sig1 = extractSignatureValue(headers1.get("signature"));
            String sig2 = extractSignatureValue(headers2.get("signature"));

            assertThat(context.validateSignature(context.components, sig1)).isTrue();
            assertThat(context.validateSignature(context.components, sig2)).isTrue();
        }

        @ParameterizedTest
        @MethodSource("modifiedComponentsProvider")
        @DisplayName("should reject signature for modified components")
        void shouldRejectSignatureForModifiedComponents(SignatureComponents modified) {
            Map<String, String> headers = context.createSignature();
            String signatureValue = extractSignatureValue(headers.get("signature"));

            assertThat(context.validateSignature(modified, signatureValue)).isFalse();
        }

        static Stream<Arguments> modifiedComponentsProvider() {
            return Stream.of(
                    Arguments
                            .of(SignatureComponents.builder().method("POST").targetUri(BASE_URI + "/modified").build()),
                    Arguments.of(SignatureComponents.builder().method("POST").targetUri(BASE_URI + "/grant")
                            .addHeader("content-type", "text/plain").build()));
        }

        @Test
        @DisplayName("should throw when validating with null components")
        void shouldThrowWhenValidatingWithNullComponents() {
            assertThatThrownBy(() -> context.service.validateSignature(null, "signature"))
                    .isInstanceOf(NullPointerException.class).hasMessageContaining("components must not be null");
        }

        @Test
        @DisplayName("should throw when validating with null signature")
        void shouldThrowWhenValidatingWithNullSignature() {
            assertThatThrownBy(() -> context.service.validateSignature(context.components, null))
                    .isInstanceOf(NullPointerException.class).hasMessageContaining("signatureValue must not be null");
        }

        @Test
        @DisplayName("should throw when signature is not base64")
        void shouldThrowWhenSignatureIsNotBase64() {
            assertThatThrownBy(() -> context.service.validateSignature(context.components, "not-base64!!!"))
                    .isInstanceOf(SignatureException.class).hasMessageContaining("Invalid signature encoding");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @ParameterizedTest
        @MethodSource("edgeCaseComponentsProvider")
        @DisplayName("should handle various edge cases")
        void shouldHandleEdgeCases(SignatureComponents components) {
            var context = new SignatureTestContext(TEST_KEY_ID, components);
            Map<String, String> headers = context.createSignature();
            String signature = extractSignatureValue(headers.get("signature"));

            assertThat(context.validateSignature(components, signature)).isTrue();
        }

        static Stream<Arguments> edgeCaseComponentsProvider() {
            return Stream.of(
                    Arguments.of(SignatureComponents.builder().method("POST").targetUri(BASE_URI).body("").build()),
                    Arguments.of(SignatureComponents.builder().method("POST").targetUri(BASE_URI)
                            .body("x".repeat(10000)).build()),
                    Arguments.of(SignatureComponents.builder().method("GET").targetUri(BASE_URI + "/path?name=Test™")
                            .build()),
                    Arguments.of(SignatureComponents.builder().method("POST").targetUri(BASE_URI)
                            .addHeader("content-type", "application/json; charset=utf-8").build()),
                    Arguments.of(SignatureComponents.builder().method("POST")
                            .targetUri("https://auth.example.com:8443/grant?client_id=123&state=abc").build()));
        }

        @ParameterizedTest
        @ValueSource(strings = {"GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"})
        @DisplayName("should handle all HTTP methods")
        void shouldHandleAllHttpMethods(String method) {
            var components = SignatureComponents.builder().method(method).targetUri(BASE_URI).build();
            var context = new SignatureTestContext(TEST_KEY_ID, components);

            Map<String, String> headers = context.createSignature();
            String signature = extractSignatureValue(headers.get("signature"));

            assertThat(context.validateSignature(components, signature)).isTrue();
        }

        @Test
        @DisplayName("should handle multiple headers in signature")
        void shouldHandleMultipleHeaders() {
            var components = SignatureComponents.builder().method("POST").targetUri(BASE_URI + "/grant")
                    .addHeader("authorization", "GNAP token").addHeader("content-type", "application/json")
                    .addHeader("content-length", "123").addHeader("content-digest", "sha-256=:abc:=").body("{}")
                    .build();

            var context = new SignatureTestContext(TEST_KEY_ID, components);
            Map<String, String> headers = context.createSignature();

            assertThat(context.validateSignature(components, extractSignatureValue(headers.get("signature")))).isTrue();
        }
    }

    // ========================================
    // Helper Methods
    // ========================================

    private static String extractNonce(String signatureInput) {
        return extractPattern(signatureInput, "nonce=\"([^\"]+)\"", "No nonce found");
    }

    private static long extractCreatedTime(String signatureInput) {
        return Long.parseLong(extractPattern(signatureInput, "created=(\\d+)", "No created time found"));
    }

    private static String extractSignatureValue(String signatureHeader) {
        return extractPattern(signatureHeader, "sig=:([^:]+):", "Invalid signature format");
    }

    private static String extractPattern(String input, String regex, String errorMessage) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException(errorMessage + ": " + input);
    }
}
