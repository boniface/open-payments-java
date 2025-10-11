package zm.hashcode.openpayments.auth.signature;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for {@link ContentDigest}.
 */
@DisplayName("ContentDigest")
class ContentDigestTest {

    @Nested
    @DisplayName("Digest Generation")
    class DigestGenerationTests {

        @Test
        @DisplayName("should generate SHA-256 digest with correct format")
        void shouldGenerateDigestWithCorrectFormat() {
            // Given
            String body = "{\"amount\":\"100\"}";

            // When
            String digest = ContentDigest.generate(body);

            // Then
            assertThat(digest).startsWith("sha-256=:").endsWith(":=").hasSizeGreaterThan(50);
        }

        @Test
        @DisplayName("should generate consistent digest for same input")
        void shouldGenerateConsistentDigest() {
            // Given
            String body = "Hello, World!";

            // When
            String digest1 = ContentDigest.generate(body);
            String digest2 = ContentDigest.generate(body);

            // Then
            assertThat(digest1).isEqualTo(digest2);
        }

        @Test
        @DisplayName("should generate different digests for different inputs")
        void shouldGenerateDifferentDigests() {
            // Given
            String digest1 = ContentDigest.generate("Hello, World!");
            String digest2 = ContentDigest.generate("Hello, World");

            // Then
            assertThat(digest1).isNotEqualTo(digest2);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "Hello, World!", "Hello, 世界! 🌍", "a"})
        @DisplayName("should generate valid digest for various inputs")
        void shouldGenerateValidDigest(String body) {
            // When
            String digest = ContentDigest.generate(body);

            // Then
            assertThat(digest).isNotEmpty().startsWith("sha-256=:").endsWith(":=");
        }

        @Test
        @DisplayName("should handle large body")
        void shouldHandleLargeBody() {
            // Given
            String body = "a".repeat(10000);

            // When
            String digest = ContentDigest.generate(body);

            // Then
            assertThat(digest).isNotEmpty();
            assertThat(ContentDigest.validate(body, digest)).isTrue();
        }

        @Test
        @DisplayName("should throw NullPointerException when body is null")
        void shouldThrowWhenBodyIsNull() {
            // When / Then
            assertThatThrownBy(() -> ContentDigest.generate(null)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("body must not be null");
        }
    }

    @Nested
    @DisplayName("Digest Validation")
    class DigestValidationTests {

        @Test
        @DisplayName("should validate correct digest")
        void shouldValidateCorrectDigest() {
            // Given
            String body = "{\"amount\":\"100\"}";
            String digest = ContentDigest.generate(body);

            // When / Then
            assertThat(ContentDigest.validate(body, digest)).isTrue();
        }

        @ParameterizedTest
        @MethodSource("invalidDigestScenarios")
        @DisplayName("should reject invalid digests")
        void shouldRejectInvalidDigests(String body, String digest) {
            // When / Then
            assertThat(ContentDigest.validate(body, digest)).isFalse();
        }

        static Stream<Arguments> invalidDigestScenarios() {
            return Stream.of(Arguments.of("{\"amount\":\"100\"}", "sha-256=:invalidhash:="),
                    Arguments.of("{\"amount\":\"200\"}", ContentDigest.generate("{\"amount\":\"100\"}")),
                    Arguments.of("different", ContentDigest.generate("original")));
        }

        @Test
        @DisplayName("should throw when validating with null body")
        void shouldThrowWhenValidatingWithNullBody() {
            assertThatThrownBy(() -> ContentDigest.validate(null, "sha-256=:test:="))
                    .isInstanceOf(NullPointerException.class).hasMessageContaining("body must not be null");
        }

        @Test
        @DisplayName("should throw when validating with null digest")
        void shouldThrowWhenValidatingWithNullDigest() {
            assertThatThrownBy(() -> ContentDigest.validate("body", null)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("digestHeader must not be null");
        }
    }

    @Nested
    @DisplayName("Hash Extraction")
    class HashExtractionTests {

        @Test
        @DisplayName("should extract hash from valid digest")
        void shouldExtractHashFromValidDigest() {
            // Given
            String digest = "sha-256=:ABC123XYZ:=";

            // When
            String hash = ContentDigest.extractHash(digest);

            // Then
            assertThat(hash).isEqualTo("ABC123XYZ");
        }

        @ParameterizedTest
        @ValueSource(strings = {"invalid-format", "sha-256:missing:=", "sha-256=:missing", ""})
        @DisplayName("should return empty string for invalid formats")
        void shouldReturnEmptyStringForInvalidFormat(String invalidDigest) {
            // When
            String hash = ContentDigest.extractHash(invalidDigest);

            // Then
            assertThat(hash).isEmpty();
        }
    }

    @Nested
    @DisplayName("Format Validation")
    class FormatValidationTests {

        @Test
        @DisplayName("should validate correct digest format")
        void shouldValidateCorrectFormat() {
            // Given
            String body = "test";
            String digest = ContentDigest.generate(body);

            // When
            boolean isValid = ContentDigest.isValidFormat(digest);

            // Then
            assertThat(isValid).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"not-a-digest", "sha-256:wrong:=", "sha-256=:!!!INVALID!!!:=", ""})
        @DisplayName("should reject incorrect digest formats")
        void shouldRejectIncorrectFormat(String invalidDigest) {
            // When
            assertThat(ContentDigest.isValidFormat(invalidDigest)).isFalse();
        }
    }
}
